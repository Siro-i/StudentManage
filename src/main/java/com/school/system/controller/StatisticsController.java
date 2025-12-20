package com.school.system.controller;

import com.school.system.common.Result;
import com.school.system.mapper.CourseMapper;
import com.school.system.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CourseMapper courseMapper;

    /**
     * 获取首页统计数据：用户分布和热门课程，仅管理员可用。
     *
     * @param currentUserType 当前用户角色
     * @return 统计数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats(@RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType)) {
            return Result.error("无权限查看统计数据");
        }
        Map<String, Object> data = new HashMap<>();

        // 1. 统计各角色人数
        List<com.school.system.entity.User> allUsers = userMapper.selectList(null);

        long studentCount = allUsers.stream().filter(u -> "student".equals(u.getUserType())).count();
        long teacherCount = allUsers.stream().filter(u -> "teacher".equals(u.getUserType())).count();
        long adminCount = allUsers.stream().filter(u -> "admin".equals(u.getUserType())).count();

        data.put("userStats", Map.of("student", studentCount, "teacher", teacherCount, "admin", adminCount));

        // 2. 统计热门课程 (选课人数最多的前5名)
        List<com.school.system.entity.Course> allCourses = courseMapper.selectList(null);
        List<Map<String, Object>> topCourses = allCourses.stream()
                .sorted((c1, c2) -> c2.getSelectedNum() - c1.getSelectedNum()) // 降序
                .limit(5)
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", c.getCourseName());
                    m.put("count", c.getSelectedNum());
                    return m;
                })
                .toList();

        data.put("courseStats", topCourses);

        return Result.success(data);
    }
}