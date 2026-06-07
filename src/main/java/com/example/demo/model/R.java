package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一 API 响应包装（Result 缩写）。
 * 命名为 R 以避免与 Swagger @ApiResponse 注解冲突。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record R<T>(
        int code,
        String message,
        T data
) {
    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data);
    }

    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data);
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> R<T> unauthorized(String message) {
        return new R<>(401, message, null);
    }
}
