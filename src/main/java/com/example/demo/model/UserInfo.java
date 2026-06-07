package com.example.demo.model;

import com.example.demo.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.format.DateTimeFormatter;

/**
 * 当前登录用户信息 DTO（不含密码等敏感字段）。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserInfo(
        Long id,
        String username,
        String email,
        String nickname,
        String avatarUrl,
        Integer status,
        String lastLoginTime
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 从 User 实体构造 UserInfo，排除敏感字段。
     */
    public static UserInfo from(User user) {
        return new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getLastLoginTime() != null ? user.getLastLoginTime().format(FORMATTER) : null
        );
    }
}
