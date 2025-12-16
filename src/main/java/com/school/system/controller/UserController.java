package com.school.system.controller;

import com.alibaba.excel.EasyExcel;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.school.system.common.MD5Utils;
import com.school.system.common.Result;
import com.school.system.common.UserImportListener;
import com.school.system.entity.User;
import com.school.system.entity.UserImportDTO;
import com.school.system.mapper.StudentMapper;
import com.school.system.mapper.TeacherMapper;
import com.school.system.mapper.UserMapper;
import com.school.system.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 仅管理员可用
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private TeacherMapper teacherMapper;

    @GetMapping
    /**
     * 分页查询用户列表，仅管理员可用。
     *
     * @param pageNum         页码
     * @param pageSize        每页数量
     * @param condition       查询条件
     * @param currentUserType 当前用户角色
     * @return 分页用户列表
     */
    public Result<PageInfo<User>> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            User condition,
            @RequestAttribute("userType") String currentUserType
    ) {
        if (!"admin".equals(currentUserType)) {
            return Result.error("无权限访问用户列表");
        }
        // 1. 开启分页
        PageHelper.startPage(pageNum, pageSize);

        // 2. 执行查询
        List<User> list = userService.listUsers(condition);

        // 3. 封装分页结果
        PageInfo<User> pageInfo = new PageInfo<>(list);

        return Result.success(pageInfo);
    }

    @DeleteMapping("/{userId}")
    /**
     * 删除用户（级联关联数据），仅管理员可用。
     *
     * @param userId          目标用户 ID
     * @param currentUserType 当前用户角色
     * @return 操作结果
     */
    public Result<Void> deleteUser(@PathVariable Long userId,
                                   @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType)) {
            return Result.error("无权限删除用户");
        }
        boolean success = userService.deleteUser(userId);
        if (success) {
            return Result.success(null);
        } else {
            return Result.error("删除失败，用户可能不存在");
        }
    }

    @PostMapping("/password")
    /**
     * 修改用户密码。管理员可改任意用户，其他角色仅能改自己。
     *
     * @param params          请求体包含 userId 和 newPwd
     * @param currentUserId   当前用户 ID
     * @param currentUserType 当前用户角色
     * @return 操作结果
     */
    public Result<Void> updatePassword(@RequestBody Map<String, Object> params,
                                       @RequestAttribute("userId") Long currentUserId,
                                       @RequestAttribute("userType") String currentUserType) {
        Long userId = Long.valueOf(params.get("userId").toString());
        if (!"admin".equals(currentUserType) && !userId.equals(currentUserId)) {
            return Result.error("无权限修改他人密码");
        }
        String newPwd = (String) params.get("newPwd");
        String encryptedPwd = MD5Utils.encrypt(newPwd);

        User user = new User();
        user.setUserId(userId);
        user.setUserPwd(encryptedPwd);
        userMapper.updateById(user);

        return Result.success(null);
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    /**
     * 新增或更新用户，仅管理员可用。
     *
     * @param params          用户及扩展信息
     * @param currentUserType 当前用户角色
     * @return 操作结果
     */
    public Result<Void> saveUser(@RequestBody Map<String, Object> params,
                                 @RequestAttribute("userType") String currentUserType) {

        if (!"admin".equals(currentUserType)) {
            return Result.error("无权限操作用户");
        }
        String phone = (String) params.get("userPhone");
        String email = (String) params.get("userEmail");
        if (phone != null && !phone.isEmpty()) {
            if (!phone.matches("^\\d{11}$")) {
                return Result.error("手机号必须为11位数字");
            }
        }
        if (email != null && !email.isEmpty()) {
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            if (!email.matches(emailRegex)) {
                return Result.error("邮箱格式不正确 (例: abc@163.com)");
            }
        }
        // 2. 提取 User 表基础数据
        User user = new User();
        if (params.get("userId") != null) user.setUserId(Long.valueOf(params.get("userId").toString()));
        user.setUserName((String) params.get("userName"));
        user.setUserRealName((String) params.get("userRealName"));
        user.setUserType((String) params.get("userType"));
        user.setUserPhone((String) params.get("userPhone"));
        user.setUserEmail((String) params.get("userEmail"));

        // 密码逻辑
        String pwd = (String) params.get("userPwd");
        if (pwd != null && !pwd.isEmpty()) {
            user.setUserPwd(pwd);
        }

        // 3. 分支处理
        if (user.getUserId() == null) {
            userService.addUser(user, params);
        } else {
            userService.updateUserBasic(user);

            // 2. 如果角色是学生，更新学生表
            if ("student".equals(user.getUserType())) {
                String college = (String) params.get("studentCollege");
                String stuClass = (String) params.get("studentClass");
                String grade = (String) params.get("studentGrade");

                com.school.system.entity.Student student = studentMapper.selectByUserId(user.getUserId());
                if (student == null) {
                    student = new com.school.system.entity.Student();
                    student.setUserId(user.getUserId());
                    student.setStudentCollege(college);
                    student.setStudentClass(stuClass);
                    student.setStudentGrade(grade);
                    studentMapper.insert(student);
                } else {
                    student.setStudentCollege(college);
                    student.setStudentClass(stuClass);
                    student.setStudentGrade(grade);
                    studentMapper.update(student);
                }
            }

            // 3. 如果角色是教师，更新教师表 (teacher_table)
            else if ("teacher".equals(user.getUserType())) {
                String college = (String) params.get("teacherCollege");
                String title = (String) params.get("teacherTitle");

                com.school.system.entity.Teacher teacher = teacherMapper.selectByUserId(user.getUserId());
                if (teacher == null) {
                    teacher = new com.school.system.entity.Teacher();
                    teacher.setUserId(user.getUserId());
                    teacher.setTeacherCollege(college);
                    teacher.setTeacherTitle(title);
                    teacher.setTeacherCreatetime(new java.util.Date());
                    teacherMapper.insert(teacher);
                } else {
                    teacher.setTeacherCollege(college);
                    teacher.setTeacherTitle(title);
                    teacherMapper.updateById(teacher);
                }
            }
        }

        return Result.success(null);
    }
    @PostMapping("/import")
    /**
     * 导入用户数据
     *
     * @param file Excel文件
     * @param currentUserType 当前用户角色
     * @return 导入结果
     */
    public Result<Void> importUsers(@RequestParam("file") MultipartFile file,
                                    @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType)) {
            return Result.error("无权限导入用户");
        }
        try {
            EasyExcel.read(file.getInputStream(), UserImportDTO.class, new UserImportListener(userService))
                    .sheet()
                    .doRead();
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/import/template")
    /**
     * 下载用户导入模板
     *
     * @param response        响应对象
     * @param currentUserType 当前用户角色
     * @throws IOException 写出异常
     */
    public void downloadTemplate(HttpServletResponse response) throws IOException {

        // 1. 设置响应头类型
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 2. 设置文件名
        String fileName = URLEncoder.encode("用户导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName + ".xlsx");

        // 3. 写出 Excel
        EasyExcel.write(response.getOutputStream(), UserImportDTO.class)
                .sheet("导入模板")
                .doWrite(new ArrayList<>());
    }
}