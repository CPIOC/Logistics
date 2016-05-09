package com.cpic.taylor.logistics.RongCloudaAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.ApiResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.Group;


/**
 * Created by Bob on 2015/1/31.
 */
public class GroupListAdapter extends BaseAdapter  {
    private static final String TAG = GroupListAdapter.class.getSimpleName();
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<ApiResult> mResults;
    private ArrayList<View> mViewList;
    HashMap<String, Group> groupMap;

    OnItemButtonClick mOnItemButtonClick;

    public OnItemButtonClick getOnItemButtonClick() {
        return mOnItemButtonClick;
    }

    public void setOnItemButtonClick(OnItemButtonClick onItemButtonClick) {
        this.mOnItemButtonClick = onItemButtonClick;
    }


    public GroupListAdapter(Context context, List<ApiResult> result, HashMap<String, Group> group) {

        this.mResults = result;
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        this.groupMap = group;
        mViewList = new ArrayList<View>();


    }

    @Override
    public int getCount() {
        return mResults.size();

    }

    @Override
    public ApiResult getItem(int position) {
        return mResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null || convertView.getTag() == null) {
            convertView = mLayoutInflater.inflate(R.layout.de_item_group, parent,false);
            viewHolder = new ViewHolder();
            viewHolder.mGroupName = (TextView) convertView.findViewById(R.id.group_adaper_name);
            viewHolder.mGroupCurrentNum = (TextView) convertView.findViewById(R.id.group_current_num);
            viewHolder.mGroupCurrentSum = (TextView) convertView.findViewById(R.id.group_current_sum);
            viewHolder.mGroupLastmessge = (TextView) convertView.findViewById(R.id.group_last_mess);
            viewHolder.mImageView = (AsyncImageView) convertView.findViewById(R.id.group_adapter_img);
            viewHolder.mSelectButton = (ImageView) convertView.findViewById(R.id.group_select);
            convertView.setTag(viewHolder);
        } else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        if (viewHolder != null) {
            viewHolder.mSelectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnItemButtonClick !=null)
                        mOnItemButtonClick.onButtonClick(position, v);
                }
            });
            viewHolder.mGroupName.setText(mResults.get(position).getName());
            viewHolder.mGroupCurrentNum.setText(mResults.get(position).getNumber() + "/");
            viewHolder.mGroupCurrentSum.setText(mResults.get(position).getMax_number());
            viewHolder.mGroupLastmessge.setText(mResults.get(position).getIntroduce());
            String groupid = mResults.get(position).getId();
            if (groupMap != null) {
                if (groupMap.containsKey(groupid)) {
                    viewHolder.mSelectButton.setBackgroundResource(R.drawable.de_group_chat_selector);
                } else {
                    viewHolder.mSelectButton.setBackgroundResource(R.drawable.de_group_join_selector);
                }
            }
        }

        return convertView;
    }

    public interface OnItemButtonClick{
         boolean onButtonClick(int position, View view);
    }

    static class ViewHolder {
        TextView mGroupName;
        TextView mGroupCurrentNum;
        TextView mGroupCurrentSum;
        TextView mGroupLastmessge;
        AsyncImageView mImageView;
        ImageView mSelectButton;
    }
}
