package com.circloop.deviceManager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by zh on 2016/8/17.
 */
public class Adapter_ListView extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Interval> ip_seg_list;
    private List<Boolean> ip_seg_enable;
    private List<ViewGroup_SlideDelete> slideDeleteArrayList = new ArrayList<ViewGroup_SlideDelete>(); // 记录侧滑界面是否显示
    private Activity_Edit_Group activity_edit_group;
    public Adapter_ListView(Context context, List<Interval> ip_seg_list, List<Boolean> ip_seg_enable){
        this.ip_seg_list=ip_seg_list;
        this.ip_seg_enable=ip_seg_enable;
        this.mInflater=LayoutInflater.from(context);
        activity_edit_group = null;
    }
    public Adapter_ListView(Context context, List<Interval> ip_seg_list, List<Boolean> ip_seg_enable, Activity_Edit_Group activity_edit_group){
        this.ip_seg_list=ip_seg_list;
        this.ip_seg_enable=ip_seg_enable;
        this.mInflater=LayoutInflater.from(context);
        this.activity_edit_group = activity_edit_group;
    }

    @Override
    public int getCount() {
        return ip_seg_list.size();
    }

    @Override
    public Object getItem(int i) {
        return ip_seg_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final int index=i;
        view = mInflater.inflate(R.layout.ip_seg_slide_layout,null);

        ViewGroup_SlideDelete mSlideDelete = (ViewGroup_SlideDelete) view.findViewById(R.id.mSlideDelete_ip_seg);
        LinearLayout slide_delete = (LinearLayout) view.findViewById(R.id.del_layout_ip_seg);

        TextView textView = (TextView) view.findViewById(R.id.tv);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.rg);

        Interval ipSeg=ip_seg_list.get(i);
        if(ipSeg.start.toString().equals(ipSeg.end.toString()))//为了同时处理分组和单个设备的情况
            textView.setText(ipSeg.start.toString());
        else
            textView.setText(ipSeg.start + "-" + ipSeg.end);
        if(ip_seg_enable.get(i))
            radioGroup.check(R.id.rb1);
        else
            radioGroup.check(R.id.rb2);
        mSlideDelete.setOnSlideDeleteListener(new ViewGroup_SlideDelete.OnSlideDeleteListener() {
            @Override
            public void onOpen(ViewGroup_SlideDelete slideDelete) {
                closeOtherItem();
                slideDeleteArrayList.add(slideDelete);
            }

            @Override
            public void onClose(ViewGroup_SlideDelete slideDelete) {
                slideDeleteArrayList.remove(slideDelete);
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeOtherItem();
            }
        });

        slide_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ip_seg_list.remove(i);
                ip_seg_enable.remove(i);
                if (activity_edit_group != null) {
                    activity_edit_group.set_ip_seg_changed(true);
                }
                notifyDataSetChanged();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb1:
                        ip_seg_enable.set(index, true);//这里在内部引用了index,应该声明为final的
                        radioGroup.check(R.id.rb1);
                        break;
                    case R.id.rb2:
                        ip_seg_enable.set(index, false);
                        radioGroup.check(R.id.rb2);
                        break;
                }
            }
        });
        return view;
    }

    private void closeOtherItem() {
        ListIterator<ViewGroup_SlideDelete> slideDeleteListIterator = slideDeleteArrayList.listIterator();
        while (slideDeleteListIterator.hasNext()) {
            ViewGroup_SlideDelete slideDelete = slideDeleteListIterator.next();
            slideDelete.isShowDelete(false);
        }
        slideDeleteArrayList.clear();
    }
}