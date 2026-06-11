package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 开发环境种子数据初始化。
 * 仅在 dev profile 下运行，确保测试账号可用。
 */
@Component
@Profile({"dev", "k8s"})
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("lihaoran")) {
            log.info("测试用户 lihaoran 已存在，跳过初始化");
            return;
        }

        User testUser = User.builder()
                .username("lihaoran")
                .email("lihaoranhelloworld@163.com")
                .passwordHash(passwordEncoder.encode("Hello123456"))
                .nickname("浩然")
                .status(1)
                .emailVerified(0)
                .registerIp("127.0.0.1")
                .build();

        userRepository.save(testUser);
        log.info("=== 测试用户已创建 ===");
        log.info("  用户名/邮箱: lihaoran / lihaoranhelloworld@163.com");
        log.info("  密码: Hello123456");
        log.info("=====================");
    }
}
