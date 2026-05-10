package com.duanju.commerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.commerce.domain.ResellerBind;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ResellerBindMapper extends BaseMapper<ResellerBind> {

    @Select("SELECT * FROM vs_drama_reseller_bind WHERE user_id = #{userId} LIMIT 1")
    ResellerBind selectByUserId(Long userId);
}
