package com.duanju.commerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.commerce.domain.ResellerOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ResellerOrderMapper extends BaseMapper<ResellerOrder> {

    @Select("SELECT * FROM vs_drama_reseller_order WHERE order_sn = #{orderSn} LIMIT 1")
    ResellerOrder selectByOrderSn(String orderSn);
}
