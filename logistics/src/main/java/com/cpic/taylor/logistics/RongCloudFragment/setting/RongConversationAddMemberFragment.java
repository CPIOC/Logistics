package com.cpic.taylor.logistics.RongCloudFragment.setting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudFragment.setting.adapter.RongConversationAddMemberAdapter;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.BaseFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 15/7/31.
 */
public class RongConversationAddMemberFragment extends BaseFragment implements  RongConversationAddMemberAdapter.OnDeleteIconListener, AdapterView.OnItemClickListener {
    static final int PREPARE_LIST = 1;
    static final int REMOVE_ITEM = 2;
    static final int SHOW_TOAST = 3;

    private Conversation.ConversationType mConversationType;
    private String mTargetId;
    private RongConversationAddMemberAdapter mAdapter;
    private List<String> mIdList = new ArrayList<String>();
    private ArrayList<UserInfo> mMembers = new ArrayList<UserInfo>();
    private GridView mGridList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent;

        RongContext.getInstance().getEventBus().register(this);

        if (getActivity() != null) {

            intent = getActivity().getIntent();

            if (intent.getData() != null) {

                mConversationType = Conversation.ConversationType
                        .valueOf(intent.getData().getLastPathSegment().toUpperCase());

                mTargetId = intent.getData().getQueryParameter("targetId");
            }
        }
        mAdapter = new RongConversationAddMemberAdapter(getActivity());
        mAdapter.setDeleteIconListener(this);

        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
            initData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.de_fr_conversation_member_list, null);
        mGridList = findViewById(view, R.id.de_list);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGridList.setAdapter(mAdapter);
        mGridList.setOnItemClickListener(this);

        mGridList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (MotionEvent.ACTION_UP == event.getAction() && mAdapter.isDeleteState()) {
                    UserInfo addBtn = new UserInfo("RongAddBtn", null, null);
                    mAdapter.add(addBtn);

                    String curUserId = RongIM.getInstance().getRongIMClient().getCurrentUserId();
                    if (mAdapter.getCreatorId() != null && mConversationType.equals(Conversation.ConversationType.DISCUSSION) && curUserId.equals(mAdapter.getCreatorId())) {
                        UserInfo deleteBtn = new UserInfo("RongDelBtn", null, null);
                        mAdapter.add(deleteBtn);

                    }

                    mAdapter.setDeleteState(false);
                    mAdapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            }
        });
    }

    private void initData() {
        if (mConversationType.equals(Conversation.ConversationType.DISCUSSION)) {
            RongIM.getInstance().getRongIMClient().getDiscussion(mTargetId, new RongIMClient.ResultCallback<Discussion>() {
                @Override
                public void onSuccess(Discussion discussion) {
                    mIdList = discussion.getMemberIdList();
                    mAdapter.setCreatorId(discussion.getCreatorId());
                    Message msg = new Message();
                    msg.what = PREPARE_LIST;
                    msg.obj = mIdList;
                    getHandler().sendMessage(msg);
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    getHandler().sendEmptyMessage(SHOW_TOAST);
                }
            });
        } else if (mConversationType.equals(Conversation.ConversationType.PRIVATE)) {
            mIdList.add(mTargetId);
            Message msg = new Message();
            msg.what = PREPARE_LIST;
            msg.obj = mIdList;
            getHandler().sendMessage(msg);
        }

    }

    public void onEventMainThread(UserInfo userInfo) {
        int count = mAdapter.getCount();

        for (int i = 0; i < count; i++) {
            UserInfo temp = mAdapter.getItem(i);
            if (userInfo.getUserId().equals(temp.getUserId())) {
                temp.setName(userInfo.getName());
                temp.setPortraitUri(userInfo.getPortraitUri());
                mAdapter.getView(i, mGridList.getChildAt(i - mGridList.getFirstVisiblePosition()), mGridList);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserInfo userInfo = mAdapter.getItem(position);
        if (userInfo.getUserId().equals("RongDelBtn")) {
            mAdapter.setDeleteState(true);
            int count = mAdapter.getCount();
            mAdapter.remove(count - 1);
            mAdapter.remove(count - 2);
            mAdapter.notifyDataSetChanged();
        } else if (userInfo.getUserId().equals("RongAddBtn")) {
            if (RongContext.getInstance().getMemberSelectListener() == null) {
                throw new ExceptionInInitializerError("The OnMemberSelectListener hasn't been set!");
            }
            RongContext.getInstance().getMemberSelectListener().startSelectMember(getActivity(), mConversationType, mTargetId);
        }
    }

    @Override
    public void onDeleteIconClick(View view, final int position) {
        UserInfo temp = mAdapter.getItem(position);
        RongIM.getInstance().getRongIMClient().removeMemberFromDiscussion(mTargetId, temp.getUserId(), new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Message msg = new Message();
                msg.what = REMOVE_ITEM;
                msg.obj = position;
                getHandler().sendMessage(msg);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                getHandler().sendEmptyMessage(SHOW_TOAST);
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case PREPARE_LIST:
                List<String> mMemberInfo = (List<String>) msg.obj;
                int i = 0;
                for (String id : mMemberInfo) {
                    if (i < 50) {
                        UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(id);
                        if (userInfo == null) {
                            mMembers.add(new UserInfo(id, null, null));
                        } else
                            mMembers.add(userInfo);
                    } else {
                        break;
                    }

                    i++;
                }
                UserInfo addBtn = new UserInfo("RongAddBtn", null, null);
                mMembers.add(addBtn);

                String curUserId = RongIM.getInstance().getRongIMClient().getCurrentUserId();
                if (mAdapter.getCreatorId() != null && mConversationType.equals(Conversation.ConversationType.DISCUSSION) && curUserId.equals(mAdapter.getCreatorId())) {
                    UserInfo deleteBtn = new UserInfo("RongDelBtn", null, null);
                    mMembers.add(deleteBtn);
                }

                mAdapter.addCollection(mMembers);
                mAdapter.notifyDataSetChanged();
                break;
            case REMOVE_ITEM:
                int position =  (Integer) msg.obj;
                mAdapter.remove(position);
                mAdapter.notifyDataSetChanged();
                break;
            case SHOW_TOAST:
                break;
        }
        return true;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onRestoreUI() {
        initData();
    }

}
