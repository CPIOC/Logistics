package com.cpic.taylor.logistics.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.base.BaseActivity;
import com.cpic.taylor.logistics.fragment.ContactsFragment;
import com.cpic.taylor.logistics.fragment.MessageFragment;

import java.util.ArrayList;
import java.util.List;

public class ChatMainActivity extends BaseActivity {

    // rbtn的管理类
    private RadioGroup rGroup;
    // 上一次选择的rbtn
    private RadioButton lastButton;
    // Fragment的管理类
    private FragmentManager mManager;
    // Fragment的事务类
    private FragmentTransaction mTrans;
    // 管理Fragment的List集合
    private List<Fragment> mFragList;
    // RadioButton
    private RadioButton rBtnHis, rBtnNew, rBtnQuery, rBtnMine;


    public String curFragmentTag = "";

    public static boolean isForeground = false;
    @Override
    protected void getIntentData(Bundle savedInstanceState) {

    }

    @Override
    protected void loadXml() {
        setContentView(R.layout.activity_chat);
    }

    @Override
    protected void initView() {
        rGroup = (RadioGroup) findViewById(R.id.main_activity_rgroup);
        lastButton = (RadioButton) findViewById(R.id.main_activity_rbtn_message);

    }

    private void initFragment() {
        // TODO Auto-generated method stub
        mFragList = new ArrayList<Fragment>();
        mFragList.add(new MessageFragment());
        mFragList.add(new ContactsFragment());

        mManager = getSupportFragmentManager();
        mTrans = mManager.beginTransaction();
        lastButton.setChecked(true);
        mTrans.add(R.id.activity_main_framlayout, mFragList.get(0), "0");
        mTrans.show(mFragList.get(0));
        mTrans.commit();
    }



    @Override
    protected void initData() {
        initFragment();
    }

    @Override
    protected void registerListener() {
        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 当前选中的radioButton
                RadioButton selectRbtn = (RadioButton) findViewById(checkedId);

                int index = Integer.parseInt(selectRbtn.getTag().toString());
                int lastIndex = Integer.parseInt(lastButton.getTag().toString());

                Fragment mFragment = mManager.findFragmentByTag(index + "");

                mTrans = mManager.beginTransaction();

                if (mFragment == null) {
                    mTrans.add(R.id.activity_main_framlayout,mFragList.get(index), "" + index);
                }

                // 设置界面隐藏与显示，避免一次性加载所有界面
                mTrans.show(mFragList.get(index));
                mTrans.hide(mFragList.get(lastIndex));
                mTrans.commit();

                lastButton = selectRbtn;
            }
        });
    }
}
