package com.school.system.entity;

import lombok.Data;
import java.util.Date;

/**
 * 用户表 (user_table)
 * 存储所有角色的通用账号信息
 */
@Data
public class User {
    /** 用户ID (主键) */
    private Long userId;

    /** 用户名 */
    private String userName;

    /** 密码 (加密存储) */
    private String userPwd;

    /** 邮箱 */
    private String userEmail;

    /** 手机号 */
    private String userPhone;

    /** 用户类型 (student/teacher/admin) */
    private String userType;

    /** 注册时间 */
    private Date userCreatetime;

    /** 更新时间 */
    private Date userUpdatetime;
    /** 真实姓名 */
    private String userRealName;
}