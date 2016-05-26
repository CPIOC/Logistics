package com.cpic.taylor.logistics.RongCloudActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudModel.RCUser;
import com.cpic.taylor.logistics.RongCloudModel.Status;
import com.cpic.taylor.logistics.RongCloudModel.User;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.RongCloudWidget.WinToast;
import com.cpic.taylor.logistics.activity.LoginActivity;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.utils.CloseActivityClass;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;
import com.sea_monster.resource.Resource;

import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ContactNotificationMessage;

/**
 * Created by Bob on 2015/4/7.
 * <p/>
 * 个人详情
 */
public class PersonalDetailActivity extends BaseApiActivity {

    private AbstractHttpRequest<Status> mDeleteFriendRequest;
    private AbstractHttpRequest<User> mUserHttpRequest;
    private AbstractHttpRequest<User> getUserInfoByUserIdHttpRequest;

    private LoadingDialog mDialog;
    /**
     * 好友id list
     */
    private List friendList;
    /**
     * 当前页面用户的 UserInfo
     */
    private UserInfo userInfo;
    /**
     * 当前页面用户的 UserId
     */
    private String currentUserId;
    private boolean isSearch;

    private HttpUtils post;
    private RequestParams params;
    private SharedPreferences sp;

    RCUser rcUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_fr_personal_intro);
        CloseActivityClass.activityList.add(this);
        getSupportActionBar().setTitle(R.string.de_actionbar_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        getSupportActionBar().hide();
        mPersonalImg = (AsyncImageView) findViewById(R.id.personal_portrait);
        mPersonalName = (TextView) findViewById(R.id.personal_name);
        mPersonalId = (TextView) findViewById(R.id.personal_id);
        mSendMessage = (Button) findViewById(R.id.send_message);
        mAddFriend = (Button) findViewById(R.id.add_friend);
        text_version_id = (TextView) findViewById(R.id.text_version_id);
        text_licence_id = (TextView) findViewById(R.id.text_licence_id);
        mDialog = new LoadingDialog(this);

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RongIM.getInstance() != null && RongYunContext.getInstance() != null) {
                    if (currentUserId != null)
                        RongIM.getInstance().startPrivateChat(PersonalDetailActivity.this, currentUserId,
                                RongYunContext.getInstance().getUserInfoById(currentUserId).getName());
                    RongIM.getInstance().getRongIMClient().getLatestMessages(Conversation.ConversationType.PRIVATE, currentUserId, 50, new RongIMClient.ResultCallback<List<Message>>() {
                        @Override
                        public void onSuccess(List<Message> messages) {

                            if (null != messages) {
                                for (int i = 0; i < messages.size(); i++) {

                                    Message msg = messages.get(i);

                                    MessageContent messageContent = messages.get(i).getContent();

                                    if (messageContent instanceof ContactNotificationMessage) {

                                        int msgArray[] = new int[1];
                                        msgArray[0] = msg.getMessageId();
                                        if (((ContactNotificationMessage) messageContent).getOperation().equals(ContactNotificationMessage.CONTACT_OPERATION_ACCEPT_RESPONSE)) {

                                        } else {

                                            RongIM.getInstance().getRongIMClient().deleteMessages(msgArray);
                                        }


                                    }


                                }
                            }

                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                }
            }
        });

        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentUserId != null) {
                    if (mDialog != null && !mDialog.isShowing())
                        mDialog.show();
                    getUserinfoAndAddFriend(currentUserId);
                    //mUserHttpRequest = RongYunContext.getInstance().getDemoApi().sendFriendInvite(currentUserId, "请添加我为好友 ", PersonalDetailActivity.this);
                    sp = PreferenceManager.getDefaultSharedPreferences(PersonalDetailActivity.this);
                    //ContactNotificationMessage contact = ContactNotificationMessage.obtain(ContactNotificationMessage.CONTACT_OPERATION_REQUEST, sp.getString("cloud_id", ""), currentUserId, "请求添加为好友");
                    //contact.setMessage(currentUserId);
                    ContactNotificationMessage contact = ContactNotificationMessage.obtain(ContactNotificationMessage.CONTACT_OPERATION_REQUEST, sp.getString("cloud_id", ""), currentUserId, "请求添加为好友");
                    contact.setMessage("请求添加你为好友");
                    sendMessage(contact, currentUserId);


                }
            }
        });

        initData();
        getUserinfoAndAddFriend(currentUserId, "");
    }

    private void sendMessage(MessageContent messageContent, final String currentUserId) {

        RongIM.getInstance().getRongIMClient().sendMessage(Conversation.ConversationType.PRIVATE, currentUserId, messageContent, "", "",
                new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onSuccess(Integer integer) {

                        RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.PRIVATE, currentUserId);

                    }

                    @Override
                    public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {

                    }
                });
    }

    public void getUserinfoAndAddFriend(String cloud_id, String str) {

        post = new HttpUtils();
        params = new RequestParams();
        params.addBodyParameter("cloud_id", cloud_id);
        String url = UrlUtils.POST_URL + UrlUtils.path_getUserinfo;
        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                showShortToast("连接失败，请检查网络连接");
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result;


                try {

                    Gson gson = new Gson();
                    java.lang.reflect.Type type = new TypeToken<RCUser>() {
                    }.getType();
                    rcUser = gson.fromJson(result, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (rcUser.getCode() == 1) {


                    if (null != rcUser.getData()) {
                        if (null == rcUser.getData().get(0).getCar_models()) {
                            text_version_id.setText("");
                        } else {
                            text_version_id.setText(rcUser.getData().get(0).getCar_models());
                        }
                        if (null == rcUser.getData().get(0).getPlate_number()) {
                            text_licence_id.setText("");
                        } else {
                            text_licence_id.setText(rcUser.getData().get(0).getPlate_number());
                        }


                    }

                } else if (rcUser.getCode() == 2) {
                    Toast.makeText(PersonalDetailActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(PersonalDetailActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 10);
                } else {
                    showShortToast(rcUser.getMsg());
                }

            }

        });

    }


    private void addFriend(String user_id) {
        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(PersonalDetailActivity.this);
        params.addBodyParameter("token", sp.getString("token", null));
        params.addBodyParameter("user_id", user_id);
        String url = UrlUtils.POST_URL + UrlUtils.path_apply;
        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                showShortToast("连接失败，请检查网络连接");
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result;
                org.json.JSONObject jsonObj = null;
                try {
                    jsonObj = new org.json.JSONObject(result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if ("1".equals(String.valueOf(jsonObj.getInt("code")))) {

                        showShortToast("请求添加成功");
                        finish();

                    } else if ("2".equals(String.valueOf(jsonObj.getInt("code")))) {
                        Toast.makeText(PersonalDetailActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(PersonalDetailActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }, 10);
                    } else {
                        showShortToast(jsonObj.getString("msg"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    public void getUserinfoAndAddFriend(String cloud_id) {

        post = new HttpUtils();
        params = new RequestParams();
        params.addBodyParameter("cloud_id", cloud_id);
        String url = UrlUtils.POST_URL + UrlUtils.path_getUserinfo;
        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                showShortToast("连接失败，请检查网络连接");
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result;


                try {

                    Gson gson = new Gson();
                    java.lang.reflect.Type type = new TypeToken<RCUser>() {
                    }.getType();
                    rcUser = gson.fromJson(result, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (rcUser.getCode() == 1) {


                    if (null != rcUser.getData()) {
                        if (null != rcUser.getData().get(0)) {
                            addFriend(rcUser.getData().get(0).getId());
                        }

                    }

                } else if (rcUser.getCode() == 2) {
                    Toast.makeText(PersonalDetailActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(PersonalDetailActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 10);
                } else {
                    showShortToast(rcUser.getMsg());
                }

            }

        });

    }

    public void backTo(View view) {
        finish();
    }

    protected void initData() {

        if (RongYunContext.getInstance() == null)
            return;

        friendList = RongYunContext.getInstance().getFriendListId();

        if (getIntent().hasExtra("USER")) {
            userInfo = getIntent().getParcelableExtra("USER");

            currentUserId = userInfo.getUserId();
            if (userInfo != null && friendList != null) {

                if (friendList.contains(userInfo.getUserId())) {
                    mAddFriend.setVisibility(View.GONE);
                    mSendMessage.setVisibility(View.VISIBLE);
                    mPersonalId.setText("Id: " + userInfo.getUserId());
                    mPersonalId.setVisibility(View.GONE);
                } else {
                    mAddFriend.setVisibility(View.VISIBLE);
                    mSendMessage.setVisibility(View.GONE);
                }
                mPersonalImg.setResource(new Resource(userInfo.getPortraitUri()));
                mPersonalName.setText(userInfo.getName());
            }
            sp = PreferenceManager.getDefaultSharedPreferences(PersonalDetailActivity.this);
            if (null != currentUserId && currentUserId.equals(sp.getString("cloud_id", ""))) {
                mAddFriend.setVisibility(View.GONE);
                mSendMessage.setVisibility(View.GONE);
            }

            Log.e("Tag", "currentUserId1" + currentUserId);
        } else if (getIntent().hasExtra("CONTACTS_USER")) {

            currentUserId = getIntent().getStringExtra("CONTACTS_USER");

            userInfo = RongYunContext.getInstance().getUserInfoById(currentUserId);
            mPersonalImg.setResource(new Resource(userInfo.getPortraitUri()));
            mPersonalName.setText(userInfo.getName());
            mPersonalId.setText("Id:" + userInfo.getUserId());
            mPersonalId.setVisibility(View.GONE);
            mAddFriend.setVisibility(View.GONE);
            mSendMessage.setVisibility(View.VISIBLE);
            Log.e("Tag", "currentUserId2" + currentUserId);
        } else if (getIntent().hasExtra("USER_SEARCH")) {
            isSearch = getIntent().getBooleanExtra("USER_SEARCH", false);
            mAddFriend.setVisibility(View.VISIBLE);
            mSendMessage.setVisibility(View.GONE);
            mPersonalImg.setResource(new Resource(userInfo.getPortraitUri()));
            mPersonalName.setText(userInfo.getName());
            currentUserId = userInfo.getUserId();
            Log.e("Tag", "currentUserId3" + currentUserId);
        }

        if (currentUserId != null)
            getUserInfoByUserIdHttpRequest = RongYunContext.getInstance().getDemoApi().getUserInfoByUserId(currentUserId, this);
    }

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        if (mDeleteFriendRequest != null && mDeleteFriendRequest.equals(request)) {
            if (mDialog != null)
                mDialog.dismiss();
            if (obj instanceof Status) {
                final Status status = (Status) obj;
                if (status.getCode() == 200) {
                    WinToast.toast(this, "删除好友成功");
                    if (RongYunContext.getInstance() != null && currentUserId != null) {
                        //删除好友成功后，将这个好友的会话从会话列表删除
                        RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.PRIVATE, currentUserId);
                        RongYunContext.getInstance().updateUserInfos(currentUserId, "2");

                        Intent intent = new Intent();
                        this.setResult(Constants.DELETE_USERNAME_REQUESTCODE, intent);

                    }
                } else if (status.getCode() == 306) {
                    WinToast.toast(this, status.getMessage());
                }
            }
        } else if (getUserInfoByUserIdHttpRequest != null && getUserInfoByUserIdHttpRequest.equals(request)) {
            if (obj instanceof User) {
                final User user = (User) obj;

                if (user.getCode() == 200) {

                    UserInfos addFriend = new UserInfos();
                    addFriend.setUsername(user.getResult().getUsername());
                    addFriend.setUserid(user.getResult().getId());
                    addFriend.setPortrait(user.getResult().getPortrait());
                    if (friendList.contains(user.getResult().getId())) {
                        addFriend.setStatus("1");
                    } else {
                        addFriend.setStatus("0");
                    }

                    if (RongYunContext.getInstance() != null)
                        RongYunContext.getInstance().insertOrReplaceUserInfos(addFriend);

                    mPersonalName.setText(user.getResult().getUsername());

                    RongIM.getInstance().refreshUserInfoCache(new UserInfo(user.getResult().getId(), user.getResult().getUsername(), Uri.parse(user.getResult().getPortrait())));
                }
            }
        } else if (mUserHttpRequest != null && mUserHttpRequest.equals(request)) {
            if (mDialog != null)
                mDialog.dismiss();

            WinToast.toast(this, "好友请求发送成功");
        }
    }

    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
        if (mDialog != null)
            mDialog.dismiss();
        Log.e("PersonalDetailActivity", "-----onCallApiFailure------" + e);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.per_menu, menu);

        if (userInfo != null && friendList != null) {
            if (!friendList.contains(userInfo.getUserId())) {
                menu.getItem(0).setVisible(false);
            }
        } else if (isSearch) {
            menu.getItem(0).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.per_item1://加入黑名单
                if (RongYunContext.getInstance() != null && RongIM.getInstance().getRongIMClient() != null && currentUserId != null) {
                    RongIM.getInstance().getRongIMClient().addToBlacklist(currentUserId, new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            WinToast.toast(PersonalDetailActivity.this, "加入黑名单成功");
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                }

                break;
            case R.id.per_item2://删除好友
                final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
                alterDialog.setMessage("是否删除好友？");
                alterDialog.setCancelable(true);

                alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (RongYunContext.getInstance() != null && currentUserId != null) {

                            if (mDialog != null && !mDialog.isShowing())
                                mDialog.show();

                            mDeleteFriendRequest = RongYunContext.getInstance().getDemoApi().deletefriends(currentUserId, PersonalDetailActivity.this);
                        }
                    }
                });
                alterDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alterDialog.show();

                break;
            case android.R.id.home:
                finish();
                break;

        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * 头像
     */
    private AsyncImageView mPersonalImg;
    /**
     * 昵称
     */
    private TextView mPersonalName;
    /**
     * 用户 id
     */
    private TextView mPersonalId;
    /**
     * 发送消息
     */
    private Button mSendMessage;
    /**
     * 添加到通讯录
     */
    private Button mAddFriend;

    /**
     * Toast短显示
     *
     * @param msg
     */
    /*
    车牌号
     */
    private TextView text_licence_id;

    /**
     * 车型
     */
    private TextView text_version_id;

    protected void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


}
