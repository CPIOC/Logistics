package com.cpic.taylor.logistics.bean;

import java.util.ArrayList;

/**
 * Created by Taylor on 2016/5/12.
 */
public class PoliceDataInfo {

    private String id;
    private String name;
    private ArrayList<PoliceDataChildInfo> children;

    public PoliceDataInfo() {
        super();
    }

    @Override
    public String toString() {
        return "PoliceDataInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", children=" + children +
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

    public ArrayList<PoliceDataChildInfo> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<PoliceDataChildInfo> children) {
        this.children = children;
    }
}
