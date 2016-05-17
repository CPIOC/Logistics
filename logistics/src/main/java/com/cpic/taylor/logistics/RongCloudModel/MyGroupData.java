package com.cpic.taylor.logistics.RongCloudModel;

import java.io.Serializable;

/**
 * Created by xuan on 2016/5/13.
 */
public class MyGroupData implements Serializable {


    public String id;
    public String user_id;
    public String user_name;
    public String chat_name;
    public String created_at;
    public String target_ids;

    @Override
    public String toString() {
        return "MyGroupData{" +
                "id='" + id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", user_name='" + user_name + '\'' +
                ", chat_name='" + chat_name + '\'' +
                ", created_at='" + created_at + '\'' +
                ", target_id='" + target_ids + '\'' +
                '}';
    }

    public String getTarget_id() {
        return target_ids;
    }

    public void setTarget_id(String target_id) {
        this.target_ids = target_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getChat_name() {
        return chat_name;
    }

    public void setChat_name(String chat_name) {
        this.chat_name = chat_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

}
