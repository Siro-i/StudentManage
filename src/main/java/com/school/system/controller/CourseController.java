package com.school.system.controller;

import com.school.system.common.Result;
import com.school.system.common.ServiceException;
import com.school.system.entity.Course;
import com.school.system.mapper.CourseMapper;
import com.school.system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseMapper courseMapper;

    @GetMapping
    /**
     * 查询课程列表，支持按课程名、教师、状态等条件筛选。
     *
     * @param condition 查询条件（自动绑定请求参数）
     * @return 课程列表
     */
    public Result<List<Course>> listCourses(Course condition) {
        List<Course> list = courseService.listCourses(condition);
        return Result.success(list);
    }

    @PostMapping
    /**
     * 添加课程。管理员或教师可调用，教师仅能为自己创建课程,目前仅实现教师调用。
     *
     * @param course          课程信息
     * @param currentUserType 当前用户角色
     * @param currentUserId   当前用户 ID
     * @return 操作结果
     */
    public Result<Void> addCourse(@RequestBody Course course,
                                  @RequestAttribute("userType") String currentUserType,
                                  @RequestAttribute("userId") Long currentUserId) {
        if (!"admin".equals(currentUserType) && !"teacher".equals(currentUserType)) {
            return Result.error("无权限添加课程");
        }
        if ("teacher".equals(currentUserType)) {
            course.setTeacherId(currentUserId);
        }
        checkRoomConflict(course);
        if (course.getSelectedNum() == null) course.setSelectedNum(0);
        course.setCourseStatus(1);
        course.setCourseCreatetime(new Date());
        courseMapper.insert(course);
        return Result.success(null);
    }


    @PostMapping("/{courseId}/select")
    /**
     * 学生选课，使用当前登录学生身份绑定。
     *
     * @param courseId        课程 ID
     * @param currentUserId   当前用户 ID
     * @param currentUserType 当前用户角色
     * @return 操作结果
     */
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
    /**
     * 学生退课，绑定当前登录学生。
     *
     * @param courseId        课程 ID
     * @param currentUserId   当前用户 ID
     * @param currentUserType 当前用户角色
     * @return 操作结果
     */
    public Result<Void> dropCourse(@PathVariable Long courseId,
                                   @RequestAttribute("userId") Long currentUserId,
                                   @RequestAttribute("userType") String currentUserType) {
        if (!"student".equals(currentUserType)) {
            return Result.error("仅学生可退课");
        }
        courseService.dropCourse(currentUserId, courseId);
        return Result.success(null);
    }

    @DeleteMapping("/{courseId}")
    /**
     * 删除课程并级联删除选课记录。教师仅能删除自己的课程。
     *
     * @param courseId        课程 ID
     * @param currentUserId   当前用户 ID
     * @param currentUserType 当前用户角色
     * @return 操作结果
     */
    public Result<Void> deleteCourse(@PathVariable Long courseId,
                                     @RequestAttribute("userId") Long currentUserId,
                                     @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType) && !"teacher".equals(currentUserType)) {
            return Result.error("无权限删除课程");
        }
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            return Result.success(null);
        }
        if ("teacher".equals(currentUserType) && course.getTeacherId() != null && !course.getTeacherId().equals(currentUserId)) {
            return Result.error("无权限删除其他教师的课程");
        }
        courseService.deleteCourse(courseId);
        return Result.success(null);
    }

    @PutMapping
    /**
     * 修改课程信息，教师仅能修改自己的课程。
     *
     * @param course          待更新课程信息
     * @param currentUserId   当前用户 ID
     * @param currentUserType 当前用户角色
     * @return 操作结果
     */
    public Result<Void> updateCourse(@RequestBody Course course,
                                     @RequestAttribute("userId") Long currentUserId,
                                     @RequestAttribute("userType") String currentUserType) {
        if (!"admin".equals(currentUserType) && !"teacher".equals(currentUserType)) {
            return Result.error("无权限修改课程");
        }
        Course dbCourse = courseMapper.selectById(course.getCourseId());
        if (dbCourse != null && "teacher".equals(currentUserType) && dbCourse.getTeacherId() != null
                && !dbCourse.getTeacherId().equals(currentUserId)) {
            return Result.error("无权限修改其他教师的课程");
        }
        checkRoomConflict(course);
        courseMapper.update(course);
        return Result.success(null);
    }

    /**
     * 检查教室占用冲突
     *
     * @param course 待排课课程信息
     * @throws ServiceException 若发现教室冲突，抛出异常
     *
     */
    private void checkRoomConflict(Course course) {
        if (course.getCourseTime() == null || course.getCourseTime().isEmpty()) return;
        if (course.getCourseRoom() == null || course.getCourseRoom().isEmpty()) return;
        List<Course> conflicts = courseMapper.selectByTimeAndRoom(course.getCourseTime(), course.getCourseRoom());

        for (Course existing : conflicts) {
            // 冲突判定规则：
            // 1. 如果是【新增课程】(courseId为null)：只要查到有课，就是冲突。
            // 2. 如果是【修改课程】(courseId不为null)：查到的课不是我自己(ID不同)，才是冲突。
            if (course.getCourseId() == null || !existing.getCourseId().equals(course.getCourseId())) {
                throw new ServiceException(
                        String.format("排课冲突！[%s] 的 [%s] 已被课程《%s》占用",
                                course.getCourseTime(), course.getCourseRoom(), existing.getCourseName())
                );
            }
        }
    }




}