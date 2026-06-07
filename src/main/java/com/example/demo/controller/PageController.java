package com.example.demo.controller;

import com.example.demo.model.UserInfo;
import com.example.demo.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

/**
 * 页面路由控制器。
 * 负责返回 Thymeleaf 模板页面。
 */
@Controller
public class PageController {

    private final AuthService authService;

    public PageController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 登录页面 —— GET /login
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 注册页面 —— GET /register
     * 已登录用户自动跳转到首页/dashboard。
     */
    @GetMapping("/register")
    public String registerPage() {
        // 已登录用户跳转到 dashboard
        if (authService.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    /**
     * 仪表盘页面 —— GET /dashboard
     * 需要登录后才能访问，未登录会被 SecurityConfig 拦截重定向。
     */
    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        Optional<UserInfo> userInfo = authService.getCurrentUser();
        userInfo.ifPresent(info -> model.addAttribute("user", info));
        return "dashboard";
    }
}
