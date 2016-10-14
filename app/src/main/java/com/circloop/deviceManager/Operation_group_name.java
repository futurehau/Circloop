package com.circloop.deviceManager;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by zh on 2016/9/19.
 */
public class Operation_group_name {
    private EditText edit_group_name;
    private Context context;
    Button button;

    public Operation_group_name(EditText edit_group_name, Button button, Context context) {
        this.edit_group_name = edit_group_name;
        this.button = button;
        this.context = context;
    }
    public String getGroupName(){
        return edit_group_name.getText().toString();
    }
    public void groupNamesetOnClickListener() {
        edit_group_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasfocus) {
                    if (!hasfocus) {
                        button.setVisibility(View.INVISIBLE);
                    }
                    else {
                        if (getGroupName().length() != 0){
                            button.setVisibility(View.VISIBLE);
                        }
                        edit_group_name.setTextColor(context.getResources().getColor(R.color.textColorBlack));
                    }
                }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_group_name.setText("");
                edit_group_name.setError(null);
                button.setVisibility(View.INVISIBLE);
            }
        });
    }
}
