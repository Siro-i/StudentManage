package com.school.system.controller;

import com.school.system.common.Result;
import com.school.system.entity.Student;
import com.school.system.entity.User;
import com.school.system.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 获取学生详细个人信息
     * GET /api/student/info/{userId}
     * 对应文档 3. 功能详细设计 - 学生的个人信息查看功能
     */
    @GetMapping("/info/{userId}")
    public Result<Student> getStudentInfo(@PathVariable Long userId) {
        Student student = studentMapper.selectByUserId(userId);
        if (student == null) {
            return Result.error("未找到学生档案信息");
        }
        return Result.success(student);
    }

    @Autowired
    private com.school.system.service.UserService userService; // 注入 UserService 用于更新 user_table

    /**
     * 学生修改个人信息 (仅限手机号、邮箱)
     * PUT /api/student/profile
     */
    @PutMapping("/profile")
    public Result<Void> updateStudentProfile(@RequestBody Map<String, String> params,
                                             @RequestAttribute("userId") Long userId) {

        // 1. 获取允许修改的字段
        String phone = params.get("userPhone");
        String email = params.get("userEmail");
        if (phone != null && !phone.isEmpty()) {
            if (!phone.matches("^\\d{11}$")) {
                return Result.error("手机号必须为11位数字");
            }
        }
        if (email != null && !email.isEmpty()) {
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            if (!email.matches(emailRegex)) {
                return Result.error("邮箱格式不正确");
            }
        }

        // 2. 这里的 userId 来自 Token 拦截器
        User user = new User();
        user.setUserId(userId);
        user.setUserPhone(phone);
        user.setUserEmail(email);

        // 3. 调用 UserService 现有的基础更新方法
        userService.updateUserBasic(user);

        return Result.success(null);
    }
}