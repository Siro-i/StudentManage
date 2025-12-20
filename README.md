# 教务信息管理系统
>
> 基于 Spring Boot + Vue 3 + Element Plus 的前后端分离教务管理系统。
> 包含完整的 RBAC 权限控制、排课冲突检测、成绩分析、Excel 导入导出以及**数据库定时备份/还原**功能。

## 项目简介

本项目是一个的教务/学生信息管理系统。系统区分 **管理员 (Admin)**、**教师 (Teacher)**、**学生 (Student)** 三种角色，实现了从用户管理、课程安排到成绩录入、数据分析的全业务闭环。

支持手动/自动数据库备份、一键还原以及过期备份自动清理。

## 核心功能

### 1.  权限与用户管理

- **RBAC 模型**： `Admin` / `Teacher` / `Student` 三级权限隔离。
- **用户导入**：支持 Excel 批量导入用户数据（使用 EasyExcel）。
- **防空校验**：后端 Service 层实现参数防控与业务逻辑校验。

### 2.  课程与排课管理

- **排课系统**：支持按周次/节次/教室进行排课。
- **冲突检测**：
  - **教室冲突**：自动检测同一时间、同一教室是否已有课程。
  - **时间冲突**：学生选课时，自动检测是否与已选课程时间冲突。
- **选课/退课**：学生端自主选课，实时更新名额，并发安全。

### 3.  成绩与数据分析

- **成绩录入**：教师可在线录入、修改学生成绩。
- **智能分析**：自动计算课程的**平均分、最高分、最低分、及格率**。
- **数据可视化**：
  - 管理员仪表盘：人员构成饼图、热门课程柱状图（ECharts）。
  - 成绩报表导出：一键导出课程成绩单为 Excel 文件。

### 4.  数据安全

- **定时备份**：系统集成 `mysqldump`，支持 Cron 表达式定时自动备份。
- **一键还原**：管理员可在界面上查看备份列表，点击“还原”一键回滚数据库。
- **自动清理**：自动检测并删除 30 天前的过期备份文件，节省磁盘空间。
- **环境隔离**：通过配置文件灵活指定 MySQL 二进制文件路径。

## 技术栈

### 后端 (Backend)

- **核心框架**: Spring Boot 3.x
- **ORM 框架**: MyBatis + MyBatis-PageHelper
- **工具库**:
  - `EasyExcel`: 高性能 Excel 读写
  - `FastJson2`: JSON 处理
  - `Lombok`: 简化实体类
- **数据库**: MySQL 8.0 (开启 GTID 兼容模式)

### 前端 (Frontend)

- **框架**: Vue.js 3 (Composition API)
- **UI 组件**: Element Plus
- **图表**: ECharts 5.x
- **交互**: Fetch API (封装 Token 拦截器)

## 快速开始

### 1. 环境准备

- **JDK**: 17+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **IDE**: IntelliJ IDEA

### 2. 数据库初始化

运行项目根目录下的 `script.sql` 脚本，它将自动创建表结构并写入初始化测试数据（包含默认管理员账号）。

### 3. 配置文件设置

修改 `src/main/resources/application.yml`，重点配置数据库连接和 MySQL 工具路径：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/school_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root          # 修改为你的数据库账号
    password: your_password # 修改为你的数据库密码

// 请提前配置mysql环境变量，确保 mysqldump 和 mysql 命令可在 PATH 中访问
project:
  mysql:
    dump-path: "mysqldump"
    client-path: "mysql"
```

### 4. 启动项目运行 com.school.system.Start 类的主方法。控制台看到 Started Start in x.xxx seconds 即为启动成功。浏览器访问：<http://localhost:3030>

 默认账号角色账号密码权限说明管理员admin123456用户管理、备份还原、系统监控、全量数据查询
 教师teacher1123456课程发布、成绩录入、个人信息维护
 学生student1123456选课/退课、成绩查询、个人信息维护
