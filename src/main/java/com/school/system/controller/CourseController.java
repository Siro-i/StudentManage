package com.school.system.controller;

import com.school.system.common.Result;
import com.school.system.entity.Course;
import com.school.system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 查询课程列表
     * 
     * @param condition 查询条件
     * @return 课程列表结果
     */
    @GetMapping
    public Result<List<Course>> listCourses(Course condition,
                                            @RequestAttribute("userType") String userType,
                                            @RequestAttribute("userId") Long userId) {

        if ("student".equals(userType)) {
            condition.setCourseStatus(1);
        }

        else if ("teacher".equals(userType)) {
            condition.setTeacherId(userId);
        }
        return Result.success(courseService.listCourses(condition));
    }

    /**
     * 添加课程
     * 
     * @param course 课程信息
     * @param userType 用户类型
     * @param userId 用户ID
     * @return 添加结果
     */
    @PostMapping
    public Result<Void> addCourse(@RequestBody Course course,
                                  @RequestAttribute("userType") String userType,
                                  @RequestAttribute("userId") Long userId) {
        courseService.addCourse(course, userType, userId);
        return Result.success(null);
    }

    /**
     * 更新课程
     * 
     * @param course 课程信息
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 更新结果
     */
    @PutMapping
    public Result<Void> updateCourse(@RequestBody Course course,
                                     @RequestAttribute("userId") Long userId,
                                     @RequestAttribute("userType") String userType) {
        courseService.updateCourse(course, userType, userId);
        return Result.success(null);
    }

    /**
     * 删除课程
     * 
     * @param courseId 课程ID
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 删除结果
     */
    @DeleteMapping("/{courseId}")
    public Result<Void> deleteCourse(@PathVariable Long courseId,
                                     @RequestAttribute("userId") Long userId,
                                     @RequestAttribute("userType") String userType) {
        courseService.deleteCourse(courseId, userType, userId);
        return Result.success(null);
    }

    /**
     * 学生选课
     * 
     * @param courseId 课程ID
     * @param currentUserId 当前用户ID
     * @param currentUserType 当前用户类型
     * @return 选课结果
     */
    @PostMapping("/{courseId}/select")
    public Result<Void> selectCourse(@PathVariable Long courseId,
                                     @RequestAttribute("userId") Long currentUserId,
                                     @RequestAttribute("userType") String currentUserType) {
        if (!"student".equals(currentUserType)) {
            return Result.error("仅学生可选课");
        }
        courseService.selectCourse(currentUserId, courseId);
        return Result.success(null);
    }

    /**
     * 学生退课
     * 
     * @param courseId 课程ID
     * @param currentUserId 当前用户ID
     * @param currentUserType 当前用户类型
     * @return 退课结果
     */
    @PostMapping("/{courseId}/drop")
    public Result<Void> dropCourse(@PathVariable Long courseId,
                                   @RequestAttribute("userId") Long currentUserId,
                                   @RequestAttribute("userType") String currentUserType) {
        if (!"student".equals(currentUserType)) {
            return Result.error("仅学生可退课");
        }
        courseService.dropCourse(currentUserId, courseId);
        return Result.success(null);
    }
}