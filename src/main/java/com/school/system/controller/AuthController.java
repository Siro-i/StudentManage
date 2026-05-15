package com.school.system.controller;

import com.school.system.common.JwtUtils;
import com.school.system.common.Result;
import com.school.system.entity.User;
import com.school.system.mapper.UserMapper;
import com.school.system.service.CaptchaService;
import com.school.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        Map<String, String> captcha = captchaService.generateCaptcha();
        return Result.success(captcha);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("userName");
        String password = loginData.get("userPwd");
        String captchaId = loginData.get("captchaId");
        String captchaCode = loginData.get("captchaCode");

        if (captchaId == null || captchaCode == null || captchaId.isEmpty() || captchaCode.isEmpty()) {
            return Result.error("请输入验证码");
        }

        try {
            boolean isSuccess = userService.login(username, password, captchaId, captchaCode);
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


}