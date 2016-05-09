package com.cpic.taylor.logistics.RongCloudaAdapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.Friend;
import com.sea_monster.resource.Resource;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.UserInfo;

public class FriendMultiChoiceAdapter extends FriendListAdapter {

    private static final String TAG = FriendMultiChoiceAdapter.class.getSimpleName();
    private List<String> mChoiceFriendIds;
    private MutilChoiceCallback mCallback;
    private ArrayList<Friend> mFriends;
    private Context mContext;


    /**
     *
     * @param context
     * @param friends
     * @param mSelectedList
     */
    public FriendMultiChoiceAdapter(Context context, List<Friend> friends, List<String> mSelectedList) {
        super(context, friends);
        this.mContext = context;
        this.mFriends = (ArrayList<Friend>) friends;
        mChoiceFriendIds = mSelectedList;
    }

    @Override
    protected void bindView(View v, int partition, List<Friend> data, int position) {
        super.bindView(v, partition, data, position);

        ViewHolder holder = (ViewHolder) v.getTag();
        TextView name = holder.name;
        AsyncImageView photo = holder.photo;

        Friend friend = data.get(position);
        name.setText(friend.getNickname());

        Resource res = new Resource(friend.getPortrait());

        photo.setDefaultDrawable(mContext.getResources().getDrawable(R.drawable.de_default_portrait));
        photo.setResource(res);

        String userId = friend.getUserId();
        holder.userId = userId;
        if (friend.isSelected()) {
            holder.choice.setButtonDrawable(mContext.getResources().getDrawable(R.drawable.de_ui_friend_checkbox_gray));
            holder.choice.setChecked(true);
            holder.choice.setEnabled(false);
        } else {
            holder.choice.setEnabled(true);
            holder.choice.setChecked(mChoiceFriendIds.contains(friend.getUserId()));
            holder.choice.setButtonDrawable(mContext.getResources().getDrawable(R.drawable.de_ui_friend_check_selector));
        }
    }

    @Override
    protected void newSetTag(View view, ViewHolder holder, int position, List<Friend> data) {
        super.newSetTag(view, holder, position, data);

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.de_ui_friend_checkbox);
        checkBox.setVisibility(View.VISIBLE);
        holder.choice = checkBox;
    }

    @Override
    public void onItemClick(String friendId, CheckBox checkBox) {
        if (!checkBox.isEnabled()) {
            return;
        }

        boolean isChoose = checkBox.isChecked();
        if (isChoose) {
            checkBox.setChecked(!isChoose);
            mChoiceFriendIds.remove(friendId);

            if (mCallback != null) {
                mCallback.callback(mChoiceFriendIds.size());
            }
        } else {
            int setMaxCount = 60;
            if (mChoiceFriendIds.size() <= setMaxCount - 2) {
                checkBox.setChecked(!isChoose);
                mChoiceFriendIds.add(friendId);

                if (mCallback != null) {
                    mCallback.callback(mChoiceFriendIds.size());
                }
            } else {

                if (mCallback != null) {
                    mCallback.callback(setMaxCount);
                }
            }
        }


    }

    public List<String> getChoiceList() {
        return mChoiceFriendIds;
    }

    public ArrayList<UserInfo> getChoiceUserInfos() {
        ArrayList<UserInfo> userInfos = new ArrayList<UserInfo>();

        if (mChoiceFriendIds.size() > 0 && mFriends != null) {

            for (String userId : mChoiceFriendIds) {

                for (Friend friend : mFriends) {
                    //TODO Add Friend
                    if (userId.equals(friend.getUserId()) && !friend.isSelected()) {
                        UserInfo userInfo = new UserInfo(friend.getUserId(), friend.getNickname(), Uri.parse(friend.getPortrait()));
                        userInfos.add(userInfo);
                    }
                }
            }
        }

        return userInfos;
    }

    public void setCallback(MutilChoiceCallback callback) {
        this.mCallback = callback;
    }


    public interface MutilChoiceCallback {
        public void callback(int count);
    }

}
