package com.school.system.controller;

import com.alibaba.excel.EasyExcel;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.school.system.common.MD5Utils;
import com.school.system.common.Result;
import com.school.system.common.UserImportListener;
import com.school.system.entity.User;
import com.school.system.entity.UserImportDTO;
import com.school.system.mapper.StudentMapper;
import com.school.system.mapper.TeacherMapper;
import com.school.system.mapper.UserMapper;
import com.school.system.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 对应文档 2.3.4 系统管理模块 - 用户管理
 * 仅管理员可用
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private TeacherMapper teacherMapper;

    /**
     * 分页查询用户列表
     * GET /api/users?pageNum=1&pageSize=10
     */
    @GetMapping
    public Result<PageInfo<User>> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            User condition
    ) {
        // 1. 开启分页
        PageHelper.startPage(pageNum, pageSize);

        // 2. 执行查询
        List<User> list = userService.listUsers(condition);

        // 3. 封装分页结果
        PageInfo<User> pageInfo = new PageInfo<>(list);

        return Result.success(pageInfo);
    }

    /**
     * 删除用户
     * 前端请求: DELETE /api/users/{userId}
     * 对应 UserOperable.deleteUser() 接口
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        boolean success = userService.deleteUser(userId);
        if (success) {
            return Result.success(null);
        } else {
            return Result.error("删除失败，用户可能不存在");
        }
    }

    /**
     * 修改密码
     * POST /api/users/password
     */
    @PostMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String newPwd = (String) params.get("newPwd");

        // --- 加密新密码 ---
        String encryptedPwd = MD5Utils.encrypt(newPwd);

        User user = new User();
        user.setUserId(userId);
        user.setUserPwd(encryptedPwd); // 存入密文
        userMapper.updateById(user);

        return Result.success(null);
    }

    /**
     * 新增或更新用户
     */
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public Result<Void> saveUser(@RequestBody Map<String, Object> params) {

        // --- 1. 数据校验  ---
        String phone = (String) params.get("userPhone");
        String email = (String) params.get("userEmail");

        // 校验手机号: 必须是非空且为11位数字
        if (phone != null && !phone.isEmpty()) {
            if (!phone.matches("^\\d{11}$")) {
                return Result.error("手机号必须为11位数字");
            }
        }

        // 校验邮箱: 必须符合邮箱格式
        if (email != null && !email.isEmpty()) {
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            if (!email.matches(emailRegex)) {
                return Result.error("邮箱格式不正确 (例: abc@163.com)");
            }
        }
        // 2. 提取 User 表基础数据
        User user = new User();
        if (params.get("userId") != null) user.setUserId(Long.valueOf(params.get("userId").toString()));
        user.setUserName((String) params.get("userName"));
        user.setUserRealName((String) params.get("userRealName"));
        user.setUserType((String) params.get("userType"));
        user.setUserPhone((String) params.get("userPhone"));
        user.setUserEmail((String) params.get("userEmail"));

        // 密码逻辑
        String pwd = (String) params.get("userPwd");
        if (pwd != null && !pwd.isEmpty()) {
            user.setUserPwd(pwd);
        }

        // 3. 分支处理
        if (user.getUserId() == null) {
            userService.addUser(user, params);
        } else {
            // 更新逻辑
            userService.updateUserBasic(user);

            // 2. 如果角色是学生，更新学生表 (原有逻辑)
            if ("student".equals(user.getUserType())) {
                String college = (String) params.get("studentCollege");
                String stuClass = (String) params.get("studentClass");
                String grade = (String) params.get("studentGrade");

                com.school.system.entity.Student student = studentMapper.selectByUserId(user.getUserId());
                if (student == null) {
                    // 如果改了角色变成学生，可能还没有档案，新建一个
                    student = new com.school.system.entity.Student();
                    student.setUserId(user.getUserId());
                    student.setStudentCollege(college);
                    student.setStudentClass(stuClass);
                    student.setStudentGrade(grade);
                    studentMapper.insert(student);
                } else {
                    student.setStudentCollege(college);
                    student.setStudentClass(stuClass);
                    student.setStudentGrade(grade);
                    studentMapper.update(student);
                }
            }

            // 3. 新增：如果角色是教师，更新教师表 (teacher_table)
            else if ("teacher".equals(user.getUserType())) {
                String college = (String) params.get("teacherCollege");
                String title = (String) params.get("teacherTitle");

                com.school.system.entity.Teacher teacher = teacherMapper.selectByUserId(user.getUserId());
                if (teacher == null) {
                    // 如果改了角色变成教师，新建档案
                    teacher = new com.school.system.entity.Teacher();
                    teacher.setUserId(user.getUserId());
                    teacher.setTeacherCollege(college);
                    teacher.setTeacherTitle(title);
                    teacher.setTeacherCreatetime(new java.util.Date());
                    teacherMapper.insert(teacher);
                } else {
                    // 更新现有档案
                    teacher.setTeacherCollege(college);
                    teacher.setTeacherTitle(title);
                    teacherMapper.updateById(teacher);
                }
            }
        }

        return Result.success(null);
    }
    /**
     * 批量导入学生
     */
    @PostMapping("/import")
    public Result<Void> importUsers(@RequestParam("file") MultipartFile file) {
        try {
            // 这里把 userService 传给监听器
            EasyExcel.read(file.getInputStream(), UserImportDTO.class, new UserImportListener(userService))
                    .sheet()
                    .doRead();
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 下载导入模板 (动态生成)
     */
    @GetMapping("/import/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // 1. 设置响应头类型
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 2. 设置文件名
        String fileName = URLEncoder.encode("用户导入模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName + ".xlsx");

        // 3. 写出 Excel
        EasyExcel.write(response.getOutputStream(), UserImportDTO.class)
                .sheet("导入模板")
                .doWrite(new ArrayList<>());
    }
}