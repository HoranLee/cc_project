package com.example.demo.controller;

import com.example.demo.model.ApiResponse;
import com.example.demo.model.LoginRequest;
import com.example.demo.model.LoginResponse;
import com.example.demo.model.RegisterRequest;
import com.example.demo.model.UserInfo;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证 REST 接口控制器。
 * 处理登录、退出、会话校验等请求。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 登录接口 —— POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        try {
            UserInfo userInfo = authService.login(request, ip, userAgent, httpResponse);
            LoginResponse loginResponse = LoginResponse.success(userInfo);
            return ResponseEntity.ok(ApiResponse.ok("登录成功", loginResponse));
        } catch (AuthService.AuthException e) {
            log.warn("登录失败: account={}, ip={}, reason={}", request.account(), ip, e.getMessage());
            return ResponseEntity.status(401)
                    .body(ApiResponse.unauthorized(e.getMessage()));
        }
    }

    /**
     * 退出接口 —— POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok(ApiResponse.ok("已退出登录", null));
    }

    /**
     * 获取当前登录用户信息 —— GET /api/auth/current-user
     */
    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse<UserInfo>> currentUser() {
        return authService.getCurrentUser()
                .map(userInfo -> ResponseEntity.ok(ApiResponse.ok(userInfo)))
                .orElse(ResponseEntity.status(401)
                        .body(ApiResponse.unauthorized("未登录")));
    }

    /**
     * 注册接口 —— POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        try {
            authService.register(request, ip, userAgent);
            return ResponseEntity.ok(ApiResponse.ok("注册成功", null));
        } catch (AuthService.RegisterException e) {
            log.warn("注册失败: username={}, ip={}, code={}, reason={}",
                    request.username(), ip, e.getCode(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(e.getCode(), e.getMessage()));
        }
    }

    /**
     * 用户名/邮箱唯一性校验 —— GET /api/auth/check-availability
     */
    @GetMapping("/check-availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @RequestParam("type") String type,
            @RequestParam("value") String value) {

        AuthService.AvailableResult result = authService.checkAvailability(type, value);

        if (result.available()) {
            return ResponseEntity.ok(Map.of("available", true));
        }
        return ResponseEntity.ok(Map.of("available", false, "message", result.message()));
    }

    /**
     * 会话校验 —— GET /api/auth/check
     */
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> check() {
        if (authService.isAuthenticated()) {
            return ResponseEntity.ok(ApiResponse.ok(true));
        }
        return ResponseEntity.status(401)
                .body(ApiResponse.unauthorized("未登录"));
    }

    /**
     * 获取客户端真实 IP。
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
