package com.example.demo.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

/**
 * 注册请求体 DTO。
 * 前端校验 + 后端二次校验（业务校验在 AuthService 中完成）。
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        String username,

        @NotBlank(message = "邮箱不能为空")
        String email,

        @NotBlank(message = "密码不能为空")
        String password,

        String nickname,

        @AssertTrue(message = "请先同意用户协议")
        boolean agreed
) {
}
