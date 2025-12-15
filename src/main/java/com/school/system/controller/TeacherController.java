package com.school.system.controller;

import com.school.system.common.Result;
import com.school.system.entity.Teacher;
import com.school.system.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {


    @Autowired
    private com.school.system.mapper.TeacherMapper teacherMapper;
    @Autowired
    private com.school.system.service.UserService userService;
    /**
     * 1. 获取教师个人详细档案
     */
    @GetMapping("/info/{userId}")

    public Result<Teacher> getTeacherInfo(@PathVariable Long userId) {
        Teacher teacher = teacherMapper.selectByUserId(userId);
        return Result.success(teacher);
    }

    /**
     * 2. 修改个人资料 (带正则验证)
     */
    @PutMapping("/profile")
    public Result<Void> updateTeacherProfile(@RequestBody Map<String, String> params,
                                             @RequestAttribute("userId") Long userId) {

        // 数据格式校验
        String phone = params.get("userPhone");
        String email = params.get("userEmail");

        // 校验手机号
        if (phone != null && !phone.isEmpty()) {
            if (!phone.matches("^\\d{11}$")) {
                return Result.error("手机号必须为11位数字");
            }
        }

        // 校验邮箱
        if (email != null && !email.isEmpty()) {
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            if (!email.matches(emailRegex)) {
                return Result.error("邮箱格式不正确");
            }
        }
        // -----------------------

        // 1. 更新 User 表 (手机/邮箱)
        User user = new User();
        user.setUserId(userId);
        user.setUserPhone(phone);
        user.setUserEmail(email);
        userService.updateUserBasic(user);


        return Result.success(null);
    }
}
