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

    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId,
                                   @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType)) {
            return Result.error("无权限删除用户");
        }
        boolean success = userService.deleteUser(userId);
        return success ? Result.success(null) : Result.error("删除失败");
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public Result<Void> saveUser(@RequestBody Map<String, Object> params,
                                 @RequestAttribute("userType") String userType) {
        userService.saveOrUpdateUser(params, userType);
        return Result.success(null);
    }

    @PostMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, Object> params,
                                       @RequestAttribute("userId") Long currentUserId,
                                       @RequestAttribute("userType") String currentUserType) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String newPwd = (String) params.get("newPwd");
        userService.updatePassword(userId, newPwd, currentUserType, currentUserId);
        return Result.success(null);
    }

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