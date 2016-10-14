package com.circloop.deviceManager;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.circloop.database.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by zh on 2016/7/18.
 */
public class Fragment_add_device extends Fragment implements View.OnClickListener {
    private String TAG = "Fragment_add_device";
    private EditText et_device_name;
    private EditText et_device_desc;
    private View layout_add_device_ip_address;
    private EditText[] edit_ips_address;
    private Button btn_delete_all_ip_address;
    private Button button_continue_add;
    private Button button_finish_add;
    private String device_name;
    private String device_desc;
    private String ip_address;
    private ListView listView;
    private Adapter_ListView adapter;
    private List<Interval> ip_seg_data;
    private List<Boolean> ip_seg_enable;
    private HashSet<String> groups;
    MyDatabaseHelper dbHelper = MainActivity.dbHelper;

    private String ip;
    private Operation_Ip Operation_Ip;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_add_device_layout, container, false);
        findById(view);
        initial();
        onClickListener(view);
        return view;
    }


    private void findById(View view){
        et_device_name = (EditText) view.findViewById(R.id.et_device_name);
        et_device_desc = (EditText) view.findViewById(R.id.et_device_desc);
        //ip_address
        layout_add_device_ip_address = view.findViewById(R.id.add_device_ip_address);
        edit_ips_address = new EditText[4];
        edit_ips_address[0] = (EditText) layout_add_device_ip_address.findViewById(R.id.et_ip_0);
        edit_ips_address[1] = (EditText) layout_add_device_ip_address.findViewById(R.id.et_ip_1);
        edit_ips_address[2] = (EditText) layout_add_device_ip_address.findViewById(R.id.et_ip_2);
        edit_ips_address[3] = (EditText) layout_add_device_ip_address.findViewById(R.id.et_ip_3);
        btn_delete_all_ip_address = (Button) layout_add_device_ip_address.findViewById(R.id.btn_delete_all);
        listView= (ListView) view.findViewById(R.id.list_ip_seg_add_device);
    }
    private void initial() {
        ip_seg_data=new ArrayList<Interval>();
        ip_seg_enable=new ArrayList<Boolean>();
        adapter=new Adapter_ListView(this.getActivity(),ip_seg_data,ip_seg_enable);
        listView.setAdapter(adapter);
        Operation_Ip = new Operation_Ip(edit_ips_address,btn_delete_all_ip_address,getContext());
    }
    private void onClickListener(View view){
        setOnClickListener(view);
        button_continue_add.setOnClickListener(this);
        button_finish_add.setOnClickListener(this);
    }

    private void setOnClickListener(View view){
        button_continue_add= (Button) view.findViewById(R.id.btn_continue_add_device);
        button_finish_add= (Button) view.findViewById(R.id.btn_finish_add_device);
        Operation_Ip.ip_setOnClickListener();
    }


    @Override
    public void onClick(View view) {
        getGroupNames();
        switch (view.getId()){
            case R.id.btn_continue_add_device:
                onContinueButtonClicked();
                break;
            case R.id.btn_finish_add_device:
                onFinishButtonClicked();
                break;
        }
    }
    private void getGroupNames(){//获取数据库中已有的分组名
        Cursor cursor=dbHelper.getWritableDatabase().rawQuery("select * from group_info", null);
        groups=new HashSet<String>();
        while (cursor.moveToNext()){
            groups.add(cursor.getString(1));
        }
    }

    private void onContinueButtonClicked(){
        ip = Operation_Ip.getIpString();
        insertSegData(ip);
    }
    private void insertSegData(String ip){
        if(checkInput()){
            if(groups.contains(et_device_name.getText().toString()))
                Toast.makeText(getActivity(), "分组已存在，请重新输入！", Toast.LENGTH_SHORT).show();
            else{
                if(Ip.isValidCIpAddress(ip)) {
                    ip_add_sucess();
                }
                else Toast.makeText(getActivity(), "Ip地址无效，请重新输入！", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void ip_add_sucess(){
        Interval newSeg=new Interval(new Ip(ip),new Ip(ip));
        ip_seg_data.add(newSeg);
        ip_seg_enable.add(true);
        adapter.notifyDataSetChanged();
        Operation_Ip.clearIp();
    }

    private void onFinishButtonClicked(){
        Interval ipSeg;
        boolean enable;
        //这里需要处理一种情况，用户在添加了一些ip段之后修改名字，导致与数据库中名字重复
        if(groups.contains(et_device_name.getText().toString())) {
            Toast.makeText(getActivity(), "设备已存在，请重新输入！", Toast.LENGTH_SHORT).show();
            return;
        }
        //处理用户在输入数据后直接点击完成添加而不点击继续添加的情况
        if(!et_device_name.getText().toString().equals("")){//ip数据不为空，表示没有点击继续添加
            onContinueButtonClicked();
        }
        //处理用户完全没有加入ip和用户虽然加入了ip段但是全都变成无效的情况。
        int num=0;
        int size=ip_seg_data.size();
        for(int i=0;i<size;i++){
            if(ip_seg_enable.get(i))
                num++;
        }
        if(num==0){//没有有效ip
            Toast.makeText(getActivity(), "当前设置下,没有有效ip,请重新设置", Toast.LENGTH_SHORT).show();
            return;
        }
        for(int i=0;i<size;i++){
            ipSeg=ip_seg_data.get(i);
            enable=ip_seg_enable.get(i);
            insertIntoIpSegInfo(et_device_name.getText().toString(), ipSeg.start.toString(), ipSeg.end.toString(), enable);//每一个ip段都插入ip段表中
        }
        //注意这里先不把添加的分组加到数据库中，等到Fragment_list中计算出有多少个ip之后再加入数据库
//        insertIntoGroupInfo(et_group_name.getText().toString(), et_group_desc.getText().toString());//所有ip段添加结束后，分组加入分组表中
        Toast.makeText(getActivity(), "添加设备成功！", Toast.LENGTH_SHORT).show();
        callback();//设备添加成功，回调，在MainActivity中更新界面显示为fg_list，并且在MainActivity的回调方法中调用fg_add的addSucess()方法来具体发现设备
        clear();//清除屏幕数据
    }
    private void insertIntoIpSegInfo(String group_name,String ip_begin,String ip_end,boolean enable){
        String enableString="false";
        if(enable)
            enableString="true";
        dbHelper.getWritableDatabase().execSQL("insert into ip_seg_info values(null,?,?,?,?)", new String[]{group_name, ip_begin, ip_end, enableString});
    }
    private void callback() {
        if (getActivity() instanceof AddDeviceBtnClickListener)
            ((AddDeviceBtnClickListener) getActivity()).onAddDeviceClick(et_device_name.getText().toString(),et_device_desc.getText().toString(),
                    new ArrayList<Interval>(ip_seg_data),new ArrayList<Boolean>(ip_seg_enable));
    }

    private boolean checkInput() {//检查是否输入完整
        if (TextUtils.isEmpty(et_device_name.getText().toString())) {
            Toast.makeText(getActivity(), "请输入设备名称！", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(Operation_Ip.getIpString())) {
            Toast.makeText(getActivity(), "请输入设备地址！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    public interface AddDeviceBtnClickListener{
        void onAddDeviceClick(String group_name,String group_desc,List<Interval> ip_seg_data,List<Boolean> ip_seg_enable);
    }

    private void clear(){
        et_device_name.setText("");
        et_device_desc.setText("");
        Operation_Ip.clearIp();
        ip_seg_data.clear();//注意这里不能用ip_seg_data=new ArrayList<>(),因为适配器其实找到的是先前那个地址上的list.
        ip_seg_enable.clear();
        adapter.notifyDataSetChanged();//添加完成后，更新listview显示
    }
//    //四位Ip数据清除
//    private String getIpString(EditText[] edit_ips){
//        String ip="";
//        for(int i = 0; i < 3; i++) {
//            ip +=edit_ips[i].getText().toString();
//            ip +=".";
//        }
//        ip +=edit_ips[3].getText().toString();
//        return ip;
//    }
//    private void clearIp(EditText[] edit_ips,Button button){
//        for(int i = 0; i < 4; i++) {
//            edit_ips[i].setText("");
//        }
//        button.setVisibility(View.INVISIBLE);
//    }
//    private void ip_setOnClickListener( final EditText[] edit_ips, final Button button) {
//        for (int i = 0; i < 4; i++) {
//            edit_ips[i].addTextChangedListener(new IpTextWatcher(edit_ips_address[i], edit_ips,this));
//            final int curIndex = i;
//            edit_ips[i].setOnKeyListener(new View.OnKeyListener() {
//                @Override
//                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {//检测删除键，为了处理没有数字加入时，删除键回不到前一个EditText的问题
//                    if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
//                        if (curIndex != 0 && edit_ips[curIndex].getText().length() == 0) {
//                            edit_ips[curIndex].clearFocus();
//                            edit_ips[curIndex - 1].requestFocus();
//                            return true;
//                        }
//                    }
//                    return false;
//                }
//            });
//            edit_ips[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View view, boolean hasfocus) {
//                    if (!hasfocus) {//失去焦点时检测是否是一个有效数
//                        button.setVisibility(View.INVISIBLE);
//                        if (edit_ips[curIndex].getText().length() != 0) {//首先应该判断是否为空，否则有bug
//                            if (Integer.valueOf(edit_ips[curIndex].getText().toString()) > 255) {
//
//                                edit_ips[curIndex].setTextColor(getResources().getColor(R.color.textColorError));
//                            } else {
//
//                                edit_ips[curIndex].setTextColor(getResources().getColor(R.color.textColorBlack));
//                            }
//                        }
//                    } else {
//                        if (curIndex != 0) {
//                            button.setVisibility(View.VISIBLE);
//                        }
//                    }
//                }
//            });
//        }
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                for (int i = 0; i < 4; i++) {
//                    edit_ips[i].setText("");
//                    edit_ips[i].setTextColor(getResources().getColor(R.color.textColorBlack));
//                }
//                edit_ips[0].requestFocus();
//                button.setVisibility(View.INVISIBLE);
//            }
//        });
//    }
//    @Override
//    public void finalEditTextHasError(EditText editText) {
//        if ( editText == edit_ips_address[3]){
//            edit_ips_address[3].setTextColor(getResources().getColor(R.color.textColorError));
//        }else{
//            edit_ips_address[3].setTextColor(getResources().getColor(R.color.textColorError));
//        }
//    }
//
//    @Override
//    public void finalEditTextClearError(EditText editText) {
//        if ( editText == edit_ips_address[3]){
//            edit_ips_address[3].setTextColor(getResources().getColor(R.color.textColorBlack));
//        }
//        else {
//            edit_ips_address[3].setTextColor(getResources().getColor(R.color.textColorBlack));
//        }
//    }

}


