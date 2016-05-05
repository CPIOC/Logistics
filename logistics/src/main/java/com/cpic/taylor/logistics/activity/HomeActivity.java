package com.cpic.taylor.logistics.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.base.BaseActivity;
import com.cpic.taylor.logistics.fragment.HomeLineFragment;
import com.cpic.taylor.logistics.fragment.HomePoliceFragment;
import com.cpic.taylor.logistics.fragment.HomeRoadFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Taylor on 2016/5/4.
 */
public class HomeActivity extends BaseActivity {

    // 记录上次点击返回键的时间
    private long lastTime;

    private DrawerLayout layout;
    private ImageView ivMine;
    private TextView tvChat;
    private RadioGroup rgroup;
    private RadioButton lastButton;
    // Fragment的管理类
    private FragmentManager mManager;
    // Fragment的事务类
    private FragmentTransaction mTrans;
    // 管理Fragment的List集合
    private List<Fragment> mFragList;
    public String curFragmentTag = "";


    @Override
    protected void getIntentData(Bundle savedInstanceState) {

    }

    @Override
    protected void loadXml() {
        setContentView(R.layout.activity_home);
    }

    @Override
    protected void initView() {
        layout = (DrawerLayout) findViewById(R.id.activity_home_drawerlayout);
        ivMine = (ImageView) findViewById(R.id.activity_home_iv_mine);
        tvChat = (TextView) findViewById(R.id.activity_home_chat);
        rgroup = (RadioGroup) findViewById(R.id.activity_home_rgroup);
        lastButton = (RadioButton) findViewById(R.id.activity_home_rbtn_line);
    }

    @Override
    protected void initData() {
        initFragment();
    }

    private void initFragment() {
        // TODO Auto-generated method stub
        mFragList = new ArrayList<Fragment>();
        mFragList.add(new HomeLineFragment());
        mFragList.add(new HomePoliceFragment());
        mFragList.add(new HomeRoadFragment());

        mManager = getSupportFragmentManager();
        mTrans = mManager.beginTransaction();
        lastButton.setChecked(true);
        mTrans.add(R.id.activity_home_framelayout, mFragList.get(0), "0");
        mTrans.show(mFragList.get(0));
        mTrans.commit();
    }

    @Override
    protected void registerListener() {
        ivMine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.openDrawer(Gravity.LEFT);
            }
        });
        tvChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ChatMainActivity.class);
                startActivity(intent);
            }
        });

        rgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 当前选中的radioButton
                RadioButton selectRbtn = (RadioButton) findViewById(checkedId);

                int index = Integer.parseInt(selectRbtn.getTag().toString());
                int lastIndex = Integer.parseInt(lastButton.getTag().toString());

                Fragment mFragment = mManager.findFragmentByTag(index + "");

                mTrans = mManager.beginTransaction();

                if (mFragment == null) {
                    mTrans.add(R.id.activity_home_framelayout, mFragList.get(index), "" + index);
                }

                // 设置界面隐藏与显示，避免一次性加载所有界面
                mTrans.show(mFragList.get(index));
                mTrans.hide(mFragList.get(lastIndex));
                mTrans.commit();

                lastButton = selectRbtn;
            }
        });
    }

    @Override
    public void onBackPressed() {
        // 获取本次点击的时间
        long currentTime = System.currentTimeMillis();
        long dTime = currentTime - lastTime;

        if (dTime < 2000) {
            finish();
        } else {
            showShortToast("再按一次退出程序");
            lastTime = currentTime;
        }
    }
}
