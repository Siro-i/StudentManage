package com.school.system.mapper;

import com.school.system.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TeacherMapper {
    int insert(Teacher teacher);

    Teacher selectByUserId(@Param("userId") Long userId);

    List<Teacher> selectList(Teacher condition);

    void updateById(Teacher teacher);


    void deleteByUserId(@Param("userId") Long userId);
}