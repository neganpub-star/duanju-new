package com.duanju.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanju.commerce.domain.Reseller;
import com.duanju.commerce.domain.ResellerOrder;
import com.duanju.commerce.service.ResellerService;
import com.duanju.commerce.service.WalletService;
import com.duanju.common.core.domain.R;
import com.duanju.common.utils.SecurityUtil;
import com.duanju.payment.service.PayService;
import com.duanju.system.domain.DramaUser;
import com.duanju.system.mapper.DramaUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "分销接口")
@RestController
@RequestMapping("/api/reseller")
@RequiredArgsConstructor
public class ResellerController {

    private final ResellerService resellerService;
    private final WalletService walletService;
    private final PayService payService;
    private final DramaUserMapper userMapper;

    @Value("${duanju.site-id:1}")
    private Integer siteId;

    @Operation(summary = "分销套餐列表（含说明）")
    @GetMapping("/list")
    public R<Map<String, Object>> list() {
        List<Reseller> resellerList = resellerService.listAll(siteId);
        List<Map<String, Object>> list = resellerList.stream().map(r -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());
            item.put("name", r.getName());
            item.put("name_i18n", r.getNameI18n());
            item.put("level", r.getLevel());
            item.put("price", r.getPrice());
            item.put("direct", r.getDirect());
            item.put("indirect", r.getIndirect());
            item.put("expire", r.getExpire());
            item.put("content", r.getContent());
            item.put("content_i18n", r.getContentI18n());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("reseller_desc", Map.of("content", ""));
        return R.ok(result);
    }

    @Operation(summary = "分销主页数据")
    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        long userId = StpUtil.getLoginIdAsLong();
        DramaUser user = userMapper.selectById(userId);
        if (user == null) return R.fail("用户不存在");

        Map<String, Object> result = new HashMap<>();
        result.put("reseller_money", user.getMoney() != null ? user.getMoney() : BigDecimal.ZERO);
        result.put("money", user.getMoney() != null ? user.getMoney() : BigDecimal.ZERO);
        result.put("reseller_desc", Map.of("content", ""));

        if (user.getResellerLevel() != null && user.getResellerLevel() > 0
                && user.getResellerExpireTime() != null && user.getResellerExpireTime().isAfter(LocalDateTime.now())) {
            List<Reseller> packages = resellerService.listAll(siteId);
            Reseller pkg = packages.stream()
                    .filter(r -> r.getLevel().equals(user.getResellerLevel()))
                    .findFirst().orElse(null);
            if (pkg != null) {
                Map<String, Object> resellerInfo = new HashMap<>();
                resellerInfo.put("level", pkg.getLevel());
                Map<String, Object> resellerJson = new HashMap<>();
                resellerJson.put("name", pkg.getName());
                resellerJson.put("direct", pkg.getDirect());
                resellerJson.put("indirect", pkg.getIndirect());
                resellerInfo.put("reseller_json", resellerJson);
                result.put("reseller", resellerInfo);
            }
        }
        return R.ok(result);
    }

    @Operation(summary = "我的团队")
    @GetMapping("/team")
    public R<Map<String, Object>> team(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pagesize) {
        long userId = StpUtil.getLoginIdAsLong();

        List<DramaUser> directList = userMapper.selectList(new LambdaQueryWrapper<DramaUser>()
                .eq(DramaUser::getParentId, userId)
                .isNull(DramaUser::getDeleteTime));

        List<Long> directIds = directList.stream().map(DramaUser::getId).collect(Collectors.toList());
        List<DramaUser> indirectList = directIds.isEmpty() ? Collections.emptyList() :
                userMapper.selectList(new LambdaQueryWrapper<DramaUser>()
                        .in(DramaUser::getParentId, directIds)
                        .isNull(DramaUser::getDeleteTime));

        List<Map<String, Object>> allMembers = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (DramaUser u : directList) {
            Map<String, Object> m = new HashMap<>();
            m.put("avatar", u.getAvatar());
            m.put("nickname", u.getNickname());
            m.put("type", "1");
            m.put("createtime", u.getCreateTime() != null ? u.getCreateTime().format(fmt) : "");
            allMembers.add(m);
        }
        for (DramaUser u : indirectList) {
            Map<String, Object> m = new HashMap<>();
            m.put("avatar", u.getAvatar());
            m.put("nickname", u.getNickname());
            m.put("type", "2");
            m.put("createtime", u.getCreateTime() != null ? u.getCreateTime().format(fmt) : "");
            allMembers.add(m);
        }

        int total = allMembers.size();
        int from = Math.min((page - 1) * pagesize, total);
        int to = Math.min(from + pagesize, total);
        List<Map<String, Object>> pageList = allMembers.subList(from, to);

        Map<String, Object> result = new HashMap<>();
        result.put("count", total);
        result.put("count_direct", directList.size());
        result.put("count_indirect", indirectList.size());
        result.put("reseller_user", pageList);
        return R.ok(result);
    }

    @Operation(summary = "佣金明细")
    @GetMapping("/commissions")
    public R<Map<String, Object>> commissions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pagesize) {
        long userId = StpUtil.getLoginIdAsLong();
        var logs = walletService.getLogs(siteId, userId, "money", page, pagesize);

        var commissionLogs = logs.stream()
                .filter(l -> "reseller_commission".equals(l.getType()))
                .collect(Collectors.toList());

        // 查询触发佣金的用户昵称
        Set<Long> referUserIds = commissionLogs.stream()
                .filter(l -> l.getItemId() != null)
                .map(l -> { try { return Long.parseLong(l.getItemId()); } catch (Exception e) { return null; } })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> nicknameMap = new HashMap<>();
        if (!referUserIds.isEmpty()) {
            userMapper.selectList(new LambdaQueryWrapper<DramaUser>()
                    .in(DramaUser::getId, referUserIds))
                    .forEach(u -> nicknameMap.put(u.getId(), u.getNickname()));
        }

        var list = commissionLogs.stream().map(l -> {
            Map<String, Object> item = new LinkedHashMap<>();
            Long refId = null;
            try { refId = l.getItemId() != null ? Long.parseLong(l.getItemId()) : null; } catch (Exception ignored) {}
            item.put("nickname", refId != null ? nicknameMap.getOrDefault(refId, "用户" + refId) : "—");
            item.put("type_text", l.getMemo() != null && l.getMemo().contains("间接") ? "间推" : "直推");
            item.put("money", l.getWallet());
            item.put("pay_money", l.getWallet());
            item.put("order_type_text", "分销佣金");
            item.put("createtime", l.getCreateTime() != null ? l.getCreateTime().toString() : "");
            return item;
        }).collect(Collectors.toList());

        BigDecimal sum = commissionLogs.stream()
                .map(l -> l.getWallet() != null ? l.getWallet() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("sum", sum);
        result.put("count", list.size());
        result.put("list", list);
        return R.ok(result);
    }

    @Operation(summary = "购买分销资格")
    @PostMapping("/buy")
    public R<Map<String, Object>> buy(@RequestBody BuyReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        String payType = req.getPayType() != null ? req.getPayType() : "wechat";
        ResellerOrder order = resellerService.createOrder(siteId, userId, req.getResellerId(), payType, req.getPlatform());
        if ("wallet".equals(payType)) {
            return R.ok(Map.of("orderSn", order.getOrderSn(), "status", "paid"));
        }
        Map<String, Object> payParams = payService.prepay(order.getOrderSn(), order.getPayFee(),
                "购买分销资格", payType, req.getPlatform(), req.getOpenid());
        payParams.put("orderSn", order.getOrderSn());
        return R.ok(payParams);
    }

    @Operation(summary = "绑定邀请人（仅首次）")
    @PostMapping("/bind")
    public R<Void> bind(@RequestParam Long inviterUserId) {
        long userId = StpUtil.getLoginIdAsLong();
        resellerService.bindInviter(userId, inviterUserId);
        return R.ok();
    }

    @Data
    static class BuyReq {
        private Long resellerId;
        private String payType;
        private String platform;
        private String openid;
    }
}
