package com.example.demo.controller;

import com.example.demo.model.R;
import com.example.demo.model.LoginRequest;
import com.example.demo.model.LoginResponse;
import com.example.demo.model.RegisterRequest;
import com.example.demo.model.UserInfo;
import com.example.demo.service.AuthService;
import com.example.demo.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证管理", description = "用户登录、注册、退出、会话校验接口")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "用户登录", description = "使用用户名/邮箱和密码登录，JWT 写入 HttpOnly Cookie")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "401", description = "账号或密码错误")
    })
    @PostMapping("/login")
    public ResponseEntity<R<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        String ip = IpUtil.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        try {
            UserInfo userInfo = authService.login(request, ip, userAgent, httpResponse);
            return ResponseEntity.ok(R.ok("登录成功", LoginResponse.success(userInfo)));
        } catch (AuthService.AuthException e) {
            log.warn("登录失败: account={}, ip={}, reason={}", request.account(), ip, e.getMessage());
            return ResponseEntity.status(401).body(R.unauthorized(e.getMessage()));
        }
    }

    @Operation(summary = "用户注册", description = "创建新账号，密码 BCrypt 加密存储")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "注册成功"),
        @ApiResponse(responseCode = "400", description = "业务错误，code=1001~1007")
    })
    @PostMapping("/register")
    public ResponseEntity<R<Void>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        String ip = IpUtil.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        try {
            authService.register(request, ip, userAgent);
            return ResponseEntity.ok(R.ok("注册成功", null));
        } catch (AuthService.RegisterException e) {
            log.warn("注册失败: username={}, ip={}, code={}", request.username(), ip, e.getCode());
            return ResponseEntity.badRequest().body(R.fail(e.getCode(), e.getMessage()));
        }
    }

    @Operation(summary = "退出登录", description = "清除 auth_token Cookie")
    @ApiResponse(responseCode = "200", description = "退出成功")
    @PostMapping("/logout")
    public ResponseEntity<R<Void>> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok(R.ok("已退出登录", null));
    }

    @Operation(summary = "获取当前用户", description = "返回用户基本信息（不含密码）")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/current-user")
    public ResponseEntity<R<UserInfo>> currentUser() {
        return authService.getCurrentUser()
                .map(u -> ResponseEntity.ok(R.ok(u)))
                .orElse(ResponseEntity.status(401).body(R.unauthorized("未登录")));
    }

    @Operation(summary = "会话校验", description = "检查登录状态，用于前端路由守卫")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "已登录"),
        @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/check")
    public ResponseEntity<R<Boolean>> check() {
        if (authService.isAuthenticated()) {
            return ResponseEntity.ok(R.ok(true));
        }
        return ResponseEntity.status(401).body(R.unauthorized("未登录"));
    }

    @Operation(summary = "唯一性校验", description = "检查用户名或邮箱是否已被注册")
    @GetMapping("/check-availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @Parameter(description = "username 或 email", required = true) @RequestParam("type") String type,
            @Parameter(description = "待校验值", required = true) @RequestParam("value") String value) {
        AuthService.AvailableResult result = authService.checkAvailability(type, value);
        if (result.available()) {
            return ResponseEntity.ok(Map.of("available", true));
        }
        return ResponseEntity.ok(Map.of("available", false, "message", result.message()));
    }
}
