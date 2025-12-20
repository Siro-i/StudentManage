package com.school.system.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserImportDTO {

    @ExcelProperty("账号")
    private String userName;

    @ExcelProperty("姓名")
    private String userRealName;

    @ExcelProperty("手机号")
    private String userPhone;

    @ExcelProperty("邮箱")
    private String userEmail;


    @ExcelProperty("学院")
    private String college;

    @ExcelProperty("班级")
    private String studentClass;

    @ExcelProperty("年级")
    private String grade;


}