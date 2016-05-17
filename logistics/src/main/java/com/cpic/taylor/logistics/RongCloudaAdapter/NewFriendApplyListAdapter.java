package com.cpic.taylor.logistics.RongCloudaAdapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.FriendApplyData;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONObject;

import java.util.List;

import io.rong.imkit.widget.AsyncImageView;

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

    public NewFriendApplyListAdapter(List<FriendApplyData> results, Context context) {
        this.mResults = results;
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
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
            viewHolder.mPortraitImg = (AsyncImageView) convertView.findViewById(R.id.item_friend_portrait);
            viewHolder.mFrienduStateRefuse = (TextView) convertView.findViewById(R.id.item_friend_state_refuse);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();

        }

        viewHolder.mFrienduUserName.setText(mResults.get(position).getName());
        viewHolder.mFrienduState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendAction(mResults.get(position).getId(), "1",position);
                mResults.remove(position);
                notifyDataSetChanged();
            }
        });

        viewHolder.mFrienduStateRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendAction(mResults.get(position).getId(), "2",position);
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
        AsyncImageView mPortraitImg;
    }

    private void friendAction(String user_id, final String type,int position) {

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
                           // UserInfo info = new UserInfo(mResults.get(position).get, mResultList.get(position).getUsername(), mResultList.get(position).getPortrait() == null ? null : Uri.parse(mResultList.get(position).getPortrait()));
                        } else if ("2".equals(type)) {
                            showShortToast("已拒绝");
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

    /**
     * Toast短显示
     *
     * @param msg
     */
    protected void showShortToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
