package com.duanju.commerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.commerce.domain.UsableOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface UsableOrderMapper extends BaseMapper<UsableOrder> {

    @Select("SELECT * FROM vs_drama_usable_order WHERE order_sn = #{orderSn} LIMIT 1")
    UsableOrder selectByOrderSn(String orderSn);

    /** 统计指定时间段内已支付点数订单的实付总额 */
    @Select("SELECT COALESCE(SUM(pay_fee), 0) FROM vs_drama_usable_order " +
            "WHERE site_id = #{siteId} AND status IN (1, 2) AND delete_time IS NULL " +
            "AND create_time >= #{start}")
    BigDecimal sumPayFeeFrom(@Param("siteId") Integer siteId, @Param("start") LocalDateTime start);
}
