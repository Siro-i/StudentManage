package com.school.system.mapper;

import com.school.system.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {
    int insert(Admin admin);

    Admin selectByUserId(@Param("userId") Long userId);

}