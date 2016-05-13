package com.cpic.taylor.logistics.RongCloudModel;

import java.io.Serializable;

/**
 * Created by xuan on 2016/5/13.
 */
public class MyNewFriend implements Serializable {
    public String id;
    public String login;
    public String name;
    public String password;
    public String img;
    public String plate_number;
    public String car_models;
    public String driving_license;
    public String device;
    public String created_at;
    public String cloud_id;
    public String cloud_token;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getPlate_number() {
        return plate_number;
    }

    public void setPlate_number(String plate_number) {
        this.plate_number = plate_number;
    }

    public String getCar_models() {
        return car_models;
    }

    public void setCar_models(String car_models) {
        this.car_models = car_models;
    }

    public String getDriving_license() {
        return driving_license;
    }

    public void setDriving_license(String driving_license) {
        this.driving_license = driving_license;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
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

    @Override
    public String toString() {
        return "MyNewFriend{" +
                "id='" + id + '\'' +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", img='" + img + '\'' +
                ", plate_number='" + plate_number + '\'' +
                ", car_models='" + car_models + '\'' +
                ", driving_license='" + driving_license + '\'' +
                ", device='" + device + '\'' +
                ", created_at='" + created_at + '\'' +
                ", cloud_id='" + cloud_id + '\'' +
                ", cloud_token='" + cloud_token + '\'' +
                '}';
    }
}
