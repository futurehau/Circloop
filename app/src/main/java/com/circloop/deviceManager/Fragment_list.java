package com.circloop.deviceManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.circloop.database.Child;
import com.circloop.database.Group;
import com.circloop.database.MyDatabaseHelper;
import com.circloop.deviceDiscover.AutoDiscover;
import com.ryg.dynamicload.internal.DLIntent;
import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.utils.DLUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import static jxl.Workbook.getWorkbook;
/**
 * Created by zh on 2016/7/13.
 */
public class Fragment_list extends Fragment implements Adapter_ExpandableListView.OnEditGroupClickListener {
    private String TAG = "Fragment_list";
    public MyDatabaseHelper dbHelper;
    public static ExpandableListView mListView = null;
    private static Adapter_ExpandableListView mAdapter = null;
    private List<List<Child>> mData;
    private static List<Group> groupList;//数据库内所有分组数据存入groupList
    private HashMap<String, Child> deviceMap = new HashMap<String, Child>();//数据库内所有设备数据存入deviceMap,key为ip,值为Child
    public static Map<String, String> oidMap = new HashMap<String, String>();
    private HashMap<String, List<Interval>> ipSegMap;
    private int edit_group_positon = 0;//编辑分组时存储分组位置，以便后边删除这个位置的元素
    public boolean isVisible_batch_delete = false;//批量删除分组的勾选框是否可见
    public boolean isVisible_batch_delete_child = false;//批量删除孩子的勾选框是否可见

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //注意此处，如果使用replace来实现各个fragment的切换的话，那么要在此处重新初始化mData，否则出现闪退现象
        View view = inflater.inflate(R.layout.fg_list, container, false);
        findById(view);
        initData();
        initView(view);
        return view;
    }

    private void findById(View view){
        mListView = (ExpandableListView) view.findViewById(R.id.listView);
    }

    private void initView(View view) {
        //箭头设置
        mListView.setGroupIndicator(getResources().getDrawable(
                R.drawable.expander_floder));
        mAdapter = new Adapter_ExpandableListView((MainActivity)getActivity(),Fragment_list.this,getActivity(),groupList, mData);
        mListView.setAdapter(mAdapter);
        mListView.setDescendantFocusability(ExpandableListView.FOCUS_AFTER_DESCENDANTS);
        mListView.setChildDivider(getResources().getDrawable(R.drawable.child_divider));
        mListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                if (isVisible_batch_delete) {
                    for (int j = 0; j < groupList.size(); j++) {
                        mListView.collapseGroup(j);
                    }
                }
            }
        });
        mAdapter.setOnEditGroupClickListener(this);//接口回调
    }

    private void initData() {
        mData = new ArrayList<List<Child>>();
        dbHelper = MainActivity.dbHelper;

        initialOidMap();
        String sql_group = "select * from group_info";
        String sql_child = "select * from device_info";
        String sql_ip_seg = "select * from ip_seg_info";
        String sql_delete_devices = "select * from deleted_devices_info";

        //从数据库读取所有分组信息
        Cursor cursor = dbHelper.getWritableDatabase().rawQuery(sql_group, null);
        groupList = new ArrayList<Group>();
        boolean isGroup;
        while (cursor.moveToNext()) {
            if (cursor.getString(4).equals("true"))
                isGroup = true;
            else
                isGroup = false;
            Group group = new Group(cursor.getString(1), cursor.getString(2), false, 0, Integer.parseInt(cursor.getString(3)), isGroup);
            groupList.add(group);
        }
        //从数据库读取所有设备信息
        cursor = dbHelper.getWritableDatabase().rawQuery(sql_child, null);
        while (cursor.moveToNext()) {
            Child child = new Child(cursor.getString(1), cursor.getString(2), cursor.getString(3));
            deviceMap.put(cursor.getString(2), child);
        }
        Set<String> deciceIps = deviceMap.keySet();
        //从数据库读取所有ip段信息到一个一分组命为键的map中。
        cursor = dbHelper.getWritableDatabase().rawQuery(sql_ip_seg, null);
        ipSegMap = new HashMap<String, List<Interval>>();
        while (cursor.moveToNext()) {
            String group_name = cursor.getString(1);
            String enable = cursor.getString(4);
            if (enable.equals("false"))
                continue;
            Interval ip_seg = new Interval(new Ip(cursor.getString(2)), new Ip(cursor.getString(3)));

            if (!ipSegMap.containsKey(group_name)) {
                ipSegMap.put(group_name, new ArrayList<Interval>());
            }
            ipSegMap.get(group_name).add(ip_seg);
        }

        //从数据库中读取所有删除了的信息到一个HashMap中，键为分组名，值为删除了的设备
        HashMap<String, HashSet<String>> deletedDevices = new HashMap<>();
        cursor = dbHelper.getWritableDatabase().rawQuery(sql_delete_devices, null);
        while (cursor.moveToNext()) {
            String group_name = cursor.getString(1);
            String device_ip = cursor.getString(2);
            if (!deletedDevices.containsKey(group_name)) {
                deletedDevices.put(group_name, new HashSet<String>());
            }
            deletedDevices.get(group_name).add(device_ip);
        }

        //对每一个分组，首先找到分组中包含的ip地址，然后看看设备表中有没有这个ip，然后为了显示顺序还要看看是否知道这个设备的设备类型
        for (int i = 0; i < groupList.size(); i++) {
            List<Child> children = new ArrayList<Child>();
            if (groupList.get(i).getTotalIp() == 0) {
                mData.add(children);
                continue;
            }
            List<String> ips = getIpOfGroup(groupList.get(i).getGroupName(), deletedDevices);
            for (String ip : ips) {
                if (deciceIps.contains(ip)) {
                    if (!deviceMap.get(ip).getDeviceType().equals("UnknownDevice"))
                        children.add(0, deviceMap.get(ip));
                    else
                        children.add(deviceMap.get(ip));
                }
            }
            mData.add(children);
        }
    }

    private void initialOidMap() {
        try {
            Workbook book = getWorkbook(getContext().getResources().getAssets().open("oidList.xls"));
            Sheet sheet = book.getSheet(0);
            int rows = sheet.getRows();
            for (int i = 0; i < rows; i++) {
                oidMap.put(sheet.getCell(0, i).getContents(), sheet.getCell(1, i).getContents());
            }
            book.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }

    private List<String> getIpOfGroup(String group_name, HashMap<String, HashSet<String>> deletedDevices) {//获得一个分组内部的ip，需要把这个分组的每一个ip段内的ip都算上
        List<String> ips = new ArrayList<String>();
        List<Interval> ip_seg = ipSegMap.get(group_name);
        List<Interval> ip_seg_after_merge = merge(ip_seg);
        String fromIp;
        String toIp;
        for (Interval interval : ip_seg_after_merge) {
            fromIp = interval.getStart().toString();
            toIp = interval.getEnd().toString();
            if (fromIp.equals(toIp)) {//只有一个ip（设备）的时候需要单独处理，因为下边的程序都进不去while()循环。
                if (deletedDevices.get(group_name) == null || !deletedDevices.get(group_name).contains(fromIp)) {
                    ips.add(fromIp);
                }
                continue;
            }
            String[] begin = fromIp.split("\\.");
            int[] current = new int[4];
            for (int i = 0; i < 4; i++)
                current[i] = Integer.parseInt(begin[i]);
            String currentString = fromIp;
            while (!currentString.equals(toIp)) {
                currentString = current[0] + "." + current[1] + "." + current[2] + "." + current[3];
                if (deletedDevices.get(group_name) == null || !deletedDevices.get(group_name).contains(currentString)) {
                    ips.add(currentString);
                }
                current[3]++;
                if (current[3] == 256) {
                    current[3] = 0;
                    current[2]++;
                    if (current[2] == 256) {
                        current[2] = 0;
                        current[1]++;
                        if (current[1] == 256) {
                            current[1] = 0;
                            current[0]++;
                            if (current[0] == 233) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return ips;
    }


    private List<Interval> getMergeInterval(List<Interval> ip_seg_data, List<Boolean> isEnable) {
        List<Interval> tmp = new ArrayList<Interval>();
        for (int i = 0; i < ip_seg_data.size(); i++) {
            if (isEnable.get(i)) {
                tmp.add(ip_seg_data.get(i));
            }
        }
        if (tmp.size() == 0)
            return tmp;
        return merge(tmp);
    }

    // ip段融合，去除重复部分
    private List<Interval> merge(List<Interval> intervals) {
        List<Interval> res = new ArrayList<Interval>();
        if (intervals == null || intervals.size() == 0)
            throw new RuntimeException();
        Collections.sort(intervals, new Comparator<Interval>() {
            public int compare(Interval a, Interval b) {
                return a.start.compare(b.start);
            }
        });
        Ip start = intervals.get(0).start;
        Ip end = intervals.get(0).end;
        for (Interval interval : intervals) {
            if (interval.start.compare(end) <= 0) {
                end = end.compare(interval.end) > 0 ? end : interval.end;
            } else {
                Interval tmp = new Interval(start, end);
                res.add(tmp);
                start = interval.start;
                end = interval.end;
            }
        }
        Interval finalone = new Interval(start, end);
        res.add(finalone);
        return res;
    }

    public int countIp(String fromIp, String toIp) {//计算从fromIp到toIp包含的ip个数
        String[] begin = fromIp.split("\\.");
        String[] end = toIp.split("\\.");
        if (begin.length != 4 || end.length != 4 || fromIp.charAt(0) == '.' || toIp.charAt(0) == '.')
            return -1;
        int count = 1;
        int tmp = 256 * 256 * 256;
        for (int i = 0; i < 4; i++) {
            count += tmp * (Integer.parseInt(end[i]) - Integer.parseInt(begin[i]));
            tmp = tmp / 256;
        }
        return count;
    }

    public void set_isVisible_batch_delete(boolean isVisible_batch_delete, boolean isVisible_batch_delete_child) {
        this.isVisible_batch_delete = isVisible_batch_delete;
        this.isVisible_batch_delete_child = isVisible_batch_delete_child;
    }

    // 动态加载
    public boolean dynamicLoad() {
        //动态加载
        String pluginFolder = Environment.getExternalStorageDirectory() + "/DynamicLoadHost";
        File file = new File(pluginFolder);
        File[] plugins = file.listFiles();
        if (plugins == null || plugins.length == 0) {
            Toast.makeText(getContext(), "没有这个APk", Toast.LENGTH_SHORT).show();
            return false;
        }
        String launcherActivityName = null;
        File plugin = plugins[0];
        String pluginPath = plugin.getAbsolutePath();
        PackageInfo packageInfo = DLUtils.getPackageInfo(getContext(), pluginPath);
        if (packageInfo.activities != null && packageInfo.activities.length > 0) {
            launcherActivityName = packageInfo.activities[0].name;
        }
        DLPluginManager.getInstance(getActivity()).loadApk(pluginPath);
        DLPluginManager pluginManager = DLPluginManager.getInstance(getContext());
        pluginManager.startPluginActivity(getContext(), new DLIntent(packageInfo.packageName, launcherActivityName));
        return true;
    }

    // 编辑分组
    @Override
    public void onEditGroupClick(int groupPositon) {//接口回调方法，实现ExpandAdapter中点击编辑按钮时的接口回调方法。
        edit_group_positon = groupPositon;
        Intent intent;
        if (groupList.get(groupPositon).isGroup())
            intent = new Intent(getActivity(), Activity_Edit_Group.class);
        else
            intent = new Intent(getActivity(), Activity_Edit_Device.class);
        Bundle data = new Bundle();
        data.putString("groupName", groupList.get(groupPositon).getGroupName());
        intent.putExtras(data);
        startActivityForResult(intent, 0);
    }

    // 添加分组
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == 1) {
            String group_name = data.getStringExtra("groupName");
            String group_desc = data.getStringExtra("groupDesc");
            List<Interval> ip_seg_data = (ArrayList<Interval>) data.getSerializableExtra("intervalList");
            List<Boolean> ip_seg_enable = (ArrayList<Boolean>) data.getSerializableExtra("enableList");
            mData.remove(edit_group_positon);
            groupList.remove(edit_group_positon);
            addSucess(group_name, group_desc, ip_seg_data, ip_seg_enable, true);
        } else if (requestCode == 0 && resultCode == 2) {
            String group_name = data.getStringExtra("groupName");
            String group_desc = data.getStringExtra("groupDesc");
            List<Interval> ip_seg_data = (ArrayList<Interval>) data.getSerializableExtra("intervalList");
            List<Boolean> ip_seg_enable = (ArrayList<Boolean>) data.getSerializableExtra("enableList");
            mData.remove(edit_group_positon);
            groupList.remove(edit_group_positon);
            addSucess(group_name, group_desc, ip_seg_data, ip_seg_enable, false);
        } else {//未成功添加分组，不做任何处理
        }
    }

    public void addSucess(String group_name, String group_desc, List<Interval> ip_seg_data, List<Boolean> isEnable, boolean isGroup) {
        List<Interval> intervals = getMergeInterval(ip_seg_data, isEnable);//进行各个ip段的融合操作，避免段间重复的ip引发的bug
        int totalNum = 0;
        for (Interval interval : intervals) {
            totalNum += countIp(interval.start.toString(), interval.end.toString());
        }
        //添加的分组到这里才加入数据库，因为此时才知道有多少个ip
        dbHelper.getWritableDatabase().execSQL("insert into group_info values(null,?,?,?,?)", new String[]{group_name, group_desc, totalNum + "", isGroup + ""});
        Group group = new Group(group_name, group_desc, true, 0, totalNum, isGroup);
        group.progressBarMax = totalNum;
        groupList.add(group);
        mData.add(new ArrayList<Child>());
        mAdapter.notifyDataSetChanged();
        if (totalNum == 0)//处理当前分组内包含0个ip的情况
            return;
        AutoDiscover.scan(intervals, mData, group);//为了避免在同时添加两个分组时，前一个分组的数据添加到了后一个分组中,把组的位置也传过去。
        //之前是如上所示传递索引，但是存在添加的同时删除的bug，所以这里直接把group传递过去，需要做的只是在后边根据group
        //在groupList中的位置来决定child数据添加到mdata的哪一个位置。这样可同时添加多个，可同时添加删除，不会有bug
    }

    public static Adapter_ExpandableListView getAdapter() {
        return mAdapter;
    }

    public static List<Group> getGroupList() {
        return groupList;
    }

}
