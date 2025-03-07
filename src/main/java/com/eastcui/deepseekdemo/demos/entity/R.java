package com.eastcui.deepseekdemo.demos.entity;

import lombok.Data;

/**
 * @Classname R
 * @Description TODO
 * @Date 2025/3/5 9:59
 * @Created by czd
 */
@Data
public class R {
    private Integer code;
    private String msg;
    private Object data;

    public static R ok(Object data) {
        R r = ok();
        r.setData(data);
        return r;
    }

    public static R ok() {
        R r = new R();
        r.setCode(200);
        r.setMsg("success");
        return r;
    }

    public static R error(Integer code, String msg) {
        R r = new R();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    public static R error(String msg) {
        R r = new R();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }

    public static R error() {
        R r = new R();
        r.setCode(500);
        r.setMsg("error");
        return r;
    }


}
