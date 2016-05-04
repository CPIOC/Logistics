package com.cpic.taylor.logistics.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.base.BaseActivity;

/**
 * Created by Taylor on 2016/5/4.
 */
public class ForgetPwdActivity extends BaseActivity{

    private Button btnNext;
    private Intent intent;
    @Override
    protected void getIntentData(Bundle savedInstanceState) {

    }

    @Override
    protected void loadXml() {
        setContentView(R.layout.activity_forget);
    }

    @Override
    protected void initView() {
        btnNext = (Button) findViewById(R.id.activity_forget_btn_next);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void registerListener() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(ForgetPwdActivity.this,ChangePwdActivity.class);
                startActivity(intent);
            }
        });
    }
}
