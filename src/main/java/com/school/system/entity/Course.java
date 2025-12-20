package com.school.system.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Course {
    /** 课程ID (主键) */
    private Long courseId;

    /** 课程名称 */
    private String courseName;

    /** 学分  */
    private Integer courseCredit;

    /** 授课教师ID */
    private Long teacherId;

    /** 授课教师姓名 */
    private String teacherName;

    /** 课程状态 (1-可选 0-不可选) */
    private Integer courseStatus;

    /** 最大选课人数 (逻辑补充字段) */
    private Integer maxNum;

    /** 当前已选人数 (逻辑补充字段) */
    private Integer selectedNum;

    /** 创建时间 */
    private Date courseCreatetime;

    /** 更新时间 */
    private Date courseUpdatetime;
    /** 上课时间 */
    private String courseTime;
    /** 上课地点 */
    private String courseRoom;
    /** 版本号（用于乐观锁） */
    private Integer version;
}