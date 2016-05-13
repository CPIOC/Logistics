package com.cpic.taylor.logistics.bean;

import java.util.ArrayList;

/**
 * Created by Taylor on 2016/5/13.
 */
public class setRoute {
    private int code;
    private String msg;
    private ArrayList<SetRouteData> data;

    public setRoute() {
        super();
    }

    @Override
    public String toString() {
        return "setRoute{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

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

    public ArrayList<SetRouteData> getData() {
        return data;
    }

    public void setData(ArrayList<SetRouteData> data) {
        this.data = data;
    }
}
