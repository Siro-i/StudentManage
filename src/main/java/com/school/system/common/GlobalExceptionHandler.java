package com.school.system.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
     * 2. 捕获所有其他未知异常 (Exception)
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
     * 3. 捕获静态资源找不到的异常
     *
     * @param e 资源未找到异常
     * @return 404 响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFoundException(NoResourceFoundException e) {
        return new Result<>(404, "资源不存在: " + e.getResourcePath(), null);
    }
}