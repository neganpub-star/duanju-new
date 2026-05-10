package com.duanju.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.system.domain.DramaUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DramaUserMapper extends BaseMapper<DramaUser> {

    @Select("SELECT * FROM vs_drama_user WHERE site_id = #{siteId} AND mobile = #{mobile} AND delete_time IS NULL LIMIT 1")
    DramaUser selectByMobile(@Param("siteId") Integer siteId, @Param("mobile") String mobile);

    @Select("SELECT * FROM vs_drama_user WHERE site_id = #{siteId} AND username = #{username} AND delete_time IS NULL LIMIT 1")
    DramaUser selectByUsername(@Param("siteId") Integer siteId, @Param("username") String username);
}
