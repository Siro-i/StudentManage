package com.school.system.common;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行跨域预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 1. 获取 Token
        String token = request.getHeader("token");

        // 2. 判空
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            return false;
        }

        // 3. 验证 Token
        try {
            Claims claims = JwtUtils.parseToken(token);
            request.setAttribute("userId", claims.get("userId", Long.class));
            request.setAttribute("userType", claims.get("userType", String.class));

            return true; // 验证通过
        } catch (Exception e) {
            // Token 过期或被篡改
            response.setStatus(401);
            return false;
        }
    }
}