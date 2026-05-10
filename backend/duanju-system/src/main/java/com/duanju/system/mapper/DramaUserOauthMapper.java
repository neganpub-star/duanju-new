package com.duanju.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.system.domain.DramaUserOauth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DramaUserOauthMapper extends BaseMapper<DramaUserOauth> {

    @Select("SELECT * FROM vs_drama_user_oauth WHERE site_id = #{siteId} AND openid = #{openid} AND platform = #{platform} LIMIT 1")
    DramaUserOauth selectByOpenid(@Param("siteId") Integer siteId, @Param("openid") String openid, @Param("platform") String platform);
}
