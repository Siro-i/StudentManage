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

    /**
     * 获取教师个人信息
     * 
     * @param currentUserId 当前用户ID
     * @param currentUserType 当前用户类型
     * @return 教师信息结果
     */
    @GetMapping("/info")
    public Result<Teacher> getTeacherInfo(@RequestAttribute("userId") Long currentUserId,
                                          @RequestAttribute("userType") String currentUserType) {
        if (!"teacher".equals(currentUserType)) {
            return Result.error("仅教师可查看个人信息");
        }
        Teacher teacher = teacherMapper.selectByUserId(currentUserId);
        return Result.success(teacher);
    }

    /**
     * 更新教师个人资料
     * 
     * @param params 更新参数，包含手机号和邮箱
     * @param userId 用户ID
     * @param currentUserType 当前用户类型
     * @return 更新结果
     */
    @PutMapping("/profile")
    public Result<Void> updateTeacherProfile(@RequestBody Map<String, String> params,
                                             @RequestAttribute("userId") Long userId,
                                             @RequestAttribute("userType") String currentUserType) {
        if (!"teacher".equals(currentUserType)) return Result.error("仅教师可修改个人资料");

        userService.updateMyProfile(userId, params.get("userPhone"), params.get("userEmail"));
        return Result.success(null);
    }
    /**
     * 管理员专用：获取指定教师的详细档案
     * @param userId 教师ID
     * @param currentUserType 当前用户类型
     * @return 教师档案结果
     */
    @GetMapping("/info/{userId}")
    public Result<Teacher> getTeacherInfoById(@PathVariable Long userId,
                                              @RequestAttribute("userType") String currentUserType) {
        // 安全检查
        if (!"admin".equals(currentUserType)) {
            return Result.error("无权限查看该教师档案");
        }
        Teacher teacher = teacherMapper.selectByUserId(userId);
        return Result.success(teacher);
    }
}