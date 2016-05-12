package com.cpic.taylor.logistics.bean;

/**
 * Created by Taylor on 2016/5/12.
 */
public class PoliceDataChildInfo {

    private String id;
    private String name;

    public PoliceDataChildInfo() {
        super();
    }

    @Override
    public String toString() {
        return "PoliceDataChildInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
