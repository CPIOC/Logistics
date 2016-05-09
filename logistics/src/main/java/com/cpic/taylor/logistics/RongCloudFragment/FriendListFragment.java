package com.cpic.taylor.logistics.RongCloudFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudaAdapter.FriendListAdapter;
import com.cpic.taylor.logistics.RongCloudaAdapter.FriendMultiChoiceAdapter;
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
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.UserInfo;

public class FriendListFragment extends Fragment implements SwitchGroup.ItemHander, OnClickListener, TextWatcher, FriendListAdapter.OnFilterFinished, OnItemClickListener {

    private static final String TAG = FriendListFragment.class.getSimpleName();
    protected FriendListAdapter mAdapter;
    private PinnedHeaderListView mListView;
    private SwitchGroup mSwitchGroup;
    private EditText mEditText;

    protected List<Friend> mFriendsList;

    private boolean isMultiChoice = false;

    private ArrayList<String> mSelectedItemIds;
    private ArrayList<String> mHaveSelectedItemIds;
    private boolean isFromSetting = false;
    private Conversation.ConversationType mConversationType;
    private String mTargetId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.de_list_friend, container, false);

        mListView = (PinnedHeaderListView) view.findViewById(R.id.de_ui_friend_list);
        mSwitchGroup = (SwitchGroup) view.findViewById(R.id.de_ui_friend_message);
        mEditText = (EditText) view.findViewById(R.id.de_ui_search);

        mListView.setPinnedHeaderView(LayoutInflater.from(this.getActivity()).inflate(R.layout.de_item_friend_index,
                mListView, false));

        mListView.setFastScrollEnabled(false);

        mListView.setOnItemClickListener(this);
        mSwitchGroup.setItemHander(this);
        mEditText.addTextChangedListener(this);

        mListView.setHeaderDividersEnabled(false);
        mListView.setFooterDividersEnabled(false);

        return view;
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();


        ArrayList<UserInfo> userInfos = null;

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

        if (mSelectedItemIds != null && isMultiChoice) {

            for (String id : mSelectedItemIds) {
                for (Friend friend : mFriendsList) {
                    if (id.equals(friend.getUserId())) {
                        friend.setSelected(true);
                        break;
                    }
                }
            }
        }

        if (intent.hasExtra("DEMO_FRIEND_TARGETID") && intent.hasExtra("DEMO_FRIEND_CONVERSATTIONTYPE") && intent.hasExtra("DEMO_FRIEND_ISTRUE")) {

            mTargetId = intent.getStringExtra("DEMO_FRIEND_TARGETID");
            isFromSetting = intent.getBooleanExtra("DEMO_FRIEND_ISTRUE", false);
            String conversationType = intent.getStringExtra("DEMO_FRIEND_CONVERSATTIONTYPE").toUpperCase();
            mConversationType = Conversation.ConversationType.valueOf(conversationType);
            Log.e(TAG, "0705--onViewCreated--mTargetId" + mTargetId + "--conversationType===" + conversationType);

        }

        if (isFromSetting) {
            mHaveSelectedItemIds = new ArrayList<String>();
            if (mConversationType.equals(Conversation.ConversationType.PRIVATE)) {
                mSelectedItemIds.add(mTargetId);

                if(mTargetId!=null && isMultiChoice){
                    for (Friend friend : mFriendsList) {
                        if(mTargetId.equals(friend.getUserId())){
                            friend.setSelected(true);
                            break;
                        }
                    }
                }
                mAdapter = isMultiChoice ? new FriendMultiChoiceAdapter(getActivity(), mFriendsList, mSelectedItemIds) : new FriendListAdapter(getActivity(), mFriendsList);
                mListView.setAdapter(mAdapter);
                fillData();

            } else if (mConversationType.equals(Conversation.ConversationType.DISCUSSION)) {
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null)
                    RongIM.getInstance().getRongIMClient().getDiscussion(mTargetId, new RongIMClient.ResultCallback<Discussion>() {
                        @Override
                        public void onSuccess(Discussion discussion) {

                            isMultiChoice = true;
                            ArrayList<String> lists = (ArrayList<String>) discussion.getMemberIdList();
                            for (int i = 0; i < lists.size(); i++) {
                                mSelectedItemIds.add(lists.get(i));
                            }

                            if (mSelectedItemIds != null && isMultiChoice) {
                                for (String id : mSelectedItemIds) {
                                    for (Friend friend : mFriendsList) {
                                        if (id.equals(friend.getUserId())) {
                                            friend.setSelected(true);
                                            break;
                                        }
                                    }
                                }
                            }
                            mAdapter = isMultiChoice ? new FriendMultiChoiceAdapter(getActivity(), mFriendsList, mSelectedItemIds) : new FriendListAdapter(getActivity(), mFriendsList);
                            mListView.setAdapter(mAdapter);
                            fillData();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
            }

        } else {
            mAdapter = isMultiChoice ? new FriendMultiChoiceAdapter(this.getActivity(), mFriendsList, mSelectedItemIds) : new FriendListAdapter(this.getActivity(), mFriendsList);
            mListView.setAdapter(mAdapter);
            fillData();
        }

        super.onViewCreated(view, savedInstanceState);
    }

    private final void fillData() {

//        mAdapter.removeAll();
        mAdapter.setAdapterData(mFriendsList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFilterFinished() {
        if (mFriendsList != null && mFriendsList.size() == 0) {
            return;
        }

        if (mAdapter == null || mAdapter.isEmpty()) {
        } else {
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mAdapter != null) {
            mAdapter.getFilter().filter(s);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

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

        if (tagObj != null && tagObj instanceof FriendListAdapter.ViewHolder) {
            FriendListAdapter.ViewHolder viewHolder = (FriendListAdapter.ViewHolder) tagObj;
            mAdapter.onItemClick(viewHolder.friend.getUserId(), viewHolder.choice);
            return;
        }
    }

    @Override
    public void onDestroyView() {
        if (mAdapter != null) {
            mAdapter.destroy();
            mAdapter = null;
        }
        super.onDestroyView();
    }

    public boolean isMultiChoice() {
        return isMultiChoice;
    }

    public void setMultiChoice(boolean isMultiChoice, ArrayList<String> selectedItemIds) {
        this.isMultiChoice = isMultiChoice;
        this.mSelectedItemIds = selectedItemIds;
    }


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

}
