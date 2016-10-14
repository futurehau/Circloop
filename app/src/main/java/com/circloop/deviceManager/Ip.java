package com.circloop.deviceManager;

import java.io.Serializable;

/**
 * Created by zh on 2016/8/19.
 */
public class Ip implements Serializable{
    int[] ips;
    public Ip(String ip){
        String[] str=ip.split("\\.");
        ips=new int[4];
        for(int i=0;i<4;i++){
            ips[i]=Integer.parseInt(str[i]);
        }
    }

    public int[] getIps() {
        return ips;
    }

    public void setIps(int[] ips) {
        this.ips = ips;
    }

    public int compare(Ip other) {
        int[] ips2=other.ips;
        for(int i=0;i<4;i++){
            if(ips[i]>ips2[i])
                return 1;
            else if(ips[i]<ips2[i])
                return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return ips[0]+"."+ips[1]+"."+ips[2]+"."+ips[3];
    }
    public static boolean isValidCIpAddress(String ip){
        if(ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")){
            String[] strs=ip.split("\\.");
            for(int i=0;i<4;i++)
                for(int j=0;j<strs[i].length();j++){
                    char c=strs[i].charAt(j);
                    if (!(c>='0'&&c<='9'))
                        return false;
                }
            if(Integer.parseInt(strs[0])>=1&&Integer.parseInt(strs[0])<=223)
                if(Integer.parseInt(strs[1])<=255)
                    if(Integer.parseInt(strs[2])<=255)
                        if(Integer.parseInt(strs[3])<=255)
                            return true;
        }
        return false;
    }
    public static int countIp(String fromIp,String toIp){//计算从fromIp到toIp包含的ip个数
        String[] begin=fromIp.split("\\.");
        String[] end=toIp.split("\\.");
        if(begin.length!=4||end.length!=4||fromIp.charAt(0)=='.'||toIp.charAt(0)=='.')
            return -1;
        int count=1;
        int tmp=256*256*256;
        for(int i=0;i<4;i++){
            count+=tmp*(Integer.parseInt(end[i])-Integer.parseInt(begin[i]));
            tmp=tmp/256;
        }
        return count;
    }
}
