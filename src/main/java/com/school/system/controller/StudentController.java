package com.school.system.controller;

import com.school.system.common.Result;
import com.school.system.entity.Student;
import com.school.system.mapper.StudentMapper;
import com.school.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public Result<Student> getStudentInfo(@RequestAttribute("userId") Long currentUserId,
                                          @RequestAttribute("userType") String currentUserType) {
        if (!"student".equals(currentUserType)) {
            return Result.error("仅学生可查看个人信息");
        }
        Student student = studentMapper.selectByUserId(currentUserId);
        return student != null ? Result.success(student) : Result.error("未找到学生档案");
    }

    @PutMapping("/profile")
    public Result<Void> updateStudentProfile(@RequestBody Map<String, String> params,
                                             @RequestAttribute("userId") Long userId,
                                             @RequestAttribute("userType") String currentUserType) {
        if (!"student".equals(currentUserType)) return Result.error("仅学生可修改个人资料");

        userService.updateMyProfile(userId, params.get("userPhone"), params.get("userEmail"));
        return Result.success(null);
    }
}