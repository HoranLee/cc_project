package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc / Swagger (OpenAPI 3.0) 配置。
 * 访问地址：http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Demo 用户认证系统 API")
                        .version("v2.5")
                        .description("基于 Spring Boot + Vue 3 的前后端分离用户认证系统")
                        .contact(new Contact()
                                .name("HoranLee")
                                .url("https://github.com/HoranLee/cc_project")));
    }
}
