package com.school.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Student {
    /** 学生ID (主键) */
    private Long studentId;

    /** 关联的用户ID */
    private Long userId;

    /** 所属学院 */
    private String studentCollege;

    /** 年级 */
    private String studentGrade;

    /** 班级 */
    private String studentClass;

    /** 创建时间 */
    private Date studentCreatetime;

    /** 更新时间 */
    private Date studentUpdatetime;
}