package com.school.system.controller;

import com.school.system.common.Result;
import com.school.system.entity.Teacher;
import com.school.system.mapper.TeacherMapper;
import com.school.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public Result<Teacher> getTeacherInfo(@RequestAttribute("userId") Long currentUserId,
                                          @RequestAttribute("userType") String currentUserType) {
        if (!"teacher".equals(currentUserType)) {
            return Result.error("仅教师可查看个人信息");
        }
        Teacher teacher = teacherMapper.selectByUserId(currentUserId);
        return Result.success(teacher);
    }

    @PutMapping("/profile")
    public Result<Void> updateTeacherProfile(@RequestBody Map<String, String> params,
                                             @RequestAttribute("userId") Long userId,
                                             @RequestAttribute("userType") String currentUserType) {
        if (!"teacher".equals(currentUserType)) return Result.error("仅教师可修改个人资料");

        userService.updateMyProfile(userId, params.get("userPhone"), params.get("userEmail"));
        return Result.success(null);
    }
}