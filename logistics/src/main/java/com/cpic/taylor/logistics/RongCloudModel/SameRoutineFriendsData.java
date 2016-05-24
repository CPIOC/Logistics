package com.cpic.taylor.logistics.RongCloudModel;

/**
 * Created by xuan on 2016/5/24.
 */
public class SameRoutineFriendsData {

    private String user_id;
    private String name;
    private String cloud_id;
    private String cloud_token;
    private String distance;
    private String img;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCloud_id() {
        return cloud_id;
    }

    public void setCloud_id(String cloud_id) {
        this.cloud_id = cloud_id;
    }

    public String getCloud_token() {
        return cloud_token;
    }

    public void setCloud_token(String cloud_token) {
        this.cloud_token = cloud_token;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return "SameRoutineFriendsData{" +
                "user_id='" + user_id + '\'' +
                ", name='" + name + '\'' +
                ", cloud_id='" + cloud_id + '\'' +
                ", cloud_token='" + cloud_token + '\'' +
                ", distance='" + distance + '\'' +
                ", img='" + img + '\'' +
                '}';
    }
}
