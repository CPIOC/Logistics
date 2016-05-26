package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudMessage.AgreedFriendRequestMessage;
import com.cpic.taylor.logistics.RongCloudModel.ApiResult;
import com.cpic.taylor.logistics.RongCloudModel.FriendApply;
import com.cpic.taylor.logistics.RongCloudModel.Friends;
import com.cpic.taylor.logistics.RongCloudModel.Status;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.RongCloudWidget.WinToast;
import com.cpic.taylor.logistics.RongCloudaAdapter.NewFriendApplyListAdapter;
import com.cpic.taylor.logistics.RongCloudaAdapter.NewFriendListAdapter;
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
import com.sea_monster.network.ApiCallback;

import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 2015/3/26.
 */
public class NewFriendListActivity extends BaseApiActivity {

    private static final String TAG = NewFriendListActivity.class.getSimpleName();
    private AbstractHttpRequest<Friends> getFriendHttpRequest;
    private AbstractHttpRequest<Status> mRequestFriendHttpRequest;

    private ListView mNewFriendList;
    private NewFriendListAdapter adapter;
    private List<ApiResult> mResultList;
    private LoadingDialog mDialog;
    private Handler mHandler;
    private HttpUtils post;
    private RequestParams params;
    private SharedPreferences sp;
    private FriendApply friendApply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_new_friendlist);
        CloseActivityClass.activityList.add(this);
        initView();
        getApplyList();

    }

    protected void initView() {
        getSupportActionBar().setTitle(R.string.de_new_friends);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        getSupportActionBar().hide();
        mNewFriendList = (ListView) findViewById(R.id.de_new_friend_list);
        mDialog = new LoadingDialog(this);

    }

    public void getApplyList() {

        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);
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


                        NewFriendApplyListAdapter mAdapter = new NewFriendApplyListAdapter(friendApply.getData(), NewFriendListActivity.this);
                        mNewFriendList.setAdapter(mAdapter);

                    }

                } else if (friendApply.getCode() == 2) {
                    Toast.makeText(NewFriendListActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(NewFriendListActivity.this, LoginActivity.class);
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

    /**
     * 添加好友成功后，向对方发送一条消息
     *
     * @param id 对方id
     */
    public void sendFirstMessage(String id) {
        final AgreedFriendRequestMessage message = new AgreedFriendRequestMessage(id, "agree");
        if (RongYunContext.getInstance() != null) {
            sp = PreferenceManager.getDefaultSharedPreferences(NewFriendListActivity.this);
            //获取当前用户的 userid
            String userid = sp.getString("cloud_id", "");
            String username = sp.getString("name", "");
            String userportrait = sp.getString("img", "");
            UserInfo userInfo = new UserInfo(userid, username, Uri.parse(userportrait));
            //把用户信息设置到消息体中，直接发送给对方，可以不设置，非必选项
            message.setUserInfo(userInfo);
            if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {

                //发送一条添加成功的自定义消息，此条消息不会在ui上展示
                RongIM.getInstance().getRongIMClient().sendMessage(Conversation.ConversationType.PRIVATE, id, message, null, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer messageId, RongIMClient.ErrorCode e) {
                        Log.e(TAG, Constants.DEBUG + "------DeAgreedFriendRequestMessage----onError--");
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Log.e(TAG, Constants.DEBUG + "------DeAgreedFriendRequestMessage----onSuccess--" + message.getMessage());

                    }
                });
            }
        }


    }

    public void backTo(View view) {
        finish();
    }

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        if (getFriendHttpRequest != null && getFriendHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();

            if (obj instanceof Friends) {
                final Friends friends = (Friends) obj;
                if (friends.getCode() == 200) {
                    if (friends.getResult().size() != 0) {
                        for (int i = 0; i < friends.getResult().size(); i++) {
                            mResultList.add(friends.getResult().get(i));
                        }
                        adapter = new NewFriendListAdapter(mResultList, NewFriendListActivity.this);
                        mNewFriendList.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        adapter.setOnItemButtonClick(mOnItemButtonClick);
                    }
                }
            }
        } else if (mRequestFriendHttpRequest == request) {

        }
    }

    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
        if (getFriendHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
            Log.e(TAG, "-----onCallApiFailure------e:" + e);
            WinToast.toast(this, "获取失败");
        }
    }

    NewFriendListAdapter.OnItemButtonClick mOnItemButtonClick = new NewFriendListAdapter.OnItemButtonClick() {

        @Override
        public boolean onButtonClick(final int position, View view, int status) {
            switch (status) {
                case 1://好友

                    break;
                case 2://请求添加

                    break;
                case 3://请求被添加
                    mResultList.get(position).getId();
                    if (mDialog != null && !mDialog.isShowing()) {
                        mDialog.show();
                    }
                    if (RongYunContext.getInstance() != null)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRequestFriendHttpRequest = RongYunContext.getInstance().getDemoApi().processRequestFriend(mResultList.get(position).getId(), "1", new ApiCallback<Status>() {
                                    @Override
                                    public void onComplete(AbstractHttpRequest<Status> statusAbstractHttpRequest, Status status) {
                                        Log.e(TAG, "----mRequestFriendHttpRequest----onComplete---");

                                        ApiResult apiResult = mResultList.get(position);
                                        apiResult.setStatus(1);
                                        mResultList.set(position, mResultList.get(position));

                                        Message mess = Message.obtain();
                                        mess.obj = mResultList;
                                        mess.what = 1;
                                        mHandler.sendMessage(mess);
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                UserInfo info = new UserInfo(mResultList.get(position).getId(), mResultList.get(position).getUsername(), mResultList.get(position).getPortrait() == null ? null : Uri.parse(mResultList.get(position).getPortrait()));
                                                if (RongYunContext.getInstance() != null) {
                                                    if (RongYunContext.getInstance().hasUserId(mResultList.get(position).getId())) {
                                                        RongYunContext.getInstance().updateUserInfos(mResultList.get(position).getId(), "1");
                                                    } else {
                                                        RongYunContext.getInstance().insertOrReplaceUserInfo(info, "1");
                                                    }
                                                }
                                                sendMessage(mResultList.get(position).getId());
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(AbstractHttpRequest<Status> statusAbstractHttpRequest, BaseException e) {
                                        if (mRequestFriendHttpRequest != null && mRequestFriendHttpRequest.equals(statusAbstractHttpRequest)) {
                                            Log.e(TAG, "----mRequestFriendHttpRequest----onFailure---" + e);
//                                            if (mDialog != null)
//                                                mDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });

                    break;
                case 4://请求被拒绝

                    break;
                case 5://我被对方删除

                    break;
            }

            return false;
        }
    };

    /**
     * 添加好友成功后，向对方发送一条消息
     *
     * @param id 对方id
     */
    private void sendMessage(String id) {
        final AgreedFriendRequestMessage message = new AgreedFriendRequestMessage(id, "agree");
        if (RongYunContext.getInstance() != null) {
            //获取当前用户的 userid
            String userid = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_ID, Constants.DEFAULT);
            String username = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_NAME, Constants.DEFAULT);
            String userportrait = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_PORTRAIT, Constants.DEFAULT);

            UserInfo userInfo = new UserInfo(userid, username, Uri.parse(userportrait));
            //把用户信息设置到消息体中，直接发送给对方，可以不设置，非必选项
            message.setUserInfo(userInfo);
            if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {

                //发送一条添加成功的自定义消息，此条消息不会在ui上展示
                RongIM.getInstance().getRongIMClient().sendMessage(Conversation.ConversationType.PRIVATE, id, message, null, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer messageId, RongIMClient.ErrorCode e) {
                        Log.e(TAG, Constants.DEBUG + "------DeAgreedFriendRequestMessage----onError--");
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Log.e(TAG, Constants.DEBUG + "------DeAgreedFriendRequestMessage----onSuccess--" + message.getMessage());

                    }
                });
            }
        }


    }

    private void updateAdapter(List<ApiResult> mResultList) {
        if (adapter != null) {
            adapter = null;
        }
        adapter = new NewFriendListAdapter(mResultList, NewFriendListActivity.this);
        mNewFriendList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setOnItemButtonClick(mOnItemButtonClick);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.de_add_friend_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.icon:
                Intent intent = new Intent(NewFriendListActivity.this, SearchFriendActivity.class);
                startActivityForResult(intent, Constants.FRIENDLIST_REQUESTCODE);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        if (adapter != null) {
            adapter = null;
        }
        super.onDestroy();
    }

    protected void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
