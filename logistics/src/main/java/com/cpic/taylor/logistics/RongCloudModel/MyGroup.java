package com.cpic.taylor.logistics.RongCloudModel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xuan on 2016/5/13.
 */
public class MyGroup implements Serializable {

    public  int code;
    public  String msg;
    public  List<MyGroupData> data;

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

    public List<MyGroupData> getData() {
        return data;
    }

    public void setData(List<MyGroupData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MyGroup{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
