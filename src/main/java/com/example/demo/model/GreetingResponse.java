package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 问候响应体 —— 不可变 DTO。
 * 使用 Java 21 Record 保证字段不可变、自动生成 equals/hashCode/toString。
 */
public record GreetingResponse(
        String message,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {

    /**
     * 静态工厂方法：语义化构造入口，避免 new 散落在各处。
     */
    public static GreetingResponse of(String message, LocalDateTime timestamp) {
        return new GreetingResponse(message, timestamp);
    }
}
