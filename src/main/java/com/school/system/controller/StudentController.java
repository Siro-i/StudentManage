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
    /**
     * 获取学生个人信息
     *
     * @param currentUserId 当前用户ID
     * @param currentUserType 当前用户类型
     * @return 学生信息结果
     */
    @GetMapping("/info")
    public Result<Student> getStudentInfo(@RequestAttribute("userId") Long currentUserId,
                                          @RequestAttribute("userType") String currentUserType) {
        if (!"student".equals(currentUserType)) {
            return Result.error("仅学生可查看个人信息");
        }
        Student student = studentMapper.selectByUserId(currentUserId);
        return student != null ? Result.success(student) : Result.error("未找到学生档案");
    }
    /**
     * 更新学生个人资料
     *
     * @param params 更新参数，包含手机号和邮箱
     * @param userId 用户ID
     * @param currentUserType 当前用户类型
     * @return 更新结果
     */
    @PutMapping("/profile")
    public Result<Void> updateStudentProfile(@RequestBody Map<String, String> params,
                                             @RequestAttribute("userId") Long userId,
                                             @RequestAttribute("userType") String currentUserType) {
        if (!"student".equals(currentUserType)) return Result.error("仅学生可修改个人资料");

        userService.updateMyProfile(userId, params.get("userPhone"), params.get("userEmail"));
        return Result.success(null);
    }
    /**
     * 管理员专用：获取指定学生的详细档案
     * @param userId 学生ID
     * @param currentUserType 当前用户类型
     * @return 学生档案结果
     */
    @GetMapping("/info/{userId}")
    public Result<Student> getStudentInfoById(@PathVariable Long userId,
                                              @RequestAttribute("userType") String currentUserType) {
        // 安全检查：只有管理员能查别人的
        if (!"admin".equals(currentUserType)) {
            return Result.error("无权限查看该学生档案");
        }
        Student student = studentMapper.selectByUserId(userId);
        return student != null ? Result.success(student) : Result.error("未找到学生档案");
    }
}