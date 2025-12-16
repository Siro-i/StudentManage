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
     * 支持按条件查询（如查询某个老师的课，或所有可选课）
     *
     * @param condition 查询条件
     * @return 课程列表
     */
    public List<Course> listCourses(Course condition) {
        return courseMapper.selectList(condition);
    }



    /**
     * 学生选课
     * 逻辑：1.校验是否重复选课 2.校验名额是否已满 3.执行选课
     *
     * @param userId   当前用户 ID（学生）
     * @param courseId 课程 ID
     */
    @Transactional
    public void selectCourse(Long userId, Long courseId) {
        // 2.通过 userId 查出真正的 studentId
        Student student = studentMapper.selectByUserId(userId);
        if (student == null) {
            throw new ServiceException("未找到学生档案，无法选课");
        }
        Long trueStudentId = student.getStudentId();

        // 3. 使用 trueStudentId 进行后续操作
        StudentCourse existing = studentCourseMapper.findByStudentAndCourse(trueStudentId, courseId);
        if (existing != null) {
            throw new ServiceException("请勿重复选课");
        }

        // 校验课程名额
        com.school.system.entity.Course course = courseMapper.selectById(courseId);
        if (course.getCourseStatus() != null && course.getCourseStatus() == 0) {
            throw new ServiceException("课程已停止选课");
        }
        if (course.getSelectedNum() >= course.getMaxNum()) {
            throw new ServiceException("该课程名额已满");
        }

        // 时间冲突检测
        if (course.getCourseTime() != null && !course.getCourseTime().isEmpty()) {
            List<Course> myCourses = courseMapper.selectByStudentId(trueStudentId);
            for (Course existingCourse : myCourses) {
                if (course.getCourseTime().equals(existingCourse.getCourseTime())) {
                    throw new ServiceException("选课冲突，该时间段已有课程: " + existingCourse.getCourseName());
                }
            }
        }

        // 写入选课记录
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
     * 
     * @param userId 当前用户ID（学生）
     * @param courseId 课程ID
     */
    @Transactional
    public void dropCourse(Long userId, Long courseId) {
        // 同样要转换 ID
        Student student = studentMapper.selectByUserId(userId);
        if (student == null) throw new RuntimeException("学生信息异常");

        Long trueStudentId = student.getStudentId();

        int rows = studentCourseMapper.deleteByStudentAndCourse(trueStudentId, courseId);
        if (rows > 0) {
            courseMapper.decrementSelectedNum(courseId);
        } else {
            throw new RuntimeException("未找到选课记录");
        }
    }

    /**
     * 删除课程（级联删除选课记录）
     * 
     * @param courseId 课程ID
     */
    @Transactional
    public void deleteCourse(Long courseId) {
        // 1. 先删除该课程所有的选课/成绩记录
        studentCourseMapper.deleteByCourseId(courseId);

        // 2. 再删除课程本身
        courseMapper.deleteById(courseId);
    }


}