package com.cpic.taylor.logistics.RongCloudaAdapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.ApiResult;

import java.util.List;

/**
 * Created by Bob on 2015/3/26.
 */

public class NewFriendListAdapter extends android.widget.BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<ApiResult> mResults;
    OnItemButtonClick mOnItemButtonClick;

    public OnItemButtonClick getOnItemButtonClick() {
        return mOnItemButtonClick;
    }

    public void setOnItemButtonClick(OnItemButtonClick onItemButtonClick) {
        this.mOnItemButtonClick = onItemButtonClick;
    }

    public NewFriendListAdapter(List<ApiResult> results, Context context){
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

        ViewHolder viewHolder ;
        if(convertView == null || convertView.getTag() == null){
            convertView = mLayoutInflater.inflate(R.layout.de_item_friend,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.mFrienduUserName = (TextView) convertView.findViewById(R.id.item_friend_username);
            viewHolder.mFrienduState = (TextView) convertView.findViewById(R.id.item_friend_state);
            viewHolder.mPortraitImg = (ImageView) convertView.findViewById(R.id.item_friend_portrait);
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }

        if(viewHolder != null) {
            viewHolder.mFrienduUserName.setText(mResults.get(position).getUsername());
            viewHolder.mFrienduState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnItemButtonClick !=null)
                        mOnItemButtonClick.onButtonClick(position, v,mResults.get(position).getStatus());
                }
            });
                switch (mResults.get(position).getStatus()){
                    case 1://好友
                        viewHolder.mFrienduState.setText("已添加");
                        viewHolder.mFrienduState.setBackground(null);
                        break;
                    case 2://请求添加
                        viewHolder.mFrienduState.setText("请求添加");
                        viewHolder.mFrienduState.setBackground(null);
                        break;
                    case 3://请求被添加
                        viewHolder.mFrienduState.setText("添加");
                        viewHolder.mFrienduState.setBackground(mContext.getResources().getDrawable(R.drawable.de_add_friend_selector));
                        break;
                    case 4://请求被拒绝
                        viewHolder.mFrienduState.setText("请求被拒绝");
                        viewHolder.mFrienduState.setBackground(null);
                        break;
                    case 5://我被对方删除
                        viewHolder.mFrienduState.setText("被删除");
                        viewHolder.mFrienduState.setBackground(null);
                        break;

                }
        }

        return convertView;
    }

    public interface OnItemButtonClick{
         boolean onButtonClick(int position, View view, int status);

    }

    static class ViewHolder{
        TextView mFrienduUserName;

        TextView mFrienduState;
        ImageView mPortraitImg;
    }
}
