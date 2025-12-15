package com.school.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Admin {
    /** 管理员ID (主键) */
    private Long adminId;

    /** 关联的用户ID */
    private Long userId;

    /** 职称 (系统管理员/超级管理员) */
    private String adminTitle;

    /** 创建时间 */
    private Date adminCreatetime;

    /** 更新时间 */
    private Date adminUpdatetime;
}