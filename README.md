# Demo 用户认证系统

> **版本**：v2.0 &emsp; **状态**：架构升级中 &emsp; **架构模式**：前后端分离

---

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 技术栈](#2-技术栈)
- [3. 系统架构](#3-系统架构)
- [4. 项目结构](#4-项目结构)
- [5. 后端设计](#5-后端设计)
- [6. 前端设计](#6-前端设计)
- [7. API 文档（Swagger）](#7-api-文档swagger)
- [8. 数据库设计](#8-数据库设计)
- [9. 安全体系](#9-安全体系)
- [10. 部署架构](#10-部署架构)
- [11. 开发规范](#11-开发规范)

---

## 1. 项目概述

### 1.1 项目定位

Demo 用户认证系统是一个基于 **Spring Boot + Vue 3** 的前后端分离项目，提供用户注册、登录、会话管理和退出等完整的身份认证闭环。系统遵循 RESTful 规范，采用 JWT 无状态认证，支持 Swagger 自动生成 API 文档。

### 1.2 功能矩阵

| 模块 | 功能点 | 说明 |
|------|--------|------|
| 🔐 认证 | 登录 / 退出 / 会话校验 | JWT + HttpOnly Cookie |
| 📝 注册 | 用户注册 / 唯一性校验 | 实时校验 + 密码强度 |
| 👤 用户 | 个人信息 / 登录日志 | 安全审计 |
| 📋 文档 | Swagger / OpenAPI 3 | 自动生成、在线调试 |

---

## 2. 技术栈

### 2.1 后端技术栈

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 基础框架 | Spring Boot | 4.0.6 | 应用框架 |
| 语言 | Java | 21 | LTS 版本 |
| Web 层 | Spring MVC (WebMvc) | — | REST 控制器 |
| 安全框架 | Spring Security | 7.x | 认证与授权 |
| Token | jjwt | 0.12.6 | JWT 生成与校验 |
| ORM | Spring Data JPA + Hibernate | — | 对象关系映射 |
| 数据库 | MySQL | 9.7 | 主存储 |
| 缓存 | Redis（可选） | 7.x | Token 黑名单 / 限流 |
| API 文档 | Knife4j (Swagger) | 4.x | OpenAPI 3.0 文档 |
| 校验 | Jakarta Validation | — | DTO 参数校验 |
| 工具库 | Lombok | 1.18+ | 减少样板代码 |
| 构建工具 | Maven | 3.8+ | 依赖管理 |

### 2.2 前端技术栈

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 框架 | Vue | 3.4+ | Composition API |
| 构建工具 | Vite | 5.x | 极速开发体验 |
| 语言 | TypeScript | 5.x | 类型安全 |
| 路由 | Vue Router | 4.x | SPA 路由 |
| 状态管理 | Pinia | 2.x | 轻量状态管理 |
| HTTP 客户端 | Axios | 1.x | 拦截器封装 |
| UI 组件库 | Element Plus | 2.x | 企业级 UI |
| CSS 方案 | UnoCSS / Tailwind CSS | — | 原子化 CSS |
| 代码规范 | ESLint + Prettier | — | 代码格式化 |
| 包管理 | pnpm | — | 高效依赖管理 |

### 2.3 开发工具

| 工具 | 用途 |
|------|------|
| IntelliJ IDEA / VS Code | IDE |
| Git + GitHub | 版本控制 |
| Postman / Swagger UI | API 调试 |
| Docker | 容器化部署 |
| MySQL Workbench | 数据库管理 |

---

## 3. 系统架构

### 3.1 架构全景图

```
┌─────────────────────────────────────────────────────────────┐
│                       Nginx (反向代理)                        │
│                   http://localhost  →  统一入口                │
└──────────────┬────────────────────────┬─────────────────────┘
               │                        │
        /api/* │                        │ /*
               ▼                        ▼
┌──────────────────────┐    ┌──────────────────────┐
│   后端 (Spring Boot)   │    │   前端 (Vue 3 + Vite)  │
│    Port: 8080         │    │   Port: 5173 (dev)    │
│                      │    │                      │
│  ┌────────────────┐  │    │  ┌────────────────┐  │
│  │  Controller 层  │  │    │  │   View 页面    │  │
│  │  (REST API)    │  │    │  │  (Vue Router)  │  │
│  ├────────────────┤  │    │  ├────────────────┤  │
│  │   Service 层    │  │    │  │  Component 组件 │  │
│  │  (业务逻辑)     │  │    │  │  (Element Plus) │  │
│  ├────────────────┤  │    │  ├────────────────┤  │
│  │  Repository 层  │  │    │  │   Store 状态    │  │
│  │  (数据访问)     │  │    │  │   (Pinia)      │  │
│  ├────────────────┤  │    │  ├────────────────┤  │
│  │   Security 层   │  │    │  │   API 请求层    │  │
│  │  (认证过滤)     │  │    │  │   (Axios)      │  │
│  └───────┬────────┘  │    │  └────────────────┘  │
│          │            │    └──────────────────────┘
└──────────┼────────────┘
           │
    ┌──────┴──────┐    ┌──────────────┐
    │   MySQL      │    │  Redis (可选)  │
    │   Port: 3306 │    │  Port: 6379   │
    └─────────────┘    └──────────────┘
```

### 3.2 请求流程

```
浏览器 (Vue SPA)
    │
    │ ① 用户操作
    ▼
Axios 拦截器 (自动附加 Cookie)
    │
    │ ② HTTP Request (JSON)
    ▼
Nginx (路由分发、静态资源)
    │
    ├── /api/* → Spring Boot (8080)
    │       │
    │       ├── JwtAuthFilter (JWT 校验)
    │       │       │
    │       │       └── 解析 Cookie → 验证 → 设置 SecurityContext
    │       │
    │       ├── Controller (参数校验 @Valid)
    │       │       │
    │       │       └── Service (业务逻辑)
    │       │               │
    │       │               ├── Repository (数据访问)
    │       │               │       └── MySQL
    │       │               │
    │       │               └── 登录日志记录
    │       │
    │       └── ③ JSON Response
    │
    └── /* → Vue SPA (5173 dev / Nginx static prod)
            │
            └── ④ 页面渲染 / 路由跳转
```

### 3.3 认证流程

```
┌──────────┐     ┌──────────────┐     ┌──────────────┐
│  登录请求  │ ──▶ │ AuthService  │ ──▶ │  UserRepository │
│ POST /api │     │ .login()     │     │  .findByXXX() │
│ /auth/    │     │              │     └──────┬───────┘
│ login     │     │ ① 查找用户    │            │
└───────────┘     │ ② BCrypt校验  │◀───────────┘
                  │ ③ 检查状态    │
                  │ ④ 记录日志    │
                  │ ⑤ 生成JWT     │
                  │ ⑥ 写入Cookie  │
                  └──────┬───────┘
                         │
                         ▼
                  ┌──────────────┐
                  │  Set-Cookie: │
                  │  auth_token  │
                  │  HttpOnly ✓  │
                  │  SameSite    │
                  │  =Strict     │
                  └──────────────┘

        ═══════ 后续请求 ═══════

┌──────────┐     ┌──────────────┐     ┌──────────────┐
│  API 请求  │ ──▶ │ JwtAuthFilter│ ──▶ │ SecurityContext│
│ (带Cookie) │     │              │     │ .setAuth()    │
└───────────┘     │ ① 提取Token  │     └──────────────┘
                  │ ② 验证签名    │
                  │ ③ 查用户状态  │
                  │ ④ 设置上下文  │
                  └──────────────┘
```

---

## 4. 项目结构

### 4.1 完整目录树

```
cc_project/
├── backend/                              # 后端 Spring Boot 工程
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/demo/
│       │   ├── DemoApplication.java       # 启动类
│       │   ├── config/                    # 配置类
│       │   │   ├── SecurityConfig.java    # Spring Security 配置
│       │   │   ├── SwaggerConfig.java     # Knife4j / Swagger 配置
│       │   │   ├── CorsConfig.java        # 跨域配置
│       │   │   └── WebMvcConfig.java      # Web MVC 配置
│       │   ├── controller/                # 控制器层
│       │   │   ├── AuthController.java    # 认证接口
│       │   │   ├── UserController.java    # 用户接口
│       │   │   └── GlobalExceptionHandler.java  # 全局异常处理
│       │   ├── service/                   # 服务层（接口）
│       │   │   ├── AuthService.java
│       │   │   └── UserService.java
│       │   ├── service/impl/              # 服务层（实现）
│       │   │   ├── AuthServiceImpl.java
│       │   │   └── UserServiceImpl.java
│       │   ├── repository/                # 数据访问层
│       │   │   ├── UserRepository.java
│       │   │   └── LoginLogRepository.java
│       │   ├── entity/                    # 数据库实体
│       │   │   ├── User.java
│       │   │   └── LoginLog.java
│       │   ├── model/                     # DTO / VO
│       │   │   ├── request/               # 请求体
│       │   │   │   ├── LoginRequest.java
│       │   │   │   └── RegisterRequest.java
│       │   │   ├── response/              # 响应体
│       │   │   │   ├── LoginResponse.java
│       │   │   │   ├── UserInfo.java
│       │   │   │   └── ApiResponse.java
│       │   │   └── enums/                 # 枚举
│       │   │       └── ErrorCode.java     # 错误码枚举
│       │   ├── security/                  # 安全组件
│       │   │   ├── JwtUtil.java           # JWT 工具
│       │   │   └── JwtAuthFilter.java     # JWT 过滤器
│       │   └── util/                      # 通用工具
│       │       └── IpUtil.java            # IP 获取工具
│       └── resources/
│           ├── application.yml            # 主配置
│           ├── application-dev.yml        # 开发环境
│           ├── application-prod.yml       # 生产环境
│           └── db/
│               └── schema.sql             # 数据库初始化
│
├── frontend/                              # 前端 Vue 3 工程
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── index.html
│   └── src/
│       ├── main.ts                        # 入口
│       ├── App.vue                        # 根组件
│       ├── router/
│       │   └── index.ts                   # 路由配置
│       ├── stores/
│       │   ├── auth.ts                    # 认证状态 (Pinia)
│       │   └── user.ts                    # 用户状态
│       ├── api/
│       │   ├── request.ts                 # Axios 实例 + 拦截器
│       │   ├── auth.ts                    # 认证 API
│       │   └── user.ts                    # 用户 API
│       ├── views/
│       │   ├── LoginView.vue              # 登录页
│       │   ├── RegisterView.vue           # 注册页
│       │   └── DashboardView.vue          # 仪表盘
│       ├── components/
│       │   ├── PasswordStrength.vue       # 密码强度组件
│       │   ├── AuthCard.vue               # 认证卡片布局
│       │   └── AppHeader.vue              # 顶部导航
│       ├── composables/
│       │   ├── useAuth.ts                 # 认证逻辑 hook
│       │   └── useValidation.ts           # 校验逻辑 hook
│       ├── utils/
│       │   └── validators.ts              # 校验工具函数
│       ├── types/
│       │   └── index.ts                   # TypeScript 类型定义
│       └── styles/
│           ├── index.scss                 # 全局样式
│           └── variables.scss             # 样式变量
│
├── docs/
│   └── api.md                             # API 文档
├── docker-compose.yml                     # Docker 编排
├── README.md                              # 项目文档
└── .gitignore
```

---

## 5. 后端设计

### 5.1 分层架构

```
┌──────────────────────────────────────────┐
│             Controller 层                 │
│  · 接收 HTTP 请求                         │
│  · @Valid 参数校验                        │
│  · 调用 Service                           │
│  · 返回 ApiResponse<T>                    │
├──────────────────────────────────────────┤
│             Service 层（接口+实现）         │
│  · 业务逻辑编排                           │
│  · 事务管理 @Transactional                 │
│  · 异常抛出（AuthException 等）            │
│  · 日志记录                               │
├──────────────────────────────────────────┤
│            Repository 层                  │
│  · Spring Data JPA                        │
│  · 自定义 @Query                          │
│  · 数据库 CRUD                            │
├──────────────────────────────────────────┤
│            Security 层                    │
│  · JWT 生成 / 验证                        │
│  · OncePerRequestFilter 认证              │
│  · SecurityContext 设置                   │
└──────────────────────────────────────────┘
```

### 5.2 核心类设计

```java
// ==================== 实体层 ====================

@Entity
@Table(name = "t_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    @JsonIgnore  // 序列化时排除
    private String passwordHash;

    @Column(length = 50)
    private String nickname;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Integer emailVerified = 0;

    @Column(name = "register_ip", length = 45)
    private String registerIp;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// ==================== DTO 层 ====================

// 请求体示例 —— 带 Swagger 注解
@Schema(description = "登录请求")
public record LoginRequest(
    @Schema(description = "用户名或邮箱", example = "admin",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "账号不能为空")
    String account,

    @Schema(description = "密码", example = "123456",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    String password,

    @Schema(description = "记住我（延长至7天）", example = "true",
            defaultValue = "false")
    boolean rememberMe
) {}

// 统一响应体
@Schema(description = "统一 API 响应")
public record ApiResponse<T>(
    @Schema(description = "状态码，200=成功", example = "200")
    int code,

    @Schema(description = "提示信息", example = "success")
    String message,

    @Schema(description = "响应数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
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

public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    UNAUTHORIZED(401, "未登录或会话已过期"),
    USERNAME_EXISTS(1001, "用户名已存在"),
    EMAIL_EXISTS(1002, "邮箱已注册"),
    PASSWORD_WEAK(1003, "密码强度不足"),
    USERNAME_INVALID(1004, "用户名格式错误"),
    EMAIL_INVALID(1005, "邮箱格式错误"),
    AGREEMENT_UNCHECKED(1006, "用户协议未同意"),
    REGISTER_TOO_FREQUENT(1007, "注册过于频繁，请稍后再试"),
    LOGIN_FAILED(401, "账号或密码错误"),
    ACCOUNT_DISABLED(401, "账号已被禁用"),
    ACCOUNT_LOCKED(401, "账号已被锁定"),
    RATE_LIMITED(401, "请求过于频繁，请稍后再试");

    private final int code;
    private final String message;
}

// ==================== 服务层 ====================

@Service
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {
    // 登录逻辑
    // 注册逻辑
    // 密码 BCrypt 加密
    // 速率限制
    // 日志记录
}

// ==================== 控制器层（Swagger 注解） ====================

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "登录、注册、退出、会话校验")
public class AuthController {

    @Operation(summary = "用户登录",
               description = "使用用户名/邮箱和密码登录，返回 JWT Cookie")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "401", description = "账号或密码错误")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) { /* ... */ }

    @Operation(summary = "用户注册",
               description = "创建新账号，密码使用 BCrypt 加密")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "注册成功"),
        @ApiResponse(responseCode = "400",
                     description = "参数校验失败，返回错误码 1xxx")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
        @Valid @RequestBody RegisterRequest request,
        HttpServletRequest httpRequest
    ) { /* ... */ }

    @Operation(summary = "退出登录", description = "清除 JWT Cookie")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
        HttpServletResponse response
    ) { /* ... */ }

    @Operation(summary = "获取当前用户",
               description = "返回当前登录用户信息（不含密码）")
    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse<UserInfo>> currentUser() { /* ... */ }

    @Operation(summary = "会话校验", description = "检查登录状态")
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> check() { /* ... */ }

    @Operation(summary = "唯一性校验",
               description = "检查用户名或邮箱是否已被注册")
    @GetMapping("/check-availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
        @Parameter(description = "校验类型", example = "username",
                   required = true)
        @RequestParam String type,
        @Parameter(description = "校验值", example = "zhangsan",
                   required = true)
        @RequestParam String value
    ) { /* ... */ }
}
```

### 5.3 Swagger / Knife4j 配置

```java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Demo 用户认证系统 API")
                .version("v2.0")
                .description("基于 Spring Boot + Vue 3 的用户认证系统")
                .contact(new Contact()
                    .name("HoranLee")
                    .url("https://github.com/HoranLee/cc_project")))
            .addSecurityItem(new SecurityRequirement().addList("Cookie"))
            .externalDocs(new ExternalDocumentation()
                .description("完整文档")
                .url("https://github.com/HoranLee/cc_project"));
    }
}
```

访问地址：`http://localhost:8080/doc.html`（Knife4j 增强 UI）

---

## 6. 前端设计

### 6.1 前端架构设计

```
┌──────────────────────────────────────────────┐
│                  Vue 3 App                    │
├──────────────────────────────────────────────┤
│  router/index.ts         路由守卫 + 权限控制     │
│  ┌────────────┬────────────┬─────────────┐    │
│  │ /login     │ /register  │ /dashboard  │    │
│  │ LoginView  │ RegisterV. │ DashboardV. │    │
│  └────────────┴────────────┴─────────────┘    │
├──────────────────────────────────────────────┤
│  stores/                   Pinia 状态管理      │
│  ┌──────────────┐  ┌───────────────────┐      │
│  │ useAuthStore │  │  useUserStore     │      │
│  │ · token      │  │  · userInfo       │      │
│  │ · isLoggedIn │  │  · fetchUser()    │      │
│  │ · login()    │  │  · clearUser()    │      │
│  │ · logout()   │  │                   │      │
│  └──────────────┘  └───────────────────┘      │
├──────────────────────────────────────────────┤
│  api/                     Axios 请求封装       │
│  ┌──────────────────────────────────────┐     │
│  │ request.ts  · 基础URL /api           │     │
│  │             · 自动携带 Cookie         │     │
│  │             · 401 拦截 → 跳转登录      │     │
│  │             · 请求/响应日志            │     │
│  ├──────────────────────────────────────┤     │
│  │ auth.ts     · login() / register()   │     │
│  │ user.ts     · getUserInfo() / check()│     │
│  └──────────────────────────────────────┘     │
├──────────────────────────────────────────────┤
│  composables/             组合式函数 (Hooks)   │
│  ┌──────────────────────────────────────┐     │
│  │ useAuth.ts    · login / logout 逻辑   │     │
│  │ useValidation.ts · 表单校验逻辑       │     │
│  └──────────────────────────────────────┘     │
├──────────────────────────────────────────────┤
│  components/              可复用组件          │
│  ┌──────────────────────────────────────┐     │
│  │ PasswordStrength.vue  · 密码强度指示   │     │
│  │ AuthCard.vue          · 认证卡片布局   │     │
│  │ AppHeader.vue         · 顶部导航栏     │     │
│  └──────────────────────────────────────┘     │
└──────────────────────────────────────────────┘
```

### 6.2 核心代码示例

```typescript
// ==================== src/api/request.ts ====================

import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true,   // 自动携带 Cookie
  headers: { 'Content-Type': 'application/json' }
})

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      router.push('/login')
      ElMessage.warning('会话已过期，请重新登录')
    }
    return Promise.reject(error)
  }
)

export default request

// ==================== src/api/auth.ts ====================

import request from './request'
import type { LoginRequest, RegisterRequest, LoginResponse, UserInfo } from '@/types'

export const authApi = {
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/auth/login', data)
  },
  register(data: RegisterRequest): Promise<ApiResponse<void>> {
    return request.post('/auth/register', data)
  },
  logout(): Promise<ApiResponse<void>> {
    return request.post('/auth/logout')
  },
  getCurrentUser(): Promise<ApiResponse<UserInfo>> {
    return request.get('/auth/current-user')
  },
  check(): Promise<ApiResponse<boolean>> {
    return request.get('/auth/check')
  },
  checkAvailability(type: 'username' | 'email', value: string) {
    return request.get('/auth/check-availability', { params: { type, value } })
  }
}

// ==================== src/stores/auth.ts ====================

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { UserInfo } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!user.value)

  async function login(account: string, password: string, rememberMe: boolean) {
    const res = await authApi.login({ account, password, rememberMe })
    if (res.code === 200) {
      await fetchUser()
    }
    return res
  }

  async function fetchUser() {
    const res = await authApi.getCurrentUser()
    if (res.code === 200) {
      user.value = res.data!
    }
  }

  async function logout() {
    await authApi.logout()
    user.value = null
  }

  return { user, isLoggedIn, login, fetchUser, logout }
})

// ==================== src/router/index.ts ====================

import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guest: true }      // 仅未登录用户可访问
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { guest: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { requiresAuth: true } // 需登录
  },
  {
    path: '/',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth) {
    if (!authStore.isLoggedIn) {
      return next('/login')
    }
  }

  if (to.meta.guest && authStore.isLoggedIn) {
    return next('/dashboard')
  }

  next()
})

export default router
```

### 6.3 组件设计

```vue
<!-- ==================== src/views/LoginView.vue ==================== -->

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import AuthCard from '@/components/AuthCard.vue'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  account: '',
  password: '',
  rememberMe: false
})

const rules = {
  account: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await authStore.login(
      form.account, form.password, form.rememberMe
    )
    if (res.code === 200) {
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(res.message || '账号或密码错误')
    }
  } catch {
    ElMessage.error('网络异常，请稍后再试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthCard title="欢迎回来" subtitle="请使用您的账号登录">
    <el-form ref="formRef" :model="form" :rules="rules"
             @keyup.enter="handleLogin">
      <el-form-item prop="account">
        <el-input v-model="form.account" placeholder="用户名或邮箱"
                  prefix-icon="User" />
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="form.password" type="password"
                  placeholder="密码" prefix-icon="Lock" show-password />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="form.rememberMe">记住我</el-checkbox>
        <el-link type="primary" class="float-right">忘记密码？</el-link>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" class="w-full" :loading="loading"
                   @click="handleLogin">
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>
      </el-form-item>
    </el-form>
    <div class="text-center">
      还没有账号？<el-link type="primary" href="/register">立即注册</el-link>
    </div>
  </AuthCard>
</template>
```

---

## 7. API 文档（Swagger）

### 7.1 接口总览

| 方法 | URL | 摘要 | 认证 |
|------|-----|------|------|
| `POST` | `/api/auth/login` | 用户登录 | 否 |
| `POST` | `/api/auth/register` | 用户注册 | 否 |
| `GET` | `/api/auth/check-availability` | 唯一性校验 | 否 |
| `POST` | `/api/auth/logout` | 退出登录 | 是 |
| `GET` | `/api/auth/current-user` | 获取当前用户 | 是 |
| `GET` | `/api/auth/check` | 会话校验 | 是 |

### 7.2 错误码规范

| 错误码 | 说明 | HTTP 状态码 |
|--------|------|------------|
| 200 | 成功 | 200 |
| 400 | 参数校验失败 | 400 |
| 401 | 未登录 / 登录失败 | 401 |
| 500 | 服务器内部错误 | 500 |
| 1001 | 用户名已存在 | 400 |
| 1002 | 邮箱已注册 | 400 |
| 1003 | 密码强度不足 | 400 |
| 1004 | 用户名格式错误 | 400 |
| 1005 | 邮箱格式错误 | 400 |
| 1006 | 用户协议未同意 | 400 |
| 1007 | 注册过于频繁 | 400 |

### 7.3 Swagger 在线访问

| 环境 | 地址 |
|------|------|
| Knife4j 增强 UI | `http://localhost:8080/doc.html` |
| Swagger UI 原生 | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

---

## 8. 数据库设计

### 8.1 用户表（`t_user`）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT UNSIGNED | PK, AUTO_INCREMENT | 主键 |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE | 登录用户名 |
| `email` | VARCHAR(100) | NOT NULL, UNIQUE | 邮箱 |
| `password_hash` | VARCHAR(255) | NOT NULL | BCrypt 哈希 |
| `nickname` | VARCHAR(50) | NULL | 显示昵称 |
| `avatar_url` | VARCHAR(255) | NULL | 头像 URL |
| `status` | TINYINT | NOT NULL, DEFAULT 1 | 0=禁用 / 1=正常 |
| `email_verified` | TINYINT | NOT NULL, DEFAULT 0 | 邮箱验证状态 |
| `register_ip` | VARCHAR(45) | NULL | 注册 IP |
| `last_login_time` | DATETIME | NULL | 最后登录时间 |
| `last_login_ip` | VARCHAR(45) | NULL | 最后登录 IP |
| `created_at` | DATETIME | NOT NULL | 注册时间 |
| `updated_at` | DATETIME | NOT NULL | 更新时间 |

### 8.2 登录日志表（`t_login_log`）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT UNSIGNED | PK, AUTO_INCREMENT | 主键 |
| `user_id` | BIGINT UNSIGNED | NULL | 关联用户 |
| `login_account` | VARCHAR(100) | NOT NULL | 登录账号 |
| `login_result` | TINYINT | NOT NULL | 0=失败 / 1=成功 |
| `fail_reason` | VARCHAR(100) | NULL | 失败原因 |
| `ip_address` | VARCHAR(45) | NOT NULL | 请求 IP |
| `user_agent` | VARCHAR(255) | NULL | 浏览器 UA |
| `created_at` | DATETIME | NOT NULL | 记录时间 |

---

## 9. 安全体系

| 层面 | 策略 | 实现 |
|------|------|------|
| 传输层 | HTTPS（生产）+ CORS 白名单 | Nginx / Spring CORS |
| 认证层 | JWT + HttpOnly Cookie | JwtAuthFilter |
| 密码 | BCrypt (cost=10) | BCryptPasswordEncoder |
| 会话 | 无状态 STATELESS | Spring Security |
| 限流 | IP 维度（登录 5/min、注册 3/min） | ConcurrentHashMap |
| 防暴力 | 5 次失败锁账号 | AuthServiceImpl |
| 审计 | 每次登录/注册写入 t_login_log | LoginLogRepository |
| 注入防护 | Prepared Statement + XSS 过滤 | JPA / Hibernate |

---

## 10. 部署架构

### 10.1 开发环境

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:9.7
    environment:
      MYSQL_ROOT_PASSWORD: hello
      MYSQL_DATABASE: cc_db
    ports:
      - "3306:3306"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis

  frontend:
    build: ./frontend
    ports:
      - "5173:5173"
    depends_on:
      - backend
```

### 10.2 生产环境

```
                     ┌──────────────────┐
                     │   Nginx (443)     │
                     │   SSL 终结        │
                     │   gzip 压缩       │
                     │   静态资源缓存     │
                     └────┬────────┬────┘
                          │        │
                 /api/*   │        │ /*
                          ▼        ▼
              ┌──────────┐  ┌──────────────┐
              │ Backend  │  │  Frontend     │
              │ :8080    │  │  /usr/share/  │
              │ (Java)   │  │  nginx/html   │
              └────┬─────┘  └──────────────┘
                   │
           ┌───────┴───────┐
           ▼               ▼
     ┌──────────┐    ┌──────────┐
     │  MySQL    │    │  Redis    │
     │  :3306    │    │  :6379    │
     └──────────┘    └──────────┘
```

Nginx 配置要点：
- `/api/*` → `proxy_pass http://backend:8080`
- `/*` → 前端静态文件（`try_files $uri /index.html`）
- 启用 HTTPS + HTTP/2
- CORS 头由 Nginx 统一处理

---

## 11. 开发规范

### 11.1 后端规范

| 规范 | 说明 |
|------|------|
| 命名 | Controller 以 `Controller` 结尾，Service 接口不加后缀、实现加 `Impl` |
| 分层 | Controller 只做协议转换，Service 承载业务逻辑 |
| 异常 | 统一通过 `GlobalExceptionHandler` 处理 |
| 响应 | 统一使用 `ApiResponse<T>` 包装 |
| 日志 | 使用 `@Slf4j`，关键操作记录 info，异常记录 warn/error |
| 事务 | Service 层 `@Transactional` |
| 校验 | 前端校验体验 + 后端 `@Valid` 双重校验 |
| 文档 | 所有公开 API 加 Swagger 注解 |

### 11.2 前端规范

| 规范 | 说明 |
|------|------|
| 组件 | 单文件组件 `<script setup lang="ts">` |
| 状态 | Pinia Store，按模块拆分 |
| 请求 | 统一使用 `api/` 目录下的模块化 API |
| 路由 | 路由守卫控制权限（`requiresAuth` / `guest`） |
| 样式 | 优先使用 Element Plus 组件，自定义样式用 Scoped |
| 类型 | TypeScript 严格模式，类型定义统一放在 `types/` |
| 目录 | views（页面）/ components（组件）/ composables（逻辑）/ stores（状态） |

### 11.3 Git 提交规范

```
<type>：<subject>

type:
  FEAT    新功能
  FIX     修复 Bug
  DOC     文档变更
  STYLE   代码格式（不影响功能）
  REFACT  重构（既不是新功能也不是修复）
  PERF    性能优化
  TEST    测试相关
  CHORE   构建/工具变动
```

---

*文档版本 v2.0 &emsp; 架构师：HoranLee &emsp; 最后更新：2026-06-07*
