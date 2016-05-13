package com.cpic.taylor.logistics.bean;

/**
 * Created by Taylor on 2016/5/13.
 */
public class SetRouteData {

    private String lat;
    private String lng;
    private String user_name;
    private String cloud_id;
    private String cloud_token;
    private String img;

    public SetRouteData() {
        super();
    }

    @Override
    public String toString() {
        return "SetRouteData{" +
                "lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", user_name='" + user_name + '\'' +
                ", cloud_id='" + cloud_id + '\'' +
                ", cloud_token='" + cloud_token + '\'' +
                ", img='" + img + '\'' +
                '}';
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
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

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
