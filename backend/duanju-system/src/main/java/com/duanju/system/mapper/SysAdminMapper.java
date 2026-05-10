package com.duanju.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.system.domain.SysAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysAdminMapper extends BaseMapper<SysAdmin> {

    @Select("SELECT * FROM vs_admin WHERE username = #{username} LIMIT 1")
    SysAdmin selectByUsername(String username);
}
