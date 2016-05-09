package com.cpic.taylor.logistics.RongCloudModel;

import android.os.Parcel;
import android.os.Parcelable;

import com.sea_monster.common.ParcelUtils;

import java.io.Serializable;

/**
 * Created by Bob on 15/9/7.
 */
public class Users implements Parcelable, Serializable {

    private String userId;

    private String passwd;

    /**
     * 返回码
     */
    private int code;
    /**
     * 错误码 message
     */
    private String message;
    /**
     * 返回信息
     */
    private int result;

    public Users() {

    }

    public Users(Parcel in) {
        userId = ParcelUtils.readFromParcel(in);

        passwd = ParcelUtils.readFromParcel(in);

    }

    public static final Creator<User> CREATOR = new Creator<User>() {

        @Override
        public User createFromParcel(Parcel source) {

            return new User(source);

        }

        @Override
        public User[] newArray(int size) {

            return new User[size];

        }

    };

    @Override
    public int describeContents() {

        return 0;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        ParcelUtils.writeToParcel(dest, userId);

        ParcelUtils.writeToParcel(dest, passwd);

    }

    public String getUserId() {

        return userId;

    }

    public void setUserId(String userId) {

        this.userId = userId;

    }


    public String getPasswd() {

        return passwd;

    }

    public void setPasswd(String passwd) {

        this.passwd = passwd;

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}