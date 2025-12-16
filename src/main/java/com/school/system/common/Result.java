package com.school.system.common;

import lombok.Data;

@Data
/**
 * 统一结果类，用于封装 API 响应
 *
 * @param <T> 数据类型
 */
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    /**
     * 构造函数，创建结果对象
     * 
     * @param code 状态码
     * @param msg 消息
     * @param data 数据
     */
    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 创建成功结果
     * 
     * @param <T> 数据类型
     * @param data 返回数据
     * @return 成功结果对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 创建错误结果
     * 
     * @param <T> 数据类型
     * @param msg 错误消息
     * @return 错误结果对象
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
}