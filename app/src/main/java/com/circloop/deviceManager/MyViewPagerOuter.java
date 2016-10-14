package com.circloop.deviceManager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by zh on 2016/8/3.
 */
public class MyViewPagerOuter extends ViewPager {

    public MyViewPagerOuter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyViewPagerOuter(Context context) {
        super(context);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(getCurrentItem()==0)
            return false;
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //批量删除界面不允许滑动
        if (Adapter_ExpandableListView.isVisible_batch_delete_child || Adapter_ExpandableListView.isVisible_batch_delete) {
            return false;
        }
        return super.onTouchEvent(ev);
    }
}
