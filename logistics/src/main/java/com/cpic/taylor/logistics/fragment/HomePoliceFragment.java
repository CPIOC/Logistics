package com.cpic.taylor.logistics.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cpic.taylor.logistics.R;

import java.util.ArrayList;

/**
 * Created by Taylor on 2016/4/29.
 */
public class HomePoliceFragment extends Fragment{
    private SharedPreferences sp;
    private TextView tvAddress;
    private ExpandableListView elv;
    private CarAdapter adapter;
    private ArrayList<String> titleList = null;
    private ArrayList<ArrayList<String>> contentList = null;

    private CheckBox cChoose,cSend,cChild;
    private TextView tvThings;

    private int groupClick = -1;
    private int childClick = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_police,null);
        initView(view);

        /**
         * 获取当前地址信息
         */
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String address = sp.getString("now_address","");
        tvAddress.setText(address);

        initDatas();
        registerListener();
        return view;
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
    }

    /**
     * 列表选择
     */
    private void initDatas() {
        titleList = new ArrayList<>();
        contentList = new ArrayList<>();
        titleList.add("交通事故出现");
        titleList.add("前方有交警定岗");
        titleList.add("前方有打劫");

        ArrayList<String> arr1 = new ArrayList<>();
        ArrayList<String> arr2 = new ArrayList<>();
        ArrayList<String> arr3 = new ArrayList<>();
        arr1.add("一般事故");
        arr1.add("重大事故");
        arr1.add("特大事故");
        arr2.add("3名交警");
        arr2.add("5名交警");
        arr2.add("8名交警");
        arr3.add("3名劫匪");
        arr3.add("5名劫匪");
        arr3.add("8名劫匪");
        contentList.add(arr1);
        contentList.add(arr2);
        contentList.add(arr3);

        adapter = new CarAdapter();
        adapter.setDatas(titleList,contentList);
        elv.setAdapter(adapter);

    }

    public class CarAdapter extends BaseExpandableListAdapter {

        private ArrayList<String> titleList = null;
        private ArrayList<ArrayList<String>> contentList = null;

        public void setDatas(ArrayList<String> Title , ArrayList<ArrayList<String>> Content){
            this.titleList = Title;
            this.contentList = Content;
        }

        @Override
        public int getGroupCount() {
            return titleList == null ? 0 :titleList.size();
        }
        @Override
        public int getChildrenCount(int groupPosition) {
            return contentList.get(groupPosition).size();
        }
        @Override
        public Object getGroup(int groupPosition) {
            return titleList.get(groupPosition);
        }
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return contentList.get(groupPosition).get(childPosition);
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
            holder.cBoxGroupChoose.setText(titleList.get(groupPosition));


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
                        }else{
                            Toast.makeText(getActivity(),contentList.get(groupClick).get(childClick),Toast.LENGTH_SHORT).show();
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
            holder.tvThings.setText(contentList.get(groupPosition).get(childPosition));
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
}
