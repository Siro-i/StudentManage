package com.school.system.mapper;

import com.school.system.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StudentMapper {
    int insert(Student student);


    // 根据账号ID查找（用于登录后获取学生身份）
    Student selectByUserId(@Param("userId") Long userId);

    // 列表查询（用于管理员管理学生，支持按班级/学院筛选）
    List<Student> selectList(Student condition);

    //更新学生信息
    int update(Student student);

    // 关键方法：根据用户ID删除学生档案（级联删除）
    void deleteByUserId(@Param("userId") Long userId);
}
