package com.cpic.taylor.logistics.RongCloudModel;

import java.util.ArrayList;

/**
 * Created by xuan on 2016/5/24.
 */
public class SameRoutineFriends {
    private int code;
    private String msg;
    private ArrayList<SameRoutineFriendsData> data;

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

    public ArrayList<SameRoutineFriendsData> getData() {
        return data;
    }

    public void setData(ArrayList<SameRoutineFriendsData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SameRoutineFriends{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
