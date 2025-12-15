package com.school.system.mapper;

import com.school.system.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CourseMapper {
    int insert(Course course);

    // 动态查询（支持查所有、查某个老师的课、查可选课）
    List<Course> selectList(Course condition);

    Course selectById(@Param("courseId") Long courseId);

    // 原子增加已选人数（核心并发控制）
    int incrementSelectedNum(@Param("courseId") Long courseId);

    // 减少已选人数（退课）
    int decrementSelectedNum(@Param("courseId") Long courseId);

    // 删除课程（物理删除）
    int deleteById(@Param("courseId") Long courseId);

    // 更新课程信息
    int update(Course course);

    // 根据学生ID查询已选课程
    List<Course> selectByStudentId(@Param("studentId") Long studentId);

    // 根据时间和地点查询可选课程
    List<Course> selectByTimeAndRoom(@Param("courseTime") String courseTime, @Param("courseRoom") String courseRoom);
}