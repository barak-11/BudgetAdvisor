package com.budgetadviser.android.budgetadvisor;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Purchase {

    private String name;
    private String uid;
    private Integer price;
    private String address;
    private String dateTime;

    public Purchase(String name, String uid, Integer price, String address, String datetime) {
        this.name = name;
        this.uid = uid;
        this.price = price;
        this.address = address;
        this.dateTime = datetime;
        //Calendar.getInstance().getTime()
    }
    public Purchase() {
    }

    public String getName() {
        return name;
    }
    public String getDate() {
        return dateTime;
    }
    public Date getRegularDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
        Date date = new Date();
        try {
            date = format.parse(dateTime);
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String name) {
        this.address = address;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getPrice() {
        if (this.price.toString()=="")
            return 0;
        else
        return this.price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
