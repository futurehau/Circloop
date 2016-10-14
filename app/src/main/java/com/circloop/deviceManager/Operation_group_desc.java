package com.circloop.deviceManager;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


/**
 * Created by zh on 2016/9/19.
 */
public class Operation_group_desc {
    private EditText edit_group_desc;
    private Context context;
    Button button;

    public Operation_group_desc(EditText edit_group_desc, Button button, Context context) {
        this.edit_group_desc = edit_group_desc;
        this.button = button;
        this.context = context;
    }
    public String getGroupName(){
        return edit_group_desc.getText().toString();
    }
    public void groupDescSetOnClickListener() {
        edit_group_desc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasfocus) {
                if (!hasfocus) {
                    button.setVisibility(View.INVISIBLE);
                } else {
                    if (getGroupName().length() != 0){
                        button.setVisibility(View.VISIBLE);
                    }
                    edit_group_desc.setTextColor(context.getResources().getColor(R.color.textColorBlack));
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_group_desc.setText("");
                button.setVisibility(View.INVISIBLE);
            }
        });
    }
}
