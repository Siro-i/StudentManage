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
    @GetMapping("/info")
    /**
     * 教师查看个人档案信息，绑定当前登录教师。
     *
     * @param currentUserId   当前用户 ID
     * @param currentUserType 当前用户角色
     * @return 教师档案
     */
    public Result<Teacher> getTeacherInfo(@RequestAttribute("userId") Long currentUserId,
                                          @RequestAttribute("userType") String currentUserType) {
        if (!"teacher".equals(currentUserType)) {
            return Result.error("仅教师可查看个人信息");
        }
        Teacher teacher = teacherMapper.selectByUserId(currentUserId);
        return Result.success(teacher);
    }

    @PutMapping("/profile")
    /**
     * 教师修改个人联系方式（手机号、邮箱），仅允许当前教师自身。
     *
     * @param params          待更新数据
     * @param userId          当前用户 ID
     * @param currentUserType 当前用户角色
     * @return 操作结果
     */
    public Result<Void> updateTeacherProfile(@RequestBody Map<String, String> params,
                                             @RequestAttribute("userId") Long userId,
                                             @RequestAttribute("userType") String currentUserType) {

        if (!"teacher".equals(currentUserType)) {
            return Result.error("仅教师可修改个人资料");
        }

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
