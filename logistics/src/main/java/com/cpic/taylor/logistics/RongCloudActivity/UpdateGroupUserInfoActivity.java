package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.WinToast;
import com.cpic.taylor.logistics.utils.CloseActivityClass;

import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupUserInfo;

/**
 * Created by Bob on 15/11/13.
 */
public class UpdateGroupUserInfoActivity extends BaseActionBarActivity {

    private EditText mNewName;
    String mGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_update_group_info);
        CloseActivityClass.activityList.add(this);
        getSupportActionBar().setTitle(R.string.de_actionbar_update_group);
        mNewName = (EditText) findViewById(R.id.et_new_name);

        Intent intent = getIntent();
        mGroupId = intent.getStringExtra("DEMO_GROUP_ID");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.de_fix_username, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.icon:
                if (RongIM.getInstance() == null || RongYunContext.getInstance() == null )

                    return true;

                    RongIM.setGroupUserInfoProvider(new RongIM.GroupUserInfoProvider() {
                        @Override
                        public GroupUserInfo getGroupUserInfo(String groupId, String userId) {

                            String currentUserId = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_ID, Constants.DEFAULT);

                            if (userId.equals(currentUserId)) {

                                WinToast.toast(UpdateGroupUserInfoActivity.this, "修改成功");

                                Intent intent = new Intent();
                                intent.putExtra("UPDATA_GROPU_INFO", mNewName.getText().toString());
                                setResult(Constants.FIX_GROUP_INFO, intent);
                                finish();
                                return new GroupUserInfo(mGroupId, userId, mNewName.getText().toString());

                            } else {
                                return null;
                            }
                        }
                    },true);

                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
