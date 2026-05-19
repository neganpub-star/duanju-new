package com.duanju.commerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.duanju.commerce.domain.UserWalletApply;
import com.duanju.commerce.vo.WithdrawStatsVO;
import com.duanju.commerce.vo.WithdrawVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserWalletApplyMapper extends BaseMapper<UserWalletApply> {

    @Select("<script>" +
            "SELECT a.*, u.nickname, u.mobile, u.avatar " +
            "FROM vs_drama_user_wallet_apply a " +
            "LEFT JOIN vs_drama_user u ON a.user_id = u.id " +
            "WHERE a.delete_time IS NULL " +
            "AND a.site_id = #{siteId} " +
            "<if test='status != null'>AND a.status = #{status} </if>" +
            "<if test='mobile != null and mobile != \"\"'>AND u.mobile LIKE CONCAT('%',#{mobile},'%') </if>" +
            "<if test='nickname != null and nickname != \"\"'>AND u.nickname LIKE CONCAT('%',#{nickname},'%') </if>" +
            "ORDER BY a.id DESC" +
            "</script>")
    IPage<WithdrawVO> selectWithUserPage(IPage<?> page,
                                         @Param("siteId") Integer siteId,
                                         @Param("status") Integer status,
                                         @Param("mobile") String mobile,
                                         @Param("nickname") String nickname);

    /**
     * 提现申请状态分布统计
     * - pending     status=0 待审核
     * - processing  status=1 审核通过/处理中
     * - done        status=2 已完成
     * - rejected    status=-1 已拒绝
     * - totalDoneAmount 全量已打款金额(status IN (1,2) 的 money 之和,与首页一致)
     */
    @Select("SELECT " +
            "  SUM(CASE WHEN status = 0  THEN 1 ELSE 0 END) AS pending_count, " +
            "  SUM(CASE WHEN status = 1  THEN 1 ELSE 0 END) AS processing_count, " +
            "  SUM(CASE WHEN status = 2  THEN 1 ELSE 0 END) AS done_count, " +
            "  SUM(CASE WHEN status = -1 THEN 1 ELSE 0 END) AS rejected_count, " +
            "  COALESCE(SUM(CASE WHEN status = 1 THEN money ELSE 0 END), 0) AS total_done_amount " +
            "FROM vs_drama_user_wallet_apply " +
            "WHERE site_id = #{siteId} AND delete_time IS NULL")
    WithdrawStatsVO selectApplyStats(@Param("siteId") Integer siteId);
}
