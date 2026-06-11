package com.example.demo.service;

import com.example.demo.entity.LoginLog;
import com.example.demo.entity.User;
import com.example.demo.model.LoginRequest;
import com.example.demo.model.RegisterRequest;
import com.example.demo.model.UserInfo;
import com.example.demo.model.enums.ErrorCode;
import com.example.demo.repository.LoginLogRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.util.IpUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 认证服务实现。
 * 包含登录业务逻辑、登录日志记录、简单的内存速率限制。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int RATE_LIMIT_WINDOW_MINUTES = 1;
    private static final int MAX_REQUESTS_PER_WINDOW = 5;

    // 注册相关
    private static final int REGISTER_MAX_PER_IP = 3;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{3,19}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$");

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 简易内存速率限制器：key = IP + 分钟窗口, value = 请求次数。
     */
    private final Map<String, Integer> rateLimitMap = new ConcurrentHashMap<>();

    public AuthServiceImpl(UserRepository userRepository,
                           LoginLogRepository loginLogRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UserInfo login(LoginRequest request, String ip, String userAgent, HttpServletResponse response) {
        // 1. 速率限制检查
        checkRateLimit(ip);

        String account = request.account().trim();

        // 2. 查找用户
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(account);

        if (userOpt.isEmpty()) {
            // 账号不存在 — 记录日志但不暴露具体原因
            saveLoginLog(null, account, 0, "账号或密码错误", ip, userAgent);
            throw new AuthException("账号或密码错误");
        }

        User user = userOpt.get();

        // 3. 检查账号状态
        if (user.getStatus() == 0) {
            saveLoginLog(user.getId(), account, 0, "账号已被禁用", ip, userAgent);
            throw new AuthException("账号已被禁用，请联系管理员");
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            saveLoginLog(user.getId(), account, 0, "账号或密码错误", ip, userAgent);

            // 检查连续失败次数，超过阈值锁定账号
            long recentFailures = countRecentFailures(account);
            if (recentFailures >= MAX_FAILED_ATTEMPTS) {
                user.setStatus(0);
                userRepository.save(user);
                log.warn("账号 {} 连续失败 {} 次，已被锁定", account, recentFailures + 1);
                throw new AuthException("账号已被锁定，请联系管理员");
            }

            throw new AuthException("账号或密码错误");
        }

        // 5. 登录成功 — 更新最后登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userRepository.save(user);

        // 6. 记录登录成功日志
        saveLoginLog(user.getId(), account, 1, null, ip, userAgent);

        // 7. 生成 JWT 并写入 Cookie
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), request.rememberMe());
        setAuthCookie(response, token, request.rememberMe());

        log.info("用户 {} ({}) 登录成功", user.getUsername(), user.getId());
        return UserInfo.from(user);
    }

    @Override
    public void register(RegisterRequest request, String ip, String userAgent) {
        // 1. IP 速率限制：同一IP每分钟最多3次注册
        checkRegisterRateLimit(ip);

        String username = request.username().trim();
        String email = request.email().trim();
        String password = request.password();

        // 2. 后端二次校验 - 用户名
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new RegisterException(ErrorCode.USERNAME_INVALID.getCode(), ErrorCode.USERNAME_INVALID.getMessage());
        }
        if (userRepository.existsByUsername(username)) {
            throw new RegisterException(ErrorCode.USERNAME_EXISTS.getCode(), ErrorCode.USERNAME_EXISTS.getMessage());
        }

        // 3. 后端二次校验 - 邮箱
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new RegisterException(ErrorCode.EMAIL_INVALID.getCode(), ErrorCode.EMAIL_INVALID.getMessage());
        }
        if (userRepository.existsByEmail(email)) {
            throw new RegisterException(ErrorCode.EMAIL_EXISTS.getCode(), ErrorCode.EMAIL_EXISTS.getMessage());
        }

        // 4. 后端二次校验 - 密码强度
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new RegisterException(ErrorCode.PASSWORD_WEAK.getCode(), ErrorCode.PASSWORD_WEAK.getMessage());
        }

        // 5. 昵称处理：为空时默认取用户名
        String nickname = (request.nickname() != null && !request.nickname().isBlank())
                ? request.nickname().trim()
                : username;

        if (nickname.length() > 20) {
            nickname = nickname.substring(0, 20);
        }

        // 6. 创建用户
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .nickname(nickname)
                .status(1)
                .emailVerified(0)
                .registerIp(ip)
                .build();

        userRepository.save(user);

        // 7. 记录注册日志
        saveLoginLog(user.getId(), username, 1, "注册", ip, userAgent);

        log.info("新用户注册成功: username={}, email={}, ip={}", username, email, ip);
    }

    @Override
    public AvailableResult checkAvailability(String type, String value) {
        if (value == null || value.isBlank()) {
            return AvailableResult.fail("值不能为空");
        }

        return switch (type) {
            case "username" -> {
                if (!USERNAME_PATTERN.matcher(value.trim()).matches()) {
                    yield AvailableResult.fail(ErrorCode.USERNAME_INVALID.getMessage());
                }
                if (userRepository.existsByUsername(value.trim())) {
                    yield AvailableResult.fail(ErrorCode.USERNAME_EXISTS.getMessage());
                }
                yield AvailableResult.ok();
            }
            case "email" -> {
                if (!EMAIL_PATTERN.matcher(value.trim()).matches()) {
                    yield AvailableResult.fail(ErrorCode.EMAIL_INVALID.getMessage());
                }
                if (userRepository.existsByEmail(value.trim())) {
                    yield AvailableResult.fail(ErrorCode.EMAIL_EXISTS.getMessage());
                }
                yield AvailableResult.ok();
            }
            default -> AvailableResult.fail("无效的校验类型");
        };
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 清除前先获取当前用户信息（用于记录登出日志）
        Optional<UserInfo> currentUser = getCurrentUser();
        String ip = IpUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // 2. 清除认证上下文
        SecurityContextHolder.clearContext();

        // 3. 清除 Cookie
        Cookie cookie = new Cookie("auth_token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 开发环境使用 HTTP，生产环境应为 true
        response.addCookie(cookie);

        // 4. 记录登出日志
        currentUser.ifPresent(user ->
            saveLoginLog(user.id(), user.username(), 1, "主动注销", ip, userAgent)
        );
    }

    @Override
    public Optional<UserInfo> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return Optional.of(UserInfo.from(user));
        }
        return Optional.empty();
    }

    @Override
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof User;
    }

    // ---- 私有辅助方法 ----

    /**
     * 简易速率限制：同一 IP 每分钟最多 MAX_REQUESTS_PER_WINDOW 次请求。
     */
    private void checkRateLimit(String ip) {
        String key = ip + "_" + System.currentTimeMillis() / 60000;
        int count = rateLimitMap.merge(key, 1, Integer::sum);
        if (count > MAX_REQUESTS_PER_WINDOW) {
            throw new AuthException("请求过于频繁，请稍后再试");
        }
    }

    /**
     * 注册速率限制：同一 IP 每分钟最多 3 次注册。
     */
    private void checkRegisterRateLimit(String ip) {
        String key = "reg_" + ip + "_" + System.currentTimeMillis() / 60000;
        int count = rateLimitMap.merge(key, 1, Integer::sum);
        if (count > REGISTER_MAX_PER_IP) {
            throw new RegisterException(ErrorCode.REGISTER_TOO_FREQUENT.getCode(), ErrorCode.REGISTER_TOO_FREQUENT.getMessage());
        }
    }

    /**
     * 统计指定账号最近的失败次数。
     */
    private long countRecentFailures(String account) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(30);
        return loginLogRepository.countByLoginAccountAndLoginResultAndCreatedAtAfter(
                account, 0, since);
    }

    /**
     * 保存登录日志。
     */
    private void saveLoginLog(Long userId, String account, int result, String failReason, String ip, String userAgent) {
        LoginLog loginLog = LoginLog.builder()
                .userId(userId)
                .loginAccount(account)
                .loginResult(result)
                .failReason(failReason)
                .ipAddress(ip)
                .userAgent(userAgent)
                .build();
        loginLogRepository.save(loginLog);
    }

    /**
     * 将 JWT 写入 HttpOnly Cookie。
     */
    private void setAuthCookie(HttpServletResponse response, String token, boolean rememberMe) {
        Cookie cookie = new Cookie("auth_token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 开发环境用 HTTP
        cookie.setAttribute("SameSite", "Strict");
        cookie.setMaxAge(rememberMe ? 7 * 24 * 60 * 60 : 1800); // 7天 或 30分钟
        response.addCookie(cookie);
    }
}
