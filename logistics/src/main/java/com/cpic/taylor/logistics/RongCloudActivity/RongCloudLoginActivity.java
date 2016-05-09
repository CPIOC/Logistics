package com.cpic.taylor.logistics.RongCloudActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.Friends;
import com.cpic.taylor.logistics.RongCloudModel.Groups;
import com.cpic.taylor.logistics.RongCloudModel.User;
import com.cpic.taylor.logistics.base.RongCloudEvent;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.EditTextHolder;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.RongCloudWidget.WinToast;
import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Group;

/**
 * Created by Bob on 2015/1/30.
 */
public class RongCloudLoginActivity extends BaseApiActivity implements View.OnClickListener, Handler.Callback, EditTextHolder.OnEditTextFocusChangeListener {
    private static final String TAG = RongCloudLoginActivity.class.getSimpleName();

    private int REQUEST_CODE_REGISTER = 200;
    public static final String INTENT_IMAIL = "intent_email";
    public static final String INTENT_PASSWORD = "intent_password";
    private int HANDLER_LOGIN_SUCCESS = 1;
    private int HANDLER_LOGIN_FAILURE = 2;
    private int HANDLER_LOGIN_HAS_FOCUS = 3;
    private int HANDLER_LOGIN_HAS_NO_FOCUS = 4;

    private AbstractHttpRequest<User> loginHttpRequest;
    private AbstractHttpRequest<Friends> getUserInfoHttpRequest;
    private AbstractHttpRequest<Groups> mGetMyGroupsRequest;
    private LoadingDialog mDialog;
    private Handler mHandler;

    EditTextHolder mEditUserNameEt;
    EditTextHolder mEditPassWordEt;
    ArrayList<UserInfos> friendsList = new ArrayList<UserInfos>();

    String userName;
    private boolean isFirst = false;
    Drawable drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mLoginImg = (ImageView) findViewById(R.id.de_login_logo);
        mUserNameEt = (EditText) findViewById(R.id.app_username_et);
        mPassWordEt = (EditText) findViewById(R.id.app_password_et);
        mSignInBt = (Button) findViewById(R.id.app_sign_in_bt);
        mRegister = (TextView) findViewById(R.id.de_login_register);
        mFogotPassWord = (TextView) findViewById(R.id.de_login_forgot);
        mImgBackgroud = (ImageView) findViewById(R.id.de_img_backgroud);
        mFrUserNameDelete = (FrameLayout) findViewById(R.id.fr_username_delete);
        mFrPasswordDelete = (FrameLayout) findViewById(R.id.fr_pass_delete);
        mIsShowTitle = (RelativeLayout) findViewById(R.id.de_merge_rel);
        mLeftTitle = (TextView) findViewById(R.id.de_left);
        mRightTitle = (TextView) findViewById(R.id.de_right);

        mSignInBt.setOnClickListener(this);
        mRegister.setOnClickListener(this);
        mLeftTitle.setOnClickListener(this);
        mRightTitle.setOnClickListener(this);
        mUserNameEt.setOnClickListener(this);
        mPassWordEt.setOnClickListener(this);

        drawable = mImgBackgroud.getDrawable();

//        下面的代码为 EditTextView 的展示以及背景动画
        mEditUserNameEt = new EditTextHolder(mUserNameEt, mFrUserNameDelete, null);
        mEditPassWordEt = new EditTextHolder(mPassWordEt, mFrPasswordDelete, null);
        mHandler = new Handler(this);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(RongCloudLoginActivity.this, R.anim.translate_anim);
                mImgBackgroud.startAnimation(animation);
                mEditPassWordEt.setmOnEditTextFocusChangeListener(RongCloudLoginActivity.this);
                mEditUserNameEt.setmOnEditTextFocusChangeListener(RongCloudLoginActivity.this);
            }
        }, 200);


       initData();
//        ViewServer.get(this).addWindow(this);
    }

    protected void initData() {
        mSoftManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mDialog = new LoadingDialog(this);

        if (RongYunContext.getInstance() != null) {
            String email = RongYunContext.getInstance().getSharedPreferences().getString(INTENT_IMAIL, "");
            String password = RongYunContext.getInstance().getSharedPreferences().getString(INTENT_PASSWORD, "");
            mUserNameEt.setText(email);
            mPassWordEt.setText(password);

            String token = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_TOKEN, Constants.DEFAULT);
            String cookie = RongYunContext.getInstance().getSharedPreferences().getString("DEMO_COOKIE", Constants.DEFAULT);
            //当应用第二次打开的时候，需要重新去获取一下这个用户所加入的群组。
            if (!token.equals(Constants.DEFAULT) && !cookie.equals(Constants.DEFAULT)) {

                if (mDialog != null && !mDialog.isShowing()) {
                    mDialog.show();
                }

                httpGetTokenSuccess(token);
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {

        if (msg.what == HANDLER_LOGIN_FAILURE) {

            if (mDialog != null)
                mDialog.dismiss();

            WinToast.toast(RongCloudLoginActivity.this, R.string.login_failure);

            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (msg.what == HANDLER_LOGIN_SUCCESS) {

            if (mDialog != null)
                mDialog.dismiss();

            WinToast.toast(RongCloudLoginActivity.this, R.string.login_success);

            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (msg.what == HANDLER_LOGIN_HAS_FOCUS) {
            mLoginImg.setVisibility(View.GONE);
            mRegister.setVisibility(View.GONE);
            mFogotPassWord.setVisibility(View.GONE);
            mIsShowTitle.setVisibility(View.VISIBLE);
            mLeftTitle.setText(R.string.app_sign_up);
            mRightTitle.setText(R.string.app_fogot_password);
        } else if (msg.what == HANDLER_LOGIN_HAS_NO_FOCUS) {
            mLoginImg.setVisibility(View.VISIBLE);
            mRegister.setVisibility(View.VISIBLE);
            mFogotPassWord.setVisibility(View.VISIBLE);
            mIsShowTitle.setVisibility(View.GONE);
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.app_sign_in_bt://登录
                Login();
                break;
            case R.id.de_left://注册
            case R.id.de_login_register://注册

                Intent intent = new Intent(this, RegisterActivity.class);
                startActivityForResult(intent, REQUEST_CODE_REGISTER);
                break;

            case R.id.de_login_forgot://忘记密码
            case R.id.de_right://忘记密码

                WinToast.toast(RongCloudLoginActivity.this, R.string.app_fogot_password);
                break;

            case R.id.app_username_et:
            case R.id.app_password_et:
                Message mess = Message.obtain();
                mess.what = HANDLER_LOGIN_HAS_FOCUS;
                mHandler.sendMessage(mess);
                break;
        }
    }

    /**
     * 点击登录按钮，登录
     */
    private void Login() {
        userName = mUserNameEt.getEditableText().toString();
        String passWord = mPassWordEt.getEditableText().toString();
        String name = null;

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(passWord)) {
            WinToast.toast(RongCloudLoginActivity.this, R.string.login_erro_is_null);
            return;
        }

        if (mDialog != null && !mDialog.isShowing())
            mDialog.show();


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

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {

        if (loginHttpRequest != null && loginHttpRequest.equals(request)) {
            loginApiSuccess(obj);
        } else if (getUserInfoHttpRequest != null && getUserInfoHttpRequest.equals(request)) {
            getFriendsApiSuccess(obj);
        } else if (mGetMyGroupsRequest != null && mGetMyGroupsRequest.equals(request)) {
            getMyGroupApiSuccess(obj);
        }
    }

    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {

        if (loginHttpRequest != null && loginHttpRequest.equals(request)) {
            if (mDialog != null)
                mDialog.dismiss();
        } else if (mGetMyGroupsRequest != null && mGetMyGroupsRequest.equals(request)) {
            Log.e(TAG, "-------getGroup failure----");
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
                    edit.putString(INTENT_PASSWORD, mPassWordEt.getText().toString());
                    edit.putString(INTENT_IMAIL, mUserNameEt.getText().toString());
                    edit.putBoolean("DEMO_ISFIRST", false);
                    edit.apply();

                    Log.i(TAG, "----login success---");
                }
            } else if (user.getCode() == 103) {

                if (mDialog != null)
                    mDialog.dismiss();

                WinToast.toast(RongCloudLoginActivity.this, R.string.app_pass_error);
            } else if (user.getCode() == 104) {

                if (mDialog != null)
                    mDialog.dismiss();

                WinToast.toast(RongCloudLoginActivity.this, R.string.app_user_error);
            } else if (user.getCode() == 111) {
                if (mDialog != null)
                    mDialog.dismiss();
                WinToast.toast(RongCloudLoginActivity.this, R.string.app_user_cookie);
            }
        }
    }

    /**
     * 融云 第二步：connect 操作
     *
     * @param token
     */
    private void httpGetTokenSuccess(String token) {

        try {
            Log.i(TAG, "----connect token--"+token);
            RongIM.connect(token, new RongIMClient.ConnectCallback() {
                        @Override
                        public void onTokenIncorrect() {
                            Log.e(TAG, "----connect onTokenIncorrect--");
                        }

                        @Override
                        public void onSuccess(String userId) {

                            Log.d(TAG, "----connect onSuccess userId----:" + userId);

                            if (isFirst) {
                                getUserInfoHttpRequest = RongYunContext.getInstance().getDemoApi().getFriends(RongCloudLoginActivity.this);
                                RongYunContext.getInstance().deleteUserInfos();
                            } else {
                                final List<UserInfos> list = RongYunContext.getInstance().loadAllUserInfos();
                                if (list == null || list.size() == 0) {
                                    //请求网络
                                    getUserInfoHttpRequest = RongYunContext.getInstance().getDemoApi().getFriends(RongCloudLoginActivity.this);
                                }
                            }

                            SharedPreferences.Editor edit = RongYunContext.getInstance().getSharedPreferences().edit();
                            edit.putString(Constants.APP_USER_ID, userId);
                            edit.apply();

                            RongCloudEvent.getInstance().setOtherListener();

                            //请求 demo server 获得自己所加入得群组。
                            mGetMyGroupsRequest = RongYunContext.getInstance().getDemoApi().getMyGroups(RongCloudLoginActivity.this);
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode e) {
                            mHandler.obtainMessage(HANDLER_LOGIN_FAILURE).sendToTarget();
                            Log.e(TAG, "----connect onError ErrorCode----:" + e);
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
                        Log.i(TAG, "----get MyGroup id----" + groups.getResult().get(i).getId());
                    }

                    if (RongYunContext.getInstance() != null)
                        RongYunContext.getInstance().setGroupMap(groupM);

                    mHandler.obtainMessage(HANDLER_LOGIN_SUCCESS).sendToTarget();

                    if ( RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
//                    if (grouplist.size() > 0 && RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {

                        final long time1 = System.currentTimeMillis();
                        RongIM.getInstance().getRongIMClient().syncGroup(grouplist, new RongIMClient.OperationCallback() {

                            @Override
                            public void onSuccess() {
                                Log.i(TAG, "---syncGroup-onSuccess---");

                                long time2 = System.currentTimeMillis() - time1;

                                Log.e(TAG, "-----syncGroup-onSuccess-" + time2);

                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                Log.e(TAG, "---syncGroup-onError---" + errorCode);
                                long time2 = System.currentTimeMillis() - time1;

                                Log.e(TAG, "-----syncGroup-onError-" + time2);
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
                        if(RongYunContext.getInstance()!=null){
                            String id = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_ID,Constants.DEFAULT);
                            String name = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_NAME,Constants.DEFAULT);
                            String portrait = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_PORTRAIT,Constants.DEFAULT);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_REGISTER && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mUserNameEt.setText(data.getStringExtra(INTENT_IMAIL));
                mPassWordEt.setText(data.getStringExtra(INTENT_PASSWORD));
            }
        }
    }

    @Override
    public void onEditTextFocusChange(View v, boolean hasFocus) {
        Message mess = Message.obtain();
        switch (v.getId()) {
            case R.id.app_username_et:
            case R.id.app_password_et:
                if (hasFocus) {
                    mess.what = HANDLER_LOGIN_HAS_FOCUS;
                }
                mHandler.sendMessage(mess);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                mSoftManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                Message mess = Message.obtain();
                mess.what = HANDLER_LOGIN_HAS_NO_FOCUS;
                mHandler.sendMessage(mess);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        event.getKeyCode();
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                Message mess = Message.obtain();
                mess.what = HANDLER_LOGIN_HAS_NO_FOCUS;
                mHandler.sendMessage(mess);
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    protected void onPause() {
        super.onPause();
        if (mSoftManager == null) {
            mSoftManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (getCurrentFocus() != null) {
            mSoftManager.hideSoftInputFromWindow(getCurrentFocus()
                    .getWindowToken(), 0);// 隐藏软键盘
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        ViewServer.get(this).setFocusedWindow(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        ViewServer.get(this).removeWindow(this);
//        if (drawable instanceof BitmapDrawable) {
//            Log.e(TAG, "------OnDestory---");
//            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//            Bitmap bitmap = bitmapDrawable.getBitmap();
//            bitmap.recycle();
//        }

        if (loginHttpRequest != null || getUserInfoHttpRequest != null || mGetMyGroupsRequest != null) {
            loginHttpRequest = null;
            getUserInfoHttpRequest = null;
            mGetMyGroupsRequest = null;
            Log.e(TAG, "------OnDestory--loginHttpRequest-");
        }
    }

    /**
     * 用户账户
     */
    private EditText mUserNameEt;
    /**
     * 密码
     */
    private EditText mPassWordEt;
    /**
     * 登录button
     */
    private Button mSignInBt;
    /**
     * 忘记密码
     */
    private TextView mFogotPassWord;
    /**
     * 注册
     */
    private TextView mRegister;
    /**
     * 输入用户名删除按钮
     */
    private FrameLayout mFrUserNameDelete;
    /**
     * 输入密码删除按钮
     */
    private FrameLayout mFrPasswordDelete;
    /**
     * logo
     */
    private ImageView mLoginImg;
    /**
     * 软键盘的控制
     */
    private InputMethodManager mSoftManager;
    /**
     * 是否展示title
     */
    private RelativeLayout mIsShowTitle;
    /**
     * 左侧title
     */
    private TextView mLeftTitle;
    /**
     * 右侧title
     */
    private TextView mRightTitle;
    private ImageView mImgBackgroud;
}
