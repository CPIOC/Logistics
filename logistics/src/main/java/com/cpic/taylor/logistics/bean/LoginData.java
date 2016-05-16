package com.cpic.taylor.logistics.bean;

/**
 * Created by Taylor on 2016/5/11.
 */


public class LoginData {
    private String id;
    private String login;
    private String name;
    private String password;
    private String img;
    private String plate_number;
    private String cloud_id;

    private String car_models;
    private String driving_license;
    private String created_at;
    private String cloud_token;
    private String token;

    @Override
    public String toString() {
        return "LoginData{" +
                "id='" + id + '\'' +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", img='" + img + '\'' +
                ", plate_number='" + plate_number + '\'' +
                ", cloud_id='" + cloud_id + '\'' +
                ", car_models='" + car_models + '\'' +
                ", driving_license='" + driving_license + '\'' +
                ", created_at='" + created_at + '\'' +
                ", cloud_token='" + cloud_token + '\'' +
                ", token='" + token + '\'' +
                '}';
    }

    public String getCloud_id() {
        return cloud_id;
    }

    public void setCloud_id(String cloud_id) {
        this.cloud_id = cloud_id;
    }

    public String getCar_models() {
        return car_models;
    }

    public void setCar_models(String car_models) {
        this.car_models = car_models;
    }

    public LoginData() {
        super();
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

    public String getDriving_license() {
        return driving_license;
    }

    public void setDriving_license(String driving_license) {
        this.driving_license = driving_license;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCloud_token() {
        return cloud_token;
    }

    public void setCloud_token(String cloud_token) {
        this.cloud_token = cloud_token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
