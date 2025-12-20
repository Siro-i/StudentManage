package com.school.system.controller;

import com.school.system.common.Result;
import com.school.system.service.BackupService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    @Autowired
    private BackupService backupService;
    /**
     * 获取备份列表
     * @param userType 用户类型
     * @return 备份列表
     */
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestAttribute("userType") String userType) {
        if (!"admin".equals(userType)) return Result.error("无权限");
        return Result.success(backupService.listBackups());
    }
    /**
     * 创建数据库备份
     * @param userType 用户类型
     * @return 成功响应
     */
    @PostMapping
    public Result<Void> createBackup(@RequestAttribute("userType") String userType) {
        if (!"admin".equals(userType)) return Result.error("无权限");

        // 检查是否忙碌
        String status = backupService.getTaskStatus("backup");
        if ("RUNNING".equals(status)) {
            return Result.error("后台已有备份任务正在进行，请稍候...");
        }

        // 触发异步任务
        backupService.backupAsync();
        return Result.success(null);
    }

    /**
     * 获取备份任务状态
     * @param type 任务类型（backup）
     * @return 任务状态
     */
    @GetMapping("/status")
    public Result<Map<String, String>> getStatus(@RequestParam String type) {
        Map<String, String> map = new HashMap<>();
        String status = backupService.getTaskStatus(type);
        map.put("status", status);

        // 如果是 Success 或 Error，前端读取一次后，可以重置为 IDLE，防止一直显示成功
        // 这里简化逻辑，只返回状态
        return Result.success(map);
    }
    /**
     * 删除备份文件
     * @param fileName 备份文件名
     * @param userType 用户类型
     * @return 成功响应
     */
    @DeleteMapping("/{fileName}")
    public Result<Void> delete(@PathVariable String fileName, @RequestAttribute("userType") String userType) {
        if (!"admin".equals(userType)) return Result.error("无权限");
        backupService.deleteBackup(fileName);
        return Result.success(null);
    }
    /**
     * 下载备份文件
     * @param fileName 备份文件名
     * @param response HTTP响应
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            File file = backupService.getBackupFile(fileName);
            if (!file.exists()) return;
            response.setContentType("application/octet-stream");
            String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);

            try (FileInputStream in = new FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 还原数据库
     * @param fileName 备份文件名
     * @param userType 用户类型
     * @return 成功响应
     */
    @PostMapping("/restore/{fileName}")
    public Result<Void> restoreBackup(@PathVariable String fileName,
                                      @RequestAttribute("userType") String userType) {
        if (!"admin".equals(userType)) return Result.error("无权限");
        backupService.doRestoreInternal(fileName);
        return Result.success(null);
    }
}