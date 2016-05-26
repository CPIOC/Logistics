package com.cpic.taylor.logistics.RongCloudFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudModel.RCUser;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.RongCloudaAdapter.FriendMultiChoiceAdapter;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.rong.imkit.RLog;
import io.rong.imkit.RongContext;
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
    private HttpUtils post;
    private RequestParams params;
    private SharedPreferences sp;
    private String groupName = "群聊";
    private String userName;
    private RCUser rcUser;

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

                if (conversation != null && conversation.getConversationType() != null) {
                    mMemberIds.add(conversation.getTargetId());
                    //Log.e("tag", "getSenderUserName"+mTargetId+conversation.getTargetId());
                }


            } else if (mConversationType.equals(Conversation.ConversationType.DISCUSSION)) {

            }
            //Log.e("tag", "mConversationType" + mConversationType + "mTargetId" + mTargetId);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setMultiChoice(true, new ArrayList<String>(mMemberIds));

        selectButton = (Button) view.findViewById(R.id.send_message_friend);
        TextView tilefinish = (TextView) view.findViewById(R.id.send_message_finish);
        //mLinearFinish = (LinearLayout) view.findViewById(R.id.liner_click);
        tilefinish.setOnClickListener(new View.OnClickListener() {
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
                //Log.e("Tag","selectButtonShowStyle"+2+444);


            } else if (mConversationType.equals(Conversation.ConversationType.DISCUSSION) && mTargetId != null) {
                if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null)
                    RongIM.getInstance().getRongIMClient().getDiscussion(mTargetId, new RongIMClient.ResultCallback<Discussion>() {
                        @Override
                        public void onSuccess(Discussion discussion) {

                            mNumberLists = (ArrayList<String>) discussion.getMemberIdList();

                            selectButtonShowStyle(mNumberLists.size() - 1, 0);
                            //Log.e("Tag","selectButtonShowStyle"+3);
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
                //Log.e("Tag", "selectButtonShowStyle" + 4);
            } else {
                selectButtonShowStyle(0, 0);
                //Log.e("Tag", "selectButtonShowStyle" + 5);
            }
        }

        mHandle = new Handler(this);

        super.onViewCreated(view, savedInstanceState);

    }


    private void selectButtonShowStyle(int selectedCount, int hasSelect) {

        if (selectedCount > 0) {
            selectButton.setEnabled(true);
            mConfirmFromatString = getResources().getString(R.string.friend_list_multi_choice_comfirt_btn);
            selectButton.setTextColor(getResources().getColor(R.color.choose_chat_number));
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
        //Log.e("tag", "mMemberIds.size()hehehehehe" );
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


        //Log.e("tag", "mConversationTyfffffffpe55555" + "2" + mTargetId + groupName + "ids" + ids + "userInfos" + userInfos.get(0).getUserId());

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
                groupName = sb.toString();

                if (isFromSetting) {

                    //Log.e("tag", "mMemberIds.size()" + mMemberIds.size());

                    if (mMemberIds.size() == 1) {
                        //Log.e(TAG, "-----selectPeopleComplete---MemberIds.size():" + sb.toString());
                        if (RongIM.getInstance() != null)
                            //addChatGroup(groupName,TextUtils.join(",", ids));
                            if (null != groupName) {
                                if (groupName.length() > 10) {
                                    groupName = groupName.substring(0, 8) + "...";
                                }
                            }

                      /*  RongIM.getInstance().getRongIMClient().createDiscussion(groupName, ids, new RongIMClient.CreateDiscussionCallback() {

                            @Override
                            public void onSuccess(String s) {
                                Log.e(TAG, "-----selectPeopleComplete---=＝onSuccess＝＝＝＝＋＋＋＋" + s);

                                getActivity().finish();
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode e) {
                                Log.e(TAG, "-----selectPeopleComplete---=＝onError＝＝＝＝＋＋＋＋" + e);
                            }
                        });*/
                        creatTwoDiscussion(mTargetId, userInfos);
                        //Log.e("tag", "getSendercajjjjjjjjjjjjjjjjjjUserName");

                    } else {


                        mLoadingDialog.show();

                        if (!TextUtils.isEmpty(mTargetId)) {
                            //Log.e("tag", "mMemberIds.size(3)" + mMemberIds.size());
                            //Log.e("tag", "userInfos" + userInfos.size());

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
                            //creatTwoDiscussion(mTargetId, userInfos);
                        }
                    }
                } else {
                    //Log.e("tag", "mConversationTyfffffffpe2" + "2" + mTargetId + groupName + "ids" + ids + "userInfos" + userInfos.get(0).getUserId());
                    if (mMemberIds.size() == 0) {
                        //RongIM.getInstance().

                        createDiscussionChat(getActivity(), ids, groupName);

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

                            //RongIM.getInstance().
                            createDiscussionChat(getActivity(), ids, groupName);

                        }
                    }
                }
            }
        } else if (mConversationType == Conversation.ConversationType.PRIVATE) {

            //RongIM.getInstance().startPrivateChat(getActivity(), userInfos.get(0).getUserId(), userInfos.get(0).getName());
            //Log.e("tag", "mConversationType2" + "2" + mTargetId + groupName + "ids" + ids + "userInfos" + userInfos.get(0).getUserId());
            //createDiscussionChat(getActivity(), ids, groupName);
            creatTwoDiscussion(mTargetId, userInfos);
            // getActivity().finish();
            return;
        }

    }

    private void creatTwoDiscussion(final String cloud_id, final ArrayList<UserInfo> userInfos) {
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
                        UserInfos f = new UserInfos();
                        f.setUserid(idd);
                        f.setUsername(named);
                        f.setPortrait(uritestd);
                        f.setStatus("1");
                        UserInfo userInfo = new UserInfo(idd, named, Uri.parse(uritestd));


                        StringBuilder sbb = new StringBuilder();
                        List<String> idss = new ArrayList<String>();

                        //idss.add(userInfos.get(0).getUserId());

                        for (UserInfo item : userInfos) {
                            idss.add(item.getUserId());
                            if (sbb.length() <= 60) {
                                if (sbb.length() > 0 && !TextUtils.isEmpty(item.getName()))
                                    sbb.append(",");
                                sbb.append(item.getName());
                            }
                        }
                        idss.add(userInfo.getUserId());
                        groupName = sbb.toString() + "," + userInfo.getName();
                        createDiscussionChat(getActivity(), idss, groupName);
                        ///Log.e("tag", "mConversationType2" + "2" + userInfo.getUserId() + userInfos.get(0).getUserId());
                        //Log.e("tag", "mConversationType2" + "2" + mTargetId + groupName + "ids" + idss.size() + "userInfos" + userInfos.get(0).getUserId());
                        getActivity().finish();

                    }


                } else if (rcUser.getCode() == 2) {

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

                    showShortToast(rcUser.getMsg());

                }

            }

        });


    }

    public void createDiscussionChat(final Context context, final List<String> targetUserIds, final String title) {
        if (context != null && targetUserIds != null && targetUserIds.size() != 0) {
            if (RongContext.getInstance() == null) {
                throw new ExceptionInInitializerError("RongCloud SDK not init");
            } else if (RongIM.getInstance().getRongIMClient() == null) {
                RLog.d(this, "disconnect", "RongIMClient does not init.");
            } else {
                RongIM.getInstance().getRongIMClient().createDiscussion(title, targetUserIds, new RongIMClient.CreateDiscussionCallback() {
                    public void onSuccess(String targetId) {
                        Uri uri = Uri.parse("rong://" + context.getApplicationInfo().packageName).buildUpon().appendPath("conversation").appendPath(Conversation.ConversationType.DISCUSSION.getName().toLowerCase()).appendQueryParameter("targetIds", TextUtils.join(",", targetUserIds)).appendQueryParameter("delimiter", ",").appendQueryParameter("targetId", targetId).appendQueryParameter("title", title).build();
                        context.startActivity(new Intent("android.intent.action.VIEW", uri));
                        Log.e("Tag", "群聊Ids" + targetId);
                        //addChatGroup(groupName, targetId);

                    }

                    public void onError(RongIMClient.ErrorCode e) {
                        RLog.d(this, "createDiscussionChat", "createDiscussion not success." + e);
                    }
                });
            }
        } else {
            throw new IllegalArgumentException();
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
                            mHandle.obtainMessage(HANDLE_UPDATE_CONFIRM_BUTTON, count - 1).sendToTarget();
                            Log.e("Tag", "selectButtonShowStylecount1" + count);
                        } else if (mConversationType.equals(Conversation.ConversationType.DISCUSSION) && mTargetId != null) {
//                            if()
                            mHandle.obtainMessage(HANDLE_UPDATE_CONFIRM_BUTTON, count - 1).sendToTarget();
                            Log.e("Tag", "selectButtonShowStylecount2" + count);
                        }
                    } else {
                        if (mTargetId != null)
                            mHandle.obtainMessage(HANDLE_UPDATE_CONFIRM_BUTTON, count + mTargetIds.length - mMemberIds.size()).sendToTarget();
                        else
                            mHandle.obtainMessage(HANDLE_UPDATE_CONFIRM_BUTTON, count - mMemberIds.size()).sendToTarget();
                    }
                    Log.e("Tag", "selectButtonShowStylecount2" + count);
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
            Log.e("Tag", "selectButtonShowStyle" + 1 + "dddd" + (Integer) msg.obj);
        }
        return false;
    }

    /**
     * Toast短显示
     *
     * @param msg
     */
    protected void showShortToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void addChatGroup(String chat_name, String ids) {

        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        params.addBodyParameter("token", sp.getString("token", null));
        params.addBodyParameter("chat_name", chat_name);
        params.addBodyParameter("target_ids", ids);
        String url = UrlUtils.POST_URL + UrlUtils.path_createChat;
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
                    jsonObj = new JSONObject(result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if ("1".equals(String.valueOf(jsonObj.getInt("code")))) {

                        showShortToast("添加成功");

                    } else if ("2".equals(String.valueOf(jsonObj.getInt("code")))) {

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
                        showShortToast(jsonObj.getString("msg"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

    }
}
