package com.school.system.controller;

import com.school.system.common.ServiceException;
import com.school.system.entity.Course;
import com.school.system.common.Result;
import com.school.system.entity.StudentCourse;
import com.school.system.mapper.CourseMapper;
import com.school.system.mapper.StudentCourseMapper;
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
    @Autowired
    private StudentCourseMapper studentCourseMapper;



    /**
     * 查询课程列表
     * 对应 CourseOperable.listCourses()
     * 支持传参筛选，例如 ?teacherId=1 或 ?name=Java
     */
    @GetMapping
    public Result<List<Course>> listCourses(Course condition) {
        // SpringMVC 会自动将请求参数映射到 Course 对象中
        List<Course> list = courseService.listCourses(condition);
        return Result.success(list);
    }

    /**
     * 添加课程 (教师/管理员)
     * 对应 CourseOperable.addCourse()
     */
    @PostMapping
    public Result<Void> addCourse(@RequestBody Course course) {
        checkRoomConflict(course);
        if (course.getSelectedNum() == null) course.setSelectedNum(0); // 防止空指针
        course.setCourseStatus(1);
        course.setCourseCreatetime(new Date());
        courseMapper.insert(course);
        return Result.success(null);
    }

    // --- 选课相关 (对应 Selectable 接口) ---

    /**
     * 学生选课
     * 对应 Selectable.selectCourse()
     */
    @PostMapping("/{courseId}/select")
    public Result<Void> selectCourse(@PathVariable Long courseId, @RequestParam Long studentId) {
        // 1. 检查是否重复选课 (原有逻辑)
        int count = studentCourseMapper.countByStudentIdAndCourseId(studentId, courseId);
        if (count > 0) throw new ServiceException("你已经选过这门课了");

        // 2. 检查课程状态和人数 (原有逻辑)
        Course course = courseMapper.selectById(courseId);
        if (course.getCourseStatus() == 0) throw new ServiceException("课程已停止选课");
        if (course.getSelectedNum() >= course.getMaxNum()) throw new ServiceException("课程人数已满");

        // === 3. 新增：时间冲突检测 ===
        // 3.1 获取学生当前已选的所有课程
        List<Course> myCourses = courseMapper.selectByStudentId(studentId); // 需要在 Mapper 确认有这个方法

        // 3.2 遍历检查
        if (course.getCourseTime() != null && !course.getCourseTime().isEmpty()) {
            for (Course existing : myCourses) {
                // 如果时间字符串完全相等，就认为冲突
                if (course.getCourseTime().equals(existing.getCourseTime())) {
                    throw new ServiceException("选课冲突！该时间段你已有课程: " + existing.getCourseName());
                }
            }
        }
        // =========================

        // 4. 执行选课 (插入记录 + 课程人数+1)
        StudentCourse sc = new StudentCourse();
        sc.setStudentId(studentId);
        sc.setCourseId(courseId);
        sc.setSelectTime(new Date());
        studentCourseMapper.insert(sc);

        course.setSelectedNum(course.getSelectedNum() + 1);
        courseMapper.update(course);

        return Result.success(null);
    }

    /**
     * 学生退课
     * POST /api/courses/{courseId}/drop
     */
    @PostMapping("/{courseId}/drop")
    public Result<Void> dropCourse(@PathVariable Long courseId, @RequestParam Long studentId) {
        try {
            courseService.dropCourse(studentId, courseId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除课程 (级联删除选课记录)
     * 对应 CourseOperable.deleteCourse()
     */
    @DeleteMapping("/{courseId}")
    public Result<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return Result.success(null);
    }

    /**
     * 修改课程
     * PUT /api/courses
     */
    @PutMapping
    public Result<Void> updateCourse(@RequestBody Course course) {
        checkRoomConflict(course);
        courseMapper.update(course);
        return Result.success(null);
    }

    /**
     * 核心逻辑：检查教室占用情况
     */
    private void checkRoomConflict(Course course) {
        if (course.getCourseTime() == null || course.getCourseTime().isEmpty()) return;
        if (course.getCourseRoom() == null || course.getCourseRoom().isEmpty()) return;

        // 查询数据库里有没有撞车的课
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