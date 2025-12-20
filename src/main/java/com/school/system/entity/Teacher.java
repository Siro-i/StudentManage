package com.school.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Teacher {
    /** 教师ID (主键) */
    private Long teacherId;

    /** 关联的用户ID */
    private Long userId;

    /** 所属学院 */
    private String teacherCollege;

    /** 职称 (讲师/副教授/教授) */
    private String teacherTitle;

    /** 创建时间 */
    private Date teacherCreatetime;

    /** 更新时间 */
    private Date teacherUpdatetime;
}