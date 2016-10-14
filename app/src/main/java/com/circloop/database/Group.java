package com.circloop.database;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 浩思于微 on 2016/5/17.
 */
public class Group {
    private String groupName;
    private String groupDesc;
    private List<String[]> ipList;
    private boolean isDiscover;
    private int progress;
    private int totalIp;
    public List<String> pingedIps;//ping通的设备
    public int doneIpNums=0;//ping结束的设备数量
    public int progressBarMax=0;//为了进度条的显示而设置的变量
    private boolean isSnmpDis=false;
    private boolean isGroup;//为了区分是一个分组还是一个设备，以便进行编辑的时候判断跳到那个界面

    public Group(String groupName,String groupDesc,boolean isDiscover,int progress,int totalIp,boolean isGroup){
        this.groupName=groupName;
        this.groupDesc=groupDesc;
        ipList=new ArrayList<String[]>();
        this.isDiscover=isDiscover;
        this.progress=progress;
        this.totalIp=totalIp;
        this.isGroup=isGroup;
        pingedIps=new ArrayList<String>();
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupDesc() {
        return groupDesc;
    }

    public boolean isDiscover(){
        return isDiscover;
    }
    public boolean isGroup(){
        return isGroup;
    }
    public int getProgress(){
        return progress;
    }
    public int getTotalIp(){return totalIp;}
    public boolean isSnmpDis(){
        return isSnmpDis;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGroupDesc(String groupDesc) {
        this.groupDesc = groupDesc;
    }

    public void setIsDiscover(boolean isDiscover){
        this.isDiscover=isDiscover;
    }
    public void setProgress(int progress){
        this.progress=progress;
    }
    public void setSnmpDis(boolean isSnmpDis){
        this.isSnmpDis=isSnmpDis;
    }

    public void addIpSeg(String ipBegin,String ipEnd){//一个分组可以包含多个ip段
        String[] ipSeg={ipBegin,ipEnd};
        ipList.add(ipSeg);
    }

}
