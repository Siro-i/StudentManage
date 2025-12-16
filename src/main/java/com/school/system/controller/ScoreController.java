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
        private Long studentId;
        private Long courseId;
        private Integer score;

    }

    @PostMapping("/entry")
    /**
     * 录入或修改课程成绩，仅教师/管理员可用。
     *
     * @param request         成绩录入请求体
     * @param currentUserId   当前用户 ID
     * @param currentUserType 当前用户角色
     * @return 操作结果
     */
    public Result<Void> setScore(@RequestBody ScoreEntryRequest request,
                                 @RequestAttribute("userId") Long currentUserId,
                                 @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType) && !"teacher".equals(currentUserType)) {
            return Result.error("无权限录入成绩");
        }
        try {
            scoreService.setScore(
                    request.getStudentId(),
                    request.getCourseId(),
                    request.getScore()
            );
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }


    @GetMapping("/statistics/{courseId}")
    /**
     * 获取课程成绩统计，仅教师/管理员可用。
     *
     * @param courseId        课程 ID
     * @param currentUserType 当前用户角色
     * @return 统计数据
     */
    public Result<Object> getCourseStats(@PathVariable Long courseId,
                                         @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType) && !"teacher".equals(currentUserType)) {
            return Result.error("无权限查看课程统计");
        }
        Object stats = scoreService.statisticCourseScore(courseId);
        return Result.success(stats);
    }

    @GetMapping("/student/me")
    /**
     * 学生查看个人成绩单，绑定当前登录学生。
     *
     * @param currentUserId   当前用户 ID
     * @param currentUserType 当前用户角色
     * @return 成绩列表
     */
    public Result<List<Map<String, Object>>> listMyScores(@RequestAttribute("userId") Long currentUserId,
                                                          @RequestAttribute("userType") String currentUserType) {
        if (!"student".equals(currentUserType)) {
            return Result.error("仅学生可查看个人成绩");
        }
        List<Map<String, Object>> list = scoreService.listMyScores(currentUserId);
        return Result.success(list);
    }

    @GetMapping("/course/{courseId}")
    /**
     * 教师/管理员查看课程成绩列表。
     *
     * @param courseId        课程 ID
     * @param currentUserType 当前用户角色
     * @return 成绩列表
     */
    public Result<List<Map<String, Object>>> listStudentScores(@PathVariable Long courseId,
                                                               @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType) && !"teacher".equals(currentUserType)) {
            return Result.error("无权限查看课程成绩");
        }
        List<Map<String, Object>> list = scoreService.listStudentScores(courseId);
        return Result.success(list);
    }


    @GetMapping("/export/{courseId}")
    /**
     * 导出课程成绩为 Excel，仅教师/管理员可用。
     *
     * @param courseId        课程 ID
     * @param response        响应对象
     * @param currentUserType 当前用户角色
     * @throws Exception 写出文件异常
     */
    public void exportScores(@PathVariable Long courseId,
                             HttpServletResponse response,
                             @RequestAttribute("userType") String currentUserType) throws Exception {
        if (!"admin".equals(currentUserType) && !"teacher".equals(currentUserType)) {
            response.setStatus(403);
            return;
        }
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
            e.printStackTrace(); 
        }
    }

}