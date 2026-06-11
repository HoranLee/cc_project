package com.example.demo.service;

import com.example.demo.model.LoginRequest;
import com.example.demo.model.RegisterRequest;
import com.example.demo.model.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

/**
 * 认证服务接口 —— 面向接口编程，依赖倒置。
 */
public interface AuthService {

    /**
     * 用户登录认证。
     */
    UserInfo login(LoginRequest request, String ip, String userAgent, HttpServletResponse response);

    /**
     * 用户注册。
     *
     * @param request   注册请求
     * @param ip        客户端IP
     * @param userAgent 浏览器UA
     * @throws RegisterException 注册失败时抛出
     */
    void register(RegisterRequest request, String ip, String userAgent);

    /**
     * 检查用户名或邮箱是否可用。
     *
     * @param type  校验类型（username 或 email）
     * @param value 校验值
     * @return 可用返回 AvailableResult，不可用返回带错误消息的结果
     */
    AvailableResult checkAvailability(String type, String value);

    /**
     * 用户退出登录。
     */
    void logout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取当前登录用户信息。
     */
    Optional<UserInfo> getCurrentUser();

    /**
     * 检查当前请求是否已认证。
     */
    boolean isAuthenticated();

    /**
     * 可用性校验结果。
     */
    record AvailableResult(boolean available, String message) {
        public static AvailableResult ok() {
            return new AvailableResult(true, null);
        }
        public static AvailableResult fail(String message) {
            return new AvailableResult(false, message);
        }
    }

    /**
     * 认证异常。
     */
    class AuthException extends RuntimeException {
        public AuthException(String message) {
            super(message);
        }
    }

    /**
     * 注册异常（含错误码）。
     */
    class RegisterException extends RuntimeException {
        private final int code;

        public RegisterException(int code, String message) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
