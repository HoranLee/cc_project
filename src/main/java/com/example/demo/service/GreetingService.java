package com.example.demo.service;

import com.example.demo.model.GreetingResponse;

/**
 * 问候服务接口 —— 面向接口编程，依赖倒置。
 * 上层模块（Controller）不依赖底层实现，便于扩展和单测 mock。
 */
@FunctionalInterface
public interface GreetingService {

    /**
     * 根据名称生成问候语
     *
     * @param name 被问候者名称
     * @return 包含问候语和时间戳的响应体
     */
    GreetingResponse greet(String name);
}
