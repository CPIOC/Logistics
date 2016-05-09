package com.cpic.taylor.logistics.RongCloudFragment.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;

import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.BaseFragment;
import io.rong.imlib.model.Conversation;

/**
 * Created by Bob on 15/7/31.
 */
public abstract class RongBaseSettingFragment extends BaseFragment implements View.OnClickListener {

    TextView mTextView;
    CheckBox mCheckBox;
    RelativeLayout mSettingItem;
    String mTargetId;
    Conversation.ConversationType mConversationType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = null;

        if (getActivity() != null) {

            intent = getActivity().getIntent();

            if (intent.getData() != null) {

                mConversationType = Conversation.ConversationType
                        .valueOf(intent.getData().getLastPathSegment().toUpperCase());

                mTargetId = intent.getData().getQueryParameter("targetId");
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.de_fr_base_setting, container, false);
        mTextView = (TextView) view.findViewById(R.id.rc_title);
        mCheckBox = (CheckBox) view.findViewById(R.id.rc_checkbox);
        mSettingItem = (RelativeLayout) view.findViewById(R.id.rc_setting_item);

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mTextView.setText(setTitle());
        mCheckBox.setEnabled(setSwitchButtonEnabled());

        if (View.GONE == setSwitchBtnVisibility())
            mCheckBox.setVisibility(View.GONE);
        else if (View.VISIBLE == setSwitchBtnVisibility())
            mCheckBox.setVisibility(View.VISIBLE);

        mCheckBox.setOnClickListener(this);
        mSettingItem.setOnClickListener(this);

        if(RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null)
            initData();

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {

        if (v == mSettingItem) {
            onSettingItemClick(v);
        } else if (v == mCheckBox) {
            toggleSwitch(mCheckBox.isChecked());
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        RongContext.getInstance().getEventBus().unregister(this);

    }

    protected Conversation.ConversationType getConversationType() {
        return mConversationType;
    }

    protected String getTargetId() {
        return mTargetId;
    }


    protected abstract String setTitle();

    protected abstract boolean setSwitchButtonEnabled();

    protected abstract int setSwitchBtnVisibility();

    protected abstract void onSettingItemClick(View v);

    protected abstract void toggleSwitch(boolean toggle);

    protected abstract void initData();

    protected void setSwitchBtnStatus(boolean status) {
        mCheckBox.setChecked(status);
    }

    protected boolean getSwitchBtnStatus() {
        return mCheckBox.isChecked();
    }


    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onRestoreUI(){
        initData();
    }
}
