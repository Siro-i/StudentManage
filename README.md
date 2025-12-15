# StudentManage

Spring Boot 3.5 应用，提供学生/教师/课程/成绩管理与用户认证、Excel 导入导出等功能。

## 技术栈
- Java 21，Spring Boot 3.5
- MyBatis + PageHelper
- JWT 认证（自带拦截器）
- EasyExcel 导入/导出

## 快速开始
1) 环境
   - 安装 JDK 21，并设置环境变量 `JAVA_HOME`
   - 安装 MySQL，准备 `application.yml` 中的数据库(文件script.sql)

2) 认证
   - 登录接口：`POST /api/auth/login`，响应头携带 `token` 字段；后续请求在 Header 传 `token`

## 常用接口
- 用户登录：`POST /api/auth/login`
- 用户管理（管理员）：`/api/users/**`
- 课程管理：`/api/courses/**`
- 成绩录入/查询：`/api/scores/**`


## 配置
- 数据源及日志：`src/main/resources/application.yml`
- MyBatis 映射：`src/main/resources/mapper/*.xml`

## 安全提示
- 生产环境请将数据库凭据、JWT 密钥改为环境变量或外部配置。


