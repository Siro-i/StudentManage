package com.school.system.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;


@Data
@ColumnWidth(20) // 设置列宽
public class ScoreExportVO {

    @ExcelProperty("学生姓名")
    private String studentName;

    @ExcelProperty("学号")
    private String studentNumber;

    @ExcelProperty("班级")
    private String studentClass;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("成绩")
    private Integer score;


}