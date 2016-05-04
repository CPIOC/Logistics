package com.cpic.taylor.logistics.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.base.BaseActivity;
import com.cpic.taylor.logistics.utils.ProgressDialogHandle;

/**
 * Created by Taylor on 2016/5/4.
 */
public class LoginActivity extends BaseActivity{

    private TextView tvForget;
    private Intent intent;
    private TextView tvRegister;
    private Button btnLogin;
    private Dialog dialog;

    @Override
    protected void getIntentData(Bundle savedInstanceState) {

    }

    @Override
    protected void loadXml() {
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void initView() {
        tvForget = (TextView) findViewById(R.id.activity_login_tv_forget);
        tvRegister = (TextView) findViewById(R.id.activity_login_tv_register);
        btnLogin = (Button) findViewById(R.id.activity_login_btn_login);
        dialog = ProgressDialogHandle.getProgressDialog(LoginActivity.this,null);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void registerListener() {
        tvForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(LoginActivity.this,ForgetPwdActivity.class);
                startActivity(intent);
            }
        });
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(LoginActivity.this,HomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
