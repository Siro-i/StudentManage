package com.school.system.controller;

import com.alibaba.excel.EasyExcel;
import com.school.system.common.Result;
import com.school.system.entity.ScoreExportVO;
import com.school.system.mapper.StudentCourseMapper;
import com.school.system.service.ScoreService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;
    @Autowired
    private StudentCourseMapper studentCourseMapper;



    @Data
    public static class ScoreEntryRequest {
        private Long teacherId;
        private Long studentId;
        private Long courseId;
        private Integer score;

    }
    /**
     * 录入/修改成绩
     * 对应 ScoreOperable.setScore()
     */
    @PostMapping("/entry")
    public Result<Void> setScore(@RequestBody ScoreEntryRequest request) {
        try {
            scoreService.setScore(
                    request.getTeacherId(),
                    request.getStudentId(),
                    request.getCourseId(),
                    request.getScore()
            );
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 统计课程成绩
     * 对应 ScoreOperable.statisticCourseScore()
     */
    @GetMapping("/statistics/{courseId}")
    public Result<Object> getCourseStats(@PathVariable Long courseId) {
        Object stats = scoreService.statisticCourseScore(courseId);
        return Result.success(stats);
    }
    /**
     * 学生查询自己的成绩单
     * GET /api/scores/student/{studentId}
     * 对应文档 2.3.3 成绩查询(学生)
     */
    @GetMapping("/student/{studentId}")
    public Result<List<Map<String, Object>>> listMyScores(@PathVariable Long studentId) {
        List<Map<String, Object>> list = scoreService.listMyScores(studentId);
        return Result.success(list);
    }
    /**
     * 教师查询课程的所有学生成绩
     * GET /api/scores/course/{courseId}
     * 对应文档 2.3.4 成绩查询(教师)
     */
    @GetMapping("/course/{courseId}")
    public Result<List<Map<String, Object>>> listStudentScores(@PathVariable Long courseId) {
        List<Map<String, Object>> list = scoreService.listStudentScores(courseId);
        return Result.success(list);
    }

    /**
     * 导出成绩单 Excel
     * GET /api/scores/export/{courseId}
     */
    @GetMapping("/export/{courseId}")
    public void exportScores(@PathVariable Long courseId, HttpServletResponse response) throws Exception {
        // 1. 查询数据
        List<Map<String, Object>> rawList = studentCourseMapper.selectStudentScoreList(courseId);
        if (!rawList.isEmpty()) {
            System.out.println("DEBUG - 查到的第一条数据: " + rawList.get(0));
        } else {
            System.out.println("DEBUG - 该课程没有学生选课数据");
        }

        // 2. 转换模型
        List<ScoreExportVO> exportList = new ArrayList<>();
        for (Map<String, Object> map : rawList) {
            ScoreExportVO vo = new ScoreExportVO();
            Object nameObj = map.get("studentRealName");
            vo.setStudentName(nameObj != null ? nameObj.toString() : "未知姓名");
            Object numObj = map.get("studentNumber");
            vo.setStudentNumber(numObj != null ? numObj.toString() : "");
            Object classObj = map.get("studentClass");
            vo.setStudentClass(classObj != null ? classObj.toString() : "");
            Object courseObj = map.get("courseName");
            vo.setCourseName(courseObj != null ? courseObj.toString() : "未命名课程");
            Object scoreObj = map.get("scScore");
            if (scoreObj != null) {
                try {
                    vo.setScore(Double.valueOf(scoreObj.toString()).intValue());
                } catch (Exception e) {
                    vo.setScore(0); // 转换失败给默认值
                }
            }
            exportList.add(vo);
        }
        // 3. 重置响应并写出
        response.reset();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("课程成绩单", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        try {
            EasyExcel.write(response.getOutputStream(), ScoreExportVO.class)
                    .sheet("成绩表")
                    .doWrite(exportList);
        } catch (Exception e) {
            e.printStackTrace(); // 在控制台打印报错
        }
    }


    // 获取某门课的学生名单
    @GetMapping("/course/{courseId}/students")
    public Result<List<Map<String, Object>>> getCourseStudents(@PathVariable Long courseId) {
        // 调用刚才写的带 JOIN 的查询
        List<Map<String, Object>> list = studentCourseMapper.selectStudentScoreList(courseId);
        return Result.success(list);
    }
}