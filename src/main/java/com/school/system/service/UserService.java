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
        // 必须使用加密后的密码进行比对
        String encryptedInput = MD5Utils.encrypt(password);
        return user.getUserPwd().equals(encryptedInput);
    }

    /**
     * 重置密码
     *
     * @param userId      目标用户 ID
     * @param newPassword 新密码（明文）
     */
    public void resetPassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new ServiceException("用户不存在");

        // 修复：密码必须加密
        user.setUserPwd(MD5Utils.encrypt(newPassword));
        user.setUserUpdatetime(new Date());
        userMapper.updateById(user);
    }

    /**
     * 新增用户 (事务管理)
     *
     * @param user      基础用户信息
     * @param extraInfo 扩展信息（学生/教师字段）
     */
    @Transactional(rollbackFor = Exception.class) // 建议加上 rollbackFor，防止异常吞掉
    public void addUser(User user, Map<String, Object> extraInfo) {
        // 1. 校验用户名唯一性
        User exist = userMapper.findByUsername(user.getUserName());
        if (exist != null) {
            throw new ServiceException("该用户名已存在");
        }

        // 2. 密码处理 (核心修复点)
        String rawPwd = user.getUserPwd();
        if (rawPwd == null || rawPwd.isEmpty()) {
            rawPwd = "123456"; // 默认密码
        }
        // !!! 必须加密后再存入数据库 !!!
        user.setUserPwd(MD5Utils.encrypt(rawPwd));

        // 3. 补全基础信息
        user.setUserCreatetime(new Date());
        user.setUserUpdatetime(new Date());
        userMapper.insert(user);

        Long userId = user.getUserId(); // 获取回填的主键 ID

        // 4. 根据角色插入扩展表
        if ("student".equals(user.getUserType())) {
            Student student = new Student();
            student.setUserId(userId);
            // 防止 extraInfo 为 null
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
            // 确保 StudentMapper 有 insert 方法
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
            // 确保 TeacherMapper 有 insert 方法
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
        // 注意：Mapper 中需要有 deleteByUserId 方法，如果没有，请在 XML 中添加
        if ("student".equals(user.getUserType())) {
            try {
                // 如果 StudentMapper 还没写 deleteByUserId，这里可能会红
                // 建议去 StudentMapper.xml 补一个: DELETE FROM student_table WHERE user_id = #{userId}
                studentMapper.deleteByUserId(userId);
            } catch (Exception e) {
                // 容错处理：如果Mapper没写这个方法，暂时忽略，依赖数据库级联或手动清理
                System.err.println("警告：尝试删除学生档案失败，可能是Mapper方法未定义: " + e.getMessage());
            }
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
        // 如果这里包含密码修改，也要加密，但通常 updateBasic 不含密码
        // 如果包含密码，请在 Controller 层处理好加密后再传进来，或者在这里判断
        user.setUserUpdatetime(new Date());
        userMapper.updateById(user);
    }
}