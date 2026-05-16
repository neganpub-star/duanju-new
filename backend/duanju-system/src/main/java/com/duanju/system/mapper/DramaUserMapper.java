package com.duanju.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.system.domain.DramaUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface DramaUserMapper extends BaseMapper<DramaUser> {

    @Select("SELECT * FROM vs_drama_user WHERE site_id = #{siteId} AND mobile = #{mobile} AND delete_time IS NULL LIMIT 1")
    DramaUser selectByMobile(@Param("siteId") Integer siteId, @Param("mobile") String mobile);

    @Select("SELECT * FROM vs_drama_user WHERE site_id = #{siteId} AND username = #{username} AND delete_time IS NULL LIMIT 1")
    DramaUser selectByUsername(@Param("siteId") Integer siteId, @Param("username") String username);

    /** 原子扣积分，余额不足时返回0（不执行），成功返回1 */
    @Update("UPDATE vs_drama_user SET usable = usable - #{amount} WHERE id = #{userId} AND usable >= #{amount}")
    int deductUsable(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
