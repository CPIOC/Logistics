package com.cpic.taylor.logistics.RongCloudActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudaAdapter.BlackMultiChoiceAdapter;
import com.cpic.taylor.logistics.RongCloudaAdapter.FriendListAdapter;
import com.cpic.taylor.logistics.RongCloudModel.Friend;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.RongCloudWidget.PinnedHeaderListView;
import com.cpic.taylor.logistics.RongCloudWidget.SwitchGroup;
import com.cpic.taylor.logistics.RongCloudWidget.SwitchItemView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 2015/4/9.
 */
public class BlackListActivity extends BaseActionBarActivity implements SwitchGroup.ItemHander, View.OnClickListener, TextWatcher, FriendListAdapter.OnFilterFinished, AdapterView.OnItemClickListener {

    private String TAG = BlackListActivity.class.getSimpleName();
    protected BlackMultiChoiceAdapter mAdapter;
    private PinnedHeaderListView mListView;
    private SwitchGroup mSwitchGroup;
    /**
     * 好友list
     */
    protected List<Friend> mFriendsList;
    /**
     * 好友列表
     */
    protected List<UserInfo> mUserInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_fr_black);

        getSupportActionBar().setTitle(R.string.the_blacklist);

        mListView = (PinnedHeaderListView) findViewById(R.id.de_ui_friend_list);
        mSwitchGroup = (SwitchGroup) findViewById(R.id.de_ui_friend_message);

        mListView.setPinnedHeaderView(LayoutInflater.from(this).inflate(R.layout.de_item_friend_index,
                mListView, false));

        mListView.setFastScrollEnabled(false);

        mListView.setOnItemClickListener(this);
        mSwitchGroup.setItemHander(this);

        mListView.setHeaderDividersEnabled(false);
        mListView.setFooterDividersEnabled(false);

        getBlackList();

    }

    private void getBlackList() {

        //获取黑名单列表
        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
            RongIM.getInstance().getRongIMClient().getBlacklist(new RongIMClient.GetBlacklistCallback() {
                @Override
                public void onSuccess(String[] userIds) {

                    if (userIds != null) {
                        mUserInfoList = RongYunContext.getInstance().getUserInfoList(userIds);

                        mFriendsList = new ArrayList<Friend>();

                        if (mUserInfoList != null) {
                            for (UserInfo userInfo : mUserInfoList) {
                                Friend friend = new Friend();
                                friend.setNickname(userInfo.getName());
                                friend.setPortrait(userInfo.getPortraitUri() + "");
                                friend.setUserId(userInfo.getUserId());
                                mFriendsList.add(friend);
                            }
                        }
                        mFriendsList = sortFriends(mFriendsList);
                        mAdapter = new BlackMultiChoiceAdapter(BlackListActivity.this, mFriendsList);
                        mListView.setAdapter(mAdapter);
                        mAdapter.setAdapterData(mFriendsList);
                        mAdapter.notifyDataSetChanged();

                    }
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                }
            });
        }

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object tagObj = view.getTag();
        if (tagObj != null && tagObj instanceof BlackMultiChoiceAdapter.ViewHolder) {
            final BlackMultiChoiceAdapter.ViewHolder viewHolder = (BlackMultiChoiceAdapter.ViewHolder) tagObj;
            mAdapter.onItemClick(viewHolder.friend.getUserId());

            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("从黑名单中删除");
            dialog.setItems(new String[]{"删除", "取消"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case 0:
                            if (RongIM.getInstance() != null)
                                RongIM.getInstance().getRongIMClient().removeFromBlacklist(viewHolder.friend.getUserId(), new RongIMClient.OperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        if (mFriendsList != null) {
                                            Friend friend = new Friend(viewHolder.friend.getUserId(), viewHolder.friend.getNickname(), viewHolder.friend.getPortrait());
                                            mFriendsList.remove(friend);
                                            mAdapter.setAdapterData(mFriendsList);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onError(RongIMClient.ErrorCode errorCode) {
                                        Log.e(TAG, "----onError--"+errorCode);
                                    }
                                });

                            break;
                    }
                }
            });
            dialog.show();
        }
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
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.destroy();
            mAdapter = null;
        }
    }

    @Override
    public void onFilterFinished() {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

}
