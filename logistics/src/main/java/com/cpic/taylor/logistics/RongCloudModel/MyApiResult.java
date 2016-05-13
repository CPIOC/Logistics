package com.cpic.taylor.logistics.RongCloudModel;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import io.rong.common.ParcelUtils;

/**
 * Created by Bob on 2015/3/4.
 */
public class MyApiResult implements Serializable, Parcelable {



    /**
     * id，用户userid，群组 groupid
     */
    private String id;
    /**
     * 用户名
     */
    private String user_id;
    private String friends_id;
    private String created_at;
    private String friend_name;
    private String friend_img;
    private String friend_plate_number;
    private String cloud_id;

    public String getCloud_id() {
        return cloud_id;
    }

    public void setCloud_id(String cloud_id) {
        this.cloud_id = cloud_id;
    }

    public String getFriend_plate_number() {
        return friend_plate_number;
    }

    public void setFriend_plate_number(String friend_plate_number) {
        this.friend_plate_number = friend_plate_number;
    }

    public String getFriend_car_models() {
        return friend_car_models;
    }

    public void setFriend_car_models(String friend_car_models) {
        this.friend_car_models = friend_car_models;
    }

    public String getFriend_driving_license() {
        return friend_driving_license;
    }

    public void setFriend_driving_license(String friend_driving_license) {
        this.friend_driving_license = friend_driving_license;
    }

    private String friend_car_models;
    private String friend_driving_license;

    public String getImg() {
        return friend_img;
    }

    public void setImg(String img) {
        this.friend_img = img;
    }

    public String getName() {
        return friend_name;
    }

    public void setName(String name) {
        this.friend_name = name;
    }

    public MyApiResult() {

    }


    public MyApiResult(Parcel in) {
        setId(ParcelUtils.readFromParcel(in));
        setUser_id(ParcelUtils.readFromParcel(in));
        setFriends_id(ParcelUtils.readFromParcel(in));
        setCreated_at(ParcelUtils.readFromParcel(in));
        setName(ParcelUtils.readFromParcel(in));
        setFriend_plate_number(ParcelUtils.readFromParcel(in));
        setFriend_car_models(ParcelUtils.readFromParcel(in));
        setFriend_driving_license(ParcelUtils.readFromParcel(in));
        setImg(ParcelUtils.readFromParcel(in));
        setCloud_id(ParcelUtils.readFromParcel(in));

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFriends_id() {
        return friends_id;
    }

    public void setFriends_id(String friends_id) {
        this.friends_id = friends_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        ParcelUtils.writeToParcel(parcel, getId());
        ParcelUtils.writeToParcel(parcel, getUser_id());
        ParcelUtils.writeToParcel(parcel, getFriends_id());
        ParcelUtils.writeToParcel(parcel, getCreated_at());
        ParcelUtils.writeToParcel(parcel, getName());
        ParcelUtils.writeToParcel(parcel, getFriend_car_models());
        ParcelUtils.writeToParcel(parcel, getFriend_driving_license());
        ParcelUtils.writeToParcel(parcel, getFriend_plate_number());
        ParcelUtils.writeToParcel(parcel, getImg());
        ParcelUtils.writeToParcel(parcel, getCloud_id());

    }

    public static final Creator<ApiResult> CREATOR = new Creator<ApiResult>() {

        @Override
        public ApiResult createFromParcel(Parcel source) {
            return new ApiResult(source);
        }

        @Override
        public ApiResult[] newArray(int size) {
            return new ApiResult[size];
        }
    };
}
