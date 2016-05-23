package com.cpic.taylor.logistics.bean;

/**
 * Created by Taylor on 2016/5/23.
 */
public class Address {

    private String details;
    private String area;

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Address() {
        super();
    }
    public Address(String details,String area){
        super();
        this.details = details;
        this.area = area;
    }

    @Override
    public String toString() {
        return "Address{" +
                "details='" + details + '\'' +
                ", area='" + area + '\'' +
                '}';
    }
}
