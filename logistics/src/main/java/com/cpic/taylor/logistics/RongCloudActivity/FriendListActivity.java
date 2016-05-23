package com.cpic.taylor.logistics.RongCloudActivity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.utils.CloseActivityClass;


/**
 * Created by Bob on 2015/3/18.
 */
public class FriendListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ui_list_test);
        CloseActivityClass.activityList.add(this);
        getSupportActionBar().setTitle(R.string.de_actionbar_set_conversation);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.de_ic_logo);
        actionBar .setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().hide();

    }
}
