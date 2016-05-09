package com.cpic.taylor.logistics.RongCloudWidget;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

/**
 * 输入框删除view
 * Created by Bob on 2015/2/26.
 */
public class EditTextHolder implements View.OnClickListener,View.OnFocusChangeListener{

    /**
     * EditText 输入框
     */
    private EditText mEditText;
    /**
     * 删除 View
     */
    private View mDeleteView;
    private OnEditTextFocusChangeListener mOnEditTextFocusChangeListener;

    public interface  OnEditTextFocusChangeListener{
        void onEditTextFocusChange(View v, boolean hasFocus);
    }

    public EditTextHolder(EditText editText, View deleteView, OnEditTextFocusChangeListener listener){
        mEditText = editText;
        mDeleteView = deleteView;
        mOnEditTextFocusChangeListener = listener;
        mEditText.setOnFocusChangeListener(this);
        mDeleteView.setOnClickListener(this);
        mEditText.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (TextUtils.isEmpty(s)) {
                    mDeleteView.setVisibility(View.GONE);
                } else {
                    if(isHasFocus)
                         mDeleteView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        mEditText.setText("");
        mDeleteView.setVisibility(View.GONE);
    }

    public OnEditTextFocusChangeListener getmOnEditTextFocusChangeListener() {
        return mOnEditTextFocusChangeListener;
    }

    public void setmOnEditTextFocusChangeListener(OnEditTextFocusChangeListener mOnEditTextFocusChangeListener) {
        this.mOnEditTextFocusChangeListener = mOnEditTextFocusChangeListener;
    }

    boolean isHasFocus = false;
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        mDeleteView.setVisibility(hasFocus&&(mEditText.length() > 0)? View.VISIBLE
                : View.GONE);
        isHasFocus  = hasFocus;
        if(mOnEditTextFocusChangeListener != null){
            mOnEditTextFocusChangeListener.onEditTextFocusChange(v,hasFocus);
        }

    }

}
