# 教务管理系统 API 接口文档

## 文档信息

- **版本**: v1.0.0
- **基准 URL**: `http://localhost:3030/api`
- **鉴权方式**: HTTP Header 中携带 token

---

## 1. 全局说明

### 1.1 统一响应格式

所有接口（除文件下载外）均返回 JSON 格式：

```json
{
  "code": 200,      // 状态码：200成功，500业务异常，401未登录
  "msg": "success", // 提示信息
  "data": { ... }   // 业务数据 payload
}
```

### 1.2 鉴权 Headers

除登录接口外，所有请求必须包含：

| Header | 说明 |
|--------|------|
| `token` | 登录后获取的 JWT 字符串 |

---

## 2. 认证模块 (Auth)

### 2.1 用户登录

- **URL**: `/auth/login`
- **Method**: `POST`
- **权限**: 公开
- **请求体**:

```json
{
  "userName": "admin",
  "userPwd": "123"
}
```

- **响应示例**:

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "userId": 1,
      "userName": "admin",
      "userType": "admin"
      // ... 其他用户信息
    }
  }
}
```

---

## 3. 用户管理模块 (User)

**权限说明**: 仅管理员 (Admin) 可操作（除修改密码外）

### 3.1 获取用户列表 (分页 + 搜索)

- **URL**: `/users`
- **Method**: `GET`
- **查询参数**:

| 参数名 | 类型 | 必需 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `pageNum` | Integer | 否 | 1 | 页码 |
| `pageSize` | Integer | 否 | 10 | 每页条数 |
| `userType` | String | 否 | - | 筛选角色：admin/teacher/student |
| `userRealName` | String | 否 | - | 姓名模糊搜索 |

- **响应示例**:

```json
{
  "data": {
    "list": [ ... ], // 用户列表
    "total": 100     // 总条数
  }
}
```

### 3.2 新增/修改用户

- **URL**: `/users`
- **Method**: `POST` (新增) / `PUT` (修改)
- **请求体**:

```json
{
  "userId": 10,              // 修改时必填，新增不填
  "userName": "S2023001",
  "userRealName": "张三",
  "userType": "student",     // student/teacher/admin
  "userPwd": "123",          // 留空则不修改密码
  // --- 扩展字段 (根据 userType 选填) ---
  "studentCollege": "计算机学院",
  "studentClass": "软件1班",
  "teacherTitle": "教授"
}
```

### 3.3 删除用户

- **URL**: `/users/{userId}`
- **Method**: `DELETE`
- **路径参数**:
  - `userId`: 用户ID

### 3.4 修改密码 (通用)

- **URL**: `/users/password`
- **Method**: `POST`
- **权限**: 管理员 或 用户本人
- **请求体**:

```json
{
  "userId": 1,
  "newPwd": "new_password_123"
}
```

### 3.5 批量导入用户

- **URL**: `/users/import`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`
- **表单数据**:
  - `file`: (Binary Excel File)

---

## 4. 学生模块 (Student)

### 4.1 获取个人信息

- **URL**: `/student/info`
- **Method**: `GET`
- **权限**: 仅学生
- **说明**: 返回 User 及 Student 表合并后的详细信息

### 4.2 获取指定学生信息 (管理员用)

- **URL**: `/student/info/{userId}`
- **Method**: `GET`
- **权限**: 仅管理员
- **路径参数**:
  - `userId`: 用户ID

### 4.3 修改个人联系方式

- **URL**: `/student/profile`
- **Method**: `PUT`
- **权限**: 仅学生
- **请求体**:

```json
{
  "userPhone": "13800138000",
  "userEmail": "test@test.com"
}
```

---

## 5. 教师模块 (Teacher)

### 5.1 获取个人信息

- **URL**: `/teacher/info`
- **Method**: `GET`
- **权限**: 仅教师

### 5.2 获取指定教师信息 (管理员用)

- **URL**: `/teacher/info/{userId}`
- **Method**: `GET`
- **权限**: 仅管理员
- **路径参数**:
  - `userId`: 用户ID

### 5.3 修改个人联系方式

- **URL**: `/teacher/profile`
- **Method**: `PUT`
- **权限**: 仅教师
- **请求体**: 同学生模块

---

## 6. 课程管理模块 (Course)

### 6.1 获取课程列表

- **URL**: `/courses`
- **Method**: `GET`
- **查询参数**:

| 参数名 | 类型 | 必需 | 说明 |
|--------|------|------|------|
| `courseName` | String | 否 | 模糊搜索 |
| `courseTime` | String | 否 | 搜索特定时间段 |
| `courseStatus` | Integer | 否 | 1=已发布，0=未发布 |

**逻辑说明**:

- 学生调用：强制只能查 `courseStatus=1`
- 教师调用：强制只能查 `teacherId=当前用户`
- 管理员调用：无限制

### 6.2 发布/新增课程

- **URL**: `/courses`
- **Method**: `POST`
- **权限**: 管理员 / 教师
- **请求体**:

```json
{
  "courseName": "高等数学",
  "courseCredit": 4,
  "maxNum": 50,
  "courseTime": "周一 1-2节",
  "courseRoom": "一教101"
}
```

### 6.3 修改课程

- **URL**: `/courses`
- **Method**: `PUT`
- **权限**: 管理员 / 课程所属教师

### 6.4 删除课程

- **URL**: `/courses/{courseId}`
- **Method**: `DELETE`
- **权限**: 管理员 / 课程所属教师
- **路径参数**:
  - `courseId`: 课程ID

### 6.5 学生选课

- **URL**: `/courses/{courseId}/select`
- **Method**: `POST`
- **权限**: 仅学生
- **路径参数**:
  - `courseId`: 课程ID
- **说明**: 系统会自动进行人数检查、时间冲突检查、重复选课检查

### 6.6 学生退课

- **URL**: `/courses/{courseId}/drop`
- **Method**: `POST`
- **权限**: 仅学生
- **路径参数**:
  - `courseId`: 课程ID

---

## 7. 成绩模块 (Score)

### 7.1 录入/修改成绩

- **URL**: `/scores/entry`
- **Method**: `POST`
- **权限**: 教师 / 管理员
- **请求体**:

```json
{
  "courseId": 101,
  "studentId": 2024001,
  "score": 95
}
```

### 7.2 获取某门课的学生成绩单

- **URL**: `/scores/course/{courseId}`
- **Method**: `GET`
- **权限**: 教师 / 管理员
- **路径参数**:
  - `courseId`: 课程ID

### 7.3 获取我的成绩单

- **URL**: `/scores/student/me`
- **Method**: `GET`
- **权限**: 仅学生

### 7.4 课程成绩统计分析

- **URL**: `/scores/statistics/{courseId}`
- **Method**: `GET`
- **权限**: 教师 / 管理员
- **路径参数**:
  - `courseId`: 课程ID
- **响应示例**:

```json
{
  "data": {
    "avgScore": 85.5,
    "maxScore": 99,
    "minScore": 60,
    "passCount": 45,
    "totalCount": 48
  }
}
```

### 7.5 导出成绩 Excel

- **URL**: `/scores/export/{courseId}`
- **Method**: `GET`
- **Response Type**: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` (二进制流)
- **路径参数**:
  - `courseId`: 课程ID

---

## 8. 统计模块 (Statistics)

### 8.1 仪表盘数据

- **URL**: `/stats/dashboard`
- **Method**: `GET`
- **权限**: 仅管理员
- **说明**: 返回用户角色分布、热门课程 Top5 数据

---

## 9. 数据库备份与还原模块 (Backup)

**权限说明**: 仅管理员 (Admin) 可操作

### 9.1 获取备份列表

- **URL**: `/backup`
- **Method**: `GET`
- **响应示例**:

```json
{
  "data": [
    {
      "fileName": "school_db_2025-12-17_1030.sql",
      "size": "15.2 KB",
      "createTime": "2025-12-17 10:30:00"
    }
  ]
}
```

### 9.2 触发立即备份

- **URL**: `/backup`
- **Method**: `POST`
- **说明**: 调用服务器 mysqldump 进行备份

### 9.3 还原数据库 (高危)

- **URL**: `/backup/restore/{fileName}`
- **Method**: `POST`
- **说明**: 将指定 SQL 文件回滚到数据库。操作前请二次确认
- **路径参数**:
  - `fileName`: 备份文件名

### 9.4 删除备份文件

- **URL**: `/backup/{fileName}`
- **Method**: `DELETE`
- **路径参数**:
  - `fileName`: 备份文件名

### 9.5 下载备份文件

- **URL**: `/backup/download/{fileName}`
- **Method**: `GET`
- **说明**: 必须在 Header 中携带 Token，返回二进制 SQL 文件流
- **路径参数**:
  - `fileName`: 备份文件名
