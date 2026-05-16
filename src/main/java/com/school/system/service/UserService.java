package com.school.system.service;

import com.school.system.common.MD5Utils;
import com.school.system.common.ServiceException;
import com.school.system.entity.Admin;
import com.school.system.entity.Student;
import com.school.system.entity.Teacher;
import com.school.system.entity.User;
import com.school.system.mapper.AdminMapper;
import com.school.system.mapper.StudentMapper;
import com.school.system.mapper.TeacherMapper;
import com.school.system.mapper.UserMapper;
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
    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private CaptchaService captchaService;

    public boolean login(String username, String password, String captchaId, String captchaCode) {
        if (!captchaService.verifyCaptcha(captchaId, captchaCode)) {
            throw new ServiceException("验证码错误");
        }
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return false;
        }
        String encryptedInput = MD5Utils.encrypt(password);
        return user.getUserPwd().equals(encryptedInput);
    }

    /**
     * 管理员：保存或更新用户 (统一入口)
     * 
     * @param params 用户参数
     * @param operatorType 操作者类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateUser(Map<String, Object> params, String operatorType) {
        if (!"admin".equals(operatorType)) {
            throw new ServiceException("无权限操作用户");
        }

        // 提取数据
        String phone = (String) params.get("userPhone");
        String email = (String) params.get("userEmail");
        validateContactInfo(phone, email);

        Long userId = params.get("userId") != null ? Long.parseLong(params.get("userId").toString()) : null;
        if (StringUtils.hasText(phone)) {
            User phoneExist = userMapper.findByPhone(phone);
            if (phoneExist != null && phoneExist.getUserId() != null && !phoneExist.getUserId().equals(userId)) {
                throw new ServiceException("手机号已被其他用户使用");
            }
        }

        User user = new User();
        if (params.get("userId") != null) {
            user.setUserId(Long.valueOf(params.get("userId").toString()));
        }
        user.setUserName((String) params.get("userName"));
        user.setUserRealName((String) params.get("userRealName"));
        user.setUserType((String) params.get("userType"));
        user.setUserPhone(phone);
        user.setUserEmail(email);

        String pwd = (String) params.get("userPwd");
        if (StringUtils.hasText(pwd)) {
            user.setUserPwd(pwd);
        }

        if (user.getUserId() == null) {
            this.addUser(user, params);
        } else {
            this.updateUserFull(user, params);
        }
    }

    /**
     * 新增用户 (事务)
     * 
     * @param user 用户信息
     * @param extraInfo 额外信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUser(User user, Map<String, Object> extraInfo) {
        if (user == null) throw new ServiceException("用户信息缺失");
        if (!StringUtils.hasText(user.getUserName())) throw new ServiceException("账号不能为空");
        if (!StringUtils.hasText(user.getUserRealName())) throw new ServiceException("姓名不能为空");

        User exist = userMapper.findByUsername(user.getUserName());
        if (exist != null) throw new ServiceException("账号已存在");

        if (StringUtils.hasText(user.getUserPhone())) {
            User phoneExist = userMapper.findByPhone(user.getUserPhone());
            if (phoneExist != null) throw new ServiceException("手机号已被使用");
        }

        // 密码处理
        String rawPwd = StringUtils.hasText(user.getUserPwd()) ? user.getUserPwd() : "123456";
        user.setUserPwd(MD5Utils.encrypt(rawPwd));
        user.setUserCreatetime(new Date());
        user.setUserUpdatetime(new Date());

        userMapper.insert(user);
        Long userId = user.getUserId();

        // 插入扩展表
        if ("student".equals(user.getUserType())) {
            Student s = new Student();
            s.setUserId(userId);
            s.setStudentCollege((String) extraInfo.getOrDefault("studentCollege", "未分配"));
            s.setStudentClass((String) extraInfo.getOrDefault("studentClass", ""));
            s.setStudentGrade((String) extraInfo.getOrDefault("studentGrade", ""));
            s.setStudentCreatetime(new Date());
            studentMapper.insert(s);

        } else if ("teacher".equals(user.getUserType())) {
            Teacher t = new Teacher();
            t.setUserId(userId);
            t.setTeacherCollege((String) extraInfo.getOrDefault("teacherCollege", "未分配"));
            t.setTeacherTitle((String) extraInfo.getOrDefault("teacherTitle", "讲师"));
            t.setTeacherCreatetime(new Date());
            teacherMapper.insert(t);

        } else if ("admin".equals(user.getUserType())) {
            Admin a = new Admin();
            a.setUserId(userId);
            a.setAdminTitle("普通管理员");
            a.setAdminCreatetime(new Date());
            adminMapper.insert(a);
        }
    }

    /**
     * 全量更新用户 (事务)
     * 
     * @param user 用户信息
     * @param extraInfo 额外信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserFull(User user, Map<String, Object> extraInfo) {
        // 更新主表
        if (StringUtils.hasText(user.getUserPwd())) {
            user.setUserPwd(MD5Utils.encrypt(user.getUserPwd()));
        }
        user.setUserUpdatetime(new Date());
        userMapper.updateById(user);

        // 更新扩展表
        if ("student".equals(user.getUserType())) {
            Student s = studentMapper.selectByUserId(user.getUserId());
            boolean isNew = (s == null);
            if (isNew) {
                s = new Student();
                s.setUserId(user.getUserId());
                s.setStudentCreatetime(new Date());
            }
            s.setStudentCollege((String) extraInfo.getOrDefault("studentCollege", ""));
            s.setStudentClass((String) extraInfo.getOrDefault("studentClass", ""));
            s.setStudentGrade((String) extraInfo.getOrDefault("studentGrade", ""));

            if (isNew) studentMapper.insert(s);
            else studentMapper.update(s);

        } else if ("teacher".equals(user.getUserType())) {
            Teacher t = teacherMapper.selectByUserId(user.getUserId());
            boolean isNew = (t == null);
            if (isNew) {
                t = new Teacher();
                t.setUserId(user.getUserId());
                t.setTeacherCreatetime(new Date());
            }
            t.setTeacherCollege((String) extraInfo.getOrDefault("teacherCollege", ""));
            t.setTeacherTitle((String) extraInfo.getOrDefault("teacherTitle", ""));

            if (isNew) teacherMapper.insert(t);
            else teacherMapper.updateById(t);
        }
    }

    /**
     * 删除用户
     * 
     * @param userId 用户ID
     * @return 删除是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return true;

        if ("student".equals(user.getUserType())) {
            studentMapper.deleteByUserId(userId);
        } else if ("teacher".equals(user.getUserType())) {
            teacherMapper.deleteByUserId(userId);
        }
        return userMapper.deleteById(userId) > 0;
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
     * 修改密码
     * 
     * @param targetUserId 目标用户ID
     * @param newPwd 新密码
     * @param operatorType 操作者类型
     * @param operatorId 操作者ID
     */
    public void updatePassword(Long targetUserId, String newPwd, String operatorType, Long operatorId) {
        if (!"admin".equals(operatorType) && !targetUserId.equals(operatorId)) {
            throw new ServiceException("无权限修改他人密码");
        }
        User user = new User();
        user.setUserId(targetUserId);
        user.setUserPwd(MD5Utils.encrypt(newPwd));
        userMapper.updateById(user);
    }

    /**
     * 更新个人资料 (学生/教师通用)
     * 
     * @param userId 用户ID
     * @param phone 手机号
     * @param email 邮箱
     */
    public void updateMyProfile(Long userId, String phone, String email) {
        validateContactInfo(phone, email);

        if (StringUtils.hasText(phone)) {
            User phoneExist = userMapper.findByPhone(phone);
            if (phoneExist != null && !phoneExist.getUserId().equals(userId)) {
                throw new ServiceException("手机号已被其他用户使用");
            }
        }

        User user = new User();
        user.setUserId(userId);
        user.setUserPhone(phone);
        user.setUserEmail(email);
        user.setUserUpdatetime(new Date());
        userMapper.updateById(user);
    }

    /**
     * 校验联系方式
     * 
     * @param phone 手机号
     * @param email 邮箱
     */
    public void validateContactInfo(String phone, String email) {
        if (StringUtils.hasText(phone) && !phone.matches("^\\d{11}$")) {
            throw new ServiceException("手机号必须为11位数字");
        }
        if (StringUtils.hasText(email) && !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new ServiceException("邮箱格式不正确");
        }
    }
}