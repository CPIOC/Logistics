package com.cpic.taylor.logistics.RongCloudaAdapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudActivity.NewFriendListActivity;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudMessage.AgreedFriendRequestMessage;
import com.cpic.taylor.logistics.RongCloudModel.FriendApplyData;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONObject;

import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.TextMessage;

/**
 * Created by Bob on 2015/3/26.
 */

public class NewFriendApplyListAdapter extends android.widget.BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<FriendApplyData> mResults;
    private HttpUtils post;
    private RequestParams params;
    private SharedPreferences sp;
    private NewFriendListActivity newFriendListActivity;

    public NewFriendApplyListAdapter(List<FriendApplyData> results, Context context) {
        this.mResults = results;
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        newFriendListActivity= (NewFriendListActivity) context;
    }


    @Override
    public int getCount() {
        return mResults.size();
    }

    @Override
    public Object getItem(int position) {
        return mResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = mLayoutInflater.inflate(R.layout.de_item_friend, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mFrienduUserName = (TextView) convertView.findViewById(R.id.item_friend_username);
            viewHolder.mFrienduState = (TextView) convertView.findViewById(R.id.item_friend_state);
            viewHolder.mPortraitImg = (ImageView) convertView.findViewById(R.id.item_friend_portrait);
            viewHolder.mFrienduStateRefuse = (TextView) convertView.findViewById(R.id.item_friend_state_refuse);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();

        }
        Glide.with(mContext).load(mResults.get(position).getImg()).placeholder(R.drawable.de_default_portrait).fitCenter().into(viewHolder.mPortraitImg);
        viewHolder.mFrienduUserName.setText(mResults.get(position).getName());
        viewHolder.mFrienduState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendAction(mResults.get(position).getId(), "1",position,mResults.get(position).getCloud_id(),mResults.get(position));
                mResults.remove(position);
                notifyDataSetChanged();
            }
        });

        viewHolder.mFrienduStateRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendAction(mResults.get(position).getId(), "2",position,mResults.get(position).getCloud_id(),mResults.get(position));
                mResults.remove(position);
                notifyDataSetChanged();
            }
        });


        return convertView;
    }


    static class ViewHolder {
        TextView mFrienduUserName;
        TextView mFrienduState;
        TextView mFrienduStateRefuse;
        ImageView mPortraitImg;
    }

    private void friendAction(String user_id, final String type, final int position, final String cloud_id, final FriendApplyData friendApplyData) {

        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        params.addBodyParameter("token", sp.getString("token", null));
        params.addBodyParameter("user_id", user_id);
        params.addBodyParameter("type", type);
        String url = UrlUtils.POST_URL + UrlUtils.path_friendAction;
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

                        if ("1".equals(type)) {
                            showShortToast("已同意");
                            UserInfos f = new UserInfos();
                            f.setUserid(cloud_id);
                            f.setUsername(friendApplyData.getName());
                            f.setPortrait(friendApplyData.getImg());
                            f.setStatus("1");
                            RongYunContext.getInstance().insertOrReplaceUserInfos(f);
                            RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.PRIVATE, cloud_id);
                           // sendFirstMessage(cloud_id);
                            Log.e("Tag","cloud_id"+cloud_id);
                            RongIM.getInstance().getRongIMClient().sendMessage(Conversation.ConversationType.PRIVATE, cloud_id, TextMessage.obtain("我通过了你的好友请求，现在我们可以开始聊天了"), "", "", new RongIMClient.SendMessageCallback() {
                                @Override
                                public void onError(Integer messageId, RongIMClient.ErrorCode e) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {

                                }
                            }, new RongIMClient.ResultCallback<Message>() {

                                @Override
                                public void onSuccess(Message o) {

                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {

                                }
                            });

                            /*ContactNotificationMessage contact = ContactNotificationMessage.obtain(ContactNotificationMessage.CONTACT_OPERATION_ACCEPT_RESPONSE, sp.getString("name", ""), sp.getString("name", ""), "已同意加你为好友");
                            sendMessage(contact, cloud_id);*/
                        } else if ("2".equals(type)) {
                            showShortToast("已拒绝");
                            RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.PRIVATE, cloud_id);
                        }


                    } else {
                        showShortToast(jsonObj.getString("msg"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

    }

    private void sendMessage(MessageContent messageContent, final String currentUserId) {

        RongIM.getInstance().getRongIMClient().sendMessage(Conversation.ConversationType.PRIVATE, currentUserId, messageContent, "加为好友", "好友",
                new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onSuccess(Integer integer) {

                        RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.PRIVATE, currentUserId);

                    }

                    @Override
                    public void onError(Integer integer, RongIMClient.ErrorCode errorCode) {

                    }
                });
    }

    /**
     * 添加好友成功后，向对方发送一条消息
     *
     * @param id 对方id
     */
    public void sendFirstMessage(String id) {
        final AgreedFriendRequestMessage message = new AgreedFriendRequestMessage(id, "agree");
        if (RongYunContext.getInstance() != null) {
            sp = PreferenceManager.getDefaultSharedPreferences(newFriendListActivity);
            //获取当前用户的 userid
            String  userid = sp.getString("cloud_id","");
            String  username = sp.getString("name","");
            String userportrait = sp.getString("img","");
            UserInfo userInfo = new UserInfo(userid, username, Uri.parse(userportrait));
            //把用户信息设置到消息体中，直接发送给对方，可以不设置，非必选项
            message.setUserInfo(userInfo);
            if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null) {

                //发送一条添加成功的自定义消息，此条消息不会在ui上展示
                RongIM.getInstance().getRongIMClient().sendMessage(Conversation.ConversationType.PRIVATE, id, message, null, null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer messageId, RongIMClient.ErrorCode e) {
                        Log.e("Tag", Constants.DEBUG + "------DeAgreedFriendRequestMessage----onError--");
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Log.e("Tag", Constants.DEBUG + "------DeAgreedFriendRequestMessage----onSuccess--" + message.getMessage());

                    }
                });
            }
        }


    }

    /**
     * Toast短显示
     *
     * @param msg
     */
    protected void showShortToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
