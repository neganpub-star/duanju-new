package com.duanju.common.core.domain;

import com.duanju.common.constant.ResultCode;
import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    private R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS, "操作成功", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS, "操作成功", data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(ResultCode.SUCCESS, msg, data);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(ResultCode.FAIL, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public boolean isOk() {
        return this.code == ResultCode.SUCCESS;
    }
}
