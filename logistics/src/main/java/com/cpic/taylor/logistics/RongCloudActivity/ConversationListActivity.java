package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.cpic.taylor.logistics.RongCloudModel.Groups;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.activity.LoginActivity;
import com.cpic.taylor.logistics.base.RongCloudEvent;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.utils.CloseActivityClass;
import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Group;

/**
 * Created by Bob on 15/11/3.
 * 会话列表，需要做 2 件事
 * 1，push 重连，收到 push 消息的时候，做一下 connect 操作
 * 2，获取所加入的群组，并做 syncGroup 操作。demo 逻辑，需要同步当前所加入的群组
 */
public class ConversationListActivity extends BaseApiActivity {

    private static final String TAG = ConversationListActivity.class.getSimpleName();
    private AbstractHttpRequest<Groups> mGetMyGroupsRequest;
    private LoadingDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CloseActivityClass.activityList.add(this);
        mDialog = new LoadingDialog(this);
        Intent intent = getIntent();

        //push
        if (intent.getData().getScheme().equals("rong")
                && intent.getData().getQueryParameter("push") != null) {

            //通过intent.getData().getQueryParameter("push") 为true，判断是否是push消息
            if (intent.getData().getQueryParameter("push").equals("true")) {
                String id = intent.getData().getQueryParameter("pushId");
                RongIM.getInstance().getRongIMClient().recordNotificationEvent(id);

                enterActivity();
            }

        } else {//通知过来
            //程序切到后台，收到消息后点击进入,会执行这里
            if (RongIM.getInstance() == null || RongIM.getInstance().getRongIMClient() == null) {

                enterActivity();
            } else {
                startActivity(new Intent(ConversationListActivity.this, MainActivity.class));
                finish();
            }
        }
    }

    /**
     * 收到 push 消息后，选择进入哪个 Activity
     * 如果程序缓存未被清理，进入 MainActivity
     * 程序缓存被清理，进入 RongCloudLoginActivity，重新获取token
     * <p/>
     * 作用：由于在 manifest 中 intent-filter 是配置在 ConversationListActivity 下面，所以收到消息后点击notifacition 会跳转到 DemoActivity。
     * 以跳到 MainActivity 为例：
     * 在 ConversationListActivity 收到消息后，选择进入 MainActivity，这样就把 MainActivity 激活了，当你读完收到的消息点击 返回键 时，程序会退到
     * MainActivity 页面，而不是直接退回到 桌面。
     */
    private void enterActivity() {

        if (RongYunContext.getInstance() == null)
            return;

        String token = RongYunContext.getInstance().getSharedPreferences()
                .getString(Constants.APP_TOKEN, Constants.DEFAULT);

        if (token.equals(Constants.DEFAULT)) {

            startActivity(new Intent(ConversationListActivity.this, LoginActivity.class));
            finish();
        } else {

            if (mDialog != null && !mDialog.isShowing()) {
                mDialog.show();
            }

            reconnect(token);
        }
    }

    /**
     * @param token
     */
    private void reconnect(String token) {

        RongIM.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {

                Log.e(TAG, "---onTokenIncorrect--");
            }

            @Override
            public void onSuccess(String s) {
                Log.i(TAG, "---onSuccess--" + s);
                if (RongCloudEvent.getInstance() != null)
                    RongCloudEvent.getInstance().setOtherListener();

                if (RongYunContext.getInstance() != null)
                    mGetMyGroupsRequest = RongYunContext.getInstance().getDemoApi().getMyGroups(ConversationListActivity.this);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                Log.e(TAG, "---onError--" + e);
            }
        });

    }

    /**
     * @param obj
     */
    private void getMyGroupApiSuccess(Object obj) {

        if (obj instanceof Groups) {
            final Groups groups = (Groups) obj;

            if (groups.getCode() == 200) {
                List<Group> grouplist = new ArrayList<Group>();
                if (groups.getResult() != null) {
                    for (int i = 0; i < groups.getResult().size(); i++) {

                        String id = groups.getResult().get(i).getId();
                        String name = groups.getResult().get(i).getName();
                        if (groups.getResult().get(i).getPortrait() != null) {
                            Uri uri = Uri.parse(groups.getResult().get(i).getPortrait());
                            grouplist.add(new Group(id, name, uri));
                        } else {
                            grouplist.add(new Group(id, name, null));
                        }
                    }
                    HashMap<String, Group> groupM = new HashMap<String, Group>();
                    for (int i = 0; i < grouplist.size(); i++) {
                        groupM.put(groups.getResult().get(i).getId(), grouplist.get(i));
                    }
                    if (RongYunContext.getInstance() != null)
                        RongYunContext.getInstance().setGroupMap(groupM);

                    if (grouplist.size() > 0)
                        RongIM.getInstance().getRongIMClient().syncGroup(grouplist, new RongIMClient.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                Log.e(TAG, "---syncGroup-onSuccess---");
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                Log.e(TAG, "---syncGroup-onError---");
                            }
                        });

                    if (mDialog != null)
                        mDialog.dismiss();

                    startActivity(new Intent(ConversationListActivity.this, MainActivity.class));
                    finish();

                }
            }
        }
    }


    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        if (mGetMyGroupsRequest != null && mGetMyGroupsRequest.equals(request)) {
            Log.e(TAG, "---push--onCallApiSuccess-");
            getMyGroupApiSuccess(obj);
        }
    }

    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
        Log.e(TAG, "---push--onCallApiFailure-");
    }
}
