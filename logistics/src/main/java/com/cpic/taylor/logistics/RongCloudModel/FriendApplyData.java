package com.cpic.taylor.logistics.RongCloudModel;

/**
 * Created by hui on 2016/5/14.
 */
public class FriendApplyData {

    private String name;
    private String id;
    private String img;
    private String cloud_id;

    @Override
    public String toString() {
        return "FriendApplyData{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", img='" + img + '\'' +
                ", cloud_id='" + cloud_id + '\'' +
                '}';
    }

    public String getCloud_id() {
        return cloud_id;
    }

    public void setCloud_id(String cloud_id) {
        this.cloud_id = cloud_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

}
