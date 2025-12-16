package com.school.system.common;

import com.school.system.common.Result; // 确保引用了你之前的 Result 类
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;

/**
 * 全局异常处理器
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 1. 捕获业务异常 (ServiceException)
     * 对应场景：密码错误、重复选课、名额已满
     * 处理策略：直接将异常信息 (msg) 返回给用户
     *
     * @param e 业务异常
     * @return 统一错误响应
     */
    @ExceptionHandler(ServiceException.class)
    public Result<?> handleServiceException(ServiceException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 2. 捕获数据库异常
     * 对应场景：外键约束失败、唯一索引冲突（如录入重复学号）
     *
     * @param e SQL 异常
     * @return 统一错误响应
     */
    @ExceptionHandler(SQLException.class)
    public Result<?> handleSqlException(SQLException e) {
        log.error("数据库异常: ", e);
        if (e.getMessage().contains("Duplicate entry")) {
            return Result.error("该信息已存在，请勿重复录入");
        }
        return Result.error("数据库操作异常，请检查输入");
    }

    /**
     * 3. 捕获所有其他未知异常 (Exception)
     * 对应场景：空指针 (NPE)、代码 Bug、数据库断连
     *
     * @param e 未知异常
     * @return 统一错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统未知异常: ", e);
        return Result.error("系统连接异常或繁忙，请稍后重试");
    }

    /**
     * 4. 捕获静态资源找不到的异常
     *
     * @param e 资源未找到异常
     * @return 404 响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFoundException(NoResourceFoundException e) {
        // 不需要打印堆栈日志，只返回 404
        return new Result<>(404, "资源不存在: " + e.getResourcePath(), null);
    }
}