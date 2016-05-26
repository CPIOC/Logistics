package com.cpic.taylor.logistics.RongCloudActivity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudFragment.ContactsFragment;
import com.cpic.taylor.logistics.RongCloudModel.FriendApply;
import com.cpic.taylor.logistics.RongCloudModel.FriendApplyData;
import com.cpic.taylor.logistics.RongCloudaAdapter.ConversationListAdapterEx;
import com.cpic.taylor.logistics.RongCloudaAdapter.NewFriendApplyListAdapter;
import com.cpic.taylor.logistics.activity.LoginActivity;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.utils.CloseActivityClass;
import com.cpic.taylor.logistics.utils.Px2DpUtils;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.ArrayList;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.message.ContactNotificationMessage;

public class MainActivity extends BaseActivity implements View.OnClickListener, ViewPager.OnPageChangeListener, ActionBar.OnMenuVisibilityListener {
    private String TAG = MainActivity.class.getSimpleName();

    public static final String ACTION_DMEO_RECEIVE_MESSAGE = "action_demo_receive_message";
    public static final String ACTION_DMEO_AGREE_REQUEST = "action_demo_agree_request";
    private boolean hasNewFriends = false;
    private Menu mMenu;
    private ReceiveMessageBroadcastReciver mBroadcastReciver;

    ActivityManager activityManager;
    // rbtn的管理类
    private RadioGroup rGroup;
    // 上一次选择的rbtn
    private RadioButton lastButton;

    private RadioButton lastButton1;

    private ActionBar actionBar;
    private ImageView plusImg;
    private TextView titleTv;

    /**
     * 指示当前在哪个fragment
     * 0:聊天记录fragment
     * 1:通讯录fragment
     */
    private static int currentIndex = 0;

    private Handler mHandler;
    private HttpUtils post;
    private RequestParams params;
    private SharedPreferences sp;
    private FriendApply friendApply;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_main);
        CloseActivityClass.activityList.add(this);
        RongIM.getInstance().enableNewComingMessageIcon(true);
        RongIM.getInstance().enableUnreadMessageIcon(true);

        mFragmentManager = getSupportFragmentManager();
        getSupportActionBar().setTitle(R.string.main_name);
        getSupportActionBar().hide();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm); // 获取屏幕信息
        indicatorWidth = dm.widthPixels / 4;// 指示器宽度为屏幕宽度的4/1

        mMainShow = (LinearLayout) findViewById(R.id.main_show);
        mMainConversationLiner = (RelativeLayout) findViewById(R.id.main_conversation_liner);
        mMainGroupLiner = (RelativeLayout) findViewById(R.id.main_group_liner);
        mMainChatroomLiner = (RelativeLayout) findViewById(R.id.main_chatroom_liner);
        mMainCustomerLiner = (RelativeLayout) findViewById(R.id.main_customer_liner);
        mMainConversationTv = (TextView) findViewById(R.id.main_conversation_tv);
        mMainGroupTv = (TextView) findViewById(R.id.main_group_tv);
        mMainChatroomTv = (TextView) findViewById(R.id.main_chatroom_tv);
        mMainCustomerTv = (TextView) findViewById(R.id.main_customer_tv);
        mViewPager = (ViewPager) findViewById(R.id.main_viewpager);
        mMainSelectImg = (ImageView) findViewById(R.id.main_switch_img);
        mUnreadNumView = (TextView) findViewById(R.id.de_num);
        mCustomerNoRead = (TextView) findViewById(R.id.customer_noread);
        titleTv = (TextView) findViewById(R.id.main_activity_title);
        plusImg = (ImageView) findViewById(R.id.main_activity_plus_sign);

        /**
         * 初始化
         */
        rGroup = (RadioGroup) findViewById(R.id.main_activity_rgroup_rc);
        lastButton = (RadioButton) findViewById(R.id.main_activity_rbtn_message_rc);
        lastButton1 = (RadioButton) findViewById(R.id.main_activity_rbtn_contacts_rc);
        lastButton.setChecked(true);
        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 当前选中的radioButton
                RadioButton selectRbtn = (RadioButton) findViewById(checkedId);
                if (checkedId == R.id.main_activity_rbtn_message_rc) {
                    mViewPager.setCurrentItem(0);
                    setTitleBarChat();
                    currentIndex = 0;
                } else {
                    mViewPager.setCurrentItem(1);
                    setTitleBarContact();
                    currentIndex = 1;
                    if (null != mContactsFragment)
                        mContactsFragment.getFriendList();
                }

            }
        });

        ViewGroup.LayoutParams cursor_Params = mMainSelectImg.getLayoutParams();
        cursor_Params.width = indicatorWidth;// 初始化滑动下标的宽
        mMainSelectImg.setLayoutParams(cursor_Params);
        // 获取布局填充器
        mInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        mMainChatroomLiner.setOnClickListener(this);
        mMainConversationLiner.setOnClickListener(this);
        mMainGroupLiner.setOnClickListener(this);
        mMainCustomerLiner.setOnClickListener(this);
        mDemoFragmentPagerAdapter = new DemoFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mDemoFragmentPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        initData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void initData() {

        final Conversation.ConversationType[] conversationTypes = {Conversation.ConversationType.PRIVATE, Conversation.ConversationType.DISCUSSION,
                Conversation.ConversationType.GROUP, Conversation.ConversationType.SYSTEM,
                Conversation.ConversationType.PUBLIC_SERVICE, Conversation.ConversationType.APP_PUBLIC_SERVICE};

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RongIM.getInstance().setOnReceiveUnreadCountChangedListener(mCountListener, conversationTypes);
                RongIM.getInstance().setOnReceiveUnreadCountChangedListener(mCountListener1, Conversation.ConversationType.APP_PUBLIC_SERVICE);
            }
        }, 500);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DMEO_RECEIVE_MESSAGE);
        if (mBroadcastReciver == null) {
            mBroadcastReciver = new ReceiveMessageBroadcastReciver();
        }
        this.registerReceiver(mBroadcastReciver, intentFilter);

        getConversationPush();

        getPushMessage();
    }

    /**
     *
     */
    private void getConversationPush() {
        if (getIntent() != null && getIntent().hasExtra("PUSH_CONVERSATIONTYPE") && getIntent().hasExtra("PUSH_TARGETID")) {

            final String conversationType = getIntent().getStringExtra("PUSH_CONVERSATIONTYPE");
            final String targetId = getIntent().getStringExtra("PUSH_TARGETID");

            if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {

                RongIM.getInstance().getRongIMClient().getConversation(Conversation.ConversationType.valueOf(conversationType), targetId, new RongIMClient.ResultCallback<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {

                        if (conversation != null) {

                            if (conversation.getLatestMessage() instanceof ContactNotificationMessage) {
                                startActivity(new Intent(MainActivity.this, NewFriendListActivity.class));
                            } else {
                                Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon().appendPath("conversation")
                                        .appendPath(conversationType).appendQueryParameter("targetId", targetId).build();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {

                    }
                });
            }
        }
    }

    /**
     * 得到不落地 push 消息
     */
    private void getPushMessage() {

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null && intent.getData().getScheme().equals("rong")) {
            String content = intent.getData().getQueryParameter("pushContent");
            String data = intent.getData().getQueryParameter("pushData");
            String id = intent.getData().getQueryParameter("pushId");
            RongIMClient.recordNotificationEvent(id);
            Log.e("RongPushActivity", "--content--" + content + "--data--" + data + "--id--" + id);
            if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
                RongIM.getInstance().getRongIMClient().clearNotifications();
            }
            if (RongYunContext.getInstance() != null) {
                String token = RongYunContext.getInstance().getSharedPreferences().getString("DEMO_TOKEN", "default");
                if (token.equals("default")) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else {
                    if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
                        RongIMClient.ConnectionStatusListener.ConnectionStatus status = RongIM.getInstance().getRongIMClient().getCurrentConnectionStatus();
                        if (RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED.equals(status)) {
                            return;
                        } else {
                            if (RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTING.equals(status)) {
                                return;
                            } else {
                                Intent intent1 = new Intent(MainActivity.this, LoginActivity.class);
                                intent1.putExtra("PUSH_MESSAGE", true);
                                startActivity(intent1);
                                finish();
                            }
                        }
                    } else {
                        Intent intent1 = new Intent(MainActivity.this, LoginActivity.class);
                        intent1.putExtra("PUSH_MESSAGE", true);
                        startActivity(intent1);
                        finish();
                    }
                }
            }
        }
    }


    public RongIM.OnReceiveUnreadCountChangedListener mCountListener = new RongIM.OnReceiveUnreadCountChangedListener() {
        @Override
        public void onMessageIncreased(int count) {
            if (count == 0) {
                mUnreadNumView.setVisibility(View.GONE);
            } else if (count > 0 && count < 100) {
                mUnreadNumView.setVisibility(View.VISIBLE);
                mUnreadNumView.setText(count + "");
            } else {
                mUnreadNumView.setVisibility(View.VISIBLE);
                mUnreadNumView.setText(R.string.no_read_message);
            }
        }
    };

    public RongIM.OnReceiveUnreadCountChangedListener mCountListener1 = new RongIM.OnReceiveUnreadCountChangedListener() {
        @Override
        public void onMessageIncreased(int count) {
            if (count == 0) {
                mCustomerNoRead.setVisibility(View.GONE);
            } else if (count > 0) {
                mCustomerNoRead.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public void onMenuVisibilityChanged(boolean b) {

    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int i) {

        switch (i) {
            case 0:
                selectNavSelection(0);
                break;
            case 1:
                selectNavSelection(1);
                if (null != mContactsFragment)
                    mContactsFragment.getFriendList();
                break;

        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private class DemoFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

        public DemoFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            switch (i) {
                case 0:
                    mMainConversationTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                    //TODO
                    if (mConversationFragment == null) {
                        ConversationListFragment listFragment = ConversationListFragment.getInstance();
                        listFragment.setAdapter(new ConversationListAdapterEx(RongContext.getInstance()));
                        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                                .appendPath("conversationlist")
                                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "true")//群组
                                .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")//讨论组
                                .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                                .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//公共服务号
                                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")//系统
                                .build();
                        listFragment.setUri(uri);
                        fragment = listFragment;
                    } else {
                        fragment = mConversationFragment;
                    }
                    break;
                case 1:
                    if (mContactsFragment == null) {
                        mContactsFragment = new ContactsFragment();
                    }

                    fragment = mContactsFragment;

                    break;


            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private void selectNavSelection(int index) {
        clearSelection();
        switch (index) {
            case 0:
                mMainConversationTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                TranslateAnimation animation = new TranslateAnimation(0, 0,
                        0f, 0f);
                animation.setInterpolator(new LinearInterpolator());
                animation.setDuration(100);
                animation.setFillAfter(true);
                mMainSelectImg.startAnimation(animation);
                lastButton.setChecked(true);
                setTitleBarChat();
                currentIndex = 0;
                break;
            case 1:
                mMainGroupTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                TranslateAnimation animation1 = new TranslateAnimation(
                        indicatorWidth, indicatorWidth,
                        0f, 0f);
                animation1.setInterpolator(new LinearInterpolator());
                animation1.setDuration(100);
                animation1.setFillAfter(true);
                mMainSelectImg.startAnimation(animation1);
                lastButton1.setChecked(true);
                setTitleBarContact();
                currentIndex = 1;
                break;
            case 2:
                mMainChatroomTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                TranslateAnimation animation2 = new TranslateAnimation(
                        2 * indicatorWidth, indicatorWidth * 2,
                        0f, 0f);
                animation2.setInterpolator(new LinearInterpolator());
                animation2.setDuration(100);
                animation2.setFillAfter(true);
                mMainSelectImg.startAnimation(animation2);

                break;
            case 3:
                mMainCustomerTv.setTextColor(getResources().getColor(R.color.de_title_bg));
                TranslateAnimation animation3 = new TranslateAnimation(
                        3 * indicatorWidth, indicatorWidth * 3,
                        0f, 0f);
                animation3.setInterpolator(new LinearInterpolator());
                animation3.setDuration(100);
                animation3.setFillAfter(true);
                mMainSelectImg.startAnimation(animation3);
                break;
        }
    }

    private void clearSelection() {
        mMainConversationTv.setTextColor(getResources().getColor(R.color.black_textview));
        mMainGroupTv.setTextColor(getResources().getColor(R.color.black_textview));
        mMainChatroomTv.setTextColor(getResources().getColor(R.color.black_textview));
        mMainCustomerTv.setTextColor(getResources().getColor(R.color.black_textview));
    }

    private class ReceiveMessageBroadcastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //收到好友添加的邀请，需要更新 Actionbar
            if (action.equals(ACTION_DMEO_RECEIVE_MESSAGE)) {
                hasNewFriends = intent.getBooleanExtra("has_message", false);
                supportInvalidateOptionsMenu();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.main_conversation_liner:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.main_group_liner:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.main_chatroom_liner:
                mViewPager.setCurrentItem(2);
                break;
            case R.id.main_customer_liner:
                mViewPager.setCurrentItem(3);
                break;*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {

        if (mBroadcastReciver != null) {
            this.unregisterReceiver(mBroadcastReciver);
        }
        super.onDestroy();
    }

    public void backTo(View view) {
        finish();
    }

    public void setTitleBarChat() {
        titleTv.setText("聊天");
        plusImg.setImageResource(R.drawable.plus_add);
    }

    public void setTitleBarContact() {
        titleTv.setText("通讯录");
        plusImg.setImageResource(R.drawable.add_friends);
    }

    public void plusFunction(View view) {
        if (currentIndex == 0) {
            showPopChat(view);
        } else {
            startActivity(new Intent(MainActivity.this, SearchFriendActivity.class));
        }
    }

    private void showPopChat(View v) {

        final PopupWindow pop = new PopupWindow(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.main_activity_pop, null);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        pop.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        pop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        ColorDrawable cd = new ColorDrawable(0x000000);
        pop.setBackgroundDrawable(cd);
        // 产生背景变暗效果
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1f;
        getWindow().setAttributes(lp);
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);
        pop.showAsDropDown(v, Integer.parseInt("-" + Px2DpUtils.dip2px(MainActivity.this, 70)), 0);
        pop.setOnDismissListener(new PopupWindow.OnDismissListener() {

            // 在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);

            }
        });

        LinearLayout addFriendll = (LinearLayout) view.findViewById(R.id.layout_add);
        LinearLayout chatGroupll = (LinearLayout) view.findViewById(R.id.layout_chat_group);
        addFriendll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 添加好友
                 */
                startActivity(new Intent(MainActivity.this, SearchFriendActivity.class));
                pop.dismiss();
            }
        });
        chatGroupll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 发起群聊
                 */
                startActivity(new Intent(MainActivity.this, FriendListActivity.class));
                pop.dismiss();
            }
        });

    }

    public void getApplyListCompare() {

        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        params.addBodyParameter("token", sp.getString("token", null));
        String url = UrlUtils.POST_URL + UrlUtils.path_applyList;
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
                    java.lang.reflect.Type type = new TypeToken<FriendApply>() {
                    }.getType();
                    friendApply = gson.fromJson(result, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (friendApply.getCode() == 1) {


                    if (null != friendApply.getData()) {


                        NewFriendApplyListAdapter mAdapter = new NewFriendApplyListAdapter(friendApply.getData(), MainActivity.this);

                        ArrayList<FriendApplyData> friendApplyDatas = new ArrayList<FriendApplyData>();
                        friendApplyDatas = friendApply.getData();

                        for (int i = 0; i < friendApplyDatas.size(); i++) {

                            UserInfos f = new UserInfos();
                            f.setUserid(friendApplyDatas.get(i).getCloud_id());
                            f.setUsername(friendApplyDatas.get(i).getName());
                            f.setPortrait(friendApplyDatas.get(i).getImg());
                            f.setStatus("1");
                            RongYunContext.getInstance().insertOrReplaceUserInfos(f);

                        }


                    }

                } else if (friendApply.getCode() == 2) {
                    Toast.makeText(MainActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 10);
                } else {
                    showShortToast(friendApply.getMsg());
                }

            }

        });

    }

    protected void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    private RelativeLayout mMainConversationLiner;
    private RelativeLayout mMainGroupLiner;
    private RelativeLayout mMainChatroomLiner;
    private RelativeLayout mMainCustomerLiner;
    /**
     * 聊天室的fragment
     */
    private Fragment mChatroomFragment = null;
    /**
     * 客服的fragment
     */
    private Fragment mCustomerFragment = null;
    /**
     * 会话列表的fragment
     */
    private Fragment mConversationFragment = null;
    /**
     * 群组的fragment
     */
    private Fragment mGroupListFragment = null;
    /**
     * 通讯录Fragment
     */
    private ContactsFragment mContactsFragment;
    /**
     * 会话TextView
     */
    private TextView mMainConversationTv;
    /**
     * 群组TextView
     */
    private TextView mMainGroupTv;

    private TextView mUnreadNumView;
    /**
     * 聊天室TextView
     */
    private TextView mMainChatroomTv;
    /**
     * 客服TextView
     */
    private TextView mMainCustomerTv;

    private FragmentManager mFragmentManager;

    private ViewPager mViewPager;
    /**
     * 下划线
     */
    private ImageView mMainSelectImg;

    private DemoFragmentPagerAdapter mDemoFragmentPagerAdapter;

    private LayoutInflater mInflater;
    /**
     * 下划线长度
     */
    int indicatorWidth;
    private LinearLayout mMainShow;
    private TextView mCustomerNoRead;
}
