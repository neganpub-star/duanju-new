package com.duanju.common.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.duanju.common.exception.ServiceException;

public class SecurityUtil {

    public static Long getUserId() {
        Object id = StpUtil.getLoginId(null);
        if (id == null) {
            throw ServiceException.unauthorized("请先登录");
        }
        return Long.parseLong(id.toString());
    }

    public static Long getUserIdOrNull() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        return Long.parseLong(StpUtil.getLoginId().toString());
    }

    public static Integer getSiteId() {
        Object siteId = StpUtil.getExtra("siteId");
        return siteId == null ? 1 : Integer.parseInt(siteId.toString());
    }

    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    public static void checkLogin() {
        StpUtil.checkLogin();
    }
}
