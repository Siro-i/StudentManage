package com.school.system.controller;

import com.alibaba.excel.EasyExcel;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.school.system.common.Result;
import com.school.system.common.UserImportListener;
import com.school.system.entity.User;
import com.school.system.entity.UserImportDTO;
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

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param condition 查询条件
     * @param currentUserType 当前用户类型
     * @return 用户列表分页结果
     */
    @GetMapping
    public Result<PageInfo<User>> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            User condition,
            @RequestAttribute("userType") String currentUserType
    ) {
        if (!"admin".equals(currentUserType)) {
            return Result.error("无权限访问用户列表");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userService.listUsers(condition);
        return Result.success(new PageInfo<>(list));
    }

    /**
     * 删除用户
     * 
     * @param userId 用户ID
     * @param currentUserType 当前用户类型
     * @return 删除结果
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId,
                                   @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType)) {
            return Result.error("无权限删除用户");
        }
        boolean success = userService.deleteUser(userId);
        return success ? Result.success(null) : Result.error("删除失败");
    }

    /**
     * 保存或更新用户
     * 
     * @param params 用户参数
     * @param userType 用户类型
     * @return 操作结果
     */
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public Result<Void> saveUser(@RequestBody Map<String, Object> params,
                                 @RequestAttribute("userType") String userType) {
        userService.saveOrUpdateUser(params, userType);
        return Result.success(null);
    }

    /**
     * 修改密码
     * 
     * @param params 密码参数，包含用户ID和新密码
     * @param currentUserId 当前用户ID
     * @param currentUserType 当前用户类型
     * @return 修改结果
     */
    @PostMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, Object> params,
                                       @RequestAttribute("userId") Long currentUserId,
                                       @RequestAttribute("userType") String currentUserType) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String newPwd = (String) params.get("newPwd");
        userService.updatePassword(userId, newPwd, currentUserType, currentUserId);
        return Result.success(null);
    }

    /**
     * 导入用户
     * 
     * @param file 导入文件
     * @param currentUserType 当前用户类型
     * @return 导入结果
     */
    @PostMapping("/import")
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

    /**
     * 下载用户导入模板
     * 
     * @param response HTTP响应对象
     * @throws IOException IO异常
     */
    @GetMapping("/import/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("用户导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), UserImportDTO.class)
                .sheet("导入模板")
                .doWrite(new ArrayList<>());
    }
}