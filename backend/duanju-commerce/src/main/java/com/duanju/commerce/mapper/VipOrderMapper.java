package com.duanju.commerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.duanju.commerce.domain.VipOrder;
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
}
