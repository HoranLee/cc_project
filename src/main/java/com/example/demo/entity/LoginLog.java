package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 登录日志实体 —— 映射 t_login_log 表。
 * 用于记录用户的登录尝试，便于安全审计和异常检测。
 */
@Entity
@Table(name = "t_login_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_account", nullable = false, length = 100)
    private String loginAccount;

    @Column(name = "login_result", nullable = false, columnDefinition = "TINYINT")
    private Integer loginResult; // 0-失败，1-成功

    @Column(name = "fail_reason", length = 100)
    private String failReason;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
