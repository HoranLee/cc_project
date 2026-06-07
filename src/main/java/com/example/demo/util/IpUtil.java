package com.example.demo.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 工具类 —— 从请求中提取客户端真实 IP。
 */
public final class IpUtil {

    private IpUtil() {
    }

    /**
     * 获取客户端真实 IP。
     * 优先级：X-Forwarded-For → X-Real-IP → RemoteAddr。
     */
    public static String getClientIp(HttpServletRequest request) {
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
