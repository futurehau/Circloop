package com.circloop.deviceManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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


public class Activity_Edit_Group extends Activity implements View.OnClickListener {
    private TextView tv_group_name;
    private EditText edit_group_desc;

    private View layout_ip_begin;
    private View layout_ip_end;
    private EditText[] edit_ips_begin;
    private EditText[] edit_ips_end;
    private Button btn_delete_add_ip_begin;
    private Button btn_delete_add_ip_end;
    private Operation_Ip ip_begin_operation;
    private Operation_Ip ip_end_operation;

    private Button button_add;
    private Button button_cancel_change;
    private Button button_conform_change;
    private ListView ip_seg_listview;
    private String group_name;
    private String group_desc;

    private List<Interval> ip_seg_data;//记录添加的ip段
    private int ip_seg_num=0;
    private List<Boolean> ip_seg_enable;//记录每个ip段是否有效
    private List<Boolean> ip_seg_enable_test_change;
    private Adapter_ListView adapter;
    private Intent intent;

    private boolean ip_seg_changed; // ip段是否改变

    MyDatabaseHelper dbHelper= MainActivity.dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.edit_group_layout);
        findById();
        initial();
        setOnClickListener();
    }

    private void findById(){
        tv_group_name= (TextView) findViewById(R.id.tv_group_name_edit);
        edit_group_desc= (EditText) findViewById(R.id.et_group_desc_edit);
        //ip_begin
        layout_ip_begin = findViewById(R.id.edit_group_ip_begin);
        edit_ips_begin = new EditText[4];
        edit_ips_begin[0] = (EditText) layout_ip_begin.findViewById(R.id.et_ip_0);
        edit_ips_begin[1] = (EditText) layout_ip_begin.findViewById(R.id.et_ip_1);
        edit_ips_begin[2] = (EditText) layout_ip_begin.findViewById(R.id.et_ip_2);
        edit_ips_begin[3] = (EditText) layout_ip_begin.findViewById(R.id.et_ip_3);
        btn_delete_add_ip_begin = (Button) layout_ip_begin.findViewById(R.id.btn_delete_all);
        //ip_end
        layout_ip_end = findViewById(R.id.edit_group_ip_end);
        edit_ips_end = new EditText[4];
        edit_ips_end[0] = (EditText) layout_ip_end.findViewById(R.id.et_ip_0);
        edit_ips_end[1] = (EditText) layout_ip_end.findViewById(R.id.et_ip_1);
        edit_ips_end[2] = (EditText) layout_ip_end.findViewById(R.id.et_ip_2);
        edit_ips_end[3] = (EditText) layout_ip_end.findViewById(R.id.et_ip_3);
        btn_delete_add_ip_end = (Button) layout_ip_end.findViewById(R.id.btn_delete_all);
        button_add= (Button) findViewById(R.id.btn_add_edit);
        ip_seg_listview= (ListView) findViewById(R.id.lv_ip_seg_edit);
        button_cancel_change= (Button) findViewById(R.id.btn_cancel_change);
        button_conform_change= (Button) findViewById(R.id.btn_conform_change);
    }

    private void setOnClickListener(){
        button_add.setOnClickListener(this);
        button_cancel_change.setOnClickListener(this);
        button_conform_change.setOnClickListener(this);

        ip_begin_operation.ip_setOnClickListener();
        ip_end_operation.ip_setOnClickListener();
    }

    private void initial(){
        ip_seg_data=new ArrayList<Interval>();
        ip_seg_enable=new ArrayList<Boolean>();
        adapter=new Adapter_ListView(Activity_Edit_Group.this,ip_seg_data,ip_seg_enable, Activity_Edit_Group.this);
        ip_seg_listview.setAdapter(adapter);
        getData();
        ip_begin_operation = new Operation_Ip(edit_ips_begin, btn_delete_add_ip_begin, Activity_Edit_Group.this);
        ip_end_operation = new Operation_Ip(edit_ips_end, btn_delete_add_ip_end, Activity_Edit_Group.this);
    }

    private void getData(){
        intent = getIntent();
        Bundle data = intent.getExtras();
        group_name = data.getString("groupName");
        tv_group_name.setText(group_name);

        Cursor cursor=dbHelper.getWritableDatabase().rawQuery("select * from group_info where group_name='"+group_name+"'", null);
        while (cursor.moveToNext()){
            group_desc=cursor.getString(2);
        }
        if(!group_desc.equals(""))
            edit_group_desc.setText(group_desc);
        cursor=dbHelper.getWritableDatabase().rawQuery("select * from ip_seg_info where group_name='"+group_name+"'",null);
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
            case R.id.btn_add_edit:
                addIpSeg(ip_begin_operation.getIpString(),ip_end_operation.getIpString());
                break;
            case R.id.btn_cancel_change:
                cancelChange();
                break;
            case R.id.btn_conform_change:
                conformChange();
                break;
        }
    }

    private void addIpSeg(String ip_begin,String ip_end){
        int ipNums;
        if(checkInput()){
            if(Ip.isValidCIpAddress(ip_begin)){
                if(Ip.isValidCIpAddress(ip_end)){
                    ipNums=Ip.countIp(ip_begin, ip_end);
                    if(ipNums>0&&ipNums<=256){
                        ip_seg_add_sucess(ip_begin,ip_end);
                    }
                    else if(ipNums<0)
                        Toast.makeText(this, "请输入有效ip地址！", Toast.LENGTH_SHORT).show();
                    else{
                        AlertDialog tooMuchIp=new AlertDialog.Builder(this).create();
                        tooMuchIp.setTitle("系统提示：");
                        tooMuchIp.setMessage("当前ip范围包含" + ipNums + "个ip地址,导致系统响应时间过长，确定要添加吗？");
                        tooMuchIp.setButton("确定", listener1);
                        tooMuchIp.setButton2("取消", listener1);
                        tooMuchIp.show();
                    }
                }
                else Toast.makeText(this, "结束地址无效，请重新输入！", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(this, "起始地址无效，请重新输入！", Toast.LENGTH_SHORT).show();
        }
    }

    private void ip_seg_add_sucess(String ip_begin,String ip_end){//点击继续添加之后,数据检查都正确后执行的操作
        Interval newSeg=new Interval(new Ip(ip_begin),new Ip(ip_end));
        ip_seg_data.add(newSeg);
        ip_seg_enable.add(true);
        adapter.notifyDataSetChanged();
        ip_begin_operation.clearIp();
        ip_end_operation.clearIp();
    }

    private void cancelChange(){
        Activity_Edit_Group.this.finish();
    }

    private void conformChange(){
        if(!ip_seg_changed && ip_seg_num==ip_seg_data.size()){
            int i = 0;
            for(;i<ip_seg_num;i++){
               if(ip_seg_enable.get(i)!=ip_seg_enable_test_change.get(i))
                   break;
            }
            if(i==ip_seg_num) {//ip段的数目一样，每个ip段的enable状态也一样，所以并没有改变此分组数据
                String new_group_desc=edit_group_desc.getText().toString();
                if(group_desc.equals(new_group_desc))
                    Toast.makeText(this, "当前分组数据没有改变！", Toast.LENGTH_SHORT).show();
                else{//只改变了分组描述
                    dbHelper.getWritableDatabase().execSQL("update group_info set group_desc ='"+new_group_desc+"' where group_name='"+group_name+"'");
                }
                Activity_Edit_Group.this.finish();//必须手动return,因为执行这个方法之后还会继续向下执行
                return;
            }
        }
        dbHelper.getWritableDatabase().execSQL("delete from group_info where group_name='"+group_name+"'");
        dbHelper.getWritableDatabase().execSQL("delete from ip_seg_info where group_name='"+group_name+"'");//删除ip段数据库中这个分组的数据
        dbHelper.getWritableDatabase().execSQL("delete from deleted_devices_info where group_name ='" + group_name + "'");//删除delete_devices表中关于这个分组的数据
        OnConformButtonClicked();
    }

    private void OnConformButtonClicked(){
        Interval ipSeg;
        int size=ip_seg_data.size();
        boolean enable;
        for(int i=0;i<size;i++){
            ipSeg=ip_seg_data.get(i);
            enable=ip_seg_enable.get(i);
            insertIntoIpSegInfo(group_name.toString(), ipSeg.start.toString(), ipSeg.end.toString(), enable);//每一个ip段都插入ip段表中
        }
        intent.putExtra("groupName",group_name);
        intent.putExtra("groupDesc",group_desc);
        intent.putExtra("intervalList", (Serializable) ip_seg_data);
        intent.putExtra("enableList", (Serializable)ip_seg_enable);
        Activity_Edit_Group.this.setResult(1, intent);//默认的resultcode是0
        Activity_Edit_Group.this.finish();
    }

    private void insertIntoIpSegInfo(String group_name,String ip_begin,String ip_end,boolean enable){
        String enableString="false";
        if(enable)
            enableString="true";
        dbHelper.getWritableDatabase().execSQL("insert into ip_seg_info values(null,?,?,?,?)", new String[]{group_name, ip_begin, ip_end, enableString});
    }

    private boolean checkInput(){//检查是否输入完整
        if(TextUtils.isEmpty(ip_begin_operation.getIpString())){
            Toast.makeText(this, "请输入开始地址！", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(TextUtils.isEmpty(ip_end_operation.getIpString())){
            Toast.makeText(this, "请输入结束地址！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void set_ip_seg_changed(boolean changed) {
        ip_seg_changed = changed;
    }

    DialogInterface.OnClickListener listener1 = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    ip_seg_add_sucess(ip_begin_operation.getIpString(),ip_end_operation.getIpString());
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };
}
