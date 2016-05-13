package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.Friend;
import com.cpic.taylor.logistics.RongCloudModel.MyGroup;
import com.cpic.taylor.logistics.RongCloudModel.MyGroupData;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.ArrayList;

import io.rong.imkit.widget.AsyncImageView;

/**
 * Created by xuan on 2016/5/13.
 */
public class GroupListActivity extends com.cpic.taylor.logistics.base.BaseActivity {

    private HttpUtils post;
    private RequestParams params;
    private SharedPreferences sp;
    private MyGroup myGroup;
    private ListView group_list_view;
    private ArrayList<MyGroupData> myGroupDatas;

    @Override
    protected void getIntentData(Bundle savedInstanceState) {

    }

    @Override
    protected void loadXml() {

    }

    @Override
    protected void initView() {

        setContentView(R.layout.group_list);
        group_list_view = (ListView) findViewById(R.id.group_list_view);

    }

    @Override
    protected void initData() {

        loadData();

    }

    @Override
    protected void registerListener() {

    }

    public  void backTo(){
        finish();
    }

    public class GroupListAdapter extends BaseAdapter {

        ArrayList<MyGroupData> myGroupDataList;
       public  GroupListAdapter(ArrayList<MyGroupData> myGroupDataList){

           this.myGroupDataList=myGroupDataList;

       }
        @Override
        public int getCount() {

            return myGroupDataList.size();

        }

        @Override
        public Object getItem(int arg0) {
            return myGroupDataList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(final int position, View convertview, ViewGroup arg2) {
            ViewHolder vh;
            if (convertview == null) {
                vh = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(GroupListActivity.this);
                convertview = inflater.inflate(R.layout.de_item_friendlist, null);
                vh.choice = (CheckBox) convertview.findViewById(R.id.de_ui_friend_checkbox);
                vh.name = (TextView) convertview.findViewById(R.id.de_ui_friend_name);
                vh.photo = (AsyncImageView) convertview.findViewById(R.id.de_ui_friend_icon);
                convertview.setTag(vh);
            } else {
                vh = (ViewHolder) convertview.getTag();
            }

            vh.name.setText(myGroupDataList.get(position).chat_name);
            return convertview;
        }

        class ViewHolder {

            public TextView name;
            public AsyncImageView photo;
            public String userId;
            public Friend friend;
            public CheckBox choice;

        }


    }

    public void loadData() {
        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(GroupListActivity.this);
        params.addBodyParameter("token", sp.getString("token", null));
        String url = UrlUtils.POST_URL + UrlUtils.path_chat_grouplist;
        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                showShortToast("连接失败，请检查网络连接");
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result;
                JSONObject jsonObj = null;
                try {

                    Gson gson = new Gson();
                    java.lang.reflect.Type type = new TypeToken<MyGroup>() {
                    }.getType();
                    myGroup = gson.fromJson(result, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (myGroup.getCode() == 1) {


                    if (null != myGroup.getData()) {
                        myGroupDatas= (ArrayList<MyGroupData>) myGroup.getData();
                        group_list_view.setAdapter(new GroupListAdapter(myGroupDatas));
                    }

                } else {
                    showShortToast(myGroup.getMsg());
                }

            }

        });
    }


}
