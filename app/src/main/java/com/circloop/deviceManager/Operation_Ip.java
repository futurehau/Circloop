package com.circloop.deviceManager;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by zh on 2016/9/14.
 */
public class Operation_Ip implements IpTextWatcher.FinalEditTextErrorListener {
    private String TAG = "Operation_Ip" ;
    private EditText[] edit_ips;
    private Context context;
    Button button;
    public Operation_Ip(EditText[] edit_ips, Button button, Context context) {
        this.edit_ips = edit_ips;
        this.button = button;
        this.context = context;
    }

    public String getIpString(){
        String ip="";
        for(int i = 0; i < 3; i++) {
            ip +=edit_ips[i].getText().toString();
            ip +=".";
        }
        ip +=edit_ips[3].getText().toString();
        return ip;
    }
    //四位Ip数据清除
    public void clearIp(){
        for(int i = 0; i < 4; i++) {
            edit_ips[i].setText("");
        }
        button.setVisibility(View.INVISIBLE);
    }
    public void ip_setOnClickListener() {
        Log.e(TAG, "ip_setOnClickListener......................................");
        for (int i = 0; i < 4; i++) {
            edit_ips[i].addTextChangedListener(new IpTextWatcher(edit_ips[i], edit_ips, this));
            final int curIndex = i;
            edit_ips[i].setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {//检测删除键，为了处理没有数字加入时，删除键回不到前一个EditText的问题
                    Log.e(TAG, "onKey......................................");
                    if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        if (curIndex != 0 && edit_ips[curIndex].getText().length() == 0) {
                            edit_ips[curIndex].clearFocus();
                            edit_ips[curIndex - 1].requestFocus();
                            return true;
                        }
                    }
                    return false;
                }
            });
            edit_ips[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View view, boolean hasfocus) {
                    if (!hasfocus) {//失去焦点时检测是否是一个有效数
                        button.setVisibility(View.INVISIBLE);
                        if (edit_ips[curIndex].getText().length() != 0) {//首先应该判断是否为空，否则有bug
                                if (Integer.valueOf(edit_ips[curIndex].getText().toString()) > 255) {
                                    edit_ips[curIndex].setTextColor(context.getResources().getColor(R.color.textColorError));
                                } else {
                                    edit_ips[curIndex].setTextColor(context.getResources().getColor(R.color.textColorBlack));
                                }
                        }
                    } else {
                        if (curIndex != 0) {
                            button.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
            Log.e(TAG, "ip_setOnClickListener....................................end..");
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < 4; i++) {
                    edit_ips[i].setText("");
                    edit_ips[i].setTextColor(context.getResources().getColor(R.color.textColorBlack));
                }
                edit_ips[0].requestFocus();
                button.setVisibility(View.INVISIBLE);
            }
        });
    }
    @Override
    public void finalEditTextHasError(EditText editText) {
        if ( editText == edit_ips[3]){
            edit_ips[3].setTextColor(context.getResources().getColor(R.color.textColorError));
        }else{
            edit_ips[3].setTextColor(context.getResources().getColor(R.color.textColorError));
        }
    }

    @Override
    public void finalEditTextClearError(EditText editText) {
        if ( editText == edit_ips[3]){
            edit_ips[3].setTextColor(context.getResources().getColor(R.color.textColorBlack));
        }
        else {
            edit_ips[3].setTextColor(context.getResources().getColor(R.color.textColorBlack));
        }
    }

}
