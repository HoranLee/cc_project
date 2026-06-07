package com.example.demo.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求体 DTO。
 */
public record LoginRequest(
        @NotBlank(message = "账号不能为空")
        String account,

        @NotBlank(message = "密码不能为空")
        String password,

        boolean rememberMe
) {
}
