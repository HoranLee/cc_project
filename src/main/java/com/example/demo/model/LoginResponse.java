package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 登录响应体 DTO。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
        Long id,
        String username,
        String email,
        String nickname,
        String avatarUrl,
        String lastLoginTime,
        String message
) {
    public static LoginResponse success(UserInfo userInfo) {
        return new LoginResponse(
                userInfo.id(),
                userInfo.username(),
                userInfo.email(),
                userInfo.nickname(),
                userInfo.avatarUrl(),
                userInfo.lastLoginTime(),
                "登录成功"
        );
    }
}
