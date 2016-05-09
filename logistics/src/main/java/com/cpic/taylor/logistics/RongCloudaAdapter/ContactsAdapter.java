package com.cpic.taylor.logistics.RongCloudaAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.Friend;
import com.cpic.taylor.logistics.RongCloudModel.FriendSectionIndexer;
import com.cpic.taylor.logistics.RongCloudWidget.PinnedHeaderAdapter;
import com.sea_monster.resource.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imkit.widget.AsyncImageView;

@SuppressLint("UseSparseArrays")
public class ContactsAdapter extends PinnedHeaderAdapter<Friend> implements Filterable {

    private static String TAG = ContactsAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    //    private FriendFilter mFilter;
    private ArrayList<View> mViewList;

    public ContactsAdapter(Context context, List<Friend> friends) {
        super(context);
        setAdapterData(friends);

        mViewList = new ArrayList<View>();

        if (context != null)
            mInflater = LayoutInflater.from(context);

    }

    public void setAdapterData(List<Friend> friends) {


        HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();

        List<List<Friend>> result = new ArrayList<List<Friend>>();
        int key = 0;

        for (Friend friend : friends) {
            key = friend.getSearchKey();

            if (hashMap.containsKey(key)) {
                int position = (Integer) hashMap.get(key);
                if (position <= result.size() - 1) {
                    result.get(position).add(friend);
                }
            } else {
                result.add(new ArrayList<Friend>());
                int length = result.size() - 1;
                result.get(length).add(friend);
                hashMap.put(key, length);
            }
        }
        updateCollection(result);

    }

    @Override
    protected View newView(Context context, int partition, List<Friend> data, int position, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.de_item_addresslist, parent, false);
        ViewHolder holder = new ViewHolder();
        newSetTag(view, holder, position, data);
        view.setTag(holder);
        return view;
    }

    @Override
    protected void bindView(View v, int partition, List<Friend> data, int position) {

        ViewHolder holder = (ViewHolder) v.getTag();
        TextView name = holder.name;
        AsyncImageView photo = holder.photo;
        TextView choice = holder.unreadnum;
        Friend friend = data.get(position);
        name.setText(friend.getNickname());

        Resource res = new Resource(friend.getPortrait());

        photo.setResource(res);

        photo.setTag(position);

        holder.friend = friend;


    }

    @Override
    protected View newHeaderView(Context context, int partition, List<Friend> data, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.de_item_friend_index, parent, false);
        view.setTag(view.findViewById(R.id.index));
        return view;
    }

    @Override
    protected void bindHeaderView(View view, int partition, List<Friend> data) {
        Object objTag = view.getTag();

        if (objTag != null) {
            ((TextView) objTag).setText(String.valueOf(data.get(0).getSearchKey()));
        }
    }


    @Override
    protected View newView(Context context, int position, ViewGroup group) {
        return null;
    }

    @Override
    protected void bindView(View v, int position, Friend data) {

    }

    class PinnedHeaderCache {
        TextView titleView;
        ColorStateList textColor;
        Drawable background;
    }

    @Override
    protected SectionIndexer updateIndexer(CompositeAdapter.Partition<Friend>[] data) {
        return new FriendSectionIndexer(data);
    }

    @Override
    public void configurePinnedHeader(View header, int position, int alpha) {


        PinnedHeaderCache cache = (PinnedHeaderCache) header.getTag();

        if (cache == null) {
            cache = new PinnedHeaderCache();
            cache.titleView = (TextView) header.findViewById(R.id.index);
            cache.textColor = cache.titleView.getTextColors();
            cache.background = header.getBackground();
            header.setTag(cache);
        }

        int section = getSectionForPosition(position);
//TODO
        if (section != -1) {
            if (section == 0) {
                cache.titleView.setText("★");
            } else if (section > 0) {
                String title = (String) getSectionIndexer().getSections()[section];
                cache.titleView.setText(title);
            }
        }

    }

    public static class ViewHolder {
        public TextView name;
        public AsyncImageView photo;
        public String userId;
        public Friend friend;
        public TextView unreadnum;
    }

    protected void newSetTag(View view, ViewHolder holder, int position, List<Friend> data) {

        AsyncImageView photo = (AsyncImageView) view.findViewById(R.id.de_ui_friend_icon);

        if (mViewList != null && !mViewList.contains(view)) {
            mViewList.add(view);
        }

        holder.name = (TextView) view.findViewById(R.id.de_ui_friend_name);
        holder.unreadnum = (TextView) view.findViewById(R.id.de_unread_num);
        holder.photo = photo;
    }

    public void destroy() {

        if (mViewList != null) {
            mViewList.clear();
            mViewList = null;
        }
    }


    @Override
    public Filter getFilter() {
        return null;
    }

    public void onItemClick(String friendId) {

    }

}
