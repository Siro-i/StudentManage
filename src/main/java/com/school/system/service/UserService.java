package com.school.system.service;

import com.school.system.common.MD5Utils;
import com.school.system.common.ServiceException;
import com.school.system.entity.Student;
import com.school.system.entity.Teacher;
import com.school.system.entity.User;
import com.school.system.mapper.UserMapper;
import com.school.system.mapper.TeacherMapper;
import com.school.system.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private StudentMapper studentMapper;

    /**
     * 登录逻辑
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 登录是否成功
     */
    public boolean login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return false;
        }
        String encryptedInput = MD5Utils.encrypt(password);
        return user.getUserPwd().equals(encryptedInput);
    }



    /**
     * 新增用户
     *
     * @param user      基础用户信息
     * @param extraInfo 扩展信息（学生/教师字段）
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUser(User user, Map<String, Object> extraInfo) {
        if (user == null) {
            throw new ServiceException("用户信息数据缺失");
        }
        // 校验账号
        if (!StringUtils.hasText(user.getUserName())) {
            throw new ServiceException("必须填写账号(用户名)");
        }
        // 校验姓名
        if (!StringUtils.hasText(user.getUserRealName())) {
            throw new ServiceException("必须填写真实姓名");
        }
        // 校验角色类型 (防止恶意篡改或空值)
        if (!"student".equals(user.getUserType())
                && !"teacher".equals(user.getUserType())
                && !"admin".equals(user.getUserType())) {
            throw new ServiceException("无效的用户角色类型: " + user.getUserType());
        }
        //  校验用户名唯一性
        User exist = userMapper.findByUsername(user.getUserName());
        if (exist != null) {
            throw new ServiceException("该用户名已存在");
        }

        //  密码处理
        String rawPwd = user.getUserPwd();
        if (rawPwd == null || rawPwd.isEmpty()) {
            rawPwd = "123456"; // 默认密码
        }
        user.setUserPwd(MD5Utils.encrypt(rawPwd));

        //  补全基础信息
        user.setUserCreatetime(new Date());
        user.setUserUpdatetime(new Date());
        userMapper.insert(user);

        Long userId = user.getUserId(); // 获取回填的主键 ID

        //  根据角色插入扩展表
        if ("student".equals(user.getUserType())) {
            Student student = new Student();
            student.setUserId(userId);
            if (extraInfo != null) {
                student.setStudentCollege((String) extraInfo.getOrDefault("studentCollege", "未分配学院"));
                student.setStudentGrade((String) extraInfo.getOrDefault("studentGrade", "2025级"));
                student.setStudentClass((String) extraInfo.getOrDefault("studentClass", "1班"));
            } else {
                student.setStudentCollege("未分配学院");
                student.setStudentGrade("2025级");
                student.setStudentClass("1班");
            }
            student.setStudentCreatetime(new Date());
            studentMapper.insert(student);

        } else if ("teacher".equals(user.getUserType())) {
            Teacher teacher = new Teacher();
            teacher.setUserId(userId);
            if (extraInfo != null) {
                teacher.setTeacherCollege((String) extraInfo.getOrDefault("teacherCollege", "未分配学院"));
                teacher.setTeacherTitle((String) extraInfo.getOrDefault("teacherTitle", "讲师"));
            } else {
                teacher.setTeacherCollege("未分配学院");
                teacher.setTeacherTitle("讲师");
            }
            teacher.setTeacherCreatetime(new Date());
            teacherMapper.insert(teacher);
        }
    }

    /**
     * 查询用户列表
     *
     * @param condition 查询条件
     * @return 用户列表
     */
    public List<User> listUsers(User condition) {
        return userMapper.selectList(condition);
    }

    /**
     * 删除用户 (级联删除)
     *
     * @param userId 用户 ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        // 1. 先查用户，确定类型
        User user = userMapper.selectById(userId);
        if (user == null) return true;

        // 2. 删除关联表数据 (防止外键报错或残留数据)
        if ("student".equals(user.getUserType())) {

                studentMapper.deleteByUserId(userId);

        } else if ("teacher".equals(user.getUserType())) {
            try {
                teacherMapper.deleteByUserId(userId);
            } catch (Exception e) {
                System.err.println("警告：尝试删除教师档案失败: " + e.getMessage());
            }
        }

        // 3. 删除主表数据
        return userMapper.deleteById(userId) > 0;
    }

    /**
     * 更新用户基本信息
     *
     * @param user 待更新的用户信息
     */
    public void updateUserBasic(User user) {
        user.setUserUpdatetime(new Date());
        userMapper.updateById(user);
    }
}