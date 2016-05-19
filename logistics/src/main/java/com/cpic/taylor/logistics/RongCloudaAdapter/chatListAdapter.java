package com.cpic.taylor.logistics.RongCloudaAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;

/**
 * Created by xuan on 2016/5/19.
 */
public class chatListAdapter extends android.widget.BaseAdapter{

    private Context context;

    public chatListAdapter(Context context){

        this.context=context;

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

       view= LayoutInflater.from(context).inflate(R.layout.chat_list_item,null);

        TextView chatName= (TextView) view.findViewById(R.id.chat_name);


        return view;
    }
}
