package com.duanju.commerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.commerce.domain.UsableOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UsableOrderMapper extends BaseMapper<UsableOrder> {

    @Select("SELECT * FROM vs_drama_usable_order WHERE order_sn = #{orderSn} LIMIT 1")
    UsableOrder selectByOrderSn(String orderSn);
}
