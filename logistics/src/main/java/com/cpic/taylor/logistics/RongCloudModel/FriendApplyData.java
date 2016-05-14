package com.cpic.taylor.logistics.RongCloudModel;

/**
 * Created by hui on 2016/5/14.
 */
public class FriendApplyData {

    private  String name;
    private  String id;
    private  String img;

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

    @Override
    public String toString() {
        return "FriendApplyData{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", img='" + img + '\'' +
                '}';
    }
}
