# Demo 用户认证系统

> **版本**：v3.0 &emsp; **状态**：架构演进中 &emsp; **架构模式**：前后端分离 + 云原生

---

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 技术栈](#2-技术栈)
- [3. 系统架构](#3-系统架构)
- [4. 项目结构](#4-项目结构)
- [5. 后端设计](#5-后端设计)
- [6. 前端设计](#6-前端设计)
- [7. API 文档](#7-api-文档)
- [8. 数据库设计](#8-数据库设计)
- [9. 安全体系](#9-安全体系)
- [10. 可观测性](#10-可观测性)
- [11. 测试体系](#11-测试体系)
- [12. DevOps & CI/CD](#12-devops--cicd)
- [13. 部署架构](#13-部署架构)
- [14. 开发规范](#14-开发规范)
- [15. 版本演进路线](#15-版本演进路线)

---

## 1. 项目概述

### 1.1 项目定位

Demo 用户认证系统，基于 **Spring Boot + Vue 3** 前后端分离，覆盖注册、登录、会话管理、退出等完整认证闭环。采用 JWT 无状态认证、Swagger 自动文档、Docker 容器化部署，具备生产级可观测性和自动化测试体系。

### 1.2 功能矩阵

| 模块 | 功能 |
|------|------|
| 🔐 认证 | 登录 / 退出 / 会话校验 / OAuth2 第三方登录 / MFA 双因素 |
| 📝 注册 | 邮箱注册 / 唯一性校验 / 验证码 / 异步欢迎邮件 |
| 👤 用户 | 个人信息 / 登录日志 / 密码策略 |
| 📋 文档 | Swagger / Knife4j / OpenAPI 3 / GraphQL Schema |
| 📊 监控 | Micrometer + Prometheus + Grafana + OpenTelemetry 链路追踪 |

### 1.3 架构演进路线

```
v1.0 ──────────▶ v2.0 ──────────▶ v3.0（当前）
单体 MVC        前后端分离        云原生全栈
Thymeleaf       Vue 3 SPA        Nuxt 3 SSR
无文档           Swagger          GraphQL 双协议
单机部署         Docker           K8s + GraalVM
```

---

## 2. 技术栈

### 2.1 后端

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 基础框架 | Spring Boot | 4.0.6 | 应用框架 |
| 语言 | Java | 21 LTS | Virtual Threads / Pattern Matching / Sealed Classes |
| Web | Spring MVC + GraphQL | — | REST + GraphQL 双协议 |
| 安全 | Spring Security | 7.x | JWT + OAuth2 + TOTP |
| Token | jjwt | 0.12.6 | JWT 生成与校验 |
| ORM | Spring Data JPA + JdbcClient | — | 常规查询用 JPA，性能敏感用 JdbcClient |
| 数据库迁移 | Flyway | 10.x | 版本化数据库迁移 |
| 数据库 | MySQL | 9.7 | 主存储 |
| 缓存 | Redis + Spring Cache | 7.x | Token 黑名单 / 分布式限流 / `@Cacheable` |
| 消息队列 | RabbitMQ（可选） | 3.x | 异步事件驱动 |
| 定时任务 | XXL-Job（可选） | 2.x | 分布式定时调度 |
| API 文档 | Knife4j + SpringDoc | 4.x | OpenAPI 3.0 |
| 校验 | Jakarta Validation + Passay | — | 参数校验 + 密码策略 |
| 虚拟线程 | Project Loom | Java 21 内置 | 高并发轻量线程 |
| 原生编译 | GraalVM Native Image | 21 | 毫秒级启动 |
| 可观测性 | Micrometer + OpenTelemetry | — | 指标 + 链路 |
| 工具库 | Lombok | 1.18+ | 减少样板代码 |

### 2.2 前端

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 全栈框架 | Nuxt 3 | 3.x | SSR + SSG + SPA 混合渲染 |
| 核心 | Vue | 3.4+ | Composition API |
| 构建 | Vite | 6.x | 极速 HMR |
| 语言 | TypeScript | 5.x | 严格模式 |
| 路由 | Vue Router | 4.x | 自动路由 + 守卫 |
| 状态管理 | Pinia | 2.x | 客户端状态 |
| 服务端状态 | TanStack Query | 5.x | 缓存 / 自动重取 / 乐观更新 |
| HTTP | Axios | 1.x | 拦截器 + 自动 Cookie |
| UI 库 | Element Plus | 2.x | 企业级组件 |
| 原子 CSS | UnoCSS | 0.x | 按需生成，5x 构建速度 |
| 测试 | Vitest + Playwright | — | 单元 + E2E |
| 组件文档 | Storybook | 8.x | 可视化组件开发 |
| Mock | MSW | 2.x | 拦截网络请求 |
| 国际化 | Vue I18n | 9.x | 多语言 |
| PWA | Vite PWA Plugin | — | 离线可用 |
| 包管理 | pnpm | 9.x | monorepo |

### 2.3 DevOps

| 类别 | 技术 |
|------|------|
| CI/CD | GitHub Actions |
| 容器 | Docker + Docker Compose |
| 编排 | Kubernetes（可选） |
| 镜像仓库 | Docker Hub / GitHub Container Registry |
| GitOps | ArgoCD（可选） |

---

## 3. 系统架构

### 3.1 架构全景图（v3.0）

```
                              ┌───────────────────────────┐
                              │        CDN / WAF           │
                              │     Cloudflare / 阿里云     │
                              └─────────────┬─────────────┘
                                            │
                              ┌─────────────▼─────────────┐
                              │     Nginx / Kong 网关       │
                              │  SSL 终结 · 限流 · 路由     │
                              │  /api/* → Backend          │
                              │  /*     → Frontend Static  │
                              └──┬────────────┬───────────┘
                                 │            │
                    /api/*       │            │ /*
                                 ▼            ▼
┌──────────────────────────┐  ┌──────────────────────────────┐
│     Backend (Spring Boot) │  │   Frontend (Nuxt 3 + Vite)   │
│     Port: 8080            │  │   Port: 3000 (SSR)           │
│                           │  │   5173 (dev HMR)             │
│  ┌─────────────────────┐  │  │                              │
│  │  REST + GraphQL 双协议│  │  │  ┌────────────────────────┐ │
│  ├─────────────────────┤  │  │  │  Nuxt 3 Hybrid Render   │ │
│  │  Spring Security     │  │  │  │  · SSR: 首屏秒开        │ │
│  │  · JWT + OAuth2+MFA │  │  │  │  · SSG: 静态预渲染      │ │
│  ├─────────────────────┤  │  │  │  · SPA: 交互页          │ │
│  │  Virtual Threads     │  │  │  ├────────────────────────┤ │
│  │  高并发轻量线程       │  │  │  │  Pinia + TanStack Query│ │
│  ├─────────────────────┤  │  │  │  · 客户端状态           │ │
│  │  Redis Cache         │  │  │  │  · 服务端缓存           │ │
│  │  · Token 黑名单       │  │  │  │  · 乐观更新             │ │
│  │  · 分布式限流         │  │  │  ├────────────────────────┤ │
│  │  · @Cacheable        │  │  │  │  UnoCSS 原子 CSS        │ │
│  ├─────────────────────┤  │  │  │  Element Plus 组件       │ │
│  │  Flyway 迁移          │  │  │  ├────────────────────────┤ │
│  │  JPA + JdbcClient    │  │  │  │  Axios + MSW Mock      │ │
│  ├─────────────────────┤  │  │  │  Vue I18n 国际化        │ │
│  │  Micrometer          │  │  │  │  PWA 离线支持           │ │
│  │  OpenTelemetry       │  │  │  └────────────────────────┘ │
│  └──────────┬──────────┘  │  └──────────────────────────────┘
│             │              │
└─────────────┼──────────────┘
              │
    ┌─────────┼──────────┐
    ▼         ▼          ▼
┌────────┐ ┌────────┐ ┌──────────┐
│ MySQL  │ │ Redis  │ │ RabbitMQ │
│ :3306  │ │ :6379  │ │ :5672    │
└────────┘ └────────┘ └──────────┘

┌──────────────────────────────────┐
│        可观测性栈                  │
│  Prometheus → Grafana（指标）     │
│  Jaeger → 链路追踪                │
│  Loki → 日志聚合                  │
└──────────────────────────────────┘
```

### 3.2 认证流程（v3.0 增强版）

```
                 ┌─────────────────────────────┐
                 │       登录方式选择            │
                 │  ┌──────────┬─────────────┐ │
                 │  │ 账号密码  │ GitHub OAuth │ │
                 │  │ + TOTP   │ 微信扫码     │ │
                 │  └──────────┴─────────────┘ │
                 └─────────────┬───────────────┘
                               │
                    ┌──────────▼──────────┐
                    │   AuthService        │
                    │   ① 验证码校验       │
                    │   ② 密码 BCrypt 校验  │
                    │   ③ TOTP MFA 校验    │
                    │   ④ 状态检查         │
                    │   ⑤ 生成 JWT         │
                    │   ⑥ 写入 Redis 白名单 │
                    │   ⑦ HttpOnly Cookie  │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
      ┌──────────┐    ┌──────────────┐    ┌──────────┐
      │ 记录日志  │    │ 异步事件      │    │ 返回响应  │
      │ t_login  │    │ 欢迎邮件      │    │ JWT Cookie│
      │ _log     │    │ 操作审计      │    │ + UserInfo│
      └──────────┘    └──────────────┘    └──────────┘
```

### 3.3 请求生命周期

```
请求 → Nginx(SSL+限流) → Spring Boot
  │
  ├── JwtAuthFilter（从 Cookie 提取 JWT）
  ├── RateLimitFilter（Redis 分布式限流）
  ├── Controller（@Valid 参数校验）
  │     └── Service（业务逻辑）
  │           ├── Repository（JPA / JdbcClient）
  │           ├── Cache（Redis @Cacheable）
  │           └── Event（Spring Event → RabbitMQ）
  └── Response → Micrometer 埋点 → OpenTelemetry Span
```

---

## 4. 项目结构

```
cc_project/
├── backend/
│   ├── pom.xml
│   ├── Dockerfile                        # 多阶段构建
│   └── src/main/
│       ├── java/com/example/demo/
│       │   ├── DemoApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   ├── SwaggerConfig.java
│       │   │   ├── CorsConfig.java
│       │   │   ├── RedisConfig.java
│       │   │   ├── VirtualThreadConfig.java
│       │   │   └── ObservabilityConfig.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── graphql/                  # GraphQL 层
│       │   │   ├── AuthGraphQLController.java
│       │   │   └── UserGraphQLController.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   └── impl/AuthServiceImpl.java
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   └── LoginLogRepository.java
│       │   ├── entity/
│       │   │   ├── User.java
│       │   │   └── LoginLog.java
│       │   ├── model/
│       │   │   ├── request/
│       │   │   │   ├── LoginRequest.java
│       │   │   │   └── RegisterRequest.java
│       │   │   ├── response/
│       │   │   │   ├── LoginResponse.java
│       │   │   │   ├── UserInfo.java
│       │   │   │   └── ApiResponse.java
│       │   │   └── enums/
│       │   │       └── ErrorCode.java
│       │   ├── security/
│       │   │   ├── JwtUtil.java
│       │   │   ├── JwtAuthFilter.java
│       │   │   ├── RateLimitFilter.java
│       │   │   └── TotpService.java      # MFA 服务
│       │   ├── event/                    # 事件驱动
│       │   │   ├── UserRegisteredEvent.java
│       │   │   ├── UserLoginEvent.java
│       │   │   └── EventListener.java
│       │   └── util/
│       │       └── IpUtil.java
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           ├── application-prod.yml
│           └── db/migration/             # Flyway 脚本
│               ├── V1__init_t_user.sql
│               └── V2__init_t_login_log.sql
│
├── frontend/
│   ├── nuxt.config.ts
│   ├── package.json
│   ├── Dockerfile
│   └── src/
│       ├── app.vue
│       ├── pages/                        # Nuxt 自动路由
│       │   ├── index.vue                 # /
│       │   ├── login.vue                 # /login
│       │   ├── register.vue              # /register
│       │   └── dashboard.vue             # /dashboard
│       ├── components/
│       │   ├── auth/
│       │   │   ├── AuthCard.vue
│       │   │   ├── LoginForm.vue
│       │   │   ├── RegisterForm.vue
│       │   │   └── PasswordStrength.vue
│       │   ├── layout/
│       │   │   └── AppHeader.vue
│       │   └── common/
│       │       └── ConfirmDialog.vue
│       ├── composables/
│       │   ├── useAuth.ts
│       │   ├── useValidation.ts
│       │   └── usePasswordStrength.ts
│       ├── stores/
│       │   ├── auth.ts
│       │   └── user.ts
│       ├── api/
│       │   ├── request.ts                # Axios 实例
│       │   ├── auth.ts
│       │   └── user.ts
│       ├── types/
│       │   └── index.ts
│       ├── locales/                      # 国际化
│       │   ├── zh-CN.json
│       │   └── en-US.json
│       └── mocks/                        # MSW Mock
│           └── handlers.ts
│
├── docker-compose.yml
├── docker-compose.prod.yml
├── .github/workflows/
│   ├── ci-backend.yml
│   ├── ci-frontend.yml
│   └── deploy.yml
└── README.md
```

---

## 5. 后端设计

### 5.1 Java 21 新特性深度应用

```java
// ── Virtual Threads（虚拟线程）──
// application.yml: spring.threads.virtual.enabled=true
// 效果：Tomcat 请求处理从 200 平台线程 → 数万虚拟线程

// ── Pattern Matching ──
// 之前：
if (obj instanceof User user) {
    String name = user.getUsername();
}

// 现在（switch 模式匹配）：
var result = switch (authResult) {
    case LoginSuccess(var user)    -> "欢迎 " + user.getNickname();
    case LoginFailed(var reason)   -> "登录失败: " + reason;
    case AccountLocked(var until)  -> "账号锁定至 " + until;
};

// ── Sealed Classes ──
public sealed interface AuthResult
    permits LoginSuccess, LoginFailed, AccountLocked {
}
public record LoginSuccess(UserInfo user) implements AuthResult {}
public record LoginFailed(String reason) implements AuthResult {}
public record AccountLocked(LocalDateTime until) implements AuthResult {}

// ── String Templates（预览）──
var msg = STR."用户 \{username} 登录成功，IP: \{ip}";
```

### 5.2 虚拟线程配置

```java
@Configuration
public class VirtualThreadConfig {

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerCustomizer() {
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }

    // 或者直接：spring.threads.virtual.enabled=true
}
```

```yaml
# application.yml
spring:
  threads:
    virtual:
      enabled: true    # 一行开启虚拟线程
```

### 5.3 核心类设计（Swagger 注解完整版）

```java
// ==================== 实体 ====================
@Entity
@Table(name = "t_user")
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 50) private String nickname;
    @Column(name = "avatar_url", length = 255) private String avatarUrl;

    @Builder.Default private Integer status = 1;
    @Builder.Default
    @Column(name = "email_verified")
    private Integer emailVerified = 0;

    @Column(name = "register_ip", length = 45) private String registerIp;
    @Column(name = "last_login_time") private LocalDateTime lastLoginTime;
    @Column(name = "last_login_ip", length = 45) private String lastLoginIp;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// ==================== DTO（含完整 Swagger 注解）====================
@Schema(description = "登录请求")
public record LoginRequest(
    @Schema(description = "用户名或邮箱", example = "admin",
            requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "账号不能为空")
    String account,

    @Schema(description = "密码", example = "123456",
            requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    String password,

    @Schema(description = "记住我（延长至7天）", defaultValue = "false")
    boolean rememberMe,

    @Schema(description = "TOTP 验证码（开启 MFA 时必填）")
    String totpCode
) {}

@Schema(description = "注册请求")
public record RegisterRequest(
    @Schema(description = "用户名", example = "zhangsan", requiredMode = REQUIRED)
    @NotBlank String username,

    @Schema(description = "邮箱", example = "test@example.com", requiredMode = REQUIRED)
    @NotBlank String email,

    @Schema(description = "密码（至少8位，含字母和数字）", example = "Abc12345",
            requiredMode = REQUIRED)
    @NotBlank String password,

    @Schema(description = "昵称，为空默认取用户名", example = "张三")
    String nickname,

    @Schema(description = "必须同意用户协议", requiredMode = REQUIRED)
    @AssertTrue(message = "请先同意用户协议")
    boolean agreed
) {}

// ==================== 统一响应 ====================
@Schema(description = "统一 API 响应")
public record ApiResponse<T>(
    @Schema(description = "业务状态码", example = "200")
    int code,
    @Schema(description = "提示信息", example = "success")
    String message,
    @Schema(description = "响应数据")
    @JsonInclude(NON_NULL)
    T data
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data);
    }
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}

// ==================== 错误码枚举 ====================
@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    UNAUTHORIZED(401, "未登录或会话已过期"),
    USERNAME_EXISTS(1001, "用户名已存在"),
    EMAIL_EXISTS(1002, "邮箱已注册"),
    PASSWORD_WEAK(1003, "密码强度不足"),
    USERNAME_INVALID(1004, "用户名格式错误"),
    EMAIL_INVALID(1005, "邮箱格式错误"),
    AGREEMENT_UNCHECKED(1006, "用户协议未同意"),
    REGISTER_TOO_FREQUENT(1007, "注册过于频繁"),
    TOTP_REQUIRED(1008, "需要双因素验证"),
    TOTP_INVALID(1009, "验证码错误"),
    LOGIN_FAILED(401, "账号或密码错误"),
    ACCOUNT_DISABLED(401, "账号已被禁用"),
    ACCOUNT_LOCKED(401, "账号已被锁定"),
    RATE_LIMITED(401, "请求过于频繁");

    private final int code;
    private final String message;
}

// ==================== 控制器（Swagger + GraphQL）====================
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证管理", description = "登录、注册、退出、MFA")
public class AuthController {

    @Operation(summary = "用户登录")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "401", description = "账号或密码错误"),
        @ApiResponse(responseCode = "1008", description = "需要 TOTP 验证")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) { /* ... */ }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
        @Valid @RequestBody RegisterRequest request,
        HttpServletRequest httpRequest
    ) { /* ... */ }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
        HttpServletResponse response
    ) { /* ... */ }

    @Operation(summary = "获取当前用户")
    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse<UserInfo>> currentUser() { /* ... */ }

    @Operation(summary = "会话校验")
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> check() { /* ... */ }

    @Operation(summary = "唯一性校验")
    @GetMapping("/check-availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
        @Parameter(description = "校验类型", example = "username", required = true)
        @RequestParam String type,
        @Parameter(description = "校验值", example = "zhangsan", required = true)
        @RequestParam String value
    ) { /* ... */ }

    @Operation(summary = "绑定 TOTP MFA")
    @PostMapping("/mfa/setup")
    public ResponseEntity<ApiResponse<String>> setupMfa() { /* ... */ }
}

// ==================== GraphQL 控制器 ====================
@Controller
public class AuthGraphQLController {

    @QueryMapping
    @SchemaMapping(typeName = "Query", field = "currentUser")
    public UserInfo currentUser() { /* ... */ }

    @MutationMapping
    @SchemaMapping(typeName = "Mutation", field = "login")
    public LoginResponse login(@Argument LoginInput input) { /* ... */ }
}

// ==================== 异步事件 ====================
@Service
@Slf4j
public class AuthEventListener {

    @Async
    @TransactionalEventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        // 发送欢迎邮件（异步）
        // 记录审计日志
        log.info("用户注册成功: {}", event.username());
    }

    @Async
    @TransactionalEventListener
    public void handleUserLogin(UserLoginEvent event) {
        // 异常登录检测
        // 登录位置分析
    }
}

// ==================== Redis 分布式限流 ====================
@Component
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean tryAcquire(String key, int limit, int windowSeconds) {
        String redisKey = "rate:" + key + ":" + Instant.now().getEpochSecond() / windowSeconds;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
        }
        return count <= limit;
    }
}
```

### 5.4 Swagger 在线文档

| 地址 | 说明 |
|------|------|
| `http://localhost:8080/doc.html` | Knife4j 增强 UI（离线导出、全局参数） |
| `http://localhost:8080/swagger-ui.html` | Swagger UI 原生 |
| `http://localhost:8080/v3/api-docs` | OpenAPI 3.0 JSON |
| `http://localhost:8080/graphiql` | GraphQL 调试台（如启用） |

---

## 6. 前端设计

### 6.1 Nuxt 3 渲染策略

```
┌─────────────────────────────────────────────┐
│              Nuxt 3 Hybrid Render            │
├─────────────────────────────────────────────┤
│  路由          渲染模式         说明          │
│  /login        SSG（静态生成）  登录页预渲染  │
│  /register     SSG（静态生成）  注册页预渲染  │
│  /dashboard    SSR（服务端）   用户数据实时    │
│  /dashboard/*  SPA（客户端）   交互页          │
└─────────────────────────────────────────────┘
```

### 6.2 核心代码示例

```typescript
// ── stores/auth.ts（Pinia + TanStack Query）──

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!user.value)

  const { mutateAsync: loginMutation } = useMutation({
    mutationFn: (data: LoginRequest) => authApi.login(data),
    onSuccess: (res) => {
      if (res.code === 200) fetchUser()
    }
  })

  async function login(account: string, password: string, rememberMe: boolean) {
    return loginMutation({ account, password, rememberMe })
  }

  return { user, isLoggedIn, login, logout }
})

// ── composables/usePasswordStrength.ts ──

export function usePasswordStrength(password: Ref<string>) {
  const strength = computed(() => {
    const val = password.value
    let score = 0
    if (val.length >= 8) score++
    if (/[a-zA-Z]/.test(val)) score++
    if (/\d/.test(val)) score++
    if (/[^a-zA-Z0-9]/.test(val)) score++
    if (val.length >= 10) score++

    if (score <= 2) return { level: 'weak', color: '#e74c3c', label: '弱', width: '33%' }
    if (score <= 3) return { level: 'medium', color: '#f39c12', label: '中', width: '66%' }
    return { level: 'strong', color: '#27ae60', label: '强', width: '100%' }
  })
  return { strength }
}

// ── api/request.ts（Axios + 401 自动处理）──

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  withCredentials: true
})

request.interceptors.response.use(
  res => res.data,
  error => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      navigateTo('/login')
    }
    return Promise.reject(error)
  }
)

// ── mocks/handlers.ts（MSW Mock）──

export const handlers = [
  http.post('/api/v1/auth/login', async ({ request }) => {
    const body = await request.json()
    if (body.account === 'admin' && body.password === '123456') {
      return HttpResponse.json({ code: 200, message: '登录成功', data: mockUser })
    }
    return HttpResponse.json(
      { code: 401, message: '账号或密码错误' },
      { status: 401 }
    )
  })
]
```

### 6.3 组件设计

```vue
<!-- PasswordStrength.vue — 密码强度指示器 -->
<script setup lang="ts">
const props = defineProps<{ password: string }>()
const { strength } = usePasswordStrength(toRef(props, 'password'))
</script>

<template>
  <div v-if="props.password" class="password-strength">
    <div class="strength-bar">
      <div class="strength-fill"
        :style="{ width: strength.width, background: strength.color }" />
    </div>
    <span :style="{ color: strength.color }">{{ strength.label }}</span>
  </div>
</template>
```

---

## 7. API 文档

### 7.1 REST API

| 方法 | URL | 摘要 | 认证 | 限流 |
|------|-----|------|------|------|
| `POST` | `/api/v1/auth/login` | 登录（支持 TOTP） | 否 | 5/min |
| `POST` | `/api/v1/auth/register` | 注册 | 否 | 3/min |
| `GET` | `/api/v1/auth/check-availability` | 唯一性校验 | 否 | — |
| `POST` | `/api/v1/auth/logout` | 退出 | 是 | — |
| `GET` | `/api/v1/auth/current-user` | 当前用户 | 是 | — |
| `GET` | `/api/v1/auth/check` | 会话校验 | 是 | — |
| `POST` | `/api/v1/auth/mfa/setup` | 绑定 MFA | 是 | — |

### 7.2 GraphQL（可选）

```graphql
type Query {
  currentUser: UserInfo
}

type Mutation {
  login(input: LoginInput!): LoginResponse
  register(input: RegisterInput!): RegisterResponse
  logout: Boolean
}

type UserInfo {
  id: ID!
  username: String!
  email: String!
  nickname: String
  avatarUrl: String
  status: Int!
  lastLoginTime: String
}
```

### 7.3 错误码

| 错误码 | 说明 | HTTP |
|--------|------|------|
| 200 | 成功 | 200 |
| 400 | 参数校验失败 | 400 |
| 401 | 未登录 / 登录失败 | 401 |
| 500 | 服务器内部错误 | 500 |
| 1001 | 用户名已存在 | 400 |
| 1002 | 邮箱已注册 | 400 |
| 1003 | 密码强度不足 | 400 |
| 1007 | 注册过于频繁 | 400 |
| 1008 | 需要双因素验证 | 401 |
| 1009 | 验证码错误 | 401 |

---

## 8. 数据库设计

> 与 v1.0 一致，新增 Flyway 版本化管理。

```sql
-- V1__init_t_user.sql
CREATE TABLE t_user (...);

-- V2__init_t_login_log.sql
CREATE TABLE t_login_log (...);
```

**Flyway 优势**：版本化、可回滚、CI/CD 自动执行、多环境统一。

---

## 9. 安全体系

| 层面 | 策略 | 实现 |
|------|------|------|
| 传输层 | HTTPS + HSTS | Nginx / CDN |
| 跨域 | CORS 白名单 | CorsConfig |
| 认证 | JWT + HttpOnly Cookie + OAuth2 | Spring Security |
| 密码 | BCrypt(cost=10) + Passay 策略 | BCryptPasswordEncoder |
| 双因素 | TOTP（RFC 6238） | TotpService |
| 限流 | Redis 分布式限流 | RateLimitService |
| 防暴力 | 5 次失败锁账号 + 验证码 | AuthServiceImpl |
| 审计 | 每次登录/注册写入日志 | t_login_log |
| 注入防护 | PreparedStatement + XSS 过滤 | JPA / Hibernate |
| CSRF | API 无状态，Cookie SameSite=Strict | SecurityConfig |

---

## 10. 可观测性

### 10.1 三支柱

```
┌──────────────────────────────────────────────────────┐
│                    可观测性三支柱                        │
├──────────────┬──────────────────┬────────────────────┤
│   Metrics    │     Tracing      │      Logging       │
│   (指标)     │    (链路追踪)     │     (日志聚合)       │
├──────────────┼──────────────────┼────────────────────┤
│ Micrometer   │ OpenTelemetry    │ Logback JSON       │
│ Prometheus   │ Jaeger           │ Loki               │
│ Grafana      │ Grafana Tempo    │ Grafana            │
└──────────────┴──────────────────┴────────────────────┘
```

### 10.2 配置

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info
  metrics:
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 1.0    # 开发环境全量采样
```

### 10.3 Grafana 大盘

- **QPS / 延迟 / 错误率** — 黄金指标
- **登录成功率 / 注册成功率** — 业务指标
- **JVM 内存 / GC / 线程** — 运行时指标
- **慢查询 Top 10** — 数据库指标

---

## 11. 测试体系

### 11.1 测试金字塔

```
          ╱  E2E  ╲            Playwright（浏览器端到端）
         ╱──────────╲          · 登录全流程
        ╱  集成测试   ╲         · 注册 → 登录 → 仪表盘 → 退出
       ╱──────────────╲
      ╱    API 测试    ╲        Spring MockMvc + Testcontainers
     ╱──────────────────╲       · Controller 层全部端点
    ╱     单元测试       ╲      Vitest + JUnit 5
   ╱──────────────────────╲     · Service 层 > 90%
  ──────────────────────────    · Pinia Store
```

### 11.2 测试工具链

| 层级 | 后端 | 前端 |
|------|------|------|
| 单元 | JUnit 5 + Mockito | Vitest |
| API | MockMvc + Testcontainers | MSW Mock |
| E2E | — | Playwright |
| 契约 | Spring Cloud Contract | — |
| 变异 | PIT Mutation Testing | — |

### 11.3 测试示例

```java
// 集成测试 — Testcontainers 启动真实 MySQL
@SpringBootTest
@Testcontainers
class AuthServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:9.7")
        .withDatabaseName("cc_db")
        .withUsername("root")
        .withPassword("hello");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired AuthService authService;

    @Test
    void shouldRegisterAndLogin() {
        // 注册
        var req = new RegisterRequest("test", "test@test.com",
                                       "Pass1234", "Test", true);
        authService.register(req, "127.0.0.1", "JUnit");

        // 登录
        var loginReq = new LoginRequest("test", "Pass1234", false);
        var userInfo = authService.login(loginReq, "127.0.0.1", "JUnit",
                          new MockHttpServletResponse());
        assertThat(userInfo.username()).isEqualTo("test");
    }
}
```

---

## 12. DevOps & CI/CD

### 12.1 GitHub Actions 流水线

```yaml
# .github/workflows/ci-backend.yml
name: Backend CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:9.7
        env:
          MYSQL_ROOT_PASSWORD: hello
          MYSQL_DATABASE: cc_db
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: 21
          distribution: 'temurin'
      - name: Unit Tests
        run: mvn test
      - name: Integration Tests
        run: mvn verify -Pintegration
      - name: Mutation Tests
        run: mvn pitest:run -Pmutation

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - name: Build Docker Image
        run: |
          docker build -t cc_project-backend:${{ github.sha }} backend/
      - name: Push to Registry
        run: |
          docker push ghcr.io/horanlee/cc_project-backend:${{ github.sha }}
```

### 12.2 Docker 多阶段构建

```dockerfile
# backend/Dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ src/
RUN mvn package -DskipTests

FROM bellsoft/liberica-runtime-container:21-musl AS runtime
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseZGC", "-jar", "app.jar"]
```

---

## 13. 部署架构

### 13.1 Docker Compose（开发/测试）

```yaml
services:
  mysql:
    image: mysql:9.7
    environment:
      MYSQL_ROOT_PASSWORD: hello
      MYSQL_DATABASE: cc_db
    ports: ["3306:3306"]
    volumes: [mysql_data:/var/lib/mysql]

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  backend:
    build: ./backend
    ports: ["8080:8080"]
    depends_on: [mysql, redis]
    environment:
      SPRING_PROFILES_ACTIVE: dev

  frontend:
    build: ./frontend
    ports: ["3000:3000"]
    depends_on: [backend]

  nginx:
    image: nginx:alpine
    ports: ["80:80", "443:443"]
    volumes: [./nginx.conf:/etc/nginx/nginx.conf]
    depends_on: [backend, frontend]

volumes:
  mysql_data:
```

### 13.2 K8s 生产部署（可选）

```
┌───────────────────────────────────────┐
│              K8s Cluster              │
│  ┌─────────┐  ┌─────────┐  ┌───────┐ │
│  │ Backend │  │Frontend │  │ Nginx │ │
│  │  Pod×3  │  │  Pod×2  │  │Ingress│ │
│  └─────────┘  └─────────┘  └───────┘ │
│  ┌─────────┐  ┌─────────────────────┐ │
│  │  MySQL  │  │  Redis + RabbitMQ   │ │
│  │  State  │  │      StatefulSet    │ │
│  └─────────┘  └─────────────────────┘ │
└───────────────────────────────────────┘
```

---

## 14. 开发规范

### 14.1 后端

| 规范 | 说明 |
|------|------|
| 分层 | Controller → Service → Repository，单向依赖 |
| 异常 | 统一 GlobalExceptionHandler，业务异常用 ErrorCode 枚举 |
| 响应 | 统一 `ApiResponse<T>` |
| 日志 | `@Slf4j`，关键操作 info，异常 warn/error |
| 事务 | Service 层 `@Transactional(readOnly = true)` 默认只读 |
| 校验 | `@Valid` 双重校验（前端 + 后端） |
| 文档 | 所有公开 API 加 Swagger 注解 |
| 异步 | 非核心逻辑用 `@Async` + Spring Events |

### 14.2 前端

| 规范 | 说明 |
|------|------|
| 组件 | `<script setup lang="ts">` |
| 状态 | Pinia（客户端）+ TanStack Query（服务端） |
| 请求 | `api/` 目录模块化，Axios 统一拦截 |
| 路由 | Nuxt 自动路由 + `middleware/auth.ts` 守卫 |
| 样式 | UnoCSS 原子类 + Element Plus |
| 类型 | TypeScript 严格模式 |
| 国际化 | Vue I18n，key 命名 `模块.字段` |

### 14.3 Git 提交

```
FEAT：新功能    FIX：修复    DOC：文档
REFACT：重构    PERF：性能    TEST：测试
CHORE：构建    STYLE：格式
```

---

## 15. 版本演进路线

```
v1.0 ✅ 已完成
  └─ 单体 MVC + Thymeleaf + JWT + BCrypt
  └─ 6 个 REST API + 3 个前端页面
  └─ 登录/注册/退出 + 限流 + 日志

v2.0 📋 架构方案
  └─ 前后端分离（Spring Boot + Vue 3 SPA）
  └─ Swagger / Knife4j API 文档
  └─ Element Plus UI + Pinia + Axios

v3.0 🎯 前沿升级（本章案）
  └─ Java 21 Virtual Threads + Pattern Matching
  └─ Nuxt 3 SSR/SSG 混合渲染
  └─ Redis 分布式限流 + Spring Cache
  └─ Flyway 数据库版本迁移
  └─ GraphQL 双协议（REST + GraphQL）
  └─ OAuth2 第三方登录 + TOTP MFA
  └─ Micrometer + OpenTelemetry + Grafana
  └─ GraalVM Native Image（毫秒启动）
  └─ Testcontainers + Playwright + PIT
  └─ Docker 多阶段构建 + K8s 部署
  └─ GitHub Actions CI/CD 全自动流水线
  └─ 异步事件驱动（Spring Events + RabbitMQ）
```

---

*文档版本 v3.0 &emsp; 架构师：HoranLee &emsp; 最后更新：2026-06-07*
