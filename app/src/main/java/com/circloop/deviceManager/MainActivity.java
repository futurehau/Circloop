package com.circloop.deviceManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.circloop.database.Child;
import com.circloop.database.Group;
import com.circloop.database.MyDatabaseHelper;
import com.circloop.deviceDiscover.AutoDiscover;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zh on 2016/7/13.
 */
public class MainActivity extends FragmentActivity implements View.OnClickListener,
        Fragment_add_group.AddGroupBtnClickListener,Fragment_add_device.AddDeviceBtnClickListener,
        Adapter_ExpandableListView.OnLongClickListener{

    private String TAG = "MainActivity";

    public static MyDatabaseHelper dbHelper;

    //定义3个Fragment的对象
    private Fragment_list fg_list;
    private Fragment_add fg_add;
    private Fragment_mine fg_mine;
    private List<Fragment> fragments=new ArrayList<Fragment>();
    //定义底部导航栏的三个布局
    private RelativeLayout list_layout;
    private RelativeLayout add_layout;
    private RelativeLayout mine_layout;

    private LinearLayout main_bottom_layout;
    //定义底部导航栏中的ImageView与TextView
    private ImageView list_image;
    private ImageView add_image;
    private ImageView mine_image;
    private TextView list_text;
    private TextView add_text;
    private TextView mine_text;
    //定义要用的颜色值
    private int whirt = 0xFFFFFFFF;
    private int gray = 0xFF7597B3;
    private int blue =0xFF0AB2FB;

    private MyViewPagerOuter viewPager;
    private FragAdapter mFragAdapter;

    private ViewGroup normal_navigation_bar;
    //批量删除分组
    private boolean isVisible_batch_delete = false;//批量删除分组的勾选框是否可见
    private ViewGroup batch_delete_navagation_bar;
    private TextView batch_delete_textView;
    private ImageView delete_image;
    private LinearLayout cancal_delete_linearlayout;

    //批量删除孩子
    private boolean isVisible_batch_delete_child = false;//批量删除孩子的勾选框是否可见
    private ViewGroup batch_delete_navagation_bar_child;
    private TextView batch_delete_textView_child;
    private ImageView delete_image_child;
    private LinearLayout cancal_delete_linearlayout_child;

    Adapter_ExpandableListView mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findById();
        init();
        setOnClickListener();
        list_layout.setOnClickListener(this);
        add_layout.setOnClickListener(this);
        mine_layout.setOnClickListener(this);
    }
    private void findById(){
        list_image = (ImageView) findViewById(R.id.list_image);
        add_image = (ImageView) findViewById(R.id.add_image);
        mine_image = (ImageView) findViewById(R.id.mine_image);
        list_text = (TextView) findViewById(R.id.list_text);
        add_text = (TextView) findViewById(R.id.add_text);
        mine_text = (TextView) findViewById(R.id.mine_text);
        list_layout = (RelativeLayout) findViewById(R.id.list_layout);
        add_layout = (RelativeLayout) findViewById(R.id.add_layout);
        mine_layout = (RelativeLayout) findViewById(R.id.mine_layout);
        main_bottom_layout = (LinearLayout) findViewById(R.id.main_bottom_layout);
        viewPager= (MyViewPagerOuter) findViewById(R.id.main_viewPager);


        normal_navigation_bar = (ViewGroup) findViewById(R.id.normal_navigation_bar);
        batch_delete_navagation_bar = (ViewGroup) findViewById(R.id.batch_delete_nacagation_bar);
        batch_delete_textView = (TextView) findViewById(R.id.view_navigation_bar_textview_title);
        delete_image = (ImageView) findViewById(R.id.view_navigation_bar_imageview_deletebtn);
        cancal_delete_linearlayout = (LinearLayout) findViewById(R.id.view_navigation_bar_layout_cancelbtn);

        batch_delete_navagation_bar_child = (ViewGroup) findViewById(R.id.batch_delete_nacagation_bar_child);
        batch_delete_textView_child = (TextView) findViewById(R.id.view_navigation_bar_textview_title_child);
        delete_image_child = (ImageView) findViewById(R.id.view_navigation_bar_imageview_deletebtn_child);
        cancal_delete_linearlayout_child = (LinearLayout) findViewById(R.id.view_navigation_bar_layout_cancelbtn_child);
    }
    private void init(){
        fg_list=new Fragment_list();
        fg_add=new Fragment_add();
        fg_mine=new Fragment_mine();
        fragments.add(fg_list);
        fragments.add(fg_add);
        fragments.add(fg_mine);
        mFragAdapter=new FragAdapter(getSupportFragmentManager(),fragments);
        viewPager.setAdapter(mFragAdapter);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        dbHelper=new MyDatabaseHelper(this,"Device.db3",null,1);
        setDefaultFragment();
    }
    //设置登录之后的默认Fragment界面。
    private void setDefaultFragment(){
        list_image.setImageResource(R.drawable.ic_tabbar_course_pressed);
        list_text.setTextColor(blue);
        viewPager.setCurrentItem(0);
    }
    private void setOnClickListener() {
        delete_image.setOnClickListener(this);
        cancal_delete_linearlayout.setOnClickListener(this);

        delete_image_child.setOnClickListener(this);
        cancal_delete_linearlayout_child.setOnClickListener(this);

    }
    //重写onClick事件
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.list_layout:
                viewPager.setCurrentItem(0);
                break;
            case R.id.add_layout:
                viewPager.setCurrentItem(1);
                break;
            case R.id.mine_layout:
                viewPager.setCurrentItem(2);
                break;
            case R.id.view_navigation_bar_imageview_deletebtn:
                mAdapter = Fragment_list.getAdapter();
                fg_list.set_isVisible_batch_delete(false, false);
                mAdapter.setIsVisible_batch_delete(false, false);
                isVisible_batch_delete = !isVisible_batch_delete;
                mAdapter.removeItems();
                changeModel(isVisible_batch_delete, isVisible_batch_delete_child);
                break;
            case R.id.view_navigation_bar_layout_cancelbtn:
                mAdapter = Fragment_list.getAdapter();
                mAdapter.setIsVisible_batch_delete(false, false);
                fg_list.set_isVisible_batch_delete(false, false);
                isVisible_batch_delete = !isVisible_batch_delete;
                changeModel(isVisible_batch_delete, isVisible_batch_delete_child);
                break;
            case R.id.view_navigation_bar_imageview_deletebtn_child:
                mAdapter = Fragment_list.getAdapter();
                fg_list.set_isVisible_batch_delete(false, false);
                mAdapter.setIsVisible_batch_delete(false, false);
                isVisible_batch_delete_child = !isVisible_batch_delete_child;
                //删除逻辑：------------------------------------------------------------------------------------------------------------------------------------
                mAdapter.removeItems_child();
                changeModel(isVisible_batch_delete, isVisible_batch_delete_child);
                break;
            case R.id.view_navigation_bar_layout_cancelbtn_child:
                mAdapter = Fragment_list.getAdapter();
                fg_list.set_isVisible_batch_delete(false, false);
                mAdapter.setIsVisible_batch_delete(false, false);
                isVisible_batch_delete_child = !isVisible_batch_delete_child;
                changeModel(isVisible_batch_delete, isVisible_batch_delete_child);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//处理在批量删除界面点击返回再打开的bug
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Log.e(TAG, "按下返回键");
            if (isVisible_batch_delete || isVisible_batch_delete_child) {
                mAdapter = Fragment_list.getAdapter();
                mAdapter.setIsVisible_batch_delete(false, false);
                fg_list.set_isVisible_batch_delete(false, false);
                isVisible_batch_delete = false;
                isVisible_batch_delete_child = false;
                changeModel(isVisible_batch_delete, isVisible_batch_delete_child);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    //定义一个重置所有选项的方法
    public void clearChioce() {
        list_image.setImageResource(R.drawable.ic_tabbar_course_normal);
        list_layout.setBackgroundColor(whirt);
        list_text.setTextColor(gray);
        add_image.setImageResource(R.drawable.ic_tabbar_found_normal);
        add_layout.setBackgroundColor(whirt);
        add_text.setTextColor(gray);
        mine_image.setImageResource(R.drawable.ic_tabbar_settings_normal);
        mine_layout.setBackgroundColor(whirt);
        mine_text.setTextColor(gray);
    }
    @Override
       public void onAddGroupClick(String group_name,String group_desc,List<Interval> ip_seg_data,List<Boolean> isEnable) {
        addSucessUIChange();
        fg_list.addSucess(group_name, group_desc, ip_seg_data, isEnable, true);//true用来表示当前添加的是分组，以便记录进数据库，编辑的时候跳到相应界面
    }
    @Override
    public void onAddDeviceClick(String group_name, String group_desc,List<Interval> ip_seg_data,List<Boolean> ip_seg_enable) {
        addSucessUIChange();
        fg_list.addSucess(group_name, group_desc, ip_seg_data, ip_seg_enable, false);
    }
    //成功添加设备之后跳转回列表界面，不使用之前的点击跳转是因为此时fg_add界面editText中的数据不应该保存
    private void addSucessUIChange(){
        viewPager.setCurrentItem(0);
    }


    //回调方法，用于控制长按分组item之后的界面变换
    @Override
    public void modelChange(boolean isVisible, boolean isVisible_child) {
        mAdapter = Fragment_list.getAdapter();
        isVisible_batch_delete = isVisible;
        isVisible_batch_delete_child = isVisible_child;
        changeModel(isVisible, isVisible_child);
    }
    private void changeModel(boolean isVisible, boolean isVisible_child) {
        if (isVisible) {
            normal_navigation_bar.setVisibility(View.GONE);
            main_bottom_layout.setVisibility(View.GONE);
            batch_delete_navagation_bar.setVisibility(View.VISIBLE);
            delete_image.setVisibility(View.VISIBLE);
            batch_delete_textView.setText("已选中0项");
        } else if (isVisible_child) {
            normal_navigation_bar.setVisibility(View.GONE);
            main_bottom_layout.setVisibility(View.GONE);
            batch_delete_navagation_bar_child.setVisibility(View.VISIBLE);
            delete_image_child.setVisibility(View.VISIBLE);
            batch_delete_textView_child.setText("已选中0项");
        }
        else {
            normal_navigation_bar.setVisibility(View.VISIBLE);
            main_bottom_layout.setVisibility(View.VISIBLE);
            batch_delete_navagation_bar.setVisibility(View.GONE);
            batch_delete_navagation_bar_child.setVisibility(View.GONE);
            delete_image.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }
    //显示已选中分组项数
    public void setSelectedItemnum(int number) {
        batch_delete_textView.setText("已选中" + number + "项");
    }
    //显示已选中孩子项数
    public void setSelectedItemnum_child(int number) {
        batch_delete_textView_child.setText("已选中" + number + "项");
    }
    class FragAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragments;
        public FragAdapter(FragmentManager fm,List<Fragment> fragments) {
            super(fm);
            mFragments=fragments;
        }

        @Override
        public Fragment getItem(int i) {
            return mFragments.get(i);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }
    class MyOnPageChangeListener implements ViewPager.OnPageChangeListener{

        /**
         * 搞清楚当前界面的概念就是可见界面的最左端。所以向右滑动时，offset逐渐增加，向左滑动时，offset逐渐减小。
         * position :表示可见的最左边的页面
         * offset:当前页面偏移的百分比
         * offsetPixels:当前页面偏移的像素位置
         */
        @Override
        public void onPageScrolled(int position, float offset, int offsetPixels) {
        }
        @Override
        public void onPageSelected(int position) {//position代表当前滑动到的页面
            clearChioce();
            switch (position) {
                case 0:
                    list_image.setImageResource(R.drawable.ic_tabbar_course_pressed);
                    list_text.setTextColor(blue);
                    viewPager.setCurrentItem(0);
                    break;
                case 1:
                    add_image.setImageResource(R.drawable.ic_tabbar_found_pressed);
                    add_text.setTextColor(blue);
                    viewPager.setCurrentItem(1);
                    break;
                case 2:
                    mine_image.setImageResource(R.drawable.ic_tabbar_settings_pressed);
                    mine_text.setTextColor(blue);
                    viewPager.setCurrentItem(2);
                    break;
            }
        }

        /**
         * state滑动中的状态 有三种状态（0，1，2） 1：正在滑动 2：滑动完毕 0：什么都没做。
         */
        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}
