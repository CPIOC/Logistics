package com.cpic.taylor.logistics.bean;

import java.util.ArrayList;

/**
 * Created by Taylor on 2016/5/12.
 */
public class Police {

    private int code;
    private String msg;
    private ArrayList<PoliceDataInfo> data;

    public Police() {
        super();
    }

    @Override
    public String toString() {
        return "Police{" +
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

    public ArrayList<PoliceDataInfo> getData() {
        return data;
    }

    public void setData(ArrayList<PoliceDataInfo> data) {
        this.data = data;
    }
}
