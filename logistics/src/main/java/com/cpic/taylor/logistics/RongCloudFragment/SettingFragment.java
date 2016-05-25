package com.cpic.taylor.logistics.RongCloudFragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudActivity.FriendListActivity;
import com.cpic.taylor.logistics.RongCloudActivity.MainActivity;
import com.cpic.taylor.logistics.RongCloudActivity.UpdateDiscussionActivity;
import com.cpic.taylor.logistics.RongCloudActivity.UpdateGroupUserInfoActivity;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;

import java.util.Locale;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.DispatchResultFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;

/**
 * Created by Bob on 2015/3/27.
 */
public class SettingFragment extends DispatchResultFragment implements View.OnClickListener {
    private String TAG = SettingFragment.class.getSimpleName();
    private String targetId;
    private Conversation.ConversationType mConversationType;
    private Button mDeleteBtn;
    private RelativeLayout mChatRoomRel;
    private RelativeLayout mGroupUserInfoRel;
    private TextView mChatRoomName;
    private TextView mGroupUserInfo;

    private android.support.v4.app.Fragment mAddNumberFragment;
    private android.support.v4.app.Fragment mToTopFragment;
    private String mDiscussionName;
    private FragmentTransaction fragmentTransaction;
    private android.support.v4.app.Fragment mCollectFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.de_ac_friend_setting, container, false);

        mDeleteBtn = (Button) view.findViewById(R.id.de_fr_delete);
        mChatRoomRel = (RelativeLayout) view.findViewById(R.id.de_set_chatroom_name);
        mGroupUserInfoRel = (RelativeLayout) view.findViewById(R.id.set_group_user_info);
        mChatRoomName = (TextView) view.findViewById(R.id.de_chatroom_name);
        mGroupUserInfo = (TextView) view.findViewById(R.id.de_group_user_info);

        mAddNumberFragment = getChildFragmentManager().findFragmentById(R.id.de_fr_add_friend);
        mToTopFragment = getChildFragmentManager().findFragmentById(R.id.de_fr_to_top);
        mCollectFragment=getChildFragmentManager().findFragmentById(R.id.de_fr_collection);
        fragmentTransaction = getFragmentManager().beginTransaction();

        mDeleteBtn.setOnClickListener(this);
        mChatRoomRel.setOnClickListener(this);
        mGroupUserInfoRel.setOnClickListener(this);

        Intent intent = getActivity().getIntent();

        targetId = intent.getData().getQueryParameter("targetId");

        if (targetId != null) {

            mConversationType = Conversation.ConversationType.valueOf(intent.getData().getLastPathSegment().toUpperCase(Locale.getDefault()));
            showViewByConversationType(mConversationType);

            RongContext.getInstance().setOnMemberSelectListener(new RongIM.OnSelectMemberListener() {
                @Override
                public void startSelectMember(Context context, Conversation.ConversationType conversationType, String id) {
                    mConversationType = Conversation.ConversationType.valueOf(getActivity().getIntent().getData().getLastPathSegment().toUpperCase(Locale.getDefault()));

                    Intent in = new Intent(getActivity(), FriendListActivity.class);
                    in.putExtra("DEMO_FRIEND_TARGETID", id);
                    in.putExtra("DEMO_FRIEND_CONVERSATTIONTYPE", conversationType.toString());
                    in.putExtra("DEMO_FRIEND_ISTRUE", true);
                    startActivityForResult(in, 22);
                }
            });
        }
        return view;
    }

    /**
     * 通过 会话类型选择要展示的 fragment
     *
     * @param mConversationType 会话类型
     */
    private void showViewByConversationType(Conversation.ConversationType mConversationType) {
        if (mConversationType.equals(Conversation.ConversationType.DISCUSSION)) {
            mDeleteBtn.setVisibility(View.VISIBLE);
            mChatRoomRel.setVisibility(View.VISIBLE);
            RongIM.getInstance().getRongIMClient().getDiscussion(targetId, new RongIMClient.ResultCallback<Discussion>() {
                @Override
                public void onSuccess(Discussion discussion) {
                    mDiscussionName = discussion.getName();

                    mChatRoomName.setText(mDiscussionName);
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });
        } else if (mConversationType.equals(Conversation.ConversationType.PRIVATE)) {

            fragmentTransaction.hide(mCollectFragment);
            fragmentTransaction.commit();
        } else if (mConversationType.equals(Conversation.ConversationType.CHATROOM)) {
            fragmentTransaction.hide(mAddNumberFragment);
            fragmentTransaction.hide(mToTopFragment);
            fragmentTransaction.commit();
        } else if (mConversationType.equals(Conversation.ConversationType.GROUP)) {

            if (RongYunContext.getInstance() == null)
                return;

            String username = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_NAME, Constants.DEFAULT);
            mGroupUserInfo.setText(username);
            mGroupUserInfoRel.setVisibility(View.VISIBLE);
            fragmentTransaction.hide(mAddNumberFragment);
            fragmentTransaction.commit();
        } else if (mConversationType.equals(Conversation.ConversationType.CUSTOMER_SERVICE)) {
            fragmentTransaction.hide(mAddNumberFragment);
            fragmentTransaction.hide(mToTopFragment);
            fragmentTransaction.commit();
        }
    }


    @Override
    protected void initFragment(Uri uri) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.de_fr_delete:

                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null)
                    RongIM.getInstance().getRongIMClient().quitDiscussion(targetId, new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.DISCUSSION, targetId, new RongIMClient.ResultCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {

                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                    getActivity().finish();
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {

                                }
                            });
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                break;
            case R.id.de_set_chatroom_name:
                Intent intent = new Intent(getActivity(), UpdateDiscussionActivity.class);
                intent.putExtra("DEMO_DISCUSSIONIDS", targetId);
                if (mDiscussionName != null)
                    intent.putExtra("DEMO_DISCUSSIONNAME", mDiscussionName.toString());
                startActivityForResult(intent, 21);
                break;

            case R.id.set_group_user_info:

                Intent groupIntent = new Intent(getActivity(), UpdateGroupUserInfoActivity.class);
                groupIntent.putExtra("DEMO_GROUP_ID", targetId);
                startActivityForResult(groupIntent, 22);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case Constants.FIX_DISCUSSION_NAME:
                if (data != null)
                    mChatRoomName.setText(data.getStringExtra("UPDATA_DISCUSSION_RESULT"));

                break;
            case Constants.FIX_GROUP_INFO:
                if (data != null) {
                    mGroupUserInfo.setText(data.getStringExtra("UPDATA_GROPU_INFO"));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
