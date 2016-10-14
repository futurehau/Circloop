package com.circloop.deviceManager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zh on 2016/7/13.
 */
public class Fragment_mine extends Fragment{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        System.out.println("Mine.onCreateView");
        View view = inflater.inflate(R.layout.fg_mine, container,false);
        initial();
        return view;
    }
    public void initial(){

    }
}
