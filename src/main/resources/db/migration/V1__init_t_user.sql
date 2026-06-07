CREATE TABLE IF NOT EXISTS `t_user` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
    `username` varchar(50) NOT NULL,
    `email` varchar(100) NOT NULL,
    `password_hash` varchar(255) NOT NULL,
    `nickname` varchar(50) DEFAULT NULL,
    `avatar_url` varchar(255) DEFAULT NULL,
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '0-禁用，1-正常',
    `email_verified` tinyint NOT NULL DEFAULT '0' COMMENT '0-未验证，1-已验证',
    `register_ip` varchar(45) DEFAULT NULL,
    `last_login_time` datetime DEFAULT NULL,
    `last_login_ip` varchar(45) DEFAULT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
