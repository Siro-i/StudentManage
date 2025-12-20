package com.school.system.common;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.school.system.entity.User;
import com.school.system.entity.UserImportDTO;
import com.school.system.service.UserService;
import java.util.HashMap;
import java.util.Map;

/**
 * 监听器：负责读取 Excel 每一行并调用 Service 保存
 */
public class UserImportListener implements ReadListener<UserImportDTO> {

    private UserService userService;

    /**
     * 构造函数，初始化用户服务
     * 
     * @param userService 用户服务实例
     */
    public UserImportListener(UserService userService) {
        this.userService = userService;
    }

    /**
     * 处理Excel导入的每一行数据
     * 
     * @param data Excel行数据
     * @param context 分析上下文
     */
    @Override
    public void invoke(UserImportDTO data, AnalysisContext context) {
        // 1. 数据清洗：如果账号为空，跳过
        if (data.getUserName() == null || data.getUserName().isEmpty()) return;

        try {
            // 2. 构造 User 对象
            User user = new User();
            user.setUserName(data.getUserName());
            user.setUserRealName(data.getUserRealName());
            user.setUserPhone(data.getUserPhone());
            user.setUserEmail(data.getUserEmail());
            user.setUserType("student"); // 默认批量导入的都是学生
            user.setUserPwd("123456");   // 默认密码

            // 3. 构造扩展信息 Map
            Map<String, Object> extraInfo = new HashMap<>();
            extraInfo.put("studentCollege", data.getCollege());
            extraInfo.put("studentClass", data.getStudentClass());
            extraInfo.put("studentGrade", data.getGrade());

            // 4. 调用 Service 保存 (复用之前的逻辑)
            userService.addUser(user, extraInfo);

        } catch (Exception e) {
            // 遇到重复账号或其他错误，打印日志并跳过，不中断整个流程
            System.err.println("导入失败: " + data.getUserName() + " 原因: " + e.getMessage());
        }
    }

    /**
     * Excel导入完成后执行的操作
     * 
     * @param context 分析上下文
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        System.out.println("Excel 导入完成！");
    }
}