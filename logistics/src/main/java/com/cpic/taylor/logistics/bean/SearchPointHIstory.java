package com.cpic.taylor.logistics.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by Taylor on 2016/5/11.
 */
@Table(name = "address")
public class SearchPointHistory {

    @Id(column = "_id")
    private int id;
    @Column(column = "area")
    private String area;
    @Column(column = "details")
    private String details;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SearchPointHistory{" +
                "id=" + id +
                ", area='" + area + '\'' +
                ", details='" + details + '\'' +
                '}';
    }

    public SearchPointHistory() {
        super();
    }

    public SearchPointHistory(int id, String area, String details) {
        this.id = id;
        this.area = area;
        this.details = details;
    }

}
