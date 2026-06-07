package com.example.demo.service;

import com.example.demo.model.GreetingResponse;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 默认问候服务实现。
 * 通过 Clock 抽象时间源，便于测试时注入固定时钟。
 */
@Service
public class DefaultGreetingService implements GreetingService {

    private final Clock clock;

    /**
     * 生产环境使用系统默认时钟。
     */
    public DefaultGreetingService() {
        this(Clock.systemDefaultZone());
    }

    /**
     * 测试环境可注入自定义 Clock。
     */
    public DefaultGreetingService(Clock clock) {
        this.clock = clock;
    }

    @Override
    public GreetingResponse greet(String name) {
        var message = "Hello, " + name + "!";
        var now = LocalDateTime.now(clock);
        return GreetingResponse.of(message, now);
    }
}
