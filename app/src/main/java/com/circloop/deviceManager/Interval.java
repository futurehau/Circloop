package com.circloop.deviceManager;

import java.io.Serializable;

/**
 * Created by zh on 2016/8/19.
 */
public class Interval implements Serializable{
    Ip start;
    Ip end;
    public Interval(Ip s, Ip e) { start = s; end = e; }

    public Ip getStart() {
        return start;
    }

    public Ip getEnd() {
        return end;
    }

    public void setStart(Ip start) {
        this.start = start;
    }

    public void setEnd(Ip end) {
        this.end = end;
    }
}
