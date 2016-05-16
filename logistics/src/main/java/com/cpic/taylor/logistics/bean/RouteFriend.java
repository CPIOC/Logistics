package com.cpic.taylor.logistics.bean;

import java.util.ArrayList;

/**
 * Created by xuan on 2016/5/16.
 */
public class RouteFriend {
    private int code;
    private String msg;
    private ArrayList<RouteFriendData> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ArrayList<RouteFriendData> getData() {
        return data;
    }

    public void setData(ArrayList<RouteFriendData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RouteFriend{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
