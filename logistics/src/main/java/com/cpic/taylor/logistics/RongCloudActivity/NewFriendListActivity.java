package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudaAdapter.NewFriendListAdapter;
import com.cpic.taylor.logistics.RongCloudModel.ApiResult;
import com.cpic.taylor.logistics.RongCloudModel.Friends;
import com.cpic.taylor.logistics.RongCloudModel.Status;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.RongCloudMessage.AgreedFriendRequestMessage;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.RongCloudWidget.WinToast;
import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;
import com.sea_monster.network.ApiCallback;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 2015/3/26.
 */
public class NewFriendListActivity extends BaseApiActivity implements Handler.Callback {

    private static final String TAG = NewFriendListActivity.class.getSimpleName();
    private AbstractHttpRequest<Friends> getFriendHttpRequest;
    private AbstractHttpRequest<Status> mRequestFriendHttpRequest;

    private ListView mNewFriendList;
    private NewFriendListAdapter adapter;
    private List<ApiResult> mResultList;
    private LoadingDialog mDialog;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_new_friendlist);
        initView();

    }

    protected void initView() {
        getSupportActionBar().setTitle(R.string.de_new_friends);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        mNewFriendList = (ListView) findViewById(R.id.de_new_friend_list);
        mDialog = new LoadingDialog(this);
        mResultList = new ArrayList<ApiResult>();
        mHandler = new Handler(this);

        if (RongYunContext.getInstance() != null) {
            getFriendHttpRequest = RongYunContext.getInstance().getDemoApi().getNewFriendlist(this);
            if (mDialog != null && !mDialog.isShowing()) {
                mDialog.show();
            }
        }
        Intent in = new Intent();
        in.setAction(MainActivity.ACTION_DMEO_RECEIVE_MESSAGE);
        in.putExtra("has_message", false);
        sendBroadcast(in);
    }

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        if (getFriendHttpRequest!= null && getFriendHttpRequest == request) {
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
            Log.e(TAG,"-----onCallApiFailure------e:"+e);
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
                                        if(mRequestFriendHttpRequest!=null&& mRequestFriendHttpRequest.equals(statusAbstractHttpRequest)){
                                            Log.e(TAG,"----mRequestFriendHttpRequest----onFailure---"+e);
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

            UserInfo userInfo = new UserInfo(userid,username,Uri.parse(userportrait));
            //把用户信息设置到消息体中，直接发送给对方，可以不设置，非必选项
            message.setUserInfo(userInfo);
            if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient()!=null) {

                //发送一条添加成功的自定义消息，此条消息不会在ui上展示
                RongIM.getInstance().getRongIMClient().sendMessage(Conversation.ConversationType.PRIVATE, id, message, null,null, new RongIMClient.SendMessageCallback() {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.SEARCH_REQUESTCODE) {

            if (adapter != null) {
                adapter = null;
                mResultList.clear();
            }

            if (RongYunContext.getInstance() != null) {
                getFriendHttpRequest = RongYunContext.getInstance().getDemoApi().getNewFriendlist(this);

            }
            if (mDialog != null && !mDialog.isShowing()) {
                mDialog.show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                mResultList = (List<ApiResult>) msg.obj;
                updateAdapter(mResultList);
                if (mDialog != null)
                    mDialog.dismiss();
                break;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if (adapter != null) {
            adapter = null;
        }
        super.onDestroy();
    }
}
