package com.cpic.taylor.logistics.RongCloudModel;

import com.cpic.taylor.logistics.bean.LoginData;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by hui on 2016/5/14.
 */
public class RCUser {

    private int code;
    private String msg;

    public ArrayList<RCUserData> getData() {
        return data;
    }

    public void setData(ArrayList<RCUserData> data) {
        this.data = data;
    }

    private ArrayList<RCUserData> data;




    @Override
    public String toString() {
        return "RCUser{" +
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
}
