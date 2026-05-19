package com.duanju.commerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.duanju.commerce.domain.VipOrder;
import com.duanju.commerce.vo.VipOrderStatsVO;
import com.duanju.commerce.vo.VipOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VipOrderMapper extends BaseMapper<VipOrder> {

    @Select("SELECT * FROM vs_drama_vip_order WHERE order_sn = #{orderSn} LIMIT 1")
    VipOrder selectByOrderSn(String orderSn);

    @Select("<script>" +
            "SELECT o.*, u.nickname, u.mobile, u.avatar " +
            "FROM vs_drama_vip_order o " +
            "LEFT JOIN vs_drama_user u ON o.user_id = u.id " +
            "WHERE o.delete_time IS NULL " +
            "AND o.site_id = #{siteId} " +
            "<if test='status != null'>AND o.status = #{status} </if>" +
            "<if test='mobile != null and mobile != \"\"'>AND u.mobile LIKE CONCAT('%',#{mobile},'%') </if>" +
            "<if test='nickname != null and nickname != \"\"'>AND u.nickname LIKE CONCAT('%',#{nickname},'%') </if>" +
            "ORDER BY o.id DESC" +
            "</script>")
    IPage<VipOrderVO> selectWithUserPage(IPage<?> page,
                                          @Param("siteId") Integer siteId,
                                          @Param("status") Integer status,
                                          @Param("mobile") String mobile,
                                          @Param("nickname") String nickname);

    /**
     * 全量统计（与列表查询条件保持一致）
     * - 总订单数 / 已支付数(status=1 或 2) / 待支付数(status=0) / 已取消关闭数(status<0) / 累计 GMV(已支付 payFee 之和)
     */
    @Select("<script>" +
            "SELECT " +
            "  COUNT(*) AS total_count, " +
            "  SUM(CASE WHEN o.status IN (1,2) THEN 1 ELSE 0 END) AS paid_count, " +
            "  SUM(CASE WHEN o.status = 0 THEN 1 ELSE 0 END) AS pending_count, " +
            "  SUM(CASE WHEN o.status IN (-1, -2) THEN 1 ELSE 0 END) AS canceled_count, " +
            "  COALESCE(SUM(CASE WHEN o.status IN (1,2) THEN o.pay_fee ELSE 0 END), 0) AS gmv " +
            "FROM vs_drama_vip_order o " +
            "LEFT JOIN vs_drama_user u ON o.user_id = u.id " +
            "WHERE o.delete_time IS NULL " +
            "AND o.site_id = #{siteId} " +
            "<if test='status != null'>AND o.status = #{status} </if>" +
            "<if test='mobile != null and mobile != \"\"'>AND u.mobile LIKE CONCAT('%',#{mobile},'%') </if>" +
            "<if test='nickname != null and nickname != \"\"'>AND u.nickname LIKE CONCAT('%',#{nickname},'%') </if>" +
            "</script>")
    VipOrderStatsVO selectOrderStats(@Param("siteId") Integer siteId,
                                     @Param("status") Integer status,
                                     @Param("mobile") String mobile,
                                     @Param("nickname") String nickname);

    /** 统计指定时间段内已支付 VIP 订单的实付总额 */
    @Select("SELECT COALESCE(SUM(pay_fee), 0) FROM vs_drama_vip_order " +
            "WHERE site_id = #{siteId} AND status IN (1, 2) AND delete_time IS NULL " +
            "AND create_time >= #{start}")
    java.math.BigDecimal sumPayFeeFrom(@Param("siteId") Integer siteId,
                                       @Param("start") java.time.LocalDateTime start);
}
