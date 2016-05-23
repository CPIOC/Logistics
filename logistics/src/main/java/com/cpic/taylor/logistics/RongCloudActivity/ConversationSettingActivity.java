package com.cpic.taylor.logistics.RongCloudActivity;

import android.os.Bundle;
import android.view.View;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.utils.CloseActivityClass;


/**
 * Created by Bob on 2015/3/27.
 * 设置页面采用 Activity 嵌套 SettingFragment，在 SettingFragment 中嵌套 置顶，新消息通知，清空聊天信息 这三个 fragment
 * 目的：
 *      1，方便大家集成，会话的设置界面可以全部复制到自己的 app 中
 *      2，展示 Fragment 如何集成设置页面， SettingFragment 必须继承 DispatchResultFragment 才可以。
 *      3，在 Activity 中只需要继承 FragmentActivity 即可。
 *
 */
public class ConversationSettingActivity extends BaseActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CloseActivityClass.activityList.add(this);
        setContentView(R.layout.de_ac_setting);
        getSupportActionBar().setTitle(R.string.de_actionbar_set_conversation);
        getSupportActionBar().hide();
    }

    public void backTo(View veiw){
        finish();
    }
}
