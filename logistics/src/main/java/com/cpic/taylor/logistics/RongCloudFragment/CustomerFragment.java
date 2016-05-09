package com.cpic.taylor.logistics.RongCloudFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;

import io.rong.imkit.RongIM;

/**
 * Created by Administrator on 2015/3/6.
 */
public class CustomerFragment extends Fragment implements View.OnClickListener {

    /**
     * 客服聊天的按钮
     */
    private TextView mCustomerChat;
    EditText editText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.de_fr_customer, container, false);
        mCustomerChat = (TextView) view.findViewById(R.id.customer_chat);
        mCustomerChat.setOnClickListener(this);
        editText = (EditText)view.findViewById(R.id.kefu_id);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.customer_chat:
                if (RongIM.getInstance() != null) {
                    String id = editText.getEditableText().toString();
                    RongIM.getInstance().startCustomerServiceChat(getActivity(), id, "在线客服");
//                    RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.APP_PUBLIC_SERVICE, "KEFU144542424649464", "在线客服");
                }
                break;
        }
    }
}
