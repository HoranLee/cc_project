# CLAUDE.md

## 项目约束（代码里看不出来的）

- MySQL 在宿主机本地运行（不在 K8s 中），地址 `host.docker.internal:3306`，库名 `cc_db`
- K8s 集群是 Kind（不是生产集群），namespace: `demo`
- K8s 中只部署应用（backend + frontend），不部署数据库
- 数据库表结构由 Flyway 管理，JPA 仅做校验（ddl-auto: validate）
