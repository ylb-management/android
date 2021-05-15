package com.whjstech.ylb;

import java.util.Date;

public class MyPoint {
    private Date date;
    private float value;

    public MyPoint(Date date, float value){
        this.date = date;
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public float getValue() {
        return value;
    }
}
