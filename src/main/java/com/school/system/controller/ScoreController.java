package com.school.system.controller;

import com.alibaba.excel.EasyExcel;
import com.school.system.common.Result;
import com.school.system.entity.ScoreExportVO;
import com.school.system.service.ScoreService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @Data
    public static class ScoreEntryRequest {
        private Long studentId;
        private Long courseId;
        private Integer score;
    }

    @PostMapping("/entry")
    public Result<Void> setScore(@RequestBody ScoreEntryRequest request,
                                 @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType) && !"teacher".equals(currentUserType)) {
            return Result.error("无权限录入成绩");
        }
        scoreService.setScore(request.getStudentId(), request.getCourseId(), request.getScore());
        return Result.success(null);
    }

    @GetMapping("/statistics/{courseId}")
    public Result<Object> getCourseStats(@PathVariable Long courseId,
                                         @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType) && !"teacher".equals(currentUserType)) {
            return Result.error("无权限查看课程统计");
        }
        return Result.success(scoreService.statisticCourseScore(courseId));
    }

    @GetMapping("/student/me")
    public Result<List<Map<String, Object>>> listMyScores(@RequestAttribute("userId") Long currentUserId,
                                                          @RequestAttribute("userType") String currentUserType) {
        if (!"student".equals(currentUserType)) {
            return Result.error("仅学生可查看个人成绩");
        }
        return Result.success(scoreService.listMyScores(currentUserId));
    }

    @GetMapping("/course/{courseId}")
    public Result<List<Map<String, Object>>> listStudentScores(@PathVariable Long courseId,
                                                               @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType) && !"teacher".equals(currentUserType)) {
            return Result.error("无权限查看课程成绩");
        }
        return Result.success(scoreService.listStudentScores(courseId));
    }

    @GetMapping("/export/{courseId}")
    public void exportScores(@PathVariable Long courseId,
                             HttpServletResponse response,
                             @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType) && !"teacher".equals(currentUserType)) {
            response.setStatus(403);
            return;
        }

        List<ScoreExportVO> exportList = scoreService.getExportData(courseId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        try {
            String fileName = URLEncoder.encode("课程成绩单", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), ScoreExportVO.class)
                    .sheet("成绩表")
                    .doWrite(exportList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}