package com.cpic.taylor.logistics.RongCloudModel;

import android.os.Parcel;
import android.os.Parcelable;

import com.sea_monster.common.ParcelUtils;

import java.io.Serializable;


/**
 * Entity mapped to table USER.
 */

public class User implements Parcelable, Serializable {

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
    private ApiResult result;

    public User() {

    }

    public User(Parcel in) {
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

    public ApiResult getResult() {
        return result;
    }

    public void setResult(ApiResult result) {
        this.result = result;
    }


}