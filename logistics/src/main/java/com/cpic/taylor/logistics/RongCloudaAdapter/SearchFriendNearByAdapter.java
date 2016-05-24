package com.cpic.taylor.logistics.RongCloudaAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudModel.SameRoutineFriendsData;
import com.sea_monster.resource.Resource;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.widget.AsyncImageView;

/**
 * Created by Bob on 2015/3/26.
 */
public class SearchFriendNearByAdapter extends android.widget.BaseAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<UserInfos> mResults;
    private SharedPreferences sp;
    private Activity activity;
    private ArrayList<SameRoutineFriendsData> sameRoutineFriendsData;

    public SearchFriendNearByAdapter(List<UserInfos> results, Context context,ArrayList<SameRoutineFriendsData> sameRoutineFriendsData){
        this.mResults = results;
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        activity= (Activity) context;
        sp = PreferenceManager.getDefaultSharedPreferences(activity);
        this.sameRoutineFriendsData=sameRoutineFriendsData;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;


        if(null==mResults.get(position).getPortrait()){
            mResults.get(position).setPortrait("www.cpioc.com");
        }
        Resource res = new Resource(mResults.get(position).getPortrait());

        if(convertView == null || convertView.getTag() == null){
            convertView = mLayoutInflater.inflate(R.layout.de_item_search_nearby,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.mSearchName = (TextView) convertView.findViewById(R.id.search_item_name_near);
            viewHolder.mImageView = (AsyncImageView) convertView.findViewById(R.id.search_adapter_img_near);
            viewHolder.mDistance= (TextView) convertView.findViewById(R.id.search_item_distance);
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }

        if(viewHolder != null) {
            viewHolder.mSearchName.setText(mResults.get(position).getUsername());
            viewHolder.mImageView.setDefaultDrawable(mContext.getResources().getDrawable(R.drawable.de_default_portrait));
            viewHolder.mImageView.setResource(res);
            viewHolder.mDistance.setText(sameRoutineFriendsData.get(position).getDistance()+"米以内");
        }

        return convertView;
    }

    static class ViewHolder{
        TextView mSearchName;

        AsyncImageView mImageView;

        TextView mDistance;
    }
}