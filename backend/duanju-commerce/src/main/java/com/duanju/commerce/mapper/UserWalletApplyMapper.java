package com.duanju.commerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.duanju.commerce.domain.UserWalletApply;
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
}
