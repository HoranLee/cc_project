package com.example.demo.controller;

import com.example.demo.model.GreetingResponse;
import com.example.demo.service.GreetingService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello 接口控制器。
 * 只做协议转换（HTTP → 业务调用 → HTTP 响应），不含任何业务逻辑。
 */
@RestController
public class HelloController {

    @Resource
    private  GreetingService greetingService;



    @GetMapping("/hello")
    public GreetingResponse hello(@RequestParam(defaultValue = "World") String name) {
        return greetingService.greet(name);
    }
}
