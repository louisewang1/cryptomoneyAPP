package entity;

import java.io.Serializable;
import java.util.Date;

public class Record implements Serializable {
    private int index;
    private int from;
    private int to;
    private double value;
    private String time;


//    public Record(int index, int from, int to, double value, String time) {
//        this.index = index;
//        this.from = from;
//        this.to = to;
//        this.value = value;
//        this.time = time;
//    }

    public Record() { }

    public Integer getIndex() {
        return index;
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getTo() {
        return to;
    }

    public Double getValue() {
        return value;
    }

    public String getTime() {
        return time;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
