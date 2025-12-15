package com.school.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class StudentCourse {
    /** 选课记录ID (主键) */
    private Long scId;

    /** 学生ID */
    private Long studentId;

    /** 学生姓名 */
    private String studentName;

    /** 学生班级 */
    private String studentClass;

    /** 课程ID */
    private Long courseId;

    /** 成绩 (0-100, NULL表示未出分) */
    private Integer scScore;

    /** 选课时间 */
    private Date scSelecttime;

    /** 更新时间 (成绩录入时间) */
    private Date scUpdatetime;


    /**
     * 设置选课时间
     * 
     * @param date 选课时间
     */
    public void setSelectTime(Date date) {
        this.scSelecttime = date;
    }
}