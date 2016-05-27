package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudModel.Groups;
import com.cpic.taylor.logistics.RongCloudModel.RCUser;
import com.cpic.taylor.logistics.RongCloudModel.RongEvent;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.RongCloudWidget.WinToast;
import com.cpic.taylor.logistics.activity.LoginActivity;
import com.cpic.taylor.logistics.base.RongCloudEvent;
import com.cpic.taylor.logistics.base.RongYunApi;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.fragment.UriFragment;
import io.rong.imkit.widget.AlterDialogFragment;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imkit.widget.provider.TextInputProvider;
import io.rong.imlib.MessageTag;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.TypingMessage.TypingStatus;
import io.rong.imlib.location.RealTimeLocationConstant;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

//import io.rong.imlib.TypingMessage.TypingStatus;

/**
 * Created by Bob on 15/11/3.
 * 会话页面
 * 1，设置 ActionBar title
 * 2，加载会话页面
 * 3，push 和 通知 判断
 */
public class ConversationActivity extends BaseApiActivity implements RongIMClient.RealTimeLocationListener, Handler.Callback {

    private String TAG = ConversationActivity.class.getSimpleName();
    /**
     * 对方id
     */
    private String mTargetId;
    /**
     * 刚刚创建完讨论组后获得讨论组的targetIds
     */
    private String mTargetIds;
    /**
     * 会话类型
     */
    private Conversation.ConversationType mConversationType;

    /**
     * title
     */
    private String title;
    /**
     * 是否在讨论组内，如果不在讨论组内，则进入不到讨论组设置页面
     */
    private boolean isDiscussion = false;

    private RelativeLayout mRealTimeBar;//real-time bar
    private RealTimeLocationConstant.RealTimeLocationStatus currentLocationStatus;
    private AbstractHttpRequest<Groups> mGetMyGroupsRequest;
    private LoadingDialog mDialog;

    private final String TextTypingTitle = "对方正在输入...";
    private final String VoiceTypingTitle = "对方正在讲话...";

    private Handler mHandler;

    public static final int SET_TEXT_TYPING_TITLE = 1;
    public static final int SET_VOICE_TYPING_TITLE = 2;
    public static final int SET_TARGETID_TITLE = 0;

    private TextView chatNameTv;
    private ImageView chatLogo;

    private HttpUtils post;
    private RequestParams params;
    private SharedPreferences sp;
    RCUser rcUser;
    /**
     * 是否是朋友0：不是朋友   1：是朋友
     */
    private int chatType = 0;

    private PopupWindow popupApplyFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation);
        CloseActivityClass.activityList.add(this);
        chatNameTv = (TextView) findViewById(R.id.chat_activity_title);
        chatLogo = (ImageView) findViewById(R.id.chat_member_logo);
        mDialog = new LoadingDialog(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.dingbu_fanhui2);
        getSupportActionBar().hide();

        Intent intent = getIntent();

        if (intent == null || intent.getData() == null)
            return;

        mTargetId = intent.getData().getQueryParameter("targetId");
        //10000 为 Demo Server 加好友的 id，若 targetId 为 10000，则为加好友消息，默认跳转到 NewFriendListActivity
        // Demo 逻辑
        if (mTargetId != null && mTargetId.equals("10000")) {
            startActivity(new Intent(ConversationActivity.this, NewFriendListActivity.class));
            return;
        }
        //intent.getData().getLastPathSegment();//获得当前会话类型
        mConversationType = Conversation.ConversationType.valueOf(intent.getData()
                .getLastPathSegment().toUpperCase(Locale.getDefault()));

        title = intent.getData().getQueryParameter("title");

        mTargetIds = intent.getData().getQueryParameter("targetIds");

        setActionBarTitle(mConversationType, mTargetId);

        //讨论组 @ 消息
        checkTextInputEditTextChanged();

        isPushMessage(intent);

        //地理位置共享，若不是用地理位置共享，可忽略
        setRealTime();


        if ("ConversationActivity".equals(this.getClass().getSimpleName()))
            EventBus.getDefault().register(this);

        mHandler = new Handler(this);

        RongIMClient.setTypingStatusListener(new RongIMClient.TypingStatusListener() {
            @Override
            public void onTypingStatusChanged(Conversation.ConversationType type, String targetId, Collection<TypingStatus> typingStatusSet) {
                //当输入状态的会话类型和targetID与当前会话一致时，才需要显示
                if (type.equals(mConversationType) && targetId.equals(mTargetId)) {
                    int count = typingStatusSet.size();
                    RLog.d(this, "onTypingStatusChanged", "count = " + count);
                    //count表示当前会话中正在输入的用户数量，目前只支持单聊，所以判断大于0就可以给予显示了
                    if (count > 0) {
                        Iterator iterator = typingStatusSet.iterator();
                        TypingStatus status = (TypingStatus) iterator.next();
                        String objectName = status.getTypingContentType();

                        MessageTag textTag = TextMessage.class.getAnnotation(MessageTag.class);
                        MessageTag voiceTag = VoiceMessage.class.getAnnotation(MessageTag.class);
                        //匹配对方正在输入的是文本消息还是语音消息
                        if (objectName.equals(textTag.value())) {
                            mHandler.sendEmptyMessage(SET_TEXT_TYPING_TITLE);
                        } else if (objectName.equals(voiceTag.value())) {
                            mHandler.sendEmptyMessage(SET_VOICE_TYPING_TITLE);
                        }
                    } else {//当前会话没有用户正在输入，标题栏仍显示原来标题
                        mHandler.sendEmptyMessage(SET_TARGETID_TITLE);
                    }
                }
            }
        });

        chatLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chatType==0){

                    /*Intent intent=new Intent(ConversationActivity.this,PersonalDetailActivity.class);
                    startActivity(intent);*/
                    showApplyFriend(chatLogo,mTargetId);

                }else{

                    enterSettingActivity();

                }

            }
        });

    }

    public void backTo(View view) {
        finish();
    }

    public void goToNext(View view) {
        enterSettingActivity();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent == null || intent.getData() == null)
            return;

        mTargetId = intent.getData().getQueryParameter("targetId");

        mConversationType = Conversation.ConversationType.valueOf(intent.getData()
                .getLastPathSegment().toUpperCase(Locale.getDefault()));

        title = intent.getData().getQueryParameter("title");

        mTargetIds = intent.getData().getQueryParameter("targetIds");

        setActionBarTitle(mConversationType, mTargetId);

        ConversationFragment fragment = (ConversationFragment) getSupportFragmentManager().findFragmentById(R.id.conversation);

        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversation").appendPath(mConversationType.getName().toLowerCase())
                .appendQueryParameter("targetId", mTargetId).build();

        fragment.setUri(uri);
    }


    private String mEditText;

    private void checkTextInputEditTextChanged() {

        InputProvider.MainInputProvider provider = RongContext.getInstance().getPrimaryInputProvider();
        if (provider instanceof TextInputProvider) {
            TextInputProvider textInputProvider = (TextInputProvider) provider;
            textInputProvider.setEditTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (mConversationType.equals(Conversation.ConversationType.DISCUSSION)) {

                        if (s.length() > 0) {
                            String str = s.toString().substring(s.toString().length() - 1, s.toString().length());

                            if (str.equals("@")) {

                                Intent intent = new Intent(ConversationActivity.this, NewTextMessageActivity.class);
                                intent.putExtra("DEMO_REPLY_CONVERSATIONTYPE", mConversationType.toString());

                                if (mTargetIds != null) {
                                    UriFragment fragment = (UriFragment) getSupportFragmentManager().getFragments().get(0);
                                    //得到讨论组的 targetId
                                    mTargetId = fragment.getUri().getQueryParameter("targetId");
                                }
                                intent.putExtra("DEMO_REPLY_TARGETID", mTargetId);
                                startActivityForResult(intent, 29);

                                mEditText = s.toString();
                            }
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    /**
     * 判断是否是 Push 消息，判断是否需要做 connect 操作
     *
     * @param intent
     */
    private void isPushMessage(Intent intent) {

        if (intent == null || intent.getData() == null)
            return;

        //push
        if (intent.getData().getScheme().equals("rong")
                && intent.getData().getQueryParameter("push") != null) {

            //通过intent.getData().getQueryParameter("push") 为true，判断是否是push消息
            if (intent.getData().getQueryParameter("push").equals("true")) {
                //只有收到系统消息和不落地 push 消息的时候，pushId 不为 null。而且这两种消息只能通过 server 来发送，客户端发送不了。
                String id = intent.getData().getQueryParameter("pushId");
                RongIM.getInstance().getRongIMClient().recordNotificationEvent(id);

                if (mDialog != null && !mDialog.isShowing()) {
                    mDialog.show();
                }

                enterActivity();
            }

        } else {//通知过来
            //程序切到后台，收到消息后点击进入,会执行这里
            if (RongIM.getInstance() == null || RongIM.getInstance().getRongIMClient() == null) {

                if (mDialog != null && !mDialog.isShowing()) {
                    mDialog.show();
                }
                enterActivity();
            }
        }
    }


    /**
     * 收到 push 消息后，选择进入哪个 Activity
     * 如果程序缓存未被清理，进入 MainActivity
     * 程序缓存被清理，进入 RongCloudLoginActivity，重新获取token
     * <p/>
     * 作用：由于在 manifest 中 intent-filter 是配置在 ConversationActivity 下面，所以收到消息后点击notifacition 会跳转到 DemoActivity。
     * 以跳到 MainActivity 为例：
     * 在 ConversationActivity 收到消息后，选择进入 MainActivity，这样就把 MainActivity 激活了，当你读完收到的消息点击 返回键 时，程序会退到
     * MainActivity 页面，而不是直接退回到 桌面。
     */
    private void enterActivity() {

        if (RongYunContext.getInstance() == null)
            return;

        String token = RongYunContext.getInstance().getSharedPreferences()
                .getString(Constants.APP_TOKEN, Constants.DEFAULT);

        if (token.equals(Constants.DEFAULT)) {

            startActivity(new Intent(ConversationActivity.this, LoginActivity.class));
            finish();
        } else {
            reconnect(token);
        }
    }

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
                    mGetMyGroupsRequest = RongYunContext.getInstance().getDemoApi().getMyGroups(ConversationActivity.this);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                Log.e(TAG, "---onError--" + e);
            }
        });

    }


    /**
     * 设置会话页面 Title
     *
     * @param conversationType 会话类型
     * @param targetId         目标 Id
     */
    private void setActionBarTitle(Conversation.ConversationType conversationType, String targetId) {

        if (conversationType == null)
            return;

        if (RongIM.getInstance() == null || RongIM.getInstance().getRongIMClient() == null)
            return;

        if (conversationType.equals(Conversation.ConversationType.PRIVATE)) {
            setPrivateActionBar(targetId);
        } else if (conversationType.equals(Conversation.ConversationType.GROUP)) {
            setGroupActionBar(targetId);
        } else if (conversationType.equals(Conversation.ConversationType.DISCUSSION)) {
            setDiscussionActionBar(targetId, mTargetIds);
            // chatNameTv.setText(title);
        } else if (conversationType.equals(Conversation.ConversationType.CHATROOM)) {
            getSupportActionBar().setTitle(title);
            chatNameTv.setText(title);
        } else if (conversationType.equals(Conversation.ConversationType.SYSTEM)) {
            getSupportActionBar().setTitle(R.string.de_actionbar_system);
            chatNameTv.setText(R.string.de_actionbar_system);
        } else if (conversationType.equals(Conversation.ConversationType.APP_PUBLIC_SERVICE)) {
            setAppPublicServiceActionBar(targetId);
        } else if (conversationType.equals(Conversation.ConversationType.PUBLIC_SERVICE)) {
            setPublicServiceActionBar(targetId);
        } else if (conversationType.equals(Conversation.ConversationType.CUSTOMER_SERVICE)) {
            getSupportActionBar().setTitle(R.string.main_customer);
            chatNameTv.setText(R.string.main_customer);
        } else {
            getSupportActionBar().setTitle(R.string.de_actionbar_sub_defult);
            chatNameTv.setText(R.string.de_actionbar_sub_defult);
        }

    }

    /**
     * 设置群聊界面 ActionBar
     *
     * @param targetId
     */
    private void setGroupActionBar(String targetId) {
        if (targetId == null)
            return;

        if (RongYunContext.getInstance() != null) {
            getSupportActionBar().setTitle(RongYunContext.getInstance().getGroupNameById(targetId));
            chatNameTv.setText(RongYunContext.getInstance().getGroupNameById(targetId));
        }
    }

    /**
     * 设置应用公众服务界面 ActionBar
     */
    private void setAppPublicServiceActionBar(String targetId) {
        if (targetId == null)
            return;
        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {

            RongIM.getInstance().getRongIMClient().getPublicServiceProfile(Conversation.PublicServiceType.APP_PUBLIC_SERVICE
                    , targetId, new RongIMClient.ResultCallback<PublicServiceProfile>() {
                        @Override
                        public void onSuccess(PublicServiceProfile publicServiceProfile) {
                            getSupportActionBar().setTitle(publicServiceProfile.getName().toString());
                            chatNameTv.setText(publicServiceProfile.getName().toString());
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
        }
    }

    /**
     * 设置公共服务号 ActionBar
     */
    private void setPublicServiceActionBar(String targetId) {

        if (targetId == null)
            return;

        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {

            RongIM.getInstance().getRongIMClient().getPublicServiceProfile(Conversation.PublicServiceType.PUBLIC_SERVICE
                    , targetId, new RongIMClient.ResultCallback<PublicServiceProfile>() {
                        @Override
                        public void onSuccess(PublicServiceProfile publicServiceProfile) {

                            if (publicServiceProfile != null && publicServiceProfile.getName() != null)

                                getSupportActionBar().setTitle(publicServiceProfile.getName());
                            chatNameTv.setText(publicServiceProfile.getName());
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
        }
    }

    /**
     * 设置讨论组界面 ActionBar
     */
    private void setDiscussionActionBar(String targetId, String targetIds) {

        if (targetId != null) {

            RongIM.getInstance().getRongIMClient().getDiscussion(targetId
                    , new RongIMClient.ResultCallback<Discussion>() {
                        @Override
                        public void onSuccess(Discussion discussion) {
                            getSupportActionBar().setTitle(discussion.getName());
                            chatNameTv.setText(discussion.getName());
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode e) {
                            if (e.equals(RongIMClient.ErrorCode.NOT_IN_DISCUSSION)) {
                                getSupportActionBar().setTitle("不在讨论组中");
                                chatNameTv.setText("不在讨论组中");
                                isDiscussion = true;
                                supportInvalidateOptionsMenu();
                            }
                        }
                    });
        } else if (targetIds != null) {
            setDiscussionName(targetIds);
        } else {
            getSupportActionBar().setTitle("讨论组");
            chatNameTv.setText("讨论组");
        }
    }


    /**
     * 设置讨论组名称
     *
     * @param targetIds
     */
    private void setDiscussionName(String targetIds) {

        StringBuilder sb = new StringBuilder();
        getSupportActionBar().setTitle(targetIds);
        chatNameTv.setText(targetIds);
        String[] ids = targetIds.split(",");

        if (RongYunContext.getInstance() != null) {

            for (int i = 0; i < ids.length; i++) {
                sb.append(RongYunContext.getInstance().getUserInfoById(ids[i]).getName().toString());
                sb.append(",");
            }

            sb.append(RongYunContext.getInstance().getSharedPreferences()
                    .getString(Constants.APP_USER_NAME, "0.0"));
        }

        getSupportActionBar().setTitle(sb);
        chatNameTv.setText(sb);
    }

    /**
     * 设置私聊界面 ActionBar
     */
    private void setPrivateActionBar(String targetId) {

        if (RongYunContext.getInstance() != null) {

            UserInfos userInfos = RongYunContext.getInstance().getUserInfosById(targetId);

            if (userInfos == null) {
                /*getSupportActionBar().setTitle("陌生人");
                chatNameTv.setText("陌生人");*/
                getUserinfoName(targetId);
                //chatLogo.setVisibility(View.GONE);
                Log.e("Tag", "chatLogo");
            } else {

                ArrayList<UserInfo> userInfoList = null;
                //获取好友列表
                if (RongYunContext.getInstance() != null) {
                    userInfoList = RongYunContext.getInstance().getFriendList();
                }

                if (null != userInfoList) {
                    for (int i = 0; i < userInfoList.size(); i++) {

                        if (userInfos.getUserid().equals(userInfoList.get(i).getUserId())) {
                            Log.e("Tag", userInfos.getUserid() + userInfoList.get(i).getUserId());
                            chatType = 1;
                        }
                    }
                }
                if (1 == chatType) {

                    getSupportActionBar().setTitle(userInfos.getUsername().toString());
                    chatNameTv.setText(userInfos.getUsername().toString());

                } else {

                    getUserinfoName(targetId);
                   /* getSupportActionBar().setTitle("陌生人");
                    chatNameTv.setText("陌生人");*/
                    //chatLogo.setVisibility(View.GONE);

                }


            }
        }

    }
    public void showApplyFriend(View view, final String currentUserId) {
        LayoutInflater mLayoutInflater = LayoutInflater.from(this);
        View contentView1 = mLayoutInflater.inflate(R.layout.apply_friend, null);
        if (popupApplyFriend == null) {

            popupApplyFriend = new PopupWindow(contentView1, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        ColorDrawable cd = new ColorDrawable(0x000000);
        popupApplyFriend.setBackgroundDrawable(cd);
        // 产生背景变暗效果
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.6f;
        getWindow().setAttributes(lp);
        popupApplyFriend.setOutsideTouchable(true);
        popupApplyFriend.setFocusable(true);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        popupApplyFriend.setWidth(display.getWidth() * 80 / 100);
        popupApplyFriend.showAtLocation((View) view.getParent(), Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);

        popupApplyFriend.update();
        TextView cancelTv = (TextView) contentView1.findViewById(R.id.cancel_tv);
        TextView ringTv = (TextView) contentView1.findViewById(R.id.ring_tv);
        cancelTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popupApplyFriend.dismiss();
            }
        });
        ringTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                applyAddFriend(currentUserId);

                popupApplyFriend.dismiss();
            }
        });
        popupApplyFriend.setOnDismissListener(new PopupWindow.OnDismissListener() {

            // 在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);

            }
        });

    }

    private void setChatName(String name) {

        getSupportActionBar().setTitle(name);
        chatNameTv.setText(name);

    }
    private  void applyAddFriend(String currentUserId){

        getUserinfoAndAddFriend(currentUserId);
        //mUserHttpRequest = RongYunContext.getInstance().getDemoApi().sendFriendInvite(currentUserId, "请添加我为好友 ", PersonalDetailActivity.this);
        sp = PreferenceManager.getDefaultSharedPreferences(ConversationActivity.this);
        //ContactNotificationMessage contact = ContactNotificationMessage.obtain(ContactNotificationMessage.CONTACT_OPERATION_REQUEST, sp.getString("cloud_id", ""), currentUserId, "请求添加为好友");
        //contact.setMessage(currentUserId);
        ContactNotificationMessage contact = ContactNotificationMessage.obtain(ContactNotificationMessage.CONTACT_OPERATION_REQUEST, sp.getString("cloud_id", ""), currentUserId, "请求添加为好友");
        contact.setMessage("请求添加你为好友");
        sendMessage(contact, currentUserId);

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
                    Toast.makeText(ConversationActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(ConversationActivity.this, LoginActivity.class);
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
        sp = PreferenceManager.getDefaultSharedPreferences(ConversationActivity.this);
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
                        Toast.makeText(ConversationActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(ConversationActivity.this, LoginActivity.class);
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.de_conversation_menu, menu);

        if (mConversationType == null)
            return true;

        if (mConversationType.equals(Conversation.ConversationType.CHATROOM)) {
            menu.getItem(0).setVisible(false);
        } else if (mConversationType.equals(Conversation.ConversationType.DISCUSSION)
                && isDiscussion) {
            menu.getItem(0).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.icon:

                if (mConversationType == null)
                    return true;

                //enterSettingActivity();
                break;
            case android.R.id.home:
                if (!closeRealTimeLocation()) {
                    finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 根据 targetid 和 ConversationType 进入到设置页面
     */
    private void enterSettingActivity() {

        if (mConversationType == Conversation.ConversationType.PUBLIC_SERVICE
                || mConversationType == Conversation.ConversationType.APP_PUBLIC_SERVICE) {

            RongIM.getInstance().startPublicServiceProfile(this, mConversationType, mTargetId);
        } else {
            //当你刚刚创建完讨论组以后获得的是 targetIds
            if (!TextUtils.isEmpty(mTargetIds)) {
                UriFragment fragment = (UriFragment) getSupportFragmentManager().getFragments().get(0);
                //得到讨论组的 targetId
                mTargetId = fragment.getUri().getQueryParameter("targetId");

                if (TextUtils.isEmpty(mTargetId)) {
                    WinToast.toast(ConversationActivity.this, "讨论组尚未创建成功");
                }
            }

            Uri uri = Uri.parse("demo://" + getApplicationInfo().packageName).buildUpon()
                    .appendPath("conversationSetting")
                    .appendPath(mConversationType.getName())
                    .appendQueryParameter("targetId", mTargetId).build();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivity(intent);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 29 && resultCode == Constants.MESSAGE_REPLY) {
            if (data != null && data.hasExtra("REPLY_NAME") && data.hasExtra("REPLY_ID")) {
                String id = data.getStringExtra("REPLY_ID");
                String name = data.getStringExtra("REPLY_NAME");
                TextInputProvider textInputProvider = (TextInputProvider) RongContext.getInstance().getPrimaryInputProvider();
                textInputProvider.setEditTextContent(mEditText + name + " ");

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
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

                    Intent intent = new Intent();
                    intent.setClass(ConversationActivity.this, MainActivity.class);
                    intent.putExtra("PUSH_CONVERSATIONTYPE", mConversationType.toString());
                    intent.putExtra("PUSH_TARGETID", mTargetId);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }


    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
        Log.e(TAG, "---push--onCallApiFailure-");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActionBarTitle(mConversationType, mTargetId);
        showRealTimeLocationBar(null);
    }


/*－－－－－－－－－－－－－地理位置共享 start－－－－－－－－－*/

    private void setRealTime() {

        mRealTimeBar = (RelativeLayout) this.findViewById(R.id.layout);

        mRealTimeBar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (currentLocationStatus == null)
                    currentLocationStatus = RongIMClient.getInstance().getRealTimeLocationCurrentState(mConversationType, mTargetId);

                if (currentLocationStatus == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_INCOMING) {

                    final AlterDialogFragment alterDialogFragment = AlterDialogFragment.newInstance("", "加入位置共享", "取消", "加入");
                    alterDialogFragment.setOnAlterDialogBtnListener(new AlterDialogFragment.AlterDialogBtnListener() {

                        @Override
                        public void onDialogPositiveClick(AlterDialogFragment dialog) {
                            RealTimeLocationConstant.RealTimeLocationStatus status = RongIMClient.getInstance().getRealTimeLocationCurrentState(mConversationType, mTargetId);

                            if (status == null || status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_IDLE) {
                                startRealTimeLocation();
                            } else {
                                joinRealTimeLocation();
                            }

                        }

                        @Override
                        public void onDialogNegativeClick(AlterDialogFragment dialog) {
                            alterDialogFragment.dismiss();
                        }
                    });
                    alterDialogFragment.show(getSupportFragmentManager());

                } else {
                    /*Intent intent = new Intent(ConversationActivity.this, RealTimeLocationActivity.class);
                    intent.putExtra("conversationType", mConversationType.getValue());
                    intent.putExtra("targetId", mTargetId);
                    startActivity(intent);*/
                }
            }
        });

        if (!TextUtils.isEmpty(mTargetId) && mConversationType != null) {

            RealTimeLocationConstant.RealTimeLocationErrorCode errorCode = RongIMClient.getInstance().getRealTimeLocation(mConversationType, mTargetId);
            if (errorCode == RealTimeLocationConstant.RealTimeLocationErrorCode.RC_REAL_TIME_LOCATION_SUCCESS || errorCode == RealTimeLocationConstant.RealTimeLocationErrorCode.RC_REAL_TIME_LOCATION_IS_ON_GOING) {
                RongIMClient.getInstance().addRealTimeLocationListener(mConversationType, mTargetId, this);//设置监听
                currentLocationStatus = RongIMClient.getInstance().getRealTimeLocationCurrentState(mConversationType, mTargetId);

                if (currentLocationStatus == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_INCOMING) {
                    showRealTimeLocationBar(currentLocationStatus);
                }
            }
        }
    }

    //real-time location method beign

    private void startRealTimeLocation() {/*
        RongIMClient.getInstance().startRealTimeLocation(mConversationType, mTargetId);
        Intent intent = new Intent(ConversationActivity.this, RealTimeLocationActivity.class);
        intent.putExtra("conversationType", mConversationType.getValue());
        intent.putExtra("targetId", mTargetId);
        startActivity(intent);*/
    }

    private void joinRealTimeLocation() {
      /*  RongIMClient.getInstance().joinRealTimeLocation(mConversationType, mTargetId);
        Intent intent = new Intent(ConversationActivity.this, RealTimeLocationActivity.class);
        intent.putExtra("conversationType", mConversationType.getValue());
        intent.putExtra("targetId", mTargetId);
        startActivity(intent);*/
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            closeRealTimeLocation();
        }

        ConversationFragment fragment = (ConversationFragment) getSupportFragmentManager().findFragmentById(R.id.conversation);
        if (!fragment.onBackPressed()) {
            finish();
        }
    }


    private boolean closeRealTimeLocation() {

        if (mConversationType == null || TextUtils.isEmpty(mTargetId))
            return false;

        if (mConversationType != null && !TextUtils.isEmpty(mTargetId)) {

            RealTimeLocationConstant.RealTimeLocationStatus status = RongIMClient.getInstance().getRealTimeLocationCurrentState(mConversationType, mTargetId);

            if (status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_IDLE || status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_INCOMING) {
                return false;
            }
        }

        final AlterDialogFragment alterDialogFragment = AlterDialogFragment.newInstance("提示", "退出当前页面将会终止实时位置共享,确定退出？", "否", "是");
        alterDialogFragment.setOnAlterDialogBtnListener(new AlterDialogFragment.AlterDialogBtnListener() {
            @Override
            public void onDialogPositiveClick(AlterDialogFragment dialog) {
                RongIMClient.getInstance().quitRealTimeLocation(mConversationType, mTargetId);
                finish();
            }

            @Override
            public void onDialogNegativeClick(AlterDialogFragment dialog) {
                alterDialogFragment.dismiss();
            }
        });
        alterDialogFragment.show(getSupportFragmentManager());

        return true;
    }


    private void showRealTimeLocationBar(RealTimeLocationConstant.RealTimeLocationStatus status) {

        if (status == null)
            status = RongIMClient.getInstance().getRealTimeLocationCurrentState(mConversationType, mTargetId);

        final List<String> userIds = RongIMClient.getInstance().getRealTimeLocationParticipants(mConversationType, mTargetId);

        if (status != null && status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_INCOMING) {

            if (userIds != null && userIds.get(0) != null && userIds.size() == 1) {

                RongYunContext.getInstance().getDemoApi().getUserInfo(userIds.get(0), new RongYunApi.GetUserInfoListener() {

                    @Override
                    public void onSuccess(final UserInfo userInfo) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = (TextView) mRealTimeBar.findViewById(android.R.id.text1);
                                textView.setText(userInfo.getName() + " 正在共享位置");
                            }
                        });

                    }

                    @Override
                    public void onError(String userId, BaseException e) {

                    }
                });
            } else {
                if (userIds != null && userIds.size() > 0) {
                    if (mRealTimeBar != null) {
                        TextView textView = (TextView) mRealTimeBar.findViewById(android.R.id.text1);
                        textView.setText(userIds.size() + " 人正在共享位置");
                    }
                } else {
                    if (mRealTimeBar != null && mRealTimeBar.getVisibility() == View.VISIBLE) {
                        mRealTimeBar.setVisibility(View.GONE);
                    }
                }
            }

        } else if (status != null && status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_OUTGOING) {
            TextView textView = (TextView) mRealTimeBar.findViewById(android.R.id.text1);
            textView.setText("你正在共享位置");
        } else {

            if (mRealTimeBar != null && userIds != null) {
                TextView textView = (TextView) mRealTimeBar.findViewById(android.R.id.text1);
                textView.setText(userIds.size() + " 人正在共享位置");
            }
        }

        if (userIds != null && userIds.size() > 0) {

            if (mRealTimeBar != null && mRealTimeBar.getVisibility() == View.GONE) {
                mRealTimeBar.setVisibility(View.VISIBLE);
            }
        } else {

            if (mRealTimeBar != null && mRealTimeBar.getVisibility() == View.VISIBLE) {
                mRealTimeBar.setVisibility(View.GONE);
            }
        }

    }

    public void onEventMainThread(RongEvent.RealTimeLocationMySelfJoinEvent event) {

        onParticipantsJoin(RongIM.getInstance().getRongIMClient().getCurrentUserId());
    }

    private void hideRealTimeBar() {
        if (mRealTimeBar != null) {
            mRealTimeBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        if ("ConversationActivity".equals(this.getClass().getSimpleName()))
            EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    @Override
    public void onStatusChange(final RealTimeLocationConstant.RealTimeLocationStatus status) {
        currentLocationStatus = status;

        EventBus.getDefault().post(status);

        if (status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_IDLE) {
            hideRealTimeBar();

            RealTimeLocationConstant.RealTimeLocationErrorCode errorCode = RongIMClient.getInstance().getRealTimeLocation(mConversationType, mTargetId);

            if (errorCode == RealTimeLocationConstant.RealTimeLocationErrorCode.RC_REAL_TIME_LOCATION_SUCCESS) {
                RongIM.getInstance().getRongIMClient().insertMessage(mConversationType, mTargetId, RongIM.getInstance().getRongIMClient().getCurrentUserId(), InformationNotificationMessage.obtain("位置共享已结束"));
            }
        } else if (status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_OUTGOING) {//发自定义消息
            showRealTimeLocationBar(status);
        } else if (status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_INCOMING) {
            showRealTimeLocationBar(status);
        } else if (status == RealTimeLocationConstant.RealTimeLocationStatus.RC_REAL_TIME_LOCATION_STATUS_CONNECTED) {
            showRealTimeLocationBar(status);
        }

    }


    @Override
    public void onReceiveLocation(double latitude, double longitude, String userId) {
        EventBus.getDefault().post(RongEvent.RealTimeLocationReceiveEvent.obtain(userId, latitude, longitude));
    }

    @Override
    public void onParticipantsJoin(String userId) {
        EventBus.getDefault().post(RongEvent.RealTimeLocationJoinEvent.obtain(userId));

        if (RongIMClient.getInstance().getCurrentUserId().equals(userId)) {
            showRealTimeLocationBar(null);
        }
    }

    @Override
    public void onParticipantsQuit(String userId) {
        EventBus.getDefault().post(RongEvent.RealTimeLocationQuitEvent.obtain(userId));
    }

    @Override
    public void onError(RealTimeLocationConstant.RealTimeLocationErrorCode errorCode) {
        Log.e(TAG, "onError:---" + errorCode);
    }

    /*－－－－－－－－－－－－－地理位置共享 end－－－－－－－－－*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            if (!closeRealTimeLocation()) {
                this.finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case SET_TEXT_TYPING_TITLE:
                getSupportActionBar().setTitle(TextTypingTitle);
                chatNameTv.setText(TextTypingTitle);
                break;
            case SET_VOICE_TYPING_TITLE:
                getSupportActionBar().setTitle(VoiceTypingTitle);
                chatNameTv.setText(VoiceTypingTitle);
                break;
            case SET_TARGETID_TITLE:
                setActionBarTitle(mConversationType, mTargetId);
                break;
            default:
                break;
        }
        return true;
    }


    private void getUserinfoName(final String cloud_id) {
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

                showShortToast("登录失败，请检查网络连接");
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

                        String idd = rcUser.getData().get(0).getCloud_id();
                        String named = rcUser.getData().get(0).getName();
                        String uritestd = rcUser.getData().get(0).getImg();
                        setChatName(named);


                    }


                } else if (rcUser.getCode() == 2) {
                    Toast.makeText(ConversationActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(ConversationActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }, 10);
                } else {

                    showShortToast(rcUser.getMsg());

                }

            }

        });


    }

    /**
     * Toast短显示
     *
     * @param msg
     */
    protected void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
