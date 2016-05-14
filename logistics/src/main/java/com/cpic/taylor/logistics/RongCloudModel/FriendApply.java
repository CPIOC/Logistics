package com.cpic.taylor.logistics.RongCloudModel;

import java.util.ArrayList;

/**
 * Created by hui on 2016/5/14.
 */
public class FriendApply {

    private int code;
    private String msg;
    private ArrayList<FriendApplyData> data;

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

    public ArrayList<FriendApplyData> getData() {
        return data;
    }

    public void setData(ArrayList<FriendApplyData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "FriendApply{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
