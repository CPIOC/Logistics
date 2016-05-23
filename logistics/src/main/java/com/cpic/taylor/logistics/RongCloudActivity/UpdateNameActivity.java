package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.Status;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.RongCloudWidget.WinToast;
import com.cpic.taylor.logistics.utils.CloseActivityClass;
import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;


/**
 * Created by Administrator on 2015/3/3.
 */
public class UpdateNameActivity extends BaseApiActivity {

    private EditText mNewName;//昵称

    private AbstractHttpRequest<Status> httpRequest;

    private LoadingDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_update_name);
        CloseActivityClass.activityList.add(this);
        getSupportActionBar().setTitle(R.string.my_username);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);

        mNewName = (EditText) findViewById(R.id.et_new_name);
        mDialog = new LoadingDialog(this);

        if (getIntent() != null) {
            mNewName.setText(getIntent().getStringExtra("USERNAME"));
            mNewName.setSelection(getIntent().getStringExtra("USERNAME").length());
        }
    }

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        if (request == httpRequest) {
            if (obj instanceof Status) {
                Status status = (Status) obj;

                if (status.getCode() == 200) {
                    WinToast.toast(this, R.string.update_profile_success);
                    if (mDialog != null)
                        mDialog.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra("UPDATA_RESULT", mNewName.getText().toString());
                    this.setResult(Constants.FIX_USERNAME_REQUESTCODE, intent);
                    SharedPreferences.Editor edit = RongYunContext.getInstance().getSharedPreferences().edit();
                    edit.putString(Constants.APP_USER_NAME, mNewName.getText().toString());
                    edit.apply();
                    finish();
                }
            }
        }
    }

    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
        WinToast.toast(this, R.string.update_profile_faiture);
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

                if(RongYunContext.getInstance() == null)
                    return  true;

                if(!TextUtils.isEmpty(mNewName.getText().toString())){

                    if (mDialog != null && !mDialog.isShowing())
                        mDialog.show();

                    httpRequest = RongYunContext.getInstance().getDemoApi().updateProfile(mNewName.getText().toString(), this);

                } else {
                    WinToast.toast(this, R.string.profile_not_null);
                }
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
