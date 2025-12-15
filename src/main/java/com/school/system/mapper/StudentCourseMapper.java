package com.school.system.mapper;

import com.school.system.entity.StudentCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudentCourseMapper {
    // 检查是否已选课
    StudentCourse findByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    // 选课
    int insert(StudentCourse sc);

    // 退课
    int deleteByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    // 录入成绩
    int updateScore(StudentCourse sc);

    // 查询课程的所有学生成绩（包含姓名）
    List<Map<String, Object>> selectStudentScoreList(@Param("courseId") Long courseId);

    // 成绩统计（返回 Map 包含平均分、最高分等）
    Map<String, Object> getCourseStatistics(@Param("courseId") Long courseId);

    // 查询我的成绩（包含课程名称）
    List<Map<String, Object>> selectMyScoreList(@Param("studentId") Long studentId);

    // 退课（根据课程 ID 删除所有学生的成绩）
    int deleteByCourseId(@Param("courseId") Long courseId);
    // 查询学生是否已选该课程
    int countByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}