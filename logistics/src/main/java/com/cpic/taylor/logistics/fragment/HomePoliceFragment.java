package com.cpic.taylor.logistics.fragment;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.bean.Police;
import com.cpic.taylor.logistics.bean.PoliceDataInfo;
import com.cpic.taylor.logistics.utils.ProgressDialogHandle;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.ArrayList;

/**
 * Created by Taylor on 2016/4/29.
 */
public class HomePoliceFragment extends Fragment{
    private SharedPreferences sp;
    private TextView tvAddress;
    private ExpandableListView elv;
    private CarAdapter adapter;
    private ArrayList<PoliceDataInfo> datas;

    private CheckBox cChoose,cSend,cChild;
    private TextView tvThings;

    private int groupClick = -1;
    private int childClick = -1;

    private HttpUtils post;
    private RequestParams params;
    private Dialog dialog;

    private String address,token,lat,lng;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_police,null);
        initView(view);

        /**
         * 获取当前地址信息
         */
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        address = sp.getString("now_address","");
        token = sp.getString("token","");
        lat = sp.getString("now_latitude","");
        lng = sp.getString("now_longitude","");
        tvAddress.setText(address);

        initDatas();
        registerListener();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            address = sp.getString("now_address","");
            token = sp.getString("token","");
            lat = sp.getString("now_latitude","");
            lng = sp.getString("now_longitude","");
            tvAddress.setText(address);
            Log.i("oye",lat+lng+address);
        }
    }

    private void registerListener() {
        elv.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                groupClick = -1;
                childClick = -1;
                adapter.notifyDataSetChanged();
                for (int i = 0; i < adapter.getGroupCount(); i++) {
                    if (groupPosition != i) {
                        elv.collapseGroup(i);
                    }
                }
            }
        });
        elv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                cChoose = (CheckBox) view.findViewById(R.id.item_frag_police_group_cbox_choose);
                cSend = (CheckBox) view.findViewById(R.id.item_frag_police_group_cbox_send);

                if (!cChoose.isChecked()&&!cSend.isChecked()){
                    cSend.setChecked(true);
                    cChoose.setChecked(true);
                } else {
                    cSend.setChecked(false);
                    cChoose.setChecked(false);
                }
                return false;
            }
        });

        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                cChild = (CheckBox) view.findViewById(R.id.item_fragment_police_child_cbox_choose);
                tvThings = (TextView) view.findViewById(R.id.item_fragment_police_child_tv_things);
                groupClick = i;
                childClick = i1;
                adapter.notifyDataSetChanged();

                return false;
            }
        });


    }

    private void initView(View view) {
        tvAddress = (TextView) view.findViewById(R.id.fragment_home_police_tv_address);
        elv = (ExpandableListView) view.findViewById(R.id.fragment_home_police_elv);
        dialog = ProgressDialogHandle.getProgressDialog(getActivity(),null);
    }

    /**
     * 列表选择
     */
    private void initDatas() {

        post = new HttpUtils();
        params = new RequestParams();
        String url = UrlUtils.POST_URL+UrlUtils.path_categorylist;
        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {

            @Override
            public void onStart() {
                super.onStart();
                if (dialog != null){
                    dialog.show();
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (dialog != null){
                    dialog.dismiss();
                }
                Police police = JSONObject.parseObject(responseInfo.result,Police.class);
                int code = police.getCode();
                if (code == 1){
                    datas = police.getData();
                    adapter = new CarAdapter();
                    adapter.setDatas(datas);
                    elv.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (dialog != null){
                    dialog.dismiss();
                }
                Toast.makeText(getActivity(),"获取数据失败，请检查网络连接",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class CarAdapter extends BaseExpandableListAdapter {

        private ArrayList<PoliceDataInfo> titleList = null;


        public void setDatas(ArrayList<PoliceDataInfo> titleList){
            this.titleList = titleList;

        }

        @Override
        public int getGroupCount() {
            return titleList == null ? 0 :titleList.size();
        }
        @Override
        public int getChildrenCount(int groupPosition) {
            return titleList.get(groupPosition).getChildren().size();
        }
        @Override
        public Object getGroup(int groupPosition) {
            return titleList.get(groupPosition);
        }
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return titleList.get(groupPosition).getChildren().get(childPosition);
        }
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }
        @Override
        public boolean hasStableIds() {
            return false;
        }
        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
           final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.item_fragment_police_group_list, null);
                holder = new ViewHolder();
                holder.cBoxGroupChoose = (CheckBox) convertView.findViewById(R.id.item_frag_police_group_cbox_choose);
                holder.cBoxGroupSend = (CheckBox) convertView.findViewById(R.id.item_frag_police_group_cbox_send);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.cBoxGroupChoose.setText(titleList.get(groupPosition).getName());


            if (elv.isGroupExpanded(groupPosition)){
                holder.cBoxGroupChoose.setChecked(true);
                holder.cBoxGroupSend.setChecked(true);
                holder.cBoxGroupSend.setClickable(true);
                holder.cBoxGroupSend.setFocusable(true);

                holder.cBoxGroupSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (groupClick == -1&&childClick == -1){
                            Toast.makeText(getActivity(),"请选择报警的详情",Toast.LENGTH_SHORT).show();
                            holder.cBoxGroupSend.setChecked(true);
                        }else{
                            if (!"".equals(token)&&!"".equals(address)&&!"".equals(lng)&&!"".equals(lat)){
                                callPolice(token,titleList.get(groupClick).getChildren().get(childClick).getId(),address,lat,lng);
                            }else{
                                Toast.makeText(getActivity(),"暂无地理信息，请稍后重试",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            }else{
                holder.cBoxGroupChoose.setChecked(false);
                holder.cBoxGroupSend.setChecked(false);
                holder.cBoxGroupSend.setClickable(false);
                holder.cBoxGroupSend.setFocusable(false);
            }
            return convertView;
        }
        @Override
        public View getChildView(int groupPosition, int childPosition,boolean isLastChild, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.item_fragment_police_child_list, null);
                holder = new ViewHolder();
                holder.tvThings = (TextView) convertView.findViewById(R.id.item_fragment_police_child_tv_things);
                holder.cBoxChildCheck = (CheckBox) convertView.findViewById(R.id.item_fragment_police_child_cbox_choose);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvThings.setText(titleList.get(groupPosition).getChildren().get(childPosition).getName());
            if (groupClick == groupPosition&&childClick == childPosition){
                holder.cBoxChildCheck.setChecked(true);
            }else{
                holder.cBoxChildCheck.setChecked(false);
            }
            return convertView;
        }
        class ViewHolder{
            CheckBox cBoxGroupChoose, cBoxGroupSend,cBoxChildCheck;
            TextView tvThings;
        }
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    public void callPolice(String token,String cat_id,String address,String lat,String lng){
        post = new HttpUtils();
        params = new RequestParams();
        params.addBodyParameter("token",token);
        params.addBodyParameter("cat_id",cat_id);
        params.addBodyParameter("address",address);
        params.addBodyParameter("lat",lat);
        params.addBodyParameter("lng",lng);
        String url = UrlUtils.POST_URL+UrlUtils.path_warning;

        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {

            @Override
            public void onStart() {
                super.onStart();
                if (dialog!=null){
                    dialog.show();
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (dialog!=null){
                    dialog.dismiss();
                }
                JSONObject obj = JSONObject.parseObject(responseInfo.result);
                int code = obj.getIntValue("code");
                if (code == 1){
                    Toast.makeText(getActivity(),"发送成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(),obj.getString("msg"),Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (dialog!=null){
                    dialog.dismiss();
                }
                Toast.makeText(getActivity(),"发送失败，请检查网络连接",Toast.LENGTH_SHORT).show();
            }
        });



    }

}
