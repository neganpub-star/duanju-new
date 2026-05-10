package com.duanju.commerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.commerce.domain.VipOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VipOrderMapper extends BaseMapper<VipOrder> {

    @Select("SELECT * FROM vs_drama_vip_order WHERE order_sn = #{orderSn} LIMIT 1")
    VipOrder selectByOrderSn(String orderSn);
}
