package com.cpic.taylor.logistics.RongCloudFragment.setting;

import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.cpic.taylor.logistics.R;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.Event;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * Created by Bob on 15/7/29.
 */
public class RongSetConversationToTopFragment extends RongBaseSettingFragment {

    private static final String TAG = RongSetConversationToTopFragment.class.getSimpleName();

    @Override
    protected void initData() {

        if (RongContext.getInstance() != null)
            RongContext.getInstance().getEventBus().register(this);

        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {
            RongIM.getInstance().getRongIMClient().getConversation(getConversationType(), getTargetId(), new RongIMClient.ResultCallback<Conversation>() {

                @Override
                public void onSuccess(final Conversation conversation) {
                    if (conversation != null)
                        setSwitchBtnStatus(conversation.isTop());
                }

                @Override
                public void onError(RongIMClient.ErrorCode e) {

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
        return getString(R.string.de_setting_set_top);
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    @Override
    protected void onSettingItemClick(View v) {
    }

    @Override
    protected int setSwitchBtnVisibility() {
        return View.VISIBLE;
    }

    @Override
    protected void toggleSwitch(boolean toggle) {

        if (getConversationType() != null && !TextUtils.isEmpty(getTargetId())) {
            RongIM.getInstance().getRongIMClient().setConversationToTop(getConversationType(), getTargetId(), toggle,null);
        }

    }

    public void onEventMainThread(Event.ConversationTopEvent conversationTopEvent) {
        if (conversationTopEvent != null && conversationTopEvent.getTargetId().equals(getTargetId()) && conversationTopEvent.getConversationType().getValue() == getConversationType().getValue()) {
            setSwitchBtnStatus(conversationTopEvent.isTop());
        }
    }

    @Override
    public void onDestroy() {

        if (RongContext.getInstance() != null)
            RongContext.getInstance().getEventBus().unregister(this);

        super.onDestroy();

    }

}
