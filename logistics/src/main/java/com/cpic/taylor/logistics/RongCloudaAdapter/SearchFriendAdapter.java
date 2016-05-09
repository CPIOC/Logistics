package com.cpic.taylor.logistics.RongCloudaAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.ApiResult;
import com.sea_monster.resource.Resource;

import java.util.List;

import io.rong.imkit.widget.AsyncImageView;

/**
 * Created by Bob on 2015/3/26.
 */
public class SearchFriendAdapter extends android.widget.BaseAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<ApiResult> mResults;

    public SearchFriendAdapter(List<ApiResult> results, Context context){
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        Resource res = new Resource(mResults.get(position).getPortrait());

        if(convertView == null || convertView.getTag() == null){
            convertView = mLayoutInflater.inflate(R.layout.de_item_search,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.mSearchName = (TextView) convertView.findViewById(R.id.search_item_name);
            viewHolder.mImageView = (AsyncImageView) convertView.findViewById(R.id.search_adapter_img);
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (ViewHolder) convertView.getTag();
        }

        if(viewHolder != null) {
            viewHolder.mSearchName.setText(mResults.get(position).getUsername());
            viewHolder.mImageView.setDefaultDrawable(mContext.getResources().getDrawable(R.drawable.de_default_portrait));
            viewHolder.mImageView.setResource(res);
        }

        return convertView;
    }

    static class ViewHolder{
        TextView mSearchName;

        AsyncImageView mImageView;
    }
}
