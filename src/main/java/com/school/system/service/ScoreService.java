package com.school.system.service;

import com.school.system.common.ServiceException;
import com.school.system.entity.ScoreExportVO;
import com.school.system.entity.Student;
import com.school.system.entity.StudentCourse;
import com.school.system.mapper.StudentCourseMapper;
import com.school.system.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ScoreService {

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 获取成绩导出数据 (VO)
     */
    public List<ScoreExportVO> getExportData(Long courseId) {
        List<Map<String, Object>> rawList = studentCourseMapper.selectStudentScoreList(courseId);
        List<ScoreExportVO> exportList = new ArrayList<>();

        for (Map<String, Object> map : rawList) {
            ScoreExportVO vo = new ScoreExportVO();
            vo.setStudentName(String.valueOf(map.getOrDefault("studentRealName", "未知姓名")));
            vo.setStudentNumber(String.valueOf(map.getOrDefault("studentNumber", "")));
            vo.setStudentClass(String.valueOf(map.getOrDefault("studentClass", "")));
            vo.setCourseName(String.valueOf(map.getOrDefault("courseName", "未命名课程")));

            Object scoreObj = map.get("scScore");
            if (scoreObj != null) {
                try {
                    vo.setScore(Double.valueOf(scoreObj.toString()).intValue());
                } catch (Exception e) {
                    vo.setScore(0);
                }
            }
            exportList.add(vo);
        }
        return exportList;
    }

    public List<Map<String, Object>> listStudentScores(Long courseId) {
        return studentCourseMapper.selectStudentScoreList(courseId);
    }

    public boolean setScore(Long studentId, Long courseId, Integer score) {
        if (score == null || score < 0 || score > 100) {
            throw new ServiceException("分数必须在0-100之间");
        }
        StudentCourse sc = studentCourseMapper.findByStudentAndCourse(studentId, courseId);
        if (sc == null) {
            throw new ServiceException("该学生未选修此课程");
        }
        sc.setScScore(score);
        sc.setScUpdatetime(new java.util.Date());
        return studentCourseMapper.updateScore(sc) > 0;
    }

    public Object statisticCourseScore(Long courseId) {
        return studentCourseMapper.getCourseStatistics(courseId);
    }

    public List<Map<String, Object>> listMyScores(Long userId) {
        Student student = studentMapper.selectByUserId(userId);
        if (student == null) {
            throw new ServiceException("未找到学生档案");
        }
        return studentCourseMapper.selectMyScoreList(student.getStudentId());
    }
}