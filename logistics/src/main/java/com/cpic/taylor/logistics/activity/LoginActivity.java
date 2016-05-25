package com.cpic.taylor.logistics.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudModel.Friends;
import com.cpic.taylor.logistics.RongCloudModel.Groups;
import com.cpic.taylor.logistics.RongCloudModel.MyFriends;
import com.cpic.taylor.logistics.RongCloudModel.User;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.base.BaseActivity;
import com.cpic.taylor.logistics.base.RongCloudEvent;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.bean.Login;
import com.cpic.taylor.logistics.utils.ProgressDialogHandle;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;
import com.sea_monster.network.ApiCallback;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Taylor on 2016/5/4.
 */
public class LoginActivity extends BaseActivity implements ApiCallback, Handler.Callback {

    private TextView tvForget;
    private Intent intent;
    private TextView tvRegister;
    private Button btnLogin;
    private Dialog dialog;
    private EditText etName, etPwd;
    private HttpUtils post;
    private RequestParams params;
    private SharedPreferences sp;


    /**
     * 融云登录定义
     */
    public static final String INTENT_IMAIL = "intent_email";
    public static final String INTENT_PASSWORD = "intent_password";
    private boolean isFirst = false;
    private AbstractHttpRequest<User> loginHttpRequest;
    private AbstractHttpRequest<Friends> getUserInfoHttpRequest;
    private AbstractHttpRequest<Groups> mGetMyGroupsRequest;
    private LoadingDialog mDialog;
    private Handler mHandler;
    String userName;
    MyFriends myFriends;
    ArrayList<UserInfos> friendsList = new ArrayList<UserInfos>();
    private int HANDLER_LOGIN_SUCCESS = 1;
    private int HANDLER_LOGIN_FAILURE = 2;
    private int HANDLER_LOGIN_HAS_FOCUS = 3;
    private int HANDLER_LOGIN_HAS_NO_FOCUS = 4;
    private int firstLogin = 0;

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
        etName = (EditText) findViewById(R.id.activity_login_et_name);
        etPwd = (EditText) findViewById(R.id.activity_login_et_pwd);
        dialog = ProgressDialogHandle.getProgressDialog(LoginActivity.this, null);
    }

    @Override
    protected void initData() {
        mHandler = new Handler(LoginActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sp = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        etName.setText(sp.getString("mobile", ""));
        etPwd.setText(sp.getString("pwd", ""));

    }

    @Override
    protected void registerListener() {
        tvForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(LoginActivity.this, ForgetPwdActivity.class);
                startActivity(intent);
            }
        });
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (dialog!=null){
//                    dialog.show();
//                }

//                intent = new Intent(LoginActivity.this,HomeActivity.class);
//                startActivity(intent);
//                loginRongCloud();
                if (etName.getText().toString() == null || etPwd.getText().toString() == null
                        || "".equals(etName.getText().toString()) || "".equals(etPwd.getText().toString())) {
                    showShortToast("用户名和密码不得为空");
                    return;
                }
                loginAction();

            }
        });
    }

    private void loginAction() {
        post = new HttpUtils();
        params = new RequestParams();
        params.addBodyParameter("mobile", etName.getText().toString());
        params.addBodyParameter("password", etPwd.getText().toString());
        params.addBodyParameter("device", JPushInterface.getRegistrationID(getApplicationContext()));
        String url = UrlUtils.POST_URL + UrlUtils.path_login;
        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                super.onStart();
                if (dialog != null) {
                    dialog.show();
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                showShortToast("登录失败，请检查网络连接");
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {


                Login login = JSONObject.parseObject(responseInfo.result, Login.class);
                int code = login.getCode();
                sp = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                if (code == 1) {
                    if (etName.getText().toString().equals(sp.getString("mobile", ""))) {
                        /**
                         * 非第一次登录
                         */
                        firstLogin = 0;
                    } else {
                        /**
                         * 第一次登录
                         */
                        firstLogin = 1;
                    }

                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("isLogin", false);
                    editor.putString("mobile", etName.getText().toString());
                    editor.putString("pwd", etPwd.getText().toString());
                    editor.putString("img", login.getData().getImg());
                    editor.putString("name", login.getData().getName());
                    editor.putString("plate_number", login.getData().getPlate_number());
                    editor.putString("car_models", login.getData().getCar_models());
                    editor.putString("driving_license", login.getData().getDriving_license());
                    editor.putString("token", login.getData().getToken());
                    editor.putString("cloud_id", login.getData().getCloud_id());
                    editor.putString("cloud_token", login.getData().getCloud_token());
                    editor.commit();

                    /**
                     * 融云登录成功
                     */
                    httpGetTokenSuccess(login.getData().getCloud_token());
//                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
//                    finish();
                } else {
                    showShortToast(login.getMsg());
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });
    }


    /**
     * 融云 connect 操作
     *
     * @param token
     */
    private void httpGetTokenSuccess(String token) {


        try {
            RongIM.connect(token, new RongIMClient.ConnectCallback() {
                        @Override
                        public void onTokenIncorrect() {
                            showShortToast("token错误");
                        }

                        @Override
                        public void onSuccess(String userId) {

                            if (isFirst) {
                            } else {
                                final List<UserInfos> list = RongYunContext.getInstance().loadAllUserInfos();
                                if (list == null || list.size() == 0) {
                                }
                            }

                            SharedPreferences.Editor edit = RongYunContext.getInstance().getSharedPreferences().edit();
                            edit.putString(Constants.APP_USER_ID, userId);
                            edit.apply();

                            RongCloudEvent.getInstance().setOtherListener();

                            //请求 demo server 获得自己所加入得群组。
                            //getFriendsFuction();
                            // mGetMyGroupsRequest = RongYunContext.getInstance().getDemoApi().getMyGroups(LoginActivity.this);
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            if (firstLogin == 1) {
                                if (null != RongYunContext.getInstance())
                                    RongYunContext.getInstance().deleteUserInfos();

                               /* if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
                                    ArrayList<Conversation> conversationList = (ArrayList<Conversation>) RongIMClient.getInstance().getConversationList();
                                    if (null != conversationList) {
                                        for (int i = 0; i < conversationList.size(); i++) {
                                            RongIM.getInstance().getRongIMClient().clearConversations(new RongIMClient.ResultCallback<Boolean>() {

                                                @Override
                                                public void onSuccess(Boolean aBoolean) {

                                                    Log.e("Tag","new login"+"清空前用户聊天信息");

                                                }

                                                @Override
                                                public void onError(RongIMClient.ErrorCode errorCode) {

                                                }
                                            }, conversationList.get(i).getConversationType());
                                        }
                                    }
                                }*/
                            }

                            Log.e("Tag", "firstLogin" + firstLogin);

                            String id = sp.getString("cloud_id", "");
                            String name = sp.getString("name", "");
                            String uritest = sp.getString("img", "");
                            UserInfo userInfo = new UserInfo(id, name, Uri.parse(uritest));
                            RongIM.getInstance().setCurrentUserInfo(userInfo);


                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();

                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode e) {
                            Log.e("Tag", "ErrorCode" + e.getValue());
                            mHandler.obtainMessage(HANDLER_LOGIN_FAILURE).sendToTarget();
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onComplete(final AbstractHttpRequest abstractHttpRequest, final Object o) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onCallApiSuccess(abstractHttpRequest, o);
            }
        });
    }

    @Override
    public void onFailure(final AbstractHttpRequest abstractHttpRequest, final BaseException e) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onCallApiFailure(abstractHttpRequest, e);
            }
        });
    }

    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {

        if (loginHttpRequest != null && loginHttpRequest.equals(request)) {
        } else if (getUserInfoHttpRequest != null && getUserInfoHttpRequest.equals(request)) {
        } else if (mGetMyGroupsRequest != null && mGetMyGroupsRequest.equals(request)) {

        }
    }

    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {

        if (loginHttpRequest != null && loginHttpRequest.equals(request)) {
            if (mDialog != null)
                mDialog.dismiss();
        } else if (mGetMyGroupsRequest != null && mGetMyGroupsRequest.equals(request)) {
        }
    }

    @Override
    public boolean handleMessage(Message msg) {

        if (msg.what == HANDLER_LOGIN_FAILURE) {

            if (mDialog != null)
                mDialog.dismiss();

            // WinToast.toast(LoginActivity.this, R.string.login_failure);

        } else if (msg.what == HANDLER_LOGIN_SUCCESS) {

//            if (mDialog != null)
//                mDialog.dismiss();
            if (dialog != null) {
                dialog.dismiss();
            }
            /**
             * 融云登录成功
             */
//            startActivity(new Intent(this, HomeActivity.class));
//            finish();
        } else if (msg.what == HANDLER_LOGIN_HAS_FOCUS) {

        } else if (msg.what == HANDLER_LOGIN_HAS_NO_FOCUS) {

        }

        return false;
    }
}
