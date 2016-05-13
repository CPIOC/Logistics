package com.cpic.taylor.logistics.RongCloudModel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xuan on 2016/5/13.
 */
public class MyNewFriends implements Serializable {

    public int code;

    public String msg;

    private List<MyNewFriend> data;

    public MyNewFriends(){

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

    public List<MyNewFriend> getData() {
        return data;
    }

    public void setData(List<MyNewFriend> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MyNewFriends{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
