package com.cpic.taylor.logistics.RongCloudFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudaAdapter.FriendMultiChoiceAdapter;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.UserInfo;

public class FriendMultiChoiceFragment extends FriendListFragment implements Handler.Callback {


    private static final int HANDLE_UPDATE_CONFIRM_BUTTON = 10001;
    private static final String TAG = FriendMultiChoiceFragment.class.getSimpleName();

    private FriendMultiChoiceAdapter.MutilChoiceCallback mCallback;

    //    private TextView mConfirmTextView;
    private String mConfirmFromatString;
    private ArrayList<String> mMemberIds;
    private Conversation.ConversationType mConversationType;
    private String mDiscussionId;
    private Handler mHandle;
    private LoadingDialog mLoadingDialog = null;
    private Button selectButton;
    private LinearLayout mLinearFinish;
    private String mTargetId = null;
    private String[] mTargetIds = null;
    private ArrayList<String> mNumberLists;
    private boolean isFromSetting = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mMemberIds = new ArrayList<String>();
        Intent intent = getActivity().getIntent();

        if (intent == null || intent.getData() == null || !intent.getData().getScheme().equals("rong")) {
            mConversationType = Conversation.ConversationType.PRIVATE;
        } else {
            Uri uri = intent.getData();
            mDiscussionId = uri.getQueryParameter("discussionId");
            mTargetId = uri.getQueryParameter("userIds");
            String delimiter = uri.getQueryParameter("delimiter");
            if (TextUtils.isEmpty(delimiter)) {
                delimiter = ",";
            }

            mConversationType = Conversation.ConversationType
                    .valueOf(uri.getLastPathSegment().toUpperCase(Locale.getDefault()));
            if (TextUtils.isEmpty(mTargetId)) {

            } else {
                String[] ids = mTargetId.split(delimiter);

                for (String item : Arrays.asList(ids)) {
                    mMemberIds.add(item);
                }
            }
        }

        if (intent.hasExtra("DEMO_FRIEND_TARGETID") && intent.hasExtra("DEMO_FRIEND_CONVERSATTIONTYPE") && intent.hasExtra("DEMO_FRIEND_ISTRUE")) {

            mTargetId = intent.getStringExtra("DEMO_FRIEND_TARGETID");
            isFromSetting = intent.getBooleanExtra("DEMO_FRIEND_ISTRUE", false);
            String conversationType = intent.getStringExtra("DEMO_FRIEND_CONVERSATTIONTYPE").toUpperCase();
            mConversationType = Conversation.ConversationType.valueOf(conversationType);
            if (mConversationType.equals(Conversation.ConversationType.PRIVATE)) {
                Conversation conversation = RongIM.getInstance().getRongIMClient().getConversation(Conversation.ConversationType.PRIVATE, mTargetId);

                if (conversation != null && conversation.getConversationType() != null)
                    mMemberIds.add(conversation.getTargetId());
            } else if (mConversationType.equals(Conversation.ConversationType.DISCUSSION)) {

            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setMultiChoice(true, new ArrayList<String>(mMemberIds));

        selectButton = (Button) view.findViewById(R.id.send_message_friend);
        ImageView tilefinish = (ImageView) view.findViewById(R.id.send_message_finish);
        mLinearFinish = (LinearLayout) view.findViewById(R.id.liner_click);
        mLinearFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPeopleComplete();
            }
        });

        if (isFromSetting) {
            if (mConversationType.equals(Conversation.ConversationType.PRIVATE) && mTargetId != null) {
                selectButtonShowStyle(1, 0);

            } else if (mConversationType.equals(Conversation.ConversationType.DISCUSSION) && mTargetId != null) {
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null)
                    RongIM.getInstance().getRongIMClient().getDiscussion(mTargetId, new RongIMClient.ResultCallback<Discussion>() {
                        @Override
                        public void onSuccess(Discussion discussion) {

                            mNumberLists = (ArrayList<String>) discussion.getMemberIdList();

                            selectButtonShowStyle(mNumberLists.size() - 1, 0);
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
            }
        } else {
            if (mTargetId != null) {
                mTargetIds = mTargetId.split(",");
                selectButtonShowStyle(mTargetIds.length, 0);
            } else {
                selectButtonShowStyle(0, 0);
            }
        }

        mHandle = new Handler(this);

        super.onViewCreated(view, savedInstanceState);

    }

    private void selectButtonShowStyle(int selectedCount, int hasSelect) {

        if (selectedCount > 0) {
            selectButton.setEnabled(true);
            mConfirmFromatString = getResources().getString(R.string.friend_list_multi_choice_comfirt_btn);
            selectButton.setTextColor(getResources().getColor(R.color.rc_text_color_secondary_inverse));
            selectButton.setText(String.format(mConfirmFromatString, selectedCount + hasSelect));

        } else {
            selectButton.setEnabled(false);
            mConfirmFromatString = getResources().getString(R.string.friend_list_multi_choice_comfirt_btn);
            selectButton.setText(String.format(mConfirmFromatString, 0));
        }
    }

    private final void selectPeopleComplete() {
        if (mAdapter == null)
            return;
        ArrayList<UserInfo> userInfos = ((FriendMultiChoiceAdapter) mAdapter).getChoiceUserInfos();

        int selected = 0;
        if (mMemberIds != null) {
            selected = mMemberIds.size();
        }

        if (!outOfMaxPrompt(userInfos.size() + selected))
            return;

        List<String> ids = new ArrayList<String>();

        if (userInfos.size() == 0) {
            getActivity().finish();
            return;
        }

        if (mConversationType == Conversation.ConversationType.DISCUSSION || userInfos.size() + mMemberIds.size() > 1) {

            StringBuilder sb = new StringBuilder();
            String userId;
            mLoadingDialog = new LoadingDialog(this.getActivity());
            if (RongYunContext.getInstance() != null) {

                userId = RongYunContext.getInstance().getSharedPreferences().getString(Constants.APP_USER_ID, Constants.DEFAULT);
                UserInfo userInfo = RongYunContext.getInstance().getUserInfoById(userId);
                for (UserInfo item : userInfos) {
                    ids.add(item.getUserId());
                    if (sb.length() <= 60) {
                        if (sb.length() > 0 && !TextUtils.isEmpty(item.getName()))
                            sb.append(",");
                        sb.append(item.getName());
                    }
                }
                if (sb.length() <= 60 && userInfo != null) {
                    sb.append(",");
                    sb.append(userInfo.getName());
                }

                if (isFromSetting) {

                    if (mMemberIds.size() == 1) {
                        Log.e(TAG, "-----selectPeopleComplete---MemberIds.size():" + sb.toString());
                        if (RongIM.getInstance() != null)
                            RongIM.getInstance().getRongIMClient().createDiscussion(sb.toString(), ids, new RongIMClient.CreateDiscussionCallback() {
                                @Override
                                public void onSuccess(String s) {
                                    Log.e(TAG, "-----selectPeopleComplete---=＝onSuccess＝＝＝＝＋＋＋＋" + s);
                                    getActivity().finish();
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode e) {
                                    Log.e(TAG, "-----selectPeopleComplete---=＝onError＝＝＝＝＋＋＋＋" + e);
                                }
                            });

                    } else {
                        mLoadingDialog.show();

                        if (!TextUtils.isEmpty(mTargetId)) {

                            RongIM.getInstance().getRongIMClient().addMemberToDiscussion(mTargetId, ids, new RongIMClient.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    if (mLoadingDialog != null) {
                                        mLoadingDialog.dismiss();
                                    }
                                    RongIM.getInstance().startDiscussionChat(getActivity(), mTargetId, "hello");
                                    getActivity().finish();
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    if (mLoadingDialog != null) {
                                        mLoadingDialog.dismiss();
                                    }
                                }
                            });
                        }
                    }
                } else {
                    if (mMemberIds.size() == 0) {
                        RongIM.getInstance().createDiscussionChat(getActivity(), ids, sb.toString());
                        getActivity().finish();
                    } else {
                        mLoadingDialog.show();

                        if (!TextUtils.isEmpty(mDiscussionId)) {

                            RongIM.getInstance().getRongIMClient().addMemberToDiscussion(mDiscussionId, ids, new RongIMClient.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    if (mLoadingDialog != null) {
                                        mLoadingDialog.dismiss();
                                    }
                                    getActivity().finish();
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    if (mLoadingDialog != null) {
                                        mLoadingDialog.dismiss();
                                    }
                                }
                            });
                        } else {
                            ids.addAll(mMemberIds);
                            RongIM.getInstance().createDiscussionChat(getActivity(), ids, sb.toString());
                        }
                    }
                }
            }
        } else if (mConversationType == Conversation.ConversationType.PRIVATE) {

            RongIM.getInstance().startPrivateChat(getActivity(), userInfos.get(0).getUserId(), userInfos.get(0).getName());
            getActivity().finish();
            return;
        }

    }

    /**
     * 超出好友上线提示
     *
     * @param count
     * @return
     */
    private boolean outOfMaxPrompt(int count) {

//        int setMaxCount = getActivity().getResources().getInteger(Res.getInstance(getActivity()).integer("discussion_member_max_count"));
//        String countFormat = getActivity().getResources().getString(Res.getInstance(getActivity()).string("friend_multi_choice_people_max_prompt"));
//
//        if (count >= setMaxCount) {
//            RongToast.toast(this.getActivity(), String.format(countFormat, setMaxCount));
//            return false;
//        } else if (count >= RongConst.SYS.DISCUSSION_PEOPLE_MAX_COUNT) {
//            RongToast.toast(this.getActivity(), String.format(countFormat, RongConst.SYS.DISCUSSION_PEOPLE_MAX_COUNT));
//            return false;
//        }

        return true;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (mCallback == null) {

            mCallback = new FriendMultiChoiceAdapter.MutilChoiceCallback() {

                @Override
                public void callback(int count) {
                    boolean isShow = outOfMaxPrompt(count);
                    if (!isShow)
                        return;

                    if (isFromSetting) {
                        if (mConversationType.equals(Conversation.ConversationType.PRIVATE) && mTargetId != null) {
                            mHandle.obtainMessage(HANDLE_UPDATE_CONFIRM_BUTTON, count).sendToTarget();
                        } else if (mConversationType.equals(Conversation.ConversationType.DISCUSSION) && mTargetId != null) {
//                            if()
                            mHandle.obtainMessage(HANDLE_UPDATE_CONFIRM_BUTTON, count - 1).sendToTarget();
                        }
                    } else {
                        if (mTargetId != null)
                            mHandle.obtainMessage(HANDLE_UPDATE_CONFIRM_BUTTON, count + mTargetIds.length - mMemberIds.size()).sendToTarget();
                        else
                            mHandle.obtainMessage(HANDLE_UPDATE_CONFIRM_BUTTON, count - mMemberIds.size()).sendToTarget();
                    }
                }
            };
        }

        FriendMultiChoiceAdapter adapter = (FriendMultiChoiceAdapter) mAdapter;
        adapter.setCallback((FriendMultiChoiceAdapter.MutilChoiceCallback) mCallback);

        super.onItemClick(parent, view, position, id);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == HANDLE_UPDATE_CONFIRM_BUTTON) {
            selectButtonShowStyle((Integer) msg.obj, 0);
        }
        return false;
    }
}
