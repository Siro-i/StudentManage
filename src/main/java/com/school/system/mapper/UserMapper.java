package com.school.system.mapper;

import com.school.system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    // 根据用户名查找（登录用）
    User findByUsername(@Param("username") String username);

    // 根据ID查找
    User selectById(@Param("userId") Long userId);

    // 插入新用户
    int insert(User user);

    // 更新用户信息（密码、修改时间等）
    int updateById(User user);
    // 分页查询用户列表（根据条件）
    List<User> selectList(User condition);
    // 删除用户（逻辑删除）
    int deleteById(@Param("userId") Long userId);

}