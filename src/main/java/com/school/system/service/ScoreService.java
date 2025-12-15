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



    /**
     * 查询课程下所有学生成绩列表。
     *
     * @param courseId 课程 ID
     * @return 成绩列表
     */
    public List<Map<String, Object>> listStudentScores(Long courseId) {
        return studentCourseMapper.selectStudentScoreList(courseId);
    }

    /**
     * 录入/修改成绩 [cite: 82, 110]
     * 逻辑：校验分数范围，更新 student_course_table
     *
     * @param studentId 学生档案 ID
     * @param courseId  课程 ID
     * @param score     分数 0-100
     * @return 是否更新成功
     */
    public boolean setScore(Long studentId, Long courseId, Integer score) {

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
     * 成绩统计
     * 获取某门课的平均分、最高分等统计信息
     * 
     * @param courseId 课程ID
     * @return 统计结果
     */
    public Object statisticCourseScore(Long courseId) {
        return studentCourseMapper.getCourseStatistics(courseId);
    }
    /**
     * 查询我的成绩
     * 获取当前用户选修的所有课程的成绩
     * 
     * @param userId 当前登录用户ID
     * @return 成绩列表
     */
    public List<Map<String, Object>> listMyScores(Long userId) {
        Student student = studentMapper.selectByUserId(userId);
        if (student == null) {
            throw new ServiceException("未找到学生档案");
        }
        return studentCourseMapper.selectMyScoreList(student.getStudentId());
    }
}