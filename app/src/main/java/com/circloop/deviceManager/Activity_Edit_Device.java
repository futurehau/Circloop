package com.circloop.deviceManager;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.circloop.database.MyDatabaseHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zh on 2016/8/26.
 */
public class Activity_Edit_Device extends Activity implements View.OnClickListener {
    private TextView tv_device_name;
    private EditText edit_device_desc;
    private EditText edit_ip_address;
    private Button button_add;
    private Button button_cancel_change;
    private Button button_conform_change;
    private ListView ip_seg_listview;
    private String device_name;
    private String device_desc;

    private List<Interval> ip_seg_data;//记录添加的ip段
    private int ip_seg_num=0;
    private List<Boolean> ip_seg_enable;//记录每个ip段是否有效
    private List<Boolean> ip_seg_enable_test_change;
    private Adapter_ListView adapter;
    private Intent intent;

    MyDatabaseHelper dbHelper= MainActivity.dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.edit_device_layout);
        findById();
        button_add.setOnClickListener(this);
        button_cancel_change.setOnClickListener(this);
        button_conform_change.setOnClickListener(this);
        ip_seg_data=new ArrayList<Interval>();
        ip_seg_enable=new ArrayList<Boolean>();
        adapter=new Adapter_ListView(Activity_Edit_Device.this,ip_seg_data,ip_seg_enable);
        ip_seg_listview.setAdapter(adapter);
        getData();
    }
    private void findById(){
        tv_device_name= (TextView) findViewById(R.id.tv_device_name_edit);
        edit_device_desc= (EditText) findViewById(R.id.et_device_desc_edit);
        edit_ip_address= (EditText) findViewById(R.id.et_ip_address_edit);
        button_add= (Button) findViewById(R.id.btn_add_device_edit);
        ip_seg_listview= (ListView) findViewById(R.id.lv_ip_seg_device_edit);
        button_cancel_change= (Button) findViewById(R.id.btn_cancel_change_device);
        button_conform_change= (Button) findViewById(R.id.btn_conform_change_device);
    }
    private void getData(){
        intent = getIntent();
        Bundle data = intent.getExtras();
        device_name = data.getString("groupName");
        tv_device_name.setText(device_name);

        Cursor cursor=dbHelper.getWritableDatabase().rawQuery("select * from group_info where group_name='"+device_name+"'", null);
        while (cursor.moveToNext()){
            device_desc=cursor.getString(2);
        }
        if(!device_desc.equals(""))
            edit_device_desc.setText(device_desc);
        cursor=dbHelper.getWritableDatabase().rawQuery("select * from ip_seg_info where group_name='"+device_name+"'",null);
        String ip_begin;
        String ip_end;
        Interval interval;
        while (cursor.moveToNext()){
            if(cursor.getString(4).equals("true"))
                ip_seg_enable.add(true);
            else
                ip_seg_enable.add(false);
            ip_begin=cursor.getString(2);
            ip_end=cursor.getString(3);
            interval=new Interval(new Ip(ip_begin),new Ip(ip_end));
            ip_seg_data.add(interval);
        }
        ip_seg_num=ip_seg_data.size();
        ip_seg_enable_test_change=new ArrayList<Boolean>(ip_seg_enable);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_add_device_edit:
                addIpSeg(edit_ip_address.getText().toString(),edit_ip_address.getText().toString());
                break;
            case R.id.btn_cancel_change_device:
                cancelChange();
                break;
            case R.id.btn_conform_change_device:
                conformChange();
                break;
        }
    }
    private void addIpSeg(String ip_begin,String ip_end){
        if(checkInput()){
            if(Ip.isValidCIpAddress(ip_begin)){
                ip_seg_add_sucess(ip_begin,ip_end);
            }
            else Toast.makeText(this, "设备地址无效，请重新输入！", Toast.LENGTH_SHORT).show();
        }
    }
    private void ip_seg_add_sucess(String ip_begin,String ip_end){//点击继续添加之后,数据检查都正确后执行的操作
        Interval newSeg=new Interval(new Ip(ip_begin),new Ip(ip_end));
        ip_seg_data.add(newSeg);
        ip_seg_enable.add(true);
        adapter.notifyDataSetChanged();
        edit_ip_address.setText("");
    }
    private void cancelChange(){
        Activity_Edit_Device.this.finish();
    }
    private void conformChange(){
        if(ip_seg_num==ip_seg_data.size()){
            int i=0;
            for(;i<ip_seg_num;i++){
                if(ip_seg_enable.get(i)!=ip_seg_enable_test_change.get(i))
                    break;
            }
            if(i==ip_seg_num) {//ip段的数目一样，每个ip段的enable状态也一样，所以并没有改变此分组数据
                String new_group_desc=edit_device_desc.getText().toString();
                if(device_desc.equals(new_group_desc))
                    Toast.makeText(this, "当前设备数据没有改变！", Toast.LENGTH_SHORT).show();
                else{//只改变了分组描述
                    dbHelper.getWritableDatabase().execSQL("update group_info set group_desc ='"+new_group_desc+"' where group_name='"+device_name+"'");
                }
                Activity_Edit_Device.this.finish();//必须手动return,因为执行这个方法之后还会继续向下执行
                return;
            }
        }
        dbHelper.getWritableDatabase().execSQL("delete from group_info where group_name='"+device_name+"'");
        dbHelper.getWritableDatabase().execSQL("delete from ip_seg_info where group_name='"+device_name+"'");//删除ip段数据库中这个分组的数据
        OnConformButtonClicked();
    }
    private void OnConformButtonClicked(){
        Interval ipSeg;
        int size=ip_seg_data.size();
        boolean enable;
        for(int i=0;i<size;i++){
            ipSeg=ip_seg_data.get(i);
            enable=ip_seg_enable.get(i);
            insertIntoIpSegInfo(device_name.toString(), ipSeg.start.toString(), ipSeg.end.toString(), enable);//每一个ip段都插入ip段表中
        }
        intent.putExtra("groupName",device_name);
        intent.putExtra("groupDesc",device_desc);
        intent.putExtra("intervalList", (Serializable) ip_seg_data);
        intent.putExtra("enableList",(Serializable)ip_seg_enable);
        Activity_Edit_Device.this.setResult(2, intent);//默认的resultcode是0
        Activity_Edit_Device.this.finish();
    }
    private void insertIntoIpSegInfo(String group_name,String ip_begin,String ip_end,boolean enable){
        String enableString="false";
        if(enable)
            enableString="true";
        dbHelper.getWritableDatabase().execSQL("insert into ip_seg_info values(null,?,?,?,?)", new String[]{group_name, ip_begin, ip_end, enableString});
    }
    private boolean checkInput(){//检查是否输入完整
        if(TextUtils.isEmpty(edit_ip_address.getText().toString())){
            Toast.makeText(this, "请输入设备地址！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
