package com.school.system.service;

import com.school.system.common.ServiceException;
import com.school.system.entity.Course;
import com.school.system.entity.Student;
import com.school.system.entity.StudentCourse;
import com.school.system.mapper.CourseMapper;
import com.school.system.mapper.StudentCourseMapper;
import com.school.system.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    @Autowired
    private StudentMapper studentMapper;


    /**
     * 查询课程列表
     */
    public List<Course> listCourses(Course condition) {
        return courseMapper.selectList(condition);
    }

    /**
     * 添加课程
     */
    @Transactional(rollbackFor = Exception.class)
    public void addCourse(Course course, String userType, Long userId) {
        // 1. 权限与归属校验
        if (!"admin".equals(userType) && !"teacher".equals(userType)) {
            throw new ServiceException("无权限添加课程");
        }
        if ("teacher".equals(userType)) {
            course.setTeacherId(userId);
        }

        // 2. 教室冲突检测
        checkRoomConflict(course);

        // 3. 数据补全
        if (course.getSelectedNum() == null) course.setSelectedNum(0);
        course.setCourseStatus(1);
        course.setCourseCreatetime(new Date());

        courseMapper.insert(course);
    }

    /**
     * 修改课程
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(Course course, String userType, Long userId) {
        if (!"admin".equals(userType) && !"teacher".equals(userType)) {
            throw new ServiceException("无权限修改课程");
        }

        Course dbCourse = courseMapper.selectById(course.getCourseId());
        if (dbCourse == null) {
            throw new ServiceException("课程不存在");
        }

        // 教师仅能修改自己的课程
        if ("teacher".equals(userType) && dbCourse.getTeacherId() != null
                && !dbCourse.getTeacherId().equals(userId)) {
            throw new ServiceException("无权限修改其他教师的课程");
        }

        checkRoomConflict(course);
        courseMapper.update(course);
    }

    /**
     * 删除课程（级联删除选课记录）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long courseId, String userType, Long userId) {
        if (!"admin".equals(userType) && !"teacher".equals(userType)) {
            throw new ServiceException("无权限删除课程");
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) return;

        if ("teacher".equals(userType) && course.getTeacherId() != null && !course.getTeacherId().equals(userId)) {
            throw new ServiceException("无权限删除其他教师的课程");
        }

        // 1. 先删除该课程所有的选课/成绩记录
        studentCourseMapper.deleteByCourseId(courseId);

        // 2. 再删除课程本身
        courseMapper.deleteById(courseId);
    }

    /**
     * 学生选课
     */
    @Transactional(rollbackFor = Exception.class)
    public void selectCourse(Long userId, Long courseId) {
        // 转换 ID
        Student student = studentMapper.selectByUserId(userId);
        if (student == null) {
            throw new ServiceException("未找到学生档案，无法选课");
        }
        Long trueStudentId = student.getStudentId();

        // 重复校验
        StudentCourse existing = studentCourseMapper.findByStudentAndCourse(trueStudentId, courseId);
        if (existing != null) {
            throw new ServiceException("请勿重复选课");
        }

        // 名额与状态校验
        Course course = courseMapper.selectById(courseId);
        if (course == null || (course.getCourseStatus() != null && course.getCourseStatus() == 0)) {
            throw new ServiceException("课程已停止选课");
        }
        if (course.getSelectedNum() >= course.getMaxNum()) {
            throw new ServiceException("该课程名额已满");
        }

        // 时间冲突校验
        if (course.getCourseTime() != null && !course.getCourseTime().isEmpty()) {
            List<Course> myCourses = courseMapper.selectByStudentId(trueStudentId);
            for (Course existingCourse : myCourses) {
                if (course.getCourseTime().equals(existingCourse.getCourseTime())) {
                    throw new ServiceException("选课冲突，该时间段已有课程: " + existingCourse.getCourseName());
                }
            }
        }

        // 写入
        StudentCourse sc = new StudentCourse();
        sc.setStudentId(trueStudentId);
        sc.setCourseId(courseId);
        sc.setScSelecttime(new Date());
        studentCourseMapper.insert(sc);

        // 更新人数
        courseMapper.incrementSelectedNum(courseId);
    }

    /**
     * 学生退课
     */
    @Transactional(rollbackFor = Exception.class)
    public void dropCourse(Long userId, Long courseId) {
        Student student = studentMapper.selectByUserId(userId);
        if (student == null) throw new ServiceException("学生信息异常");

        Long trueStudentId = student.getStudentId();

        int rows = studentCourseMapper.deleteByStudentAndCourse(trueStudentId, courseId);
        if (rows > 0) {
            courseMapper.decrementSelectedNum(courseId);
        } else {
            throw new ServiceException("未找到选课记录");
        }
    }

    /**
     * 内部方法：检查教室占用冲突
     */
    private void checkRoomConflict(Course course) {
        if (course.getCourseTime() == null || course.getCourseTime().isEmpty()) return;
        if (course.getCourseRoom() == null || course.getCourseRoom().isEmpty()) return;

        List<Course> conflicts = courseMapper.selectByTimeAndRoom(course.getCourseTime(), course.getCourseRoom());

        for (Course existing : conflicts) {
            // 冲突判定：(新增且有记录) 或 (修改且记录ID不是当前ID)
            if (course.getCourseId() == null || !existing.getCourseId().equals(course.getCourseId())) {
                throw new ServiceException(
                        String.format("排课冲突！[%s] 的 [%s] 已被课程《%s》占用",
                                course.getCourseTime(), course.getCourseRoom(), existing.getCourseName())
                );
            }
        }
    }
}