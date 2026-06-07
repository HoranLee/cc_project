package com.example.demo.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举。
 * 所有业务异常码集中管理。
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或会话已过期"),
    LOGIN_FAILED(401, "账号或密码错误"),
    ACCOUNT_DISABLED(401, "账号已被禁用"),
    ACCOUNT_LOCKED(401, "账号已被锁定"),
    RATE_LIMITED(401, "请求过于频繁，请稍后再试"),
    SERVER_ERROR(500, "服务器内部错误"),

    USERNAME_EXISTS(1001, "用户名已存在"),
    EMAIL_EXISTS(1002, "邮箱已注册"),
    PASSWORD_WEAK(1003, "密码强度不足：至少8位，包含字母和数字"),
    USERNAME_INVALID(1004, "用户名格式错误：需4~20位字母/数字/下划线，字母开头"),
    EMAIL_INVALID(1005, "邮箱格式错误"),
    AGREEMENT_UNCHECKED(1006, "请先同意用户协议"),
    REGISTER_TOO_FREQUENT(1007, "注册过于频繁，请稍后再试");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
