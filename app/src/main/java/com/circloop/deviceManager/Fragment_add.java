package com.circloop.deviceManager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zh on 2016/7/13.
 */
public class Fragment_add extends Fragment implements View.OnClickListener{
    //添加分组和添加设备的两个布局
    private RelativeLayout rl_add_group;
    private RelativeLayout rl_add_device;

    //定义两个Fragment对象
    private Fragment fg_add_group;
    private Fragment fg_add_device;
    private List<Fragment> fragments=new ArrayList<Fragment>();


    private FragAdapter mFragAdapter;
    private ViewPager viewPager;

    private ImageView bottom_tab;
    private int currentIndex;
    private int screenWidth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_add, container,false);
        findById(view);
        init();
        rl_add_group.setOnClickListener(this);
        rl_add_device.setOnClickListener(this);
        return view;
    }
    private void findById(View view){
        rl_add_device= (RelativeLayout) view.findViewById(R.id.rl_add_device);
        rl_add_group= (RelativeLayout) view.findViewById(R.id.rl_add_group);
        bottom_tab= (ImageView) view.findViewById(R.id.top_tab);
        viewPager= (ViewPager) view.findViewById(R.id.add_group_device_viewPager);
    }
    private void init(){
        fg_add_device=new Fragment_add_device();
        fg_add_group=new Fragment_add_group();
        fragments.add(fg_add_group);
        fragments.add(fg_add_device);
        mFragAdapter=new FragAdapter(getChildFragmentManager(),fragments);
        viewPager.setAdapter(mFragAdapter);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        initTabLineWidth();
        rl_add_group.setOnClickListener(this);
        rl_add_device.setOnClickListener(this);
    }
    /**
     * 设置滑动条的宽度为屏幕的1/2(根据Tab的个数而定)
     */
    private void initTabLineWidth() {
        DisplayMetrics dpMetrics = new DisplayMetrics();

       getActivity().getWindow().getWindowManager().getDefaultDisplay()
                .getMetrics(dpMetrics);
        screenWidth = dpMetrics.widthPixels;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) bottom_tab.getLayoutParams();
        lp.width = screenWidth / 2;
        bottom_tab.setLayoutParams(lp);
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.rl_add_group:
//                System.out.println("添加分组");
                viewPager.setCurrentItem(0);
                break;
            case R.id.rl_add_device:
//                System.out.println("添加设备");
                viewPager.setCurrentItem(1);
                break;
        }
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
            LinearLayout.LayoutParams lp= (LinearLayout.LayoutParams) bottom_tab.getLayoutParams();
            /**
             * 利用currentIndex(当前所在页面)和position(下一个页面)以及offset来
             * 设置bottom_tab的左边距 滑动场景：
             * 记3个页面,
             * 从左到右分别为0,1,2
             * 0->1; 1->2; 2->1; 1->0
             */
            if (currentIndex == 0 && position == 0)// 0->1
            {
                lp.leftMargin = (int) (offset * (screenWidth * 1.0 / 2) + currentIndex
                        * (screenWidth / 2));

            } else if (currentIndex == 1 && position == 0) // 1->0
            {
                lp.leftMargin = (int) (-(1 - offset)
                        * (screenWidth * 1.0 / 2) + currentIndex
                        * (screenWidth / 2));

            } else if (currentIndex == 1 && position == 1) // 1->2
            {
                lp.leftMargin = (int) (offset * (screenWidth * 1.0 / 2) + currentIndex
                        * (screenWidth / 2));
            } else if (currentIndex == 2 && position == 1) // 2->1
            {
                lp.leftMargin = (int) (-(1 - offset)
                        * (screenWidth * 1.0 / 2) + currentIndex
                        * (screenWidth / 2));
            }
            bottom_tab.setLayoutParams(lp);
        }


        @Override
        public void onPageSelected(int position) {//position代表当前滑动到的页面
//            System.out.println("onPageSelected................................."+position);
////            resetTextView();
//            switch (position) {
//                case 0:
//                    course_text.setTextColor(Color.BLUE);
//                    break;
//                case 1:
//                    found_text.setTextColor(Color.BLUE);
//                    break;
//            }
            currentIndex = position;
        }

        /**
         * state滑动中的状态 有三种状态（0，1，2） 1：正在滑动 2：滑动完毕 0：什么都没做。
         */
        @Override
        public void onPageScrollStateChanged(int state) {
//            System.out.println("onPageScrollStateChanged......................."+state);
        }
    }
}
