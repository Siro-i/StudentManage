package com.school.system.controller;

import com.school.system.common.JwtUtils;
import com.school.system.common.Result;
import com.school.system.entity.User;
import com.school.system.mapper.UserMapper;
import com.school.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;
    /**
     * 登录接口
     * 对应 Loginable.login()
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("userName");
        String password = loginData.get("userPwd");

        try {
            boolean isSuccess = userService.login(username, password);
            if (isSuccess) {
                User user = userMapper.findByUsername(username);
                String token = JwtUtils.generateToken(user.getUserId(), user.getUserName(), user.getUserType());
                Map<String, Object> data = new java.util.HashMap<>();
                data.put("token", token);
                user.setUserPwd(null);
                data.put("userInfo", user);

                return Result.success(data);
            } else {
                return Result.error("账号或密码错误");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     * 对应 Loginable.resetPassword()
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String newPwd = params.get("newPassword").toString();
        userService.resetPassword(userId, newPwd);
        return Result.success(null);
    }
}