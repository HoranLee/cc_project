# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.6/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.6/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.6/reference/web/servlet.html)

### Guides

The following guides illustrate how to use some features concretely:

* [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

---

# Demo 用户认证系统 — 产品需求文档

> **版本**：v1.0 &emsp; **日期**：2026-06-07 &emsp; **状态**：已实现

---

## 1. 系统概述

### 1.1 产品定位

Demo 用户认证系统是一个轻量级的身份认证模块，提供用户注册、登录、会话管理和退出功能。系统采用前后端分离的 RESTful 架构，通过 JWT + HttpOnly Cookie 实现无状态认证。

### 1.2 核心功能

| 模块 | 功能 | 说明 |
|------|------|------|
| 注册 | 用户自主创建账号 | 含客户端实时校验 + 服务端二次校验 |
| 登录 | 用户名/邮箱 + 密码认证 | 支持"记住我"延长会话 |
| 会话 | JWT 无状态认证 | HttpOnly Cookie，自动续期 |
| 退出 | 清除会话凭证 | 含确认弹窗交互 |

### 1.3 用户流程图

```
注册页(/register)                登录页(/login)                 仪表盘(/dashboard)
    │                                │                              │
    │  填写信息 → 实时校验            │  输入账号密码                  │  查看个人信息
    │        ↓                       │        ↓                     │
    │  POST /api/auth/register       │  POST /api/auth/login        │  退出按钮
    │        ↓                       │        ↓                     │        ↓
    │  注册成功 → 2s后跳转            │  JWT 写入 Cookie              │  确认弹窗
    │        │                       │        │                     │        ↓
    └────────┼───────────────────────┘        │                     │  POST /api/auth/logout
             ↓                                ↓                     │        ↓
    /login?registered=1              /dashboard                     │  清除 Cookie
    (显示欢迎提示)                                                          │
                                                                          ↓
                                                                    /login
```

---

## 2. 技术架构

### 2.1 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 4.0.6 |
| 语言 | Java | 21 |
| 安全 | Spring Security + JWT | 7.x / jjwt 0.12.6 |
| 持久层 | Spring Data JPA + Hibernate | — |
| 数据库 | MySQL | 9.7 |
| 模板引擎 | Thymeleaf | — |
| 密码加密 | BCrypt (Spring Security) | cost=10 |

### 2.2 项目分层

```
controller/     ← REST 端点 + 页面路由（只做协议转换）
service/        ← 业务逻辑（认证、注册、校验、限流、日志）
repository/     ← 数据访问（Spring Data JPA）
entity/         ← 数据库实体映射
model/          ← DTO（请求/响应/通用包装）
security/       ← JWT 工具 + 认证过滤器 + Security 配置
templates/      ← Thymeleaf 页面（login / register / dashboard）
```

### 2.3 响应规范

所有 API 响应采用统一格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

| 场景 | HTTP 状态码 | code |
|------|------------|------|
| 成功 | 200 | 200 |
| 认证失败 | 401 | 401 |
| 业务错误 | 400 | 10xx |
| 参数校验失败 | 400 | 400 |
| 服务器错误 | 500 | 500 |

---

## 3. 数据库设计

**数据库**：`cc_db` &emsp; **字符集**：utf8mb4 &emsp; **排序规则**：utf8mb4_unicode_ci
**连接**：`localhost:3306` &emsp; **用户**：`root` &emsp; **密码**：`hello`

### 3.1 用户表（`t_user`）

| 字段 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | BIGINT UNSIGNED | PK, AUTO_INCREMENT | — | 用户唯一 ID |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE | — | 登录用户名，字母开头，4~20 位 |
| `email` | VARCHAR(100) | NOT NULL, UNIQUE | — | 邮箱，可用于登录 |
| `password_hash` | VARCHAR(255) | NOT NULL | — | BCrypt 密码哈希 |
| `nickname` | VARCHAR(50) | NULL | — | 显示昵称，为空时默认取 username |
| `avatar_url` | VARCHAR(255) | NULL | — | 头像 URL |
| `status` | TINYINT | NOT NULL | 1 | 0=禁用 / 1=正常 |
| `email_verified` | TINYINT | NOT NULL | 0 | 0=未验证 / 1=已验证 |
| `register_ip` | VARCHAR(45) | NULL | — | 注册时 IP 地址 |
| `last_login_time` | DATETIME | NULL | — | 最后登录时间 |
| `last_login_ip` | VARCHAR(45) | NULL | — | 最后登录 IP |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 注册时间 |
| `updated_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**建表 SQL**：

```sql
CREATE TABLE `t_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0-禁用,1-正常',
  `email_verified` tinyint NOT NULL DEFAULT '0' COMMENT '0-未验证,1-已验证',
  `register_ip` varchar(45) DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `last_login_ip` varchar(45) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3.2 登录日志表（`t_login_log`）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT UNSIGNED | PK, AUTO_INCREMENT | 日志 ID |
| `user_id` | BIGINT UNSIGNED | NULL | 关联用户 ID |
| `login_account` | VARCHAR(100) | NOT NULL | 登录时输入的账号 |
| `login_result` | TINYINT | NOT NULL | 0=失败 / 1=成功 |
| `fail_reason` | VARCHAR(100) | NULL | 失败原因 |
| `ip_address` | VARCHAR(45) | NOT NULL | 请求 IP |
| `user_agent` | VARCHAR(255) | NULL | 浏览器 User-Agent |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 记录时间 |

**建表 SQL**：

```sql
CREATE TABLE `t_login_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned DEFAULT NULL,
  `login_account` varchar(100) NOT NULL,
  `login_result` tinyint NOT NULL COMMENT '0-失败,1-成功',
  `fail_reason` varchar(100) DEFAULT NULL,
  `ip_address` varchar(45) NOT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

> **注意**：生产环境启用 `spring.jpa.hibernate.ddl-auto=update` 可自动建表。上述 SQL 供参考和手动部署使用。

---

## 4. 页面设计

### 4.1 登录页（`/login`）

**路径**：`GET /login` &emsp; **权限**：公开（所有人可访问）

**页面元素**：

| 元素 | 类型 | 说明 |
|------|------|------|
| 账号输入框 | text | placeholder="请输入用户名或邮箱" |
| 密码输入框 | password | 带小眼睛图标切换显示/隐藏 |
| 记住我 | checkbox | 勾选后 JWT 有效期从 30 分钟延长至 7 天 |
| 登录按钮 | button | type=submit |
| 立即注册链接 | `<a>` | 跳转 `/register` |
| 忘记密码链接 | `<a>` | 占位符（`#`），待后续版本实现 |

**交互规则**：
- 客户端非空校验：账号和密码不能为空，空时按钮置灰
- AJAX 异步提交，不刷新页面；按钮显示 loading 动画并禁用，防止重复提交
- 登录成功 → 跳转 `/dashboard`
- 登录失败 → 顶部红色错误横幅，统一显示"账号或密码错误"（不区分账号不存在/密码错误）
- 注册成功回调：URL 参数 `?registered=1` 时显示绿色欢迎横幅"注册成功！请使用新账号登录"，5 秒后自动消失

**样式**：居中卡片布局，响应式适配 PC（max-width: 420px）和移动端

### 4.2 注册页（`/register`）

**路径**：`GET /register` &emsp; **权限**：未登录可访问；已登录自动跳转 `/dashboard`

**页面元素**：

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|----------|
| 用户名 | text | ✅ | 4~20 位，字母/数字/下划线，字母开头 |
| 邮箱 | email | ✅ | 标准邮箱格式 |
| 密码 | password | ✅ | 至少 8 位，含字母和数字 |
| 确认密码 | password | ✅ | 必须与密码一致 |
| 昵称 | text | ❌ | 最长 20 字符，为空默认取用户名 |
| 用户协议 | checkbox | ✅ | 必须勾选 |
| 注册按钮 | button | — | 未满足条件时置灰禁用 |

**实时校验**（输入时触发，500ms 防抖）：
- **用户名**：格式校验（前端正则）→ 异步请求 `GET /api/auth/check-availability?type=username` 检查唯一性，输入框右侧显示 ✓/✗
- **邮箱**：格式校验 → 异步请求 `GET /api/auth/check-availability?type=email` 检查唯一性，输入框右侧显示 ✓/✗
- **密码**：实时强度指示条——
  - 🔴 弱（红色 33%）：仅数字或仅字母，长度 < 8
  - 🟡 中（黄色 66%）：字母 + 数字，长度 ≥ 8
  - 🟢 强（绿色 100%）：字母 + 数字 + 特殊字符，长度 ≥ 10
- **确认密码**：实时比对，不一致时显示"两次密码输入不一致"
- **注册按钮**：任一必填项无效或协议未勾选时 `disabled`

**提交行为**：
- AJAX 异步提交 `POST /api/auth/register`
- 按钮变为"注册中..."并禁用
- 成功 → 弹出成功弹窗，2 秒后跳转 `/login?registered=1`
- 失败 → 根据错误码在对应字段下方显示提示，清空密码字段，保留其他已填内容

**键盘**：表单内按 Enter 键，注册按钮可用时触发提交

**样式**：居中卡片布局，略宽于登录页（max-width: 480px），响应式适配

### 4.3 仪表盘（`/dashboard`）

**路径**：`GET /dashboard` &emsp; **权限**：需登录（未登录返回 401）

**页面内容**：
- 顶部导航栏：左侧 Logo "Demo"，右侧退出登录按钮
- 欢迎语："欢迎回来，{昵称}！"
- 个人信息卡片：
  - 头像（圆形，有图片显示图片，无图片显示昵称首字 + 蓝色背景）
  - 用户名 / 昵称 / 邮箱 / 最后登录时间 / 账号状态

**退出交互**：
- 点击"退出登录"按钮 → 弹出确认弹窗"确认退出登录吗？"
- 确认 → `POST /api/auth/logout` → 清除 JWT Cookie → 跳转 `/login`
- 取消 / 点击遮罩层 → 关闭弹窗

---

## 5. API 接口规范

### 5.1 接口总览

| 方法 | URL | 说明 | 认证 |
|------|-----|------|------|
| `POST` | `/api/auth/login` | 用户登录 | 否 |
| `POST` | `/api/auth/register` | 用户注册 | 否 |
| `GET` | `/api/auth/check-availability` | 用户名/邮箱唯一性校验 | 否 |
| `POST` | `/api/auth/logout` | 退出登录 | 是 |
| `GET` | `/api/auth/current-user` | 获取当前用户信息 | 是 |
| `GET` | `/api/auth/check` | 会话状态校验 | 是 |

### 5.2 登录 — `POST /api/auth/login`

**请求体**（JSON）：
```json
{
  "account": "zhangsan 或 zhangsan@example.com",
  "password": "123456",
  "rememberMe": true
}
```

**校验规则**：
| 字段 | 规则 |
|------|------|
| account | `@NotBlank` — 不能为空 |
| password | `@NotBlank` — 不能为空 |
| rememberMe | boolean，默认 false |

**响应**：

| 场景 | HTTP | 响应体 |
|------|------|--------|
| 成功 | 200 | `{"code":200,"message":"登录成功","data":{"id":1,"username":"...","email":"...","nickname":"...","lastLoginTime":"...","message":"登录成功"}}` |
| 失败 | 401 | `{"code":401,"message":"账号或密码错误"}` |
| 账号禁用 | 401 | `{"code":401,"message":"账号已被禁用，请联系管理员"}` |
| 账号锁定 | 401 | `{"code":401,"message":"账号已被锁定，请联系管理员"}` |
| 限流 | 401 | `{"code":401,"message":"请求过于频繁，请稍后再试"}` |

**后端处理**：
- 从 Cookie 中返回 `auth_token`（JWT），HttpOnly / SameSite=Strict
- 默认有效期 30 分钟（1800000ms），rememberMe=true 时 7 天（604800000ms）
- 记录登录日志到 `t_login_log`，含 IP 和 User-Agent
- 更新 `last_login_time` 和 `last_login_ip`

### 5.3 注册 — `POST /api/auth/register`

**请求体**（JSON）：
```json
{
  "username": "zhangsan",
  "email": "zhangsan@example.com",
  "password": "Abc123456",
  "nickname": "张三",
  "agreed": true
}
```

**校验规则与错误码**：

| 字段 | 校验规则 | 错误码 |
|------|----------|--------|
| username | `@NotBlank` + 格式 `^[a-zA-Z][a-zA-Z0-9_]{3,19}$` + 唯一 | 1001=已存在 / 1004=格式错误 |
| email | `@NotBlank` + 格式 `^[\\w.-]+@[\\w.-]+\\.\\w{2,}$` + 唯一 | 1002=已注册 / 1005=格式错误 |
| password | `@NotBlank` + `^(?=.*[a-zA-Z])(?=.*\\d).{8,}$` | 1003=强度不足 |
| nickname | 可选，最长 20 字符，为空默认取 username | — |
| agreed | `@AssertTrue` 必须为 true | 400="请先同意用户协议" |

**响应**：

| 场景 | HTTP | 响应体示例 |
|------|------|-----------|
| 成功 | 200 | `{"code":200,"message":"注册成功"}` |
| 失败 | 400 | `{"code":1001,"message":"用户名已存在"}` |
| 限流 | 400 | `{"code":1007,"message":"注册过于频繁，请稍后再试"}` |

**后端处理**：
- BCrypt 加密密码（cost=10）存入 `password_hash`
- 记录 `register_ip`
- 设置 `status=1`（正常）、`email_verified=0`（未验证）
- 同时记录注册日志到 `t_login_log`（`login_result=1`, `fail_reason="注册"`）

### 5.4 唯一性校验 — `GET /api/auth/check-availability`

**请求参数**（Query String）：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | 是 | `username` 或 `email` |
| `value` | string | 是 | 待校验的值 |

**响应**：

可用时：
```json
{ "available": true }
```

不可用时：
```json
{ "available": false, "message": "用户名已存在" }
```

**校验内容**：
- `type=username`：格式（字母开头、4~20位字母/数字/下划线）+ 唯一性
- `type=email`：格式 + 唯一性
- 格式错误和已占用均返回 `available: false` 并附带具体原因

### 5.5 退出 — `POST /api/auth/logout`

**请求**：携带 `auth_token` Cookie

**响应**：
```json
{"code":200,"message":"已退出登录"}
```

**后端处理**：
- 清除 `auth_token` Cookie（maxAge=0）
- 清除 SecurityContext

### 5.6 当前用户 — `GET /api/auth/current-user`

**请求**：携带 `auth_token` Cookie

**响应（成功）**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "nickname": "管理员",
    "avatarUrl": null,
    "status": 1,
    "lastLoginTime": "2026-06-07 15:48:03"
  }
}
```

**响应（未登录）**：
```json
{"code":401,"message":"未登录"}
```

> **注意**：响应不包含 `password_hash` 等敏感字段。

### 5.7 会话校验 — `GET /api/auth/check`

**响应**：
- 已登录：`{"code":200,"message":"success","data":true}`
- 未登录：`{"code":401,"message":"未登录"}`

---

## 6. 安全策略

### 6.1 密码安全

| 策略 | 实现 |
|------|------|
| 存储加密 | BCrypt（cost=10），通过 `BCryptPasswordEncoder` |
| 盐值 | BCrypt 内嵌，无单独 salt 字段 |
| 传输 | 生产环境强制 HTTPS；开发环境 HTTP |
| 明文 | 禁止存储或日志输出明文密码 |

### 6.2 认证与会话

| 策略 | 配置 |
|------|------|
| Token 类型 | JWT（HMAC-SHA256） |
| 签名密钥 | `application.properties` 中 `jwt.secret` |
| 默认过期 | 30 分钟（1800000ms） |
| Remember Me 过期 | 7 天（604800000ms） |
| Cookie 属性 | HttpOnly=true, SameSite=Strict, Path=/ |
| 存储方式 | Cookie（名称为 `auth_token`） |
| 会话模式 | 无状态（STATELESS），每次请求通过 JwtAuthFilter 校验 |

### 6.3 速率限制

| 接口 | 限制 | 实现 |
|------|------|------|
| 登录 | 同一 IP 每分钟 ≤ 5 次 | 内存 ConcurrentHashMap（按 IP + 分钟窗口） |
| 注册 | 同一 IP 每分钟 ≤ 3 次 | 同上，key 前缀 `reg_` |
| 超限响应 | 登录 401 / 注册 1007 | 提示"请求过于频繁，请稍后再试" |

### 6.4 登录保护

| 策略 | 配置 |
|------|------|
| 失败提示 | 统一"账号或密码错误"，不区分账号不存在/密码错误 |
| 账号禁用 | status=0 时拒绝登录，提示"账号已被禁用" |
| 自动锁定 | 连续 5 次失败 → status 置 0，提示"账号已被锁定" |
| 失败计数 | 统计最近 30 分钟内 `login_result=0` 的记录 |
| 日志记录 | 每次登录（成功/失败）均写入 `t_login_log` |

### 6.4 权限控制

| 资源 | 权限 |
|------|------|
| `/login`, `/register` | 公开 |
| `/css/**`, `/js/**`, `/images/**` | 公开 |
| `/api/auth/login`, `/api/auth/register`, `/api/auth/check-availability` | 公开 |
| `/api/auth/logout`, `/api/auth/current-user`, `/api/auth/check` | 需登录 |
| `/dashboard`, `/dashboard/**` | 需登录 |
| 其他路径 | 公开 |

---

## 7. 配置说明

### 7.1 应用配置（`application.properties`）

```properties
# 数据源
spring.datasource.url=jdbc:mysql://localhost:3306/cc_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=hello

# JPA（开发环境启用 ddl-auto=update 自动建表）
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT
jwt.secret=<256位以上密钥>
jwt.expiration=1800000                    # 30 分钟
jwt.remember-me-expiration=604800000      # 7 天
```

> **生产环境注意**：`jwt.secret` 需替换为安全密钥；`ddl-auto` 建议改为 `validate`；数据库密码使用环境变量或密钥管理服务。

---

## 8. 测试用例

### 8.1 登录测试

| # | 场景 | 输入 | 预期 |
|---|------|------|------|
| 1 | 正常登录 | account=admin, password=123456 | 200，返回用户信息 |
| 2 | 错误密码 | account=admin, password=wrong | 401，"账号或密码错误" |
| 3 | 不存在账号 | account=nobody, password=123456 | 401，"账号或密码错误" |
| 4 | 空账号 | account="", password=123456 | 400，"账号不能为空" |
| 5 | 记住我 | rememberMe=true | JWT 有效期 7 天 |

### 8.2 注册测试

| # | 场景 | 输入 | 预期 |
|---|------|------|------|
| 1 | 正常注册 | 全部合法 + 勾选协议 | 200，"注册成功" |
| 2 | 用户名已存在 | username=admin | 400，code=1001 |
| 3 | 邮箱已存在 | email=admin@example.com | 400，code=1002 |
| 4 | 密码纯数字 | password=12345678 | 400，code=1003 |
| 5 | 密码纯字母 | password=abcdefgh | 400，code=1003 |
| 6 | 用户名格式错误 | username=1abc | 400，code=1004 |
| 7 | 邮箱格式错误 | email=notanemail | 400，code=1005 |
| 8 | 未勾选协议 | agreed=false | 400，"请先同意用户协议" |
| 9 | 空字段 | username="" | 400，"用户名不能为空" |
| 10 | IP 限流 | 同一 IP 第 4 次注册 | 400，code=1007 |

### 8.3 会话测试

| # | 场景 | 操作 | 预期 |
|---|------|------|------|
| 1 | 正常访问 | GET /api/auth/check | 200 |
| 2 | 未登录访问 | 无 Cookie 访问 | 401 |
| 3 | 退出后访问 | logout → check | 401 |

---

## 9. 测试账号

应用首次启动后 JPA 自动建表，需手动或通过注册页面创建用户。以下为已创建的可用于测试的账号：

| 用户名 | 密码 | 昵称 | 说明 |
|--------|------|------|------|
| `admin` | `123456` | 管理员 | 初始管理员账号 |
| `alice01` | `Hello123` | Alice | 注册功能测试用户 |
| `finaltest` | `Test1234` | Final | 端到端测试用户 |

---

*文档结束。与程序实现保持一致，最后更新于 2026-06-07。*
