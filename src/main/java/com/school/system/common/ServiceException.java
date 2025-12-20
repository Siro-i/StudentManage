package com.school.system.common;

import lombok.Getter;

/**
 * 业务逻辑异常
 * 用于主动抛出：密码错误、名额已满、重复选课等
 */
@Getter
public class ServiceException extends RuntimeException {

    private Integer code;

    /**
     * 构造函数，创建服务异常
     * 
     * @param message 异常消息
     */
    public ServiceException(String message) {
        super(message);
        this.code = 500; 
    }


}