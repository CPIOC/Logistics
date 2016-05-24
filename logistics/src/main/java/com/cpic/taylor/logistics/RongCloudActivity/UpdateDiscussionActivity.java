package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.WinToast;
import com.cpic.taylor.logistics.utils.CloseActivityClass;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

/**
 * Created by Bob on 15/6/8.
 */
public class UpdateDiscussionActivity extends BaseActionBarActivity {

    private EditText mNewName;

    private String mTargetId;
    private String mDiscussionName;

    private TextView nameSave;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_update_name);
        CloseActivityClass.activityList.add(this);
        getSupportActionBar().setTitle(R.string.de_actionbar_update_discussion);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        if (null != getSupportActionBar()) {
            getSupportActionBar().hide();
        }
        nameSave = (TextView) findViewById(R.id.main_activity_save);

        mNewName = (EditText) findViewById(R.id.et_new_name);
        mNewName.setHint(R.string.de_actionbar_update_discussion);

        if (getIntent() != null && getIntent().hasExtra("DEMO_DISCUSSIONIDS") && getIntent().hasExtra("DEMO_DISCUSSIONNAME")) {

            mTargetId = getIntent().getStringExtra("DEMO_DISCUSSIONIDS");
            mDiscussionName = getIntent().getStringExtra("DEMO_DISCUSSIONNAME");
            mNewName.setText(mDiscussionName);
            mNewName.setSelection(mDiscussionName.length());
        }
        nameSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChangeName();
            }
        });
    }

    public void backTo(View view) {

        finish();
    }

    private void saveChangeName() {
        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null)
            RongIM.getInstance().getRongIMClient().setDiscussionName(mTargetId,
                    mNewName.getText().toString(), new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            WinToast.toast(UpdateDiscussionActivity.this, "讨论组名称修改成功");
                            Intent intent = new Intent();
                            intent.putExtra("UPDATA_DISCUSSION_RESULT", mNewName.getText().toString());
                            setResult(Constants.FIX_DISCUSSION_NAME, intent);
                            finish();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            WinToast.toast(UpdateDiscussionActivity.this, "讨论组名称修改失败");
                        }
                    });
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
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null)
                    RongIM.getInstance().getRongIMClient().setDiscussionName(mTargetId,
                            mNewName.getText().toString(), new RongIMClient.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    WinToast.toast(UpdateDiscussionActivity.this, "讨论组名称修改成功");
                                    Intent intent = new Intent();
                                    intent.putExtra("UPDATA_DISCUSSION_RESULT", mNewName.getText().toString());
                                    setResult(Constants.FIX_DISCUSSION_NAME, intent);
                                    finish();
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    WinToast.toast(UpdateDiscussionActivity.this, "讨论组名称修改失败");
                                }
                            });
                break;

            case android.R.id.home:
                finish();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
