package com.cpic.taylor.logistics.bean;

/**
 * Created by Taylor on 2016/5/11.
 */
public class Login {

    private int code;
    private String msg;
    private LoginData data;

    public Login() {
        super();
    }

    @Override
    public String toString() {
        return "Login{" +
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

    public LoginData getData() {
        return data;
    }

    public void setData(LoginData data) {
        this.data = data;
    }
}
