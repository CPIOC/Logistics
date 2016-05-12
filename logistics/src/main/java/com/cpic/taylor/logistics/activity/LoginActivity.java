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
import com.cpic.taylor.logistics.RongCloudModel.User;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.RongCloudWidget.WinToast;
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
import java.util.HashMap;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Group;

/**
 * Created by Taylor on 2016/5/4.
 */
public class LoginActivity extends BaseActivity implements ApiCallback, Handler.Callback{

    private TextView tvForget;
    private Intent intent;
    private TextView tvRegister;
    private Button btnLogin;
    private Dialog dialog;
    private EditText etName,etPwd;
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
    ArrayList<UserInfos> friendsList = new ArrayList<UserInfos>();
    private int HANDLER_LOGIN_SUCCESS = 1;
    private int HANDLER_LOGIN_FAILURE = 2;
    private int HANDLER_LOGIN_HAS_FOCUS = 3;
    private int HANDLER_LOGIN_HAS_NO_FOCUS = 4;

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
        dialog = ProgressDialogHandle.getProgressDialog(LoginActivity.this,null);
    }

    @Override
    protected void initData() {
        mHandler = new Handler(LoginActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sp = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        etName.setText(sp.getString("mobile",""));
        etPwd.setText(sp.getString("pwd",""));

    }

    @Override
    protected void registerListener() {
        tvForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(LoginActivity.this,ForgetPwdActivity.class);
                startActivity(intent);
            }
        });
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(LoginActivity.this,RegisterActivity.class);
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
        params.addBodyParameter("mobile",etName.getText().toString());
        params.addBodyParameter("password",etPwd.getText().toString());

        String url = UrlUtils.POST_URL+UrlUtils.path_login;
        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                super.onStart();
                if (dialog != null){
                    dialog.show();
                }
            }
            @Override
            public void onFailure(HttpException e, String s) {
                if (dialog != null){
                    dialog.dismiss();
                }
                showShortToast("登录失败，请检查网络连接");
            }
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {

                Login login = JSONObject.parseObject(responseInfo.result,Login.class);
                int code = login.getCode();
                if (code == 1){

                    sp = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("mobile",etName.getText().toString());
                    editor.putString("pwd",etPwd.getText().toString());
                    editor.putString("img",login.getData().getImg());
                    editor.putString("plate_number",login.getData().getPlate_number());
                    editor.putString("car_models",login.getData().getCar_models());
                    editor.putString("driving_license",login.getData().getDriving_license());
                    editor.putString("token",login.getData().getToken());
                    editor.commit();
                    httpGetTokenSuccess(login.getData().getCloud_token());
                }else{
                    showShortToast(login.getMsg());
                }

            }

        });


    }
    /**
     * 登录融云
     */
    private void loginRongCloud() {

        userName = "1149140370@qq.com";
        String passWord = "12345";
        String name = null;


        //发起登录 http请求 (注：非融云SDK接口，是demo接口)
        if (RongYunContext.getInstance() != null) {
            //如果切换了一个用户，token和 cookie 都需要重新获取
            name = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_NAME, Constants.DEFAULT);

            if (!userName.equals(name)) {

                loginHttpRequest = RongYunContext.getInstance().getDemoApi().loginToken(userName, passWord, this);
                isFirst = true;
            } else {
                isFirst = false;
                String cookie = RongYunContext.getInstance().getSharedPreferences().getString("DEMO_COOKIE", Constants.DEFAULT);
                String token = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_TOKEN, Constants.DEFAULT);
                if (!cookie.equals(Constants.DEFAULT) && !token.equals(Constants.DEFAULT)) {
                    httpGetTokenSuccess(token);
                } else {
                    loginHttpRequest = RongYunContext.getInstance().getDemoApi().loginToken(userName, passWord, this);
                }
            }
        }


    }


    /**
     * 以下是demo 的逻辑，与sdk 没有关系。
     * 1，登录成功，获得 cookie 和 token
     *
     * @param obj
     */
    private void loginApiSuccess(Object obj) {
        if (obj instanceof User) {
            final User user = (User) obj;
            if (user.getCode() == 200) {
                if (RongYunContext.getInstance() != null && user.getResult() != null) {
                    //获得token，通过token 去做 connect 操作
                    httpGetTokenSuccess(user.getResult().getToken());

                    SharedPreferences.Editor edit = RongYunContext.getInstance().getSharedPreferences().edit();
                    edit.putString(Constants.APP_USER_NAME, user.getResult().getUsername());
                    edit.putString(Constants.APP_USER_PORTRAIT, user.getResult().getPortrait());
                    edit.putString(Constants.APP_TOKEN, user.getResult().getToken());
                    edit.putString(INTENT_PASSWORD, "12345");
                    edit.putString(INTENT_IMAIL, "1149140370@qq.com");
                    edit.putBoolean("DEMO_ISFIRST", false);
                    edit.apply();

                }
            } else if (user.getCode() == 103) {

                WinToast.toast(LoginActivity.this, R.string.app_pass_error);
            } else if (user.getCode() == 104) {
                WinToast.toast(LoginActivity.this, R.string.app_user_error);
            } else if (user.getCode() == 111) {

                WinToast.toast(LoginActivity.this, R.string.app_user_cookie);
            }
        }
    }

    /**
     * 融云 第二步：connect 操作
     *
     * @param token
     */
    private void httpGetTokenSuccess(String token) {

        Log.e("Tag",token);

        try {
            RongIM.connect(token, new RongIMClient.ConnectCallback() {
                        @Override
                        public void onTokenIncorrect() {
                            showShortToast("token错误");
                        }

                        @Override
                        public void onSuccess(String userId) {


                            if (isFirst) {
                                getUserInfoHttpRequest = RongYunContext.getInstance().getDemoApi().getFriends(LoginActivity.this);
                                RongYunContext.getInstance().deleteUserInfos();
                            } else {
                                final List<UserInfos> list = RongYunContext.getInstance().loadAllUserInfos();
                                if (list == null || list.size() == 0) {
                                    //请求网络
                                    getUserInfoHttpRequest = RongYunContext.getInstance().getDemoApi().getFriends(LoginActivity.this);
                                }
                            }

                            SharedPreferences.Editor edit = RongYunContext.getInstance().getSharedPreferences().edit();
                            edit.putString(Constants.APP_USER_ID, userId);
                            edit.apply();

                            RongCloudEvent.getInstance().setOtherListener();

                            //请求 demo server 获得自己所加入得群组。
                            mGetMyGroupsRequest = RongYunContext.getInstance().getDemoApi().getMyGroups(LoginActivity.this);
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode e) {
                            mHandler.obtainMessage(HANDLER_LOGIN_FAILURE).sendToTarget();
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得自己所加入得群组成功以后，调用 syncGroup 方法，同步自己所加入得群组
     *
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

                    mHandler.obtainMessage(HANDLER_LOGIN_SUCCESS).sendToTarget();

                    if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
//                    if (grouplist.size() > 0 && RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {

                        final long time1 = System.currentTimeMillis();
                        RongIM.getInstance().getRongIMClient().syncGroup(grouplist, new RongIMClient.OperationCallback() {

                            @Override
                            public void onSuccess() {

                                long time2 = System.currentTimeMillis() - time1;


                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                long time2 = System.currentTimeMillis() - time1;

                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * 获得好友列表
     * 获取好友列表接口  返回好友数据  (注：非融云SDK接口，是demo接口)
     *
     * @param obj
     */
    private void getFriendsApiSuccess(Object obj) {
        if (obj instanceof Friends) {
            final Friends friends = (Friends) obj;
            if (friends.getCode() == 200) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < friends.getResult().size(); i++) {
                            UserInfos userInfos = new UserInfos();

                            userInfos.setUserid(friends.getResult().get(i).getId());
                            userInfos.setUsername(friends.getResult().get(i).getUsername());
                            userInfos.setStatus("1");
                            if (friends.getResult().get(i).getPortrait() != null)
                                userInfos.setPortrait(friends.getResult().get(i).getPortrait());
                            friendsList.add(userInfos);
                        }

                        UserInfos addFriend = new UserInfos();
                        addFriend.setUsername("新好友消息");
                        addFriend.setUserid("10000");
                        addFriend.setPortrait("test");
                        addFriend.setStatus("0");
                        friendsList.add(addFriend);

                        UserInfos addUserInfo = new UserInfos();
                        if (RongYunContext.getInstance() != null) {
                            String id = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_ID, Constants.DEFAULT);
                            String name = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_NAME, Constants.DEFAULT);
                            String portrait = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_PORTRAIT, Constants.DEFAULT);

                            addUserInfo.setUsername(name);
                            addUserInfo.setUserid(id);
                            addUserInfo.setPortrait(portrait);
                            addUserInfo.setStatus("0");
                            friendsList.add(addUserInfo);
                        }


                        if (friendsList != null) {
                            for (UserInfos friend : friendsList) {
                                UserInfos f = new UserInfos();
                                f.setUserid(friend.getUserid());
                                f.setUsername(friend.getUsername());
                                f.setPortrait(friend.getPortrait());
                                f.setStatus(friend.getStatus());
                                RongYunContext.getInstance().insertOrReplaceUserInfos(f);
                            }
                        }
                    }
                });
            }
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
            loginApiSuccess(obj);
        } else if (getUserInfoHttpRequest != null && getUserInfoHttpRequest.equals(request)) {
            getFriendsApiSuccess(obj);
        } else if (mGetMyGroupsRequest != null && mGetMyGroupsRequest.equals(request)) {
            getMyGroupApiSuccess(obj);
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
            if (dialog !=null){
                dialog.dismiss();
            }
            /**
             * 融云登录成功
             */
           // WinToast.toast(LoginActivity.this, R.string.login_success);
            startActivity(new Intent(this,HomeActivity.class));
            finish();
        } else if (msg.what == HANDLER_LOGIN_HAS_FOCUS) {

        } else if (msg.what == HANDLER_LOGIN_HAS_NO_FOCUS) {

        }

        return false;
    }
}
