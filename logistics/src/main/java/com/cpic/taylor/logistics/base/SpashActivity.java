package com.cpic.taylor.logistics.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.activity.HomeActivity;
import com.cpic.taylor.logistics.activity.LoginActivity;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;

/**
 * Created by Taylor on 2016/5/24.
 */
public class SpashActivity extends BaseActivity {

    private SharedPreferences sp;
    private Intent intent;

    private HttpUtils post;
    private  RequestParams params;


    @Override
    protected void getIntentData(Bundle savedInstanceState) {

    }

    @Override
    protected void loadXml() {
        setContentView(R.layout.activity_spash);
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        sp = PreferenceManager.getDefaultSharedPreferences(SpashActivity.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sp.getBoolean("isLogin", true)) {
                    // start guideactivity1
                    intent = new Intent(SpashActivity.this, LoginActivity.class);
                } else {
                    // start TVDirectActivity
                    intent = new Intent(SpashActivity.this, HomeActivity.class);
                }
                SpashActivity.this.startActivity(intent);
                SpashActivity.this.finish();
            }
        }, 10);
    }


    @Override
    protected void registerListener() {

    }
}
