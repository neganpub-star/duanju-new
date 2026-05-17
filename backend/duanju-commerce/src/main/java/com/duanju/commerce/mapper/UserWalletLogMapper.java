package com.duanju.commerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.duanju.commerce.domain.UserWalletLog;
import com.duanju.commerce.vo.WalletLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface UserWalletLogMapper extends BaseMapper<UserWalletLog> {

    @Select("<script>" +
            "SELECT l.*, u.nickname, u.mobile, u.avatar " +
            "FROM vs_drama_user_wallet_log l " +
            "LEFT JOIN vs_drama_user u ON l.user_id = u.id " +
            "WHERE l.delete_time IS NULL " +
            "AND l.site_id = #{siteId} " +
            "<if test='walletType != null and walletType != \"\"'>AND l.wallet_type = #{walletType} </if>" +
            "<if test='mobile != null and mobile != \"\"'>AND u.mobile LIKE CONCAT('%',#{mobile},'%') </if>" +
            "<if test='nickname != null and nickname != \"\"'>AND u.nickname LIKE CONCAT('%',#{nickname},'%') </if>" +
            "ORDER BY l.id DESC" +
            "</script>")
    IPage<WalletLogVO> selectWithUserPage(IPage<?> page,
                                           @Param("siteId") Integer siteId,
                                           @Param("walletType") String walletType,
                                           @Param("mobile") String mobile,
                                           @Param("nickname") String nickname);

    /** 统计指定时间段内的正向流水（充值/收入）总额 */
    @Select("SELECT COALESCE(SUM(wallet), 0) FROM vs_drama_user_wallet_log " +
            "WHERE site_id = #{siteId} AND wallet > 0 AND delete_time IS NULL " +
            "AND create_time >= #{start}")
    BigDecimal sumIncomeFrom(@Param("siteId") Integer siteId, @Param("start") LocalDateTime start);

    /** 统计指定时间段内的提现申请总额（status != -1 表示未拒绝） */
    @Select("SELECT COALESCE(SUM(money), 0) FROM vs_drama_user_wallet_apply " +
            "WHERE site_id = #{siteId} AND status != -1 AND delete_time IS NULL " +
            "AND create_time >= #{start}")
    BigDecimal sumWithdrawFrom(@Param("siteId") Integer siteId, @Param("start") LocalDateTime start);
}
