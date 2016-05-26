package com.cpic.taylor.logistics.RongCloudFragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudActivity.GroupListActivity;
import com.cpic.taylor.logistics.RongCloudActivity.MainActivity;
import com.cpic.taylor.logistics.RongCloudActivity.NewFriendListActivity;
import com.cpic.taylor.logistics.RongCloudActivity.PersonalDetailActivity;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudModel.Friend;
import com.cpic.taylor.logistics.RongCloudModel.MyFriends;
import com.cpic.taylor.logistics.RongCloudWidget.PinnedHeaderListView;
import com.cpic.taylor.logistics.RongCloudWidget.SwitchGroup;
import com.cpic.taylor.logistics.RongCloudWidget.SwitchItemView;
import com.cpic.taylor.logistics.RongCloudaAdapter.ContactsMultiChoiceAdapter;
import com.cpic.taylor.logistics.RongCloudaAdapter.FriendListAdapter;
import com.cpic.taylor.logistics.activity.LoginActivity;
import com.cpic.taylor.logistics.base.RongYunContext;
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
import java.util.HashMap;
import java.util.List;

import io.rong.imlib.model.UserInfo;

/**
 * Created by Taylor on 2016/4/29.
 */
public class ContactsFragment extends Fragment implements Handler.Callback, SwitchGroup.ItemHander, View.OnClickListener, TextWatcher, FriendListAdapter.OnFilterFinished, AdapterView.OnItemClickListener {
    protected ContactsMultiChoiceAdapter mAdapter;
    private PinnedHeaderListView mListView;
    private SwitchGroup mSwitchGroup;
    /**
     * 好友list
     */
    protected List<Friend> mFriendsList;
    private TextView textView;
    private ReceiveMessageBroadcastReciver mBroadcastReciver;
    private MainActivity mainActivity;
    private HttpUtils post;
    private RequestParams params;
    private Dialog dialog;
    private SharedPreferences sp;
    /**
     * 融云好友
     */
    MyFriends myFriends;
    ArrayList<UserInfos> friendsList = new ArrayList<UserInfos>();
    private Handler mHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.de_ac_address_fragment, null);
        mainActivity = (MainActivity) getActivity();
        //mainActivity.setTitleBar();
        mListView = (PinnedHeaderListView) view.findViewById(R.id.de_ui_friend_list);
        mSwitchGroup = (SwitchGroup) view.findViewById(R.id.de_ui_friend_message);

        mListView.setPinnedHeaderView(LayoutInflater.from(mainActivity).inflate(R.layout.de_item_friend_index,
                mListView, false));
        //TODO
        textView = (TextView) mListView.getPinnedHeaderView();

        mListView.setFastScrollEnabled(false);

        mListView.setOnItemClickListener(this);
        mSwitchGroup.setItemHander(this);

        mListView.setHeaderDividersEnabled(false);
        mListView.setFooterDividersEnabled(false);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTION_DMEO_AGREE_REQUEST);
        if (mBroadcastReciver == null) {
            mBroadcastReciver = new ReceiveMessageBroadcastReciver();
        }
        mainActivity.registerReceiver(mBroadcastReciver, intentFilter);
        mHandler = new Handler(this);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();


        getFriendsFuction();


    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("Tag", "1");

    }

    public void getFriendList() {

        ArrayList<UserInfo> userInfos = null;

        //获取好友列表
        if (RongYunContext.getInstance() != null) {
            userInfos = RongYunContext.getInstance().getFriendList();
        }
        mFriendsList = new ArrayList<Friend>();

        if (userInfos != null) {
            for (UserInfo userInfo : userInfos) {
                Friend friend = new Friend();
                friend.setNickname(userInfo.getName());
                friend.setPortrait(userInfo.getPortraitUri() + "");
                friend.setUserId(userInfo.getUserId());
                mFriendsList.add(friend);
            }
        }
        mFriendsList = sortFriends(mFriendsList);
//        mFriendsList.get(0).getSearchKey();
        mAdapter = new ContactsMultiChoiceAdapter(mainActivity, mFriendsList);
        mListView.setAdapter(mAdapter);

        fillData();

    }


    @Override
    public void onClick(View v) {
        if (v instanceof SwitchItemView) {
            CharSequence tag = ((SwitchItemView) v).getText();

            if (mAdapter != null && mAdapter.getSectionIndexer() != null) {
                Object[] sections = mAdapter.getSectionIndexer().getSections();
                int size = sections.length;

                for (int i = 0; i < size; i++) {
                    if (tag.equals(sections[i])) {
                        int index = mAdapter.getPositionForSection(i);
                        mListView.setSelection(index + mListView.getHeaderViewsCount());
                        break;
                    }
                }
            }
        }

    }

    @Override
    public void onFilterFinished() {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object tagObj = view.getTag();

        if (tagObj != null && tagObj instanceof ContactsMultiChoiceAdapter.ViewHolder) {
            ContactsMultiChoiceAdapter.ViewHolder viewHolder = (ContactsMultiChoiceAdapter.ViewHolder) tagObj;
            String friendId = viewHolder.friend.getUserId();
            if (friendId == "★001") {
                Intent intent = new Intent(mainActivity, NewFriendListActivity.class);
                startActivity(intent);
            } else if (friendId == "★002") {
                /*if (RongIM.getInstance() != null) {
                    RongIM.getInstance().startSubConversationList(mainActivity, Conversation.ConversationType.GROUP);
                }*/
                Intent intent = new Intent(getActivity(), GroupListActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(mainActivity, PersonalDetailActivity.class);
                intent.putExtra("CONTACTS_USER", viewHolder.friend.getUserId());
                startActivityForResult(intent, 19);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public boolean handleMessage(Message message) {
        return false;
    }

    private class ReceiveMessageBroadcastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MainActivity.ACTION_DMEO_AGREE_REQUEST)) {
                updateDate();
            }
        }

    }

    private void updateDate() {
        if (mAdapter != null) {
            mAdapter = null;
        }
        ArrayList<UserInfo> userInfos = null;
        //获取好友列表
        if (RongYunContext.getInstance() != null) {
            userInfos = RongYunContext.getInstance().getFriendList();
        }
        mFriendsList = new ArrayList<Friend>();

        if (userInfos != null) {
            for (UserInfo userInfo : userInfos) {
                Friend friend = new Friend();
                friend.setNickname(userInfo.getName());
                friend.setPortrait(userInfo.getPortraitUri() + "");
                friend.setUserId(userInfo.getUserId());
                mFriendsList.add(friend);
            }
        }
        mFriendsList = sortFriends(mFriendsList);
//        mFriendsList.get(0).getSearchKey();
        mAdapter = new ContactsMultiChoiceAdapter(mainActivity, mFriendsList);

        mListView.setAdapter(mAdapter);
        fillData();
    }

    private final void fillData() {

//        mAdapter.removeAll();
        mAdapter.setAdapterData(mFriendsList);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 好友数据排序
     *
     * @param friends 好友 List
     * @return 排序后的好友 List
     */
    private ArrayList<Friend> sortFriends(List<Friend> friends) {

        String[] searchLetters = getResources().getStringArray(R.array.de_search_letters);

        HashMap<String, ArrayList<Friend>> userMap = new HashMap<String, ArrayList<Friend>>();

        ArrayList<Friend> friendsArrayList = new ArrayList<Friend>();

        for (Friend friend : friends) {

            String letter = new String(new char[]{friend.getSearchKey()});

            if (userMap.containsKey(letter)) {
                ArrayList<Friend> friendList = userMap.get(letter);
                friendList.add(friend);

            } else {
                ArrayList<Friend> friendList = new ArrayList<Friend>();
                friendList.add(friend);
                userMap.put(letter, friendList);
            }

        }
        ArrayList<Friend> friendList = new ArrayList<Friend>();
        friendList.add(new Friend("★001", "新的朋友", getResources().getResourceName(R.drawable.de_address_new_friend)));
        friendList.add(new Friend("★002", "群聊", getResources().getResourceName(R.drawable.de_address_group)));
        //friendList.add(new Friend("★003", "公众号", getResources().getResourceName(R.drawable.de_address_public)));
        userMap.put("★", friendList);
        for (int i = 0; i < searchLetters.length; i++) {
            String letter = searchLetters[i];
            ArrayList<Friend> fArrayList = userMap.get(letter);
            if (fArrayList != null) {
                friendsArrayList.addAll(fArrayList);
            }
        }

        return friendsArrayList;
    }

    @Override
    public void onDestroy() {
        if (mBroadcastReciver != null) {
            mainActivity.unregisterReceiver(mBroadcastReciver);
        }
        if (mAdapter != null) {
            mAdapter.destroy();
            mAdapter = null;
        }
        super.onDestroy();
    }

    /**
     * 从服务器获取好友列表
     */

    public void getFriendsFuction() {

        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        params.addBodyParameter("token", sp.getString("token", null));
        String url = UrlUtils.POST_URL + UrlUtils.path_friendslist;
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
                JSONObject jsonObj = null;
                try {

                    Gson gson = new Gson();
                    java.lang.reflect.Type type = new TypeToken<MyFriends>() {
                    }.getType();
                    myFriends = gson.fromJson(result, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (myFriends.getCode() == 1) {

                    RongYunContext.getInstance().deleteUserInfos();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != myFriends.getdata()) {
                                for (int i = 0; i < myFriends.getdata().size(); i++) {
                                    UserInfos userInfos = new UserInfos();
                                    userInfos.setUserid(myFriends.getdata().get(i).getCloud_id());
                                    userInfos.setUsername(myFriends.getdata().get(i).getName());
                                    userInfos.setStatus("1");
                                    if (myFriends.getdata().get(i).getImg() != null)
                                        userInfos.setPortrait(myFriends.getdata().get(i).getImg());
                                    friendsList.add(userInfos);
                                }
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

                } else if (myFriends.getCode() == 2) {

                    Toast.makeText(getActivity(), "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            getActivity().startActivity(intent);
                            getActivity().finish();
                        }
                    }, 10);

                } else {
                    showShortToast(myFriends.getmsg());
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
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
