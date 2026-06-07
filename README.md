# Demo 用户认证系统

> **版本**：v2.5 &emsp; **状态**：待实施 &emsp; **原则**：务实可落地，每一项都能本地启动运行

---

## 目录

- [1. 版本定位](#1-版本定位)
- [2. 技术栈](#2-技术栈)
- [3. 系统架构](#3-系统架构)
- [4. 项目结构](#4-项目结构)
- [5. 升级清单（基于 v1.0）](#5-升级清单基于-v10)
- [6. 后端升级详解](#6-后端升级详解)
- [7. 前端工程详解](#7-前端工程详解)
- [8. API 文档](#8-api-文档)
- [9. 数据库设计](#9-数据库设计)
- [10. 安全体系](#10-安全体系)
- [11. 部署方案](#11-部署方案)
- [12. 本地启动步骤](#12-本地启动步骤)
- [13. 实施计划](#13-实施计划)

---

## 1. 版本定位

### 1.1 三版对比

```
v1.0 ✅          v2.5 🎯                 v3.0 📋
已交付            务实升级                 前沿蓝图
────────         ───────────             ──────────
单体 MVC         前后端分离               云原生全栈
Thymeleaf        Vue 3 SPA              Nuxt 3 SSR
无文档            Swagger                 GraphQL
单机              Docker Compose          K8s + GraalVM
```

### 1.2 v2.5 选型原则

> **每一项都满足三个条件：① 本地可启动 ② 改动成本低 ③ 有实际收益**

| 纳入 | 理由 | 搁置（v3.0） | 理由 |
|------|------|-------------|------|
| Virtual Threads | 一行配置 | GraalVM | 配置复杂，本地编译慢 |
| Flyway | 加依赖即可 | — | — |
| Knife4j Swagger | 加依赖+1个配置类 | GraphQL | 双协议维护成本高 |
| Vue 3 + Vite SPA | 独立工程，不依赖 Node 服务端 | Nuxt 3 SSR | 需额外 Node Server |
| Docker Compose | 一键启动 | K8s | 本地不可行 |
| ErrorCode 枚举 | 建一个文件 | — | — |

---

## 2. 技术栈

### 2.1 后端

| 类别 | 技术 | 版本 | 用途 | 新增/已有 |
|------|------|------|------|----------|
| 框架 | Spring Boot | 4.0.6 | 应用基础 | 已有 |
| 语言 | Java | 21 | Virtual Threads 支持 | 已有 |
| Web | Spring MVC | — | REST 控制器 | 已有 |
| 安全 | Spring Security | 7.x | 认证授权 | 已有 |
| JWT | jjwt | 0.12.6 | Token 生成 | 已有 |
| ORM | Spring Data JPA | — | 数据访问 | 已有 |
| 数据库 | MySQL | 9.7 | 主存储 | 已有 |
| 数据库迁移 | **Flyway** | 10.x | SQL 版本化管理 | **新增** |
| API 文档 | **Knife4j + SpringDoc** | 4.x | Swagger 在线文档+调试 | **新增** |
| 密码 | BCrypt (Spring Security) | — | 密码哈希 | 已有 |
| 校验 | Jakarta Validation | — | DTO 校验 | 已有 |
| 并发 | **Virtual Threads** | Java 21 | 一行开启，并发数 x10 | **新增** |
| 构建 | Maven | 3.8+ | 依赖管理 | 已有 |
| 工具 | Lombok | 1.18+ | 减少样板代码 | 已有 |

### 2.2 前端

| 类别 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 框架 | **Vue** | 3.4+ | Composition API |
| 构建 | **Vite** | 5.x | 极速 HMR |
| 语言 | **TypeScript** | 5.x | 类型安全 |
| 路由 | **Vue Router** | 4.x | SPA 路由 + 导航守卫 |
| 状态管理 | **Pinia** | 2.x | 轻量状态管理 |
| HTTP | **Axios** | 1.x | 请求拦截 + Cookie 自动携带 |
| UI 库 | **Element Plus** | 2.x | 企业级组件 |
| 测试 | **Vitest** | 1.x | 单元测试 |
| 包管理 | pnpm | 9.x | 快速依赖管理 |

### 2.3 基础设施

| 组件 | 用途 | 运行方式 |
|------|------|---------|
| MySQL 9.7 | 数据存储 | Docker 容器 |
| Nginx | 反向代理 + 静态资源 | Docker 容器 |
| Backend | Spring Boot | 宿主机 / Docker |
| Frontend | Vite Dev Server | 宿主机 (dev) / Nginx (prod) |

---

## 3. 系统架构

### 3.1 架构全景

```
                http://localhost
                      │
              ┌───────▼───────┐
              │    Nginx:80    │  反向代理 + 静态资源
              │  /api/* → 后端 │
              │  /*     → 前端 │
              └───┬───────┬───┘
                  │       │
       /api/*     │       │  /*
                  ▼       ▼
    ┌─────────────┐  ┌──────────────┐
    │   Backend   │  │   Frontend    │
    │   :8080     │  │   Vite :5173  │
    │             │  │   (开发模式)   │
    │ Spring Boot │  │   或 Nginx    │
    │ + Swagger   │  │   (生产模式)   │
    │ + Flyway    │  │              │
    └──────┬──────┘  └──────────────┘
           │
    ┌──────▼──────┐
    │   MySQL:3306 │
    │   Docker     │
    └─────────────┘
```

### 3.2 请求流程

```
浏览器访问 http://localhost
  │
  ▼
Nginx 路由分发
  ├── /api/auth/*  → 后端 :8080
  │     ├── JwtAuthFilter   (Cookie → JWT → SecurityContext)
  │     ├── @Valid 校验     (参数验证)
  │     ├── Service         (业务逻辑 + @Transactional)
  │     └── JSON Response   (ApiResponse<T>)
  │
  └── /*  → 前端静态文件
        └── Vue SPA → Vue Router → 页面渲染
             └── Axios → /api/* → 后端
```

### 3.3 认证流程

```
登录:  前端提交 → POST /api/v1/auth/login
         → AuthService.login()
           → ① 查找用户 (JPA)
           → ② BCrypt 密码比对
           → ③ 更新 last_login_time / ip
           → ④ 生成 JWT (Virtual Thread 处理)
           → ⑤ 写入 HttpOnly Cookie
           → ⑥ 返回 UserInfo

后续:  每个请求 → JwtAuthFilter
         → 从 Cookie 提取 JWT
         → 验证签名 + 过期
         → 查询用户状态
         → 设置 SecurityContext
         → Controller 处理
```

---

## 4. 项目结构

```
cc_project/
├── backend/                               # 后端 Spring Boot
│   ├── pom.xml                            # 新增：Flyway、Knife4j 依赖
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/example/demo/
│       │   ├── DemoApplication.java
│       │   ├── config/                     # 配置包
│       │   │   ├── SecurityConfig.java
│       │   │   ├── SwaggerConfig.java      # 新增
│       │   │   └── CorsConfig.java         # 新增：跨域
│       │   ├── controller/
│       │   │   ├── AuthController.java     # 加 Swagger 注解
│       │   │   └── GlobalExceptionHandler.java
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
│       │   │   ├── request/                # 拆分
│       │   │   │   ├── LoginRequest.java
│       │   │   │   └── RegisterRequest.java
│       │   │   ├── response/               # 拆分
│       │   │   │   ├── LoginResponse.java
│       │   │   │   ├── UserInfo.java
│       │   │   │   └── ApiResponse.java
│       │   │   └── enums/
│       │   │       └── ErrorCode.java      # 新增
│       │   ├── security/
│       │   │   ├── JwtUtil.java
│       │   │   └── JwtAuthFilter.java
│       │   └── util/
│       │       └── IpUtil.java             # 抽取
│       └── resources/
│           ├── application.yml             # 多环境
│           ├── application-dev.yml
│           └── db/migration/               # Flyway SQL
│               ├── V1__init_t_user.sql
│               └── V2__init_t_login_log.sql
│
├── frontend/                               # 前端 Vue 3
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── index.html
│   └── src/
│       ├── main.ts
│       ├── App.vue
│       ├── router/index.ts                 # 路由 + 守卫
│       ├── stores/
│       │   ├── auth.ts                     # Pinia 认证
│       │   └── user.ts                     # Pinia 用户
│       ├── api/
│       │   ├── request.ts                  # Axios 实例
│       │   └── auth.ts                     # 认证 API
│       ├── views/
│       │   ├── LoginView.vue
│       │   ├── RegisterView.vue
│       │   └── DashboardView.vue
│       ├── components/
│       │   ├── AuthCard.vue                # 认证卡片
│       │   └── PasswordStrength.vue        # 密码强度
│       ├── composables/
│       │   ├── useAuth.ts
│       │   └── useValidation.ts
│       ├── types/index.ts
│       └── styles/
│           └── variables.scss
│
├── docker-compose.yml                      # MySQL + Nginx + Backend + Frontend
├── nginx.conf                              # Nginx 配置
└── README.md
```

---

## 5. 升级清单（基于 v1.0）

### 5.1 后端改动（6 项）

| # | 改动 | 文件 | 工作量 |
|---|------|------|--------|
| 1 | Virtual Threads | `application.yml` +1 行 | 1 分钟 |
| 2 | Flyway 数据库迁移 | `pom.xml` + 移动 SQL 到 `db/migration/` | 10 分钟 |
| 3 | Knife4j Swagger | `pom.xml` + `SwaggerConfig.java` | 15 分钟 |
| 4 | ErrorCode 枚举 | 新建 `ErrorCode.java`，替换硬编码 | 10 分钟 |
| 5 | 包结构优化 | 拆 `model/` → `request/` `response/` `enums/` | 15 分钟 |
| 6 | CORS 跨域 | `CorsConfig.java` | 5 分钟 |

### 5.2 前端新建（7 项）

| # | 内容 | 文件数 | 工作量 |
|---|------|--------|--------|
| 1 | Vite + Vue 3 脚手架 | `package.json`、`vite.config.ts` 等 | 10 分钟 |
| 2 | 路由 + 守卫 | `router/index.ts` | 15 分钟 |
| 3 | Axios 封装 | `api/request.ts`、`api/auth.ts` | 15 分钟 |
| 4 | Pinia Store | `stores/auth.ts` | 15 分钟 |
| 5 | 登录页 | `views/LoginView.vue` | 30 分钟 |
| 6 | 注册页 | `views/RegisterView.vue` | 45 分钟 |
| 7 | 仪表盘 | `views/DashboardView.vue` | 20 分钟 |

### 5.3 基础设施（3 项）

| # | 内容 | 文件 | 工作量 |
|---|------|------|--------|
| 1 | Docker Compose | `docker-compose.yml` | 15 分钟 |
| 2 | Nginx 配置 | `nginx.conf` | 10 分钟 |
| 3 | 后端 Dockerfile | `Dockerfile` | 5 分钟 |

**总工作量**：后端 1 小时 + 前端 2.5 小时 + 基础设施 0.5 小时 ≈ **4 小时**

---

## 6. 后端升级详解

### 6.1 Virtual Threads（虚拟线程）

```yaml
# application.yml
spring:
  threads:
    virtual:
      enabled: true
```

**效果**：Tomcat 请求处理从 200 个平台线程 → 数万虚拟线程，阻塞 I/O 场景（如数据库查询）吞吐量提升 5-10 倍。

**成本**：一行配置，零代码改动。Java 21 内置，无需额外依赖。

### 6.2 Flyway 数据库迁移

```xml
<!-- pom.xml 新增 -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

```sql
-- 将现有 schema.sql 拆为版本化脚本，放到 resources/db/migration/

-- V1__init_t_user.sql（建表语句）
-- V2__init_t_login_log.sql（建表语句）
```

```yaml
# application.yml：Flyway 自动执行，无需手动建表
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: validate   # 改为 validate，由 Flyway 管理表结构
```

**效果**：应用启动时自动执行数据库迁移，版本可追溯、可回滚。

### 6.3 Knife4j / Swagger API 文档

```xml
<!-- pom.xml 新增 -->
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>4.5.0</version>
</dependency>
```

```java
// config/SwaggerConfig.java
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Demo 用户认证系统 API")
                .version("v2.5")
                .description("基于 Spring Boot + Vue 3 的用户认证系统")));
    }
}
```

```java
// Controller 加 Swagger 注解
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证管理", description = "登录、注册、退出、会话校验")
public class AuthController {

    @Operation(summary = "用户登录")
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
}
```

**访问地址**：

| 地址 | 说明 |
|------|------|
| `http://localhost:8080/doc.html` | Knife4j 增强 UI（可在线调试） |
| `http://localhost:8080/v3/api-docs` | OpenAPI 3.0 JSON |

### 6.4 ErrorCode 枚举

```java
// model/enums/ErrorCode.java
@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    UNAUTHORIZED(401, "未登录或会话已过期"),
    LOGIN_FAILED(401, "账号或密码错误"),
    ACCOUNT_DISABLED(401, "账号已被禁用"),
    ACCOUNT_LOCKED(401, "账号已被锁定"),
    RATE_LIMITED(401, "请求过于频繁，请稍后再试"),
    USERNAME_EXISTS(1001, "用户名已存在"),
    EMAIL_EXISTS(1002, "邮箱已注册"),
    PASSWORD_WEAK(1003, "密码强度不足：至少8位，包含字母和数字"),
    USERNAME_INVALID(1004, "用户名格式错误：需4~20位字母/数字/下划线，字母开头"),
    EMAIL_INVALID(1005, "邮箱格式错误"),
    AGREEMENT_UNCHECKED(1006, "请先同意用户协议"),
    REGISTER_TOO_FREQUENT(1007, "注册过于频繁，请稍后再试");

    private final int code;
    private final String message;
}
```

**使用**：`throw new RegisterException(ErrorCode.USERNAME_EXISTS);`

### 6.5 CORS 跨域配置

```java
// config/CorsConfig.java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")  // Vite dev server
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }
}
```

### 6.6 多环境配置

```yaml
# application.yml（公共配置）
spring:
  application:
    name: demo
  threads:
    virtual:
      enabled: true
  jpa:
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

---
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cc_db?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: hello
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: validate   # Flyway 管理表结构
  flyway:
    enabled: true

jwt:
  secret: YourSecretKeyForJWTMustBeAtLeast256BitsLongForHS256
  expiration: 1800000
  remember-me-expiration: 604800000
```

---

## 7. 前端工程详解

### 7.1 项目初始化

```bash
pnpm create vite frontend --template vue-ts
cd frontend
pnpm add vue-router pinia axios element-plus @element-plus/icons-vue
pnpm add -D @types/node unocss vitest
```

### 7.2 路由设计

```typescript
// router/index.ts
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guest: true }
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
    meta: { requiresAuth: true }
  },
  { path: '/', redirect: '/dashboard' }
]

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  // 需要登录的页面 → 未登录跳 /login
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return next('/login')
  }

  // 仅游客页面（登录/注册）→ 已登录跳 /dashboard
  if (to.meta.guest && authStore.isLoggedIn) {
    return next('/dashboard')
  }

  next()
})
```

### 7.3 Axios 封装

```typescript
// api/request.ts
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  withCredentials: true  // 自动携带 Cookie
})

// 响应拦截：401 自动跳转登录
request.interceptors.response.use(
  res => res.data,
  error => {
    if (error.response?.status === 401) {
      const { useAuthStore } = await import('@/stores/auth')
      useAuthStore().logout()
      router.push('/login')
      ElMessage.warning('会话已过期，请重新登录')
    }
    return Promise.reject(error)
  }
)

export default request
```

```typescript
// api/auth.ts
import request from './request'

export const authApi = {
  login:     (data: LoginRequest)     => request.post('/auth/login', data),
  register:  (data: RegisterRequest)  => request.post('/auth/register', data),
  logout:    ()                       => request.post('/auth/logout'),
  getUser:   ()                       => request.get('/auth/current-user'),
  check:     ()                       => request.get('/auth/check'),
  checkAvailable: (type: string, value: string) =>
    request.get('/auth/check-availability', { params: { type, value } })
}
```

### 7.4 Pinia Store

```typescript
// stores/auth.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

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
    const res = await authApi.getUser()
    if (res.code === 200) user.value = res.data!
  }

  async function logout() {
    await authApi.logout()
    user.value = null
  }

  return { user, isLoggedIn, login, fetchUser, logout }
})
```

### 7.5 页面示例（登录页）

```vue
<!-- views/LoginView.vue -->
<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ account: '', password: '', rememberMe: false })

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const res = await authStore.login(form.account, form.password, form.rememberMe)
    if (res.code === 200) {
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(res.message)
    }
  } catch {
    ElMessage.error('网络异常')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <el-card class="auth-card">
      <h2>欢迎回来</h2>
      <el-form ref="formRef" :model="form" @keyup.enter="handleLogin">
        <el-form-item prop="account"
          :rules="[{ required: true, message: '请输入用户名或邮箱' }]">
          <el-input v-model="form.account" placeholder="用户名或邮箱"
            :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password"
          :rules="[{ required: true, message: '请输入密码' }]">
          <el-input v-model="form.password" type="password"
            placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.rememberMe">记住我</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="w-full" :loading="loading"
            @click="handleLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="text-center">
        还没有账号？<el-link type="primary" to="/register">立即注册</el-link>
      </div>
    </el-card>
  </div>
</template>
```

---

## 8. API 文档

### 8.1 接口总览

| 方法 | URL | 说明 | 认证 |
|------|-----|------|------|
| `POST` | `/api/v1/auth/login` | 用户登录 | 否 |
| `POST` | `/api/v1/auth/register` | 用户注册 | 否 |
| `GET` | `/api/v1/auth/check-availability` | 唯一性校验 | 否 |
| `POST` | `/api/v1/auth/logout` | 退出登录 | 是 |
| `GET` | `/api/v1/auth/current-user` | 获取当前用户 | 是 |
| `GET` | `/api/v1/auth/check` | 会话校验 | 是 |

### 8.2 Swagger 访问

| 地址 | 说明 |
|------|------|
| `http://localhost:8080/doc.html` | Knife4j 增强 UI（支持在线调试） |
| `http://localhost:8080/v3/api-docs` | OpenAPI 3.0 JSON |

### 8.3 错误码

| 错误码 | 说明 | HTTP |
|--------|------|------|
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

---

## 9. 数据库设计

表结构与 v1.0 完全一致，区别在于通过 Flyway 版本化管理：

```sql
-- V1__init_t_user.sql
CREATE TABLE `t_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `email_verified` tinyint NOT NULL DEFAULT '0',
  `register_ip` varchar(45) DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `last_login_ip` varchar(45) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V2__init_t_login_log.sql
CREATE TABLE `t_login_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned DEFAULT NULL,
  `login_account` varchar(100) NOT NULL,
  `login_result` tinyint NOT NULL,
  `fail_reason` varchar(100) DEFAULT NULL,
  `ip_address` varchar(45) NOT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

> Flyway 启动时自动按版本顺序执行，`ddl-auto` 改为 `validate` 仅校验表结构一致性。

---

## 10. 安全体系

| 层面 | 策略 | 实现 |
|------|------|------|
| 传输层 | HTTPS（生产）/ HTTP（开发） | Nginx |
| 跨域 | CORS 白名单 | CorsConfig |
| 认证 | JWT + HttpOnly Cookie | JwtAuthFilter |
| 密码 | BCrypt (cost=10) | BCryptPasswordEncoder |
| 限流 | IP 维度（登录 5/min、注册 3/min） | ConcurrentHashMap |
| 防暴力 | 5 次失败锁账号 | AuthServiceImpl |
| 审计 | 每次登录/注册写入 t_login_log | LoginLogRepository |
| 注入 | PreparedStatement + XSS 过滤 | JPA / Hibernate |

---

## 11. 部署方案

### 11.1 Docker Compose 一键启动

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:9.7
    container_name: cc_mysql
    environment:
      MYSQL_ROOT_PASSWORD: hello
      MYSQL_DATABASE: cc_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      retries: 10

  backend:
    build: ./backend
    container_name: cc_backend
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: dev

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: cc_frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

### 11.2 Nginx 配置

```nginx
# nginx.conf
server {
    listen 80;
    server_name localhost;

    # 前端静态资源
    location / {
        root   /usr/share/nginx/html;
        index  index.html;
        try_files $uri $uri/ /index.html;   # Vue History 模式
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 11.3 前端 Dockerfile

```dockerfile
# frontend/Dockerfile — 多阶段构建
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN corepack enable && pnpm install --frozen-lockfile
COPY . .
RUN pnpm build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

---

## 12. 本地启动步骤

### 12.1 前置条件

| 软件 | 版本 | 检查 |
|------|------|------|
| Java | 21 | `java -version` |
| Maven | 3.8+ | `mvn -v` |
| Node.js | 22+ | `node -v` |
| pnpm | 9+ | `pnpm -v` |
| Docker | 最新 | `docker -v` |

### 12.2 启动步骤

```bash
# 1. 克隆仓库
git clone https://github.com/HoranLee/cc_project.git
cd cc_project

# 2. 启动 MySQL（Docker）
docker run -d --name cc_mysql \
  -e MYSQL_ROOT_PASSWORD=hello \
  -e MYSQL_DATABASE=cc_db \
  -p 3306:3306 \
  mysql:9.7

# 3. 启动后端（Flyway 自动建表）
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 4. 启动前端（新终端）
cd frontend
pnpm install
pnpm dev

# 5. 访问
# 后端 API 文档：http://localhost:8080/doc.html
# 前端页面：    http://localhost:5173
```

### 12.3 Docker Compose 一键启动（可选）

```bash
docker compose up -d
# 访问 http://localhost
```

---

## 13. 实施计划

| 步骤 | 内容 | 时间 | 可验证 |
|------|------|------|--------|
| 1 | pom.xml 加 Flyway + Knife4j 依赖 | 5 min | 依赖下载成功 |
| 2 | 建 Flyway 迁移脚本，移 SQL | 10 min | 启动后表自动创建 |
| 3 | 建 SwaggerConfig | 5 min | `/doc.html` 可访问 |
| 4 | Controller 加 Swagger 注解 | 15 min | 文档展示完整 |
| 5 | 建 ErrorCode 枚举，替换硬编码 | 10 min | 编译通过 |
| 6 | 拆 model 包，建 CorsConfig | 10 min | 编译通过 |
| 7 | `application.properties` → YAML 多环境 | 10 min | 启动正常 |
| 8 | Vite + Vue 3 脚手架初始化 | 10 min | `pnpm dev` 启动 |
| 9 | 路由 + 守卫 + Axios + Pinia | 30 min | 路由跳转正常 |
| 10 | LoginView.vue | 30 min | 登录页展示+跳转 |
| 11 | RegisterView.vue | 45 min | 注册页展示+实时校验 |
| 12 | DashboardView.vue | 20 min | 仪表盘展示+退出 |
| 13 | Docker Compose + Nginx 配置 | 15 min | `docker compose up` 启动 |
| 14 | 端到端联调测试 | 15 min | 注册→登录→仪表盘→退出 |

> **总时间：约 4 小时**

---

*文档版本 v2.5 &emsp; 原则：务实可落地 &emsp; 最后更新：2026-06-07*
