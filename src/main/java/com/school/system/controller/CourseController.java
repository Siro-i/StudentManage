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

    @GetMapping
    public Result<List<Course>> listCourses(Course condition) {
        return Result.success(courseService.listCourses(condition));
    }

    @PostMapping
    public Result<Void> addCourse(@RequestBody Course course,
                                  @RequestAttribute("userType") String userType,
                                  @RequestAttribute("userId") Long userId) {
        courseService.addCourse(course, userType, userId);
        return Result.success(null);
    }

    @PutMapping
    public Result<Void> updateCourse(@RequestBody Course course,
                                     @RequestAttribute("userId") Long userId,
                                     @RequestAttribute("userType") String userType) {
        courseService.updateCourse(course, userType, userId);
        return Result.success(null);
    }

    @DeleteMapping("/{courseId}")
    public Result<Void> deleteCourse(@PathVariable Long courseId,
                                     @RequestAttribute("userId") Long userId,
                                     @RequestAttribute("userType") String userType) {
        courseService.deleteCourse(courseId, userType, userId);
        return Result.success(null);
    }

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