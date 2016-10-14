package com.circloop.deviceDiscover;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.adventnet.snmp.beans.SnmpTarget;
import com.circloop.database.Child;
import com.circloop.database.Group;
import com.circloop.database.MyDatabaseHelper;
import com.circloop.deviceManager.Fragment_list;
import com.circloop.deviceManager.Interval;
import com.circloop.deviceManager.MainActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Created by 浩思于微 on 2016/5/5.
 */
public class AutoDiscover {
    static MyDatabaseHelper dbHelper;
    static SQLiteDatabase db;
    private static IpTask ipTask;
    private static InetAddress address;
    private static int retries=1;
    private static int timeout=5000;
    private static List<Group> groupList;
    private static List<List<Child>> mData;
    //    private static ExecutorService FULL_TASK_EXECUTOR=(ExecutorService) Executors.newCachedThreadPool();//线程池大小为无限大，所以如果ip过多可能导致内存溢出
    private static ExecutorService LIMIT_TASK_EXECUTOR=(ExecutorService) Executors.newFixedThreadPool(50);//可以限定线程池的大小


    public static void scan(List<Interval> intervals, List<List<Child>> mData1,Group group){
        mData=mData1;
        dbHelper= MainActivity.dbHelper;
        db=dbHelper.getWritableDatabase();
        groupList= Fragment_list.getGroupList();
        for(int i=0;i<intervals.size();i++){
            Interval ipSeg=intervals.get(i);
            String fromIp=ipSeg.getStart().toString();
            String toIp=ipSeg.getEnd().toString();
            oneSegScan(fromIp,toIp,mData1,group);
        }

    }
    private static void oneSegScan(String fromIp, String toIp, List<List<Child>> mData1, Group group){
        //由于单个ip的发现工作也调用此例程，后面的程序并不能正确处理，所以此处单独处理
        if(fromIp.equals(toIp)){
            ipTask=new IpTask(group);
            ipTask.executeOnExecutor(LIMIT_TASK_EXECUTOR,fromIp);
            return;
        }
        String[] begin=fromIp.split("\\.");
        int[] current=new int[4];
        for(int i=0;i<4;i++)
            current[i]=Integer.parseInt(begin[i]);
        String currentString=fromIp;
        while(!currentString.equals(toIp)){
            currentString=current[0]+"."+current[1]+"."+current[2]+"."+current[3];
            ipTask=new IpTask(group);
            ipTask.executeOnExecutor(LIMIT_TASK_EXECUTOR,currentString);
            current[3]++;
            if(current[3]==256){
                current[3]=0;
                current[2]++;
                if(current[2]==256){
                    current[2]=0;
                    current[1]++;
                    if(current[1]==256){
                        current[1]=0;
                        current[0]++;
                        if(current[0]==233){
                            break;
                        }
                    }
                }
            }
        }
    }
    private static class IpTask extends AsyncTask<String, Void, Boolean> {
        String ip;
        Group group;
        public IpTask(Group group){
            super();
            this.group=group;
        }
        @Override
        protected Boolean doInBackground(String... params) {
//            System.err.println("正在ping ip:" + params[0]);
            ip=params[0];
            int reachCount = 0;
            try {
//                System.out.println("dd"+reachCount);
                address=InetAddress.getByName(params[0]);
                for (int i = 0; i < retries; ++i) {
                    if (address.isReachable(timeout)) {
                        ++reachCount;
//                        System.out.println(ip);
                    }
                }
                if(reachCount>0){
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            group.doneIpNums++;
            int progress=group.getProgress();
            group.setProgress(progress + 1);
            if(aBoolean){
                group.pingedIps.add(ip);
//                System.out.println(ip);
            }
//            System.out.println("ping通了的ip"+group.pingedIps);
//            System.out.println("完成ip数"+group.doneIpNums);
            if(group.doneIpNums==group.getTotalIp()){//ping结束,对ping通的设备进行设备类型发现
                List<String> pingedIps=group.pingedIps;
//                System.out.println(pingedIps);
                group.progressBarMax=pingedIps.size();
                group.setProgress(0);
                group.setSnmpDis(true);
                for(int i=0;i<pingedIps.size();i++){
                    SnmpTask snmpTask=new SnmpTask(group);
                    snmpTask.executeOnExecutor(LIMIT_TASK_EXECUTOR,pingedIps.get(i));
                }
            }
            Fragment_list.getAdapter().notifyDataSetChanged();
        }
    }

    private static class SnmpTask extends AsyncTask<String,Void,String>{
        String targetHost=null;
        int targetPort = 161;
        String community = "public";
        int timeout = 5;
        int retries = 0;
        String oid=".1.3.6.1.2.1.1.2.0";
        String deviceOid;
        Group group;
        public SnmpTask(Group group){
            super();
            this.group=group;
        }
        @Override
        protected String doInBackground(String... params) {
            targetHost=params[0];
            SnmpTarget target=new SnmpTarget();
            target.setTargetHost(targetHost);
            target.setTargetPort(targetPort);
            target.setCommunity(community);
            target.setTimeout(timeout);
            target.setRetries(retries);
            target.setSnmpVersion(SnmpTarget.VERSION1);
            target.setObjectID(oid);
            deviceOid=target.snmpGet();
            return deviceOid;
        }
        @Override
        protected void onPostExecute(String params) {
            String deviceType;
            if(deviceOid==null)
                deviceType="UnknownDevice";
            else {
                deviceType="SnmpDevice";
                String[] oidArray=deviceOid.split("\\.");
                String s1;
                if(oidArray.length>=10){
                    s1=oidArray[7]+"."+oidArray[8]+"."+oidArray[9];
                    if(Fragment_list.oidMap.containsKey(s1))
                        deviceType= Fragment_list.oidMap.get(s1);
                }
                else{
                    s1=oidArray[7]+"."+oidArray[8];
                    if(Fragment_list.oidMap.containsKey(s1))
                        deviceType= Fragment_list.oidMap.get(s1);
                }
            }
            Child child=new Child(deviceType,targetHost,deviceOid);
            mData.get(getIndex(group)).add(child);
            int progress=group.getProgress();
            group.setProgress(progress+1);
            db.execSQL("insert into device_info values(null,?,?,?)", new String[]{deviceType, targetHost, deviceOid});
            Fragment_list.getAdapter().notifyDataSetChanged();
        }
    }
    private static int getIndex(Group group){//根据groupName获取group，之所以不用index来索引，是因为在添加的同时删除会带来问题
        for(int i=groupList.size()-1;i>=0;i--){
            if(groupList.get(i)==group)
                return i;
        }
        return 0;
    }

}

