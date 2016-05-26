package com.cpic.taylor.logistics.RongCloudFragment.setting;

/**
 * Created by xuan on 2016/5/25.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.activity.LoginActivity;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONObject;

import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.BaseSettingFragment;
import io.rong.imkit.widget.AlterDialogFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;

public class RongCollectGroupFragment extends BaseSettingFragment implements AlterDialogFragment.AlterDialogBtnListener {

    private Conversation conversation;
    private HttpUtils post;
    private RequestParams params;
    private SharedPreferences sp;

    @Override
    protected void initData() {

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

    }

    @Override
    protected String setTitle() {
        return getString(R.string.de_setting_collect_group);
    }

    @Override
    protected boolean setSwitchButtonEnabled() {
        return false;
    }

    @Override
    protected int setSwitchBtnVisibility() {
        return View.GONE;
    }

    @Override
    protected void onSettingItemClick(View v) {
        conversation = new Conversation();
        conversation.setConversationType(getConversationType());
        conversation.setTargetId(getTargetId());


        final AlterDialogFragment dialogFragment = AlterDialogFragment.newInstance(getString(R.string.rc_setting_name), getString(R.string.rc_setting_collect_msg_promt), getString(R.string.rc_dialog_cancel), getString(R.string.rc_dialog_ok));
        dialogFragment.setOnAlterDialogBtnListener(this);
        dialogFragment.show(getFragmentManager());

    }


    @Override
    public void onDialogNegativeClick(AlterDialogFragment dialog) {
        dialog.dismiss();
    }

    @Override
    public void onDialogPositiveClick(AlterDialogFragment dialog) {
        if (conversation == null)
            return;


        if (conversation.getConversationType().equals(Conversation.ConversationType.DISCUSSION))
            RongIM.getInstance().getRongIMClient().getDiscussion(conversation.getTargetId(), new RongIMClient.ResultCallback<Discussion>() {
                @Override
                public void onSuccess(Discussion discussion) {
                    Log.e("Tag", "mDiscussionName" + discussion.getName());
                    addChatGroup(discussion.getName(), conversation.getTargetId());

                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });

    }

    private void addChatGroup(String chat_name, String ids) {

        post = new HttpUtils();
        params = new RequestParams();

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

                        showShortToast("收藏成功");

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
                        Log.e("Tag", "msg" + jsonObj.getString("msg"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

    @Override
    protected void toggleSwitch(boolean toggle) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
