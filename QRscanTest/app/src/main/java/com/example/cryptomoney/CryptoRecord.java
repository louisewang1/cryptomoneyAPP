package com.example.cryptomoney;


import java.io.Serializable;
import java.util.Date;

public class CryptoRecord implements Serializable {
    private int index;
    private double value;
    private String addr;
    private String time;

    public CryptoRecord() { }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
