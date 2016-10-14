package com.circloop.deviceManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
 * Created by zh on 2016/7/15.
 */
public class Fragment_add_group extends Fragment implements View.OnClickListener{
    //group_name
    private View layout_group_name;
    private EditText et_group_name;
    private Button btn_delete_add_group_name;
    private Operation_group_name operation_group_name;
    //group_desc
    private View layout_group_desc;
    private EditText et_group_desc;
    private Button btn_delete_add_group_desc;
    private Operation_group_desc operation_group_desc;


    private View layout_ip_begin;
    private View layout_ip_end;
    private EditText[] edit_ips_begin;
    private EditText[] edit_ips_end;
    private Button btn_delete_add_ip_begin;
    private Button btn_delete_add_ip_end;
    private Operation_Ip ip_begin_operation;
    private Operation_Ip ip_end_operation;
    private Button continue_add_button;
    private Button finish_add_button;
    private ListView ip_seg_list;
    private String group_name;
    private String group_desc;
    private String ip_begin;
    private String ip_end;
    private int ipNums=0;
    private List<Interval> ip_seg_data;//记录添加的ip段
    private List<Boolean> ip_seg_enable;//记录每个ip段是否有效
    private Adapter_ListView adapter;
    private  HashSet<String> groups;//当前数据库中包含了的分组名，不能重复
    private MyDatabaseHelper dbHelper= MainActivity.dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fg_add_group_layout,container,false);
        findById(view);
        initial();
        setOnClickListener();
        return view;
    }
    private void findById(View view){
        //group_name
        layout_group_name = view.findViewById(R.id.add_group_group_name);
        et_group_name= (EditText)layout_group_name.findViewById(R.id.et_group_name);
        btn_delete_add_group_name = (Button) layout_group_name.findViewById(R.id.btn_delete_all_group_name);
        //group_desc
        layout_group_desc = view.findViewById(R.id.add_group_group_desc);
        et_group_desc= (EditText)layout_group_desc.findViewById(R.id.et_group_desc);
        btn_delete_add_group_desc = (Button) layout_group_desc.findViewById(R.id.btn_delete_all_group_desc);

        //ip_begin
        layout_ip_begin = view.findViewById(R.id.add_group_ip_begin);
        edit_ips_begin = new EditText[4];
        edit_ips_begin[0] = (EditText) layout_ip_begin.findViewById(R.id.et_ip_0);
        edit_ips_begin[1] = (EditText) layout_ip_begin.findViewById(R.id.et_ip_1);
        edit_ips_begin[2] = (EditText) layout_ip_begin.findViewById(R.id.et_ip_2);
        edit_ips_begin[3] = (EditText) layout_ip_begin.findViewById(R.id.et_ip_3);
        btn_delete_add_ip_begin = (Button) layout_ip_begin.findViewById(R.id.btn_delete_all);
        //ip_end
        layout_ip_end =view.findViewById(R.id.add_group_ip_end);
        edit_ips_end = new EditText[4];
        edit_ips_end[0] = (EditText) layout_ip_end.findViewById(R.id.et_ip_0);
        edit_ips_end[1] = (EditText) layout_ip_end.findViewById(R.id.et_ip_1);
        edit_ips_end[2] = (EditText) layout_ip_end.findViewById(R.id.et_ip_2);
        edit_ips_end[3] = (EditText) layout_ip_end.findViewById(R.id.et_ip_3);
        btn_delete_add_ip_end = (Button) layout_ip_end.findViewById(R.id.btn_delete_all);

        continue_add_button= (Button) view.findViewById(R.id.btn_continue_add_group);
        finish_add_button= (Button) view.findViewById(R.id.btn_finish_add_group);
        ip_seg_list= (ListView) view.findViewById(R.id.list_ip_seg);
    }
    private void initial(){
        ip_seg_data=new ArrayList<Interval>();
        ip_seg_enable=new ArrayList<Boolean>();
        adapter=new Adapter_ListView(this.getActivity(),ip_seg_data,ip_seg_enable);//布局下部是一个记录当前分组的ip段的可以上下滑动的listview,传入ip_seg_enable是为了在这个ip段有效无效被选中时进行设置
        ip_seg_list.setAdapter(adapter);
        ip_begin_operation = new Operation_Ip(edit_ips_begin, btn_delete_add_ip_begin, getContext());
        ip_end_operation = new Operation_Ip(edit_ips_end, btn_delete_add_ip_end, getContext());
        operation_group_name = new Operation_group_name(et_group_name,btn_delete_add_group_name,getContext());
        operation_group_desc = new Operation_group_desc(et_group_desc,btn_delete_add_group_desc,getContext());
    }
    private void setOnClickListener(){
        continue_add_button.setOnClickListener(this);
        finish_add_button.setOnClickListener(this);
        ip_begin_operation.ip_setOnClickListener();
        ip_end_operation.ip_setOnClickListener();
        operation_group_name.groupNamesetOnClickListener();
        operation_group_desc.groupDescSetOnClickListener();
        et_group_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (groups == null || editable.length() == 0) {
                    et_group_name.setTextColor(getResources().getColor(R.color.textColorBlack));
                } else if (!groups.contains(editable.toString())) {
                    et_group_name.setTextColor(getResources().getColor(R.color.textColorBlack));
                } else {
                    Log.e("afterTextChanged()", "adsdsdsassssss");
                    et_group_name.setTextColor(getResources().getColor(R.color.textColorError));
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        getGroupNames();//每次点击之后都应该重新获取数据库中的分组名，否则放在onCreateView中调用一次的话，之后再进来不会再更新，也就是说前边的添加和外边的删除在这里不会得到更新
        switch (view.getId()){
            case R.id.btn_continue_add_group:
                onContinueButtonClicked();//点击继续添加下一个ip段
                break;
            case R.id.btn_finish_add_group:
                onFinishButtonClicked();//点击完成添加当前分组的所有ip段
                break;
        }
    }
    private void onContinueButtonClicked(){
        //清除焦点之后有错时才可以正确获取焦点，显示错误提示信息
        et_group_name.clearFocus();
        et_group_desc.clearFocus();
        for (int i = 0; i < 4; i++) {
            edit_ips_begin[i].clearFocus();
            edit_ips_end[i].clearFocus();
        }
        ip_begin = ip_begin_operation.getIpString();
        ip_end = ip_end_operation.getIpString();
        insertSegData(ip_begin, ip_end);
    }
    private void onFinishButtonClicked(){
        et_group_name.clearFocus();
        et_group_desc.clearFocus();
        for (int i = 0; i < 4; i++) {
            edit_ips_begin[i].clearFocus();
            edit_ips_end[i].clearFocus();
        }
        Interval ipSeg;
        boolean enable;
        //这里需要处理一种情况，用户在添加了一些ip段之后修改名字，导致与数据库中名字重复
        if(groups.contains(et_group_name.getText().toString())) {
            groupHasExist();
            return;
        }

        //处理用户在输入数据后直接点击完成添加而不点击继续添加的情况
        if(!ip_begin_operation.getIpString().equals("")){//ip数据不为空，表示没有点击继续添加
            onContinueButtonClicked();
        }
        //处理用户完全没有加入ip段和用户虽然加入了ip段但是全都变成无效的情况。
        int num=0;
        int size=ip_seg_data.size();
        for(int i=0;i<size;i++){
            if(ip_seg_enable.get(i))
                num++;
        }
        if(num==0){//没有有效ip段
            Toast.makeText(getActivity(), "当前设置下,分组内没有有效ip,请重新设置", Toast.LENGTH_SHORT).show();
            return;
        }
        for(int i=0;i<size;i++){
            ipSeg=ip_seg_data.get(i);
            enable=ip_seg_enable.get(i);
            insertIntoIpSegInfo(et_group_name.getText().toString(), ipSeg.start.toString(), ipSeg.end.toString(), enable);//每一个ip段都插入ip段表中
        }
        //注意这里先不把添加的分组加到数据库中，等到Fragment_list中计算出有多少个ip之后再加入数据库
//        insertIntoGroupInfo(et_group_name.getText().toString(), et_group_desc.getText().toString());//所有ip段添加结束后，分组加入分组表中
        Toast.makeText(getActivity(), "添加分组成功！", Toast.LENGTH_SHORT).show();
        callback();//分组添加成功，回调，在MainActivity中更新界面显示为fg_list，并且在MainActivity的回调方法中调用fg_add的addSucess()方法来具体发现设备
        clear();//清除屏幕数据
    }
    private void groupHasExist(){
        Log.e("groupHasExist()", "adsdsdsassssss");
        Drawable drawable = getResources().getDrawable(R.drawable.edit_text_delete);
        drawable.setBounds(0, 0, 10, 10);

//        et_group_name.setError(Html.fromHtml("<body bgcolor=\"#FF4081\"></body>"));
        et_group_name.setDrawingCacheBackgroundColor(getResources().getColor(R.color.textColorError));


        et_group_name.setError("分组已存在", drawable);
        et_group_name.setTextColor(getResources().getColor(R.color.textColorError));
        btn_delete_add_group_name.setVisibility(View.VISIBLE);
//        Toast.makeText(getActivity(), "分组已存在，请重新输入！", Toast.LENGTH_SHORT).show();
    }

    private void getGroupNames(){//获取数据库中已有的分组名
        Cursor cursor=dbHelper.getWritableDatabase().rawQuery("select * from group_info", null);
        groups=new HashSet<String>();
        while (cursor.moveToNext()){
            groups.add(cursor.getString(1));
        }
    }
    private void insertIntoIpSegInfo(String group_name,String ip_begin,String ip_end,boolean enable){
        String enableString="false";
        if(enable)
            enableString="true";
        dbHelper.getWritableDatabase().execSQL("insert into ip_seg_info values(null,?,?,?,?)", new String[]{group_name, ip_begin, ip_end, enableString});
    }

    //新添加的ip段加入ip_seg_data
    private void insertSegData(String ip_begin,String ip_end){
        if(checkInput()){
            if(groups.contains(et_group_name.getText().toString()))
                groupHasExist();
            else{
                if(Ip.isValidCIpAddress(ip_begin)){
                    if(Ip.isValidCIpAddress(ip_end)){
                        ipNums=Ip.countIp(ip_begin,ip_end);
                        if(ipNums>0&&ipNums<=256){
                            ip_seg_add_sucess();
                        }
                        else if(ipNums<0) {
                            edit_ips_end[3].requestFocus();
                            edit_ips_end[3].setError("开始地址必须小于等于结束地址");
//                            Toast.makeText(getActivity(), "请输入有效ip地址！", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            AlertDialog tooMuchIp=new AlertDialog.Builder(getActivity()).create();
                            tooMuchIp.setTitle("系统提示：");
                            tooMuchIp.setMessage("当前ip范围包含" + ipNums + "个ip地址,导致系统响应时间过长，确定要添加吗？");
                            tooMuchIp.setButton("确定", listener1);
                            tooMuchIp.setButton2("取消", listener1);
                            tooMuchIp.show();
                        }
                    }
                    else {
                        edit_ips_end[3].requestFocus();
                        edit_ips_end[3].setError("结束地址无效");
//                        Toast.makeText(getActivity(), "结束地址无效，请重新输入！", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    edit_ips_begin[3].requestFocus();
                    edit_ips_begin[3].setError("开始地址无效");
//                    Toast.makeText(getActivity(), "起始地址无效，请重新输入！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void ip_seg_add_sucess(){//点击继续添加之后,数据检查都正确后执行的操作
        Interval newSeg=new Interval(new Ip(ip_begin),new Ip(ip_end));
        ip_seg_data.add(newSeg);
        ip_seg_enable.add(true);
        adapter.notifyDataSetChanged();
        clear1();
    }

    private void callback(){
        if(getActivity() instanceof AddGroupBtnClickListener)
            ((AddGroupBtnClickListener)getActivity()).onAddGroupClick(et_group_name.getText().toString(),et_group_desc.getText().toString(),new ArrayList<Interval>(ip_seg_data),new ArrayList<Boolean>(ip_seg_enable));
    }

    private boolean checkInput(){//检查是否输入完整
        if(TextUtils.isEmpty(et_group_name.getText().toString())){
            et_group_name.setError("分组名称不能为空");
            et_group_name.setTextColor(getResources().getColor(R.color.textColorError));
//            Toast.makeText(getActivity(), "请输入分组名称！", Toast.LENGTH_SHORT).show();
            return false;
        }
//        else if(TextUtils.isEmpty(ip_begin_operation.getIpString())){
//            edit_ips_begin[0].setError("开始地址不能为空");
//            et_group_name.setTextColor(getResources().getColor(R.color.textColorError));
//            Toast.makeText(getActivity(), "请输入开始地址！", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        else if(TextUtils.isEmpty(ip_end_operation.getIpString())){
//            edit_ips_end[0].setError("结束地址不能为空");
//            et_group_name.setTextColor(getResources().getColor(R.color.textColorError));
//            Toast.makeText(getActivity(), "请输入结束地址！", Toast.LENGTH_SHORT).show();
//            return false;
//        }
        return true;
    }

    private void clear(){
        et_group_name.setText("");
        et_group_desc.setText("");
        clear1();
        ip_seg_data.clear();//注意这里不能用ip_seg_data=new ArrayList<>(),因为适配器其实找到的是先前那个地址上的list.
        ip_seg_enable.clear();
        adapter.notifyDataSetChanged();//添加完成后，更新listview显示
    }

    private void clear1(){
        ip_begin_operation.clearIp();
        ip_end_operation.clearIp();
    }


    DialogInterface.OnClickListener listener1 = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    ip_seg_add_sucess();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };

    public interface AddGroupBtnClickListener{
        void onAddGroupClick(String group_name,String group_desc,List<Interval> ip_seg_data,List<Boolean> isEnable);
    }
}
