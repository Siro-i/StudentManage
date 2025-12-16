package com.school.system.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
/**
 * 登录拦截器，验证JWT token
 *
 *
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    /**
     * 请求预处理，验证JWT token
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @return 是否继续处理请求
     * @throws Exception 异常
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行跨域预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            writeUnauthorized(response, "未登录或凭证缺失");
            return false;
        }

        try {
            Claims claims = JwtUtils.parseToken(token);
            request.setAttribute("userId", claims.get("userId", Long.class));
            request.setAttribute("userType", claims.get("userType", String.class));
            return true;
        } catch (Exception e) {
            writeUnauthorized(response, "登录已过期或凭证无效");
            return false;
        }
    }

    /**
     * 写入未授权响应
     * 
     * @param response HTTP响应
     * @param message 错误消息
     */
    private void writeUnauthorized(HttpServletResponse response, String message) {
        try {
            response.setStatus(401);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> body = new HashMap<>();
            body.put("code", 401);
            body.put("msg", message);
            body.put("data", null);
            response.getWriter().write(MAPPER.writeValueAsString(body));
        } catch (Exception ignored) {
        }
    }
}