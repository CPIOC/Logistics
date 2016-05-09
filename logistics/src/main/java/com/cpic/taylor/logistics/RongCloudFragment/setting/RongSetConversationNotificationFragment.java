package com.cpic.taylor.logistics.RongCloudFragment.setting;

import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.cpic.taylor.logistics.R;

import io.rong.imkit.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.BaseSettingFragment;
import io.rong.imkit.model.Event;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * Created by Bob on 15/7/31.
 */
public class RongSetConversationNotificationFragment extends BaseSettingFragment {

    public static RongSetConversationNotificationFragment newInstance() {
        return new RongSetConversationNotificationFragment();
    }


    @Override
    protected void initData() {

        if (RongContext.getInstance() != null)
            RongContext.getInstance().getEventBus().register(this);

        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
            RongIM.getInstance().getRongIMClient().getConversationNotificationStatus(getConversationType(), getTargetId(), new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {

                @Override
                public void onSuccess(final Conversation.ConversationNotificationStatus notificationStatus) {

                    if (notificationStatus != null) {
                        setSwitchBtnStatus(notificationStatus == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB ? false : true);
                    }
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                    setSwitchBtnStatus(!getSwitchBtnStatus());
                }
            });
        }
    }


    @Override
    protected boolean setSwitchButtonEnabled() {
        return true;
    }

    @Override
    protected String setTitle() {
        return getString(R.string.de_setting_conversation_notify);
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    @Override
    protected void onSettingItemClick(View v) {
        RLog.i(this, "onSettingItemClick", v.toString());
    }


    @Override
    protected int setSwitchBtnVisibility() {
        return View.VISIBLE;
    }

    @Override
    protected void toggleSwitch(boolean toggle) {

        Conversation.ConversationNotificationStatus status;

        if (toggle) {
            status = Conversation.ConversationNotificationStatus.NOTIFY;
        } else {
            status = Conversation.ConversationNotificationStatus.DO_NOT_DISTURB;
        }

        if (getConversationType() != null && !TextUtils.isEmpty(getTargetId()) && RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {


            RongIM.getInstance().getRongIMClient().setConversationNotificationStatus(getConversationType(), getTargetId(), status, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {

                @Override
                public void onSuccess(Conversation.ConversationNotificationStatus status) {
                    RLog.i(this, "SetConversationNotificationFragment", "onSuccess--");
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    setSwitchBtnStatus(!getSwitchBtnStatus());
                    //Toast.makeText(getActivity(), getString(R.string.rc_setting_conversation_notify_fail), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            RLog.e(this, "SetConversationNotificationFragment", "Arguments is null");
        }

    }

    public void onEventMainThread(Event.ConversationNotificationEvent event) {
        if (event != null && event.getTargetId().equals(getTargetId()) && event.getConversationType().getValue() == getConversationType().getValue()) {
            setSwitchBtnStatus(event.getStatus() == Conversation.ConversationNotificationStatus.NOTIFY ? true : false);
        }
    }

    @Override
    public void onDestroy() {

        if (RongContext.getInstance() != null)
            RongContext.getInstance().getEventBus().unregister(this);

        super.onDestroy();

    }
}