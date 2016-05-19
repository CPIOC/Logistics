package com.cpic.taylor.logistics.RongCloudFragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.cpic.taylor.logistics.R;

/**
 * Created by Taylor on 2016/4/29.
 */
public class MessageFragment extends Fragment{

    private ListView chatList;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_message,null);

        chatList= (ListView) view.findViewById(R.id.chat_list);







        return view;
    }


}
