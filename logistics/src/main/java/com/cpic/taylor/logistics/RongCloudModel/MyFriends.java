package com.cpic.taylor.logistics.RongCloudModel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Bob on 2015/3/15.
 */
public class MyFriends implements Serializable {
    /**
     * 返回码
     */
    private int code;
    /**
     * 错误码 msg
     */
    private String msg;
    /**
     * 好友信息
     */
    private List<MyApiResult> data;

    public MyFriends(){

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getmsg() {
        return msg;
    }

    public void setmsg(String msg) {
        this.msg = msg;
    }

    public List<MyApiResult> getdata() {
        return data;
    }

    public void setdata(List<MyApiResult> data) {
        this.data = data;
    }
}
