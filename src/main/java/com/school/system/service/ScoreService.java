package com.school.system.service;

import com.school.system.common.ServiceException;
import com.school.system.entity.Student;
import com.school.system.entity.StudentCourse;
import com.school.system.mapper.StudentCourseMapper;
import com.school.system.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ScoreService {

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    @Autowired
    private StudentMapper studentMapper;



    public List<Map<String, Object>> listStudentScores(Long courseId) {
        return studentCourseMapper.selectStudentScoreList(courseId);
    }

    /**
     * 录入/修改成绩 [cite: 82, 110]
     * 逻辑：校验分数范围，更新 student_course_table
     */
    public boolean setScore(Long teacherId, Long studentId, Long courseId, Integer score) {

        // 1. 校验分数范围 [cite: 82]
        if (score == null || score < 0 || score > 100) {
            throw new ServiceException("输入格式错误，请按要求输入(0-100)"); // [cite: 136]
        }

        // 2. 更新成绩
        StudentCourse sc = studentCourseMapper.findByStudentAndCourse(studentId, courseId);
        if (sc == null) {
            throw new ServiceException("该学生未选修此课程");
        }

        sc.setScScore(score);
        sc.setScUpdatetime(new java.util.Date());

        return studentCourseMapper.updateScore(sc) > 0;
    }


    /**
     * 成绩统计 [cite: 111]
     * 获取某门课的平均分、最高分等
     */
    public Object statisticCourseScore(Long courseId) {
        // 返回 Map 或 自定义 VO
        return studentCourseMapper.getCourseStatistics(courseId);
    }
    /**
     * 查询我的成绩
     * 获取我选修的所有课程的成绩
     */
    public List<Map<String, Object>> listMyScores(Long userId) {

        return studentCourseMapper.selectMyScoreList(userId);
    }
}