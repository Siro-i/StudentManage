package com.school.system.service;

import com.school.system.common.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BackupService {

    // 读取配置文件中的数据库信息
    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPass;

    // 数据库连接URL
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${project.mysql.dump-path}")
    private String dumpPath;
    @Value("${project.mysql.client-path}")
    private String clientPath;


    // 备份文件存储路径 (项目根目录下的 backup 文件夹)
    private final String BACKUP_DIR = System.getProperty("user.dir") + File.separator + "backup";

    /**
     * 执行数据库备份
     *
     *
     */
    public void backup() {
        String dbName = getDbNameFromUrl(dbUrl);
        File file = new File(BACKUP_DIR);
        if (!file.exists()) file.mkdirs();

        String fileName = dbName + "_" + new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date()) + ".sql";
        File saveFile = new File(file, fileName);

        try {



            List<String> cmd = new ArrayList<>();
            cmd.add(dumpPath);
            cmd.add("-u" + dbUser);
            cmd.add("-p" + dbPass);
            cmd.add("--column-statistics=0");
            cmd.add("--hex-blob");
            cmd.add("--set-gtid-purged=OFF");
            cmd.add(dbName);

            ProcessBuilder processBuilder = new ProcessBuilder(cmd);

            Process process = processBuilder.start();
            new Thread(() -> {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getErrorStream(), "GBK"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                    }
                } catch (Exception e) {
                }
            }).start();

            // 主线程只负责读取“标准输出流”（纯净的 SQL 数据）并写入文件
            try (java.io.InputStream in = process.getInputStream();
                 java.io.FileOutputStream out = new java.io.FileOutputStream(saveFile)) {
                byte[] buffer = new byte[1024 * 4];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                if (saveFile.exists()) saveFile.delete();
                throw new ServiceException("备份失败，错误码: " + exitCode);
            }
            System.out.println(">>> 备份成功: " + fileName);

        } catch (Exception e) {
            e.printStackTrace();
            if (saveFile.exists()) saveFile.delete();
            throw new ServiceException("备份异常: " + e.getMessage());
        }
    }

    /**
     * 每天凌晨 2 点自动备份
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledBackup() {
        System.out.println(">>> 开始执行定时备份任务...");
        backup();
        System.out.println(">>> 定时备份完成");
    }

    /**
     * 获取备份文件列表（按时间倒序）
     * @return 备份文件列表
     */
    public List<Map<String, Object>> listBackups() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) return new ArrayList<>();

        File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));
        if (files == null) return new ArrayList<>();

        return Arrays.stream(files)
                .sorted((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()))
                .map(f -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("fileName", f.getName());
                    map.put("size", String.format("%.2f KB", f.length() / 1024.0));
                    map.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(f.lastModified())));
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 删除备份
     * @param fileName 备份文件名
     *
     */
    public void deleteBackup(String fileName) {
        File file = new File(BACKUP_DIR, fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 获取文件对象（用于下载）
     * @param fileName 备份文件名
     * @return 文件对象
     */
    public File getBackupFile(String fileName) {
        return new File(BACKUP_DIR, fileName);
    }

    // 辅助工具：从 JDBC URL 中提取数据库名
    private String getDbNameFromUrl(String url) {
        String clean = url.substring(url.indexOf("://") + 3);
        clean = clean.substring(clean.indexOf("/") + 1);
        if (clean.contains("?")) {
            clean = clean.substring(0, clean.indexOf("?"));
        }
        return clean;
    }

    /**
     * 数据库还原
     * @param fileName 备份文件名
     *
     */
    public void restore(String fileName) {
        File file = new File(BACKUP_DIR, fileName);
        if (!file.exists()) {
            throw new ServiceException("备份文件不存在");
        }

        String dbName = getDbNameFromUrl(dbUrl);

        try {


            List<String> cmd = new ArrayList<>();
            cmd.add(clientPath);
            cmd.add("-u" + dbUser);
            cmd.add("-p" + dbPass);
            cmd.add(dbName);

            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.redirectErrorStream(true); // 合并错误流

            Process process = processBuilder.start();

            try (java.io.OutputStream out = process.getOutputStream();
                 java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                byte[] buffer = new byte[1024 * 4];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.flush(); // 确保写完
            }

            // 等待执行完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // 读取报错信息
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream(), "GBK"))) { // Windows GBK
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("RESTORE LOG: " + line);
                    }
                }
                throw new ServiceException("还原失败，错误码: " + exitCode);
            }
            System.out.println(">>> 数据库还原成功: " + fileName);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("还原异常: " + e.getMessage());
        }
    }

    /**
     * 定时清理旧备份
     * 策略：每月1号凌晨3点执行，删除 30 天前的文件
     *
     */
    @Scheduled(cron = "0 0 3 1 * ?") // 每月1号 03:00:00 执行
    public void cleanOldBackups() {
        System.out.println(">>> 开始执行旧备份清理任务...");
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) return;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));
        if (files == null) return;

        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000); // 30天前的毫秒数
        int count = 0;

        for (File f : files) {
            if (f.lastModified() < thirtyDaysAgo) {
                System.out.println("正在删除过期备份: " + f.getName());
                f.delete();
                count++;
            }
        }
        System.out.println(">>> 清理完成，共删除 " + count + " 个过期文件");
    }
}