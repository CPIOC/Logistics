package com.cpic.taylor.logistics.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cpic.taylor.logistics.R;

/**
 * Created by Taylor on 2016/4/29.
 */
public class HomePoliceFragment extends Fragment{
    private SharedPreferences sp;
    private TextView tvAddress;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_police,null);
        initView(view);

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String address = sp.getString("now_address","");

        tvAddress.setText(address);

        return view;
    }

    private void initView(View view) {
        tvAddress = (TextView) view.findViewById(R.id.fragment_home_police_tv_address);
    }
}
