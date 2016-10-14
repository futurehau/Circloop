package com.circloop.deviceManager;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;


/**
 * Created by zh on 2016/9/9.
 */
public class IpTextWatcher implements TextWatcher {
    private EditText[] et_ips;
    private EditText mEditText;
    private TextView textView;
    FinalEditTextErrorListener finalEditTextErrorListener;
    protected IpTextWatcher(EditText editText,EditText[] et_ips,FinalEditTextErrorListener finalEditTextErrorListener) {
        this.et_ips = et_ips;
        this.mEditText = editText;
        this.textView = textView;
        this.finalEditTextErrorListener =finalEditTextErrorListener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mEditText.setSelection(mEditText.getText().toString().length());
        if (count == 0 && s.length() == 0) {//删除的时候的情况
            if (mEditText == et_ips[3]) {
                et_ips[2].requestFocus();
            }
            else if (mEditText == et_ips[2]) {
                et_ips[1].requestFocus();
            }
            else if (mEditText == et_ips[1]) {
                et_ips[0].requestFocus();
            }
        }
        else if (count == 1){//添加时候的情况
            if (Integer.valueOf(s.toString()) > 25) {
                if (mEditText == et_ips[0]) {
                    et_ips[1].requestFocus();
                }
                else if (mEditText == et_ips[1]) {
                    et_ips[2].requestFocus();
                }
                else if (mEditText == et_ips[2]) {
                    et_ips[3].requestFocus();
                }
            }
        }
    }
    @Override
    public void afterTextChanged(Editable editable) {
        if(mEditText == et_ips[3]) {
            if (mEditText.getText().toString().length() > 0 && Integer.valueOf(mEditText.getText().toString()) > 255) {
                finalEditTextErrorListener.finalEditTextHasError(mEditText);
            }
            else {
                finalEditTextErrorListener.finalEditTextClearError(mEditText);
            }
        }
    }
    public interface FinalEditTextErrorListener{
        public void finalEditTextHasError(EditText editText);
        public void finalEditTextClearError(EditText editText);
    }
}

