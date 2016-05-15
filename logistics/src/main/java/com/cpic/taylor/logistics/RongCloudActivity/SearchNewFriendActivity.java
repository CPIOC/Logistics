package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudModel.ApiResult;
import com.cpic.taylor.logistics.RongCloudModel.Friends;
import com.cpic.taylor.logistics.RongCloudModel.MyNewFriends;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.RongCloudaAdapter.SearchFriendAdapter;
import com.cpic.taylor.logistics.RongCloudaAdapter.SearchMyFriendAdapter;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 2015/3/26.
 */
public class SearchNewFriendActivity extends BaseApiActivity {

    private SearchView mEtSearch;
    private ListView mListSearch;
    private AbstractHttpRequest<Friends> searchHttpRequest;
    private List<ApiResult> mResultList;
    private SearchFriendAdapter adapter;
    private SearchMyFriendAdapter myAdapter;
    private LoadingDialog mDialog;
    private LinearLayout routeMembersLl, nearByMembersLl, searchFriendLl;
    private String userName=null;
    private TextView searchNewFriendTitle;
    private Handler mHandler;
    private HttpUtils post;
    private RequestParams params;
    private SharedPreferences sp;
    MyNewFriends myFriends;
    ArrayList<UserInfos> friendsList = new ArrayList<UserInfos>();

    /**
     * 代表附近的人或者路线上的人
     */
    private String stringType=null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_search);
        getSupportActionBar().setTitle(R.string.public_account_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        getSupportActionBar().hide();
        searchNewFriendTitle = (TextView) findViewById(R.id.search_activity_title);
        searchNewFriendTitle.setText("新的朋友");
        routeMembersLl = (LinearLayout) findViewById(R.id.layout_add);
        nearByMembersLl = (LinearLayout) findViewById(R.id.layout_chat_group);
        searchFriendLl = (LinearLayout) findViewById(R.id.search_friend_ll);
        routeMembersLl.setVisibility(View.GONE);
        nearByMembersLl.setVisibility(View.GONE);
        searchFriendLl.setVisibility(View.GONE);
        mEtSearch = (SearchView) findViewById(R.id.de_ui_search);
        mEtSearch.setVisibility(View.GONE);
        Button mBtSearch = (Button) findViewById(R.id.de_search);
        mBtSearch.setVisibility(View.GONE);
        mListSearch = (ListView) findViewById(R.id.de_search_list);
        mResultList = new ArrayList<ApiResult>();
        userName = getIntent().getStringExtra("userName");
        stringType=getIntent().getStringExtra("type");
        searchHttpRequest = RongYunContext.getInstance().getDemoApi().searchUserByUserName(userName, SearchNewFriendActivity.this);
        mDialog = new LoadingDialog(this);

        mEtSearch.setIconifiedByDefault(false);
        mEtSearch.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String userName = s;
                if (mDialog != null && !mDialog.isShowing())
                    mDialog.show();

                if (RongYunContext.getInstance() != null) {
                    searchHttpRequest = RongYunContext.getInstance().getDemoApi().searchUserByUserName(userName, SearchNewFriendActivity.this);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mListSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = new Intent(SearchNewFriendActivity.this, PersonalDetailActivity.class);
                if (null == friendsList.get(position).getPortrait()) {
                    friendsList.get(position).setPortrait("www.cpioc.com");
                }
                UserInfo userInfo = new UserInfo(friendsList.get(position).getUserid(), friendsList.get(position).getUsername(), Uri.parse(friendsList.get(position).getPortrait()));
                in.putExtra("USER", userInfo);
                in.putExtra("USER_SEARCH", true);
                startActivityForResult(in, Constants.SEARCH_REQUESTCODE);
            }
        });
        if(null!=userName){
            loadFriends();
        }
        if(null!=stringType){
            if(stringType.equals("near_by")){

                loadFriendNearBy("","");

            }else if(stringType.equals("same_route")){
                sp = PreferenceManager.getDefaultSharedPreferences(SearchNewFriendActivity.this);
                loadFriendNearBy(sp.getString("start", null),sp.getString("end", null));
            }
        }

    }

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        if (searchHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
            if (mResultList.size() > 0)
                mResultList.clear();
            if (obj instanceof Friends) {
                final Friends friends = (Friends) obj;

                if (friends.getCode() == 200) {
                    if (friends.getResult().size() > 0) {
                        for (int i = 0; i < friends.getResult().size(); i++) {
                            mResultList.add(friends.getResult().get(i));
                            Log.i("", "------onCallApiSuccess-user.getCode() == 200)-----" + friends.getResult().get(0).getId().toString());
                        }
                        adapter = new SearchFriendAdapter(mResultList, SearchNewFriendActivity.this);
                        mListSearch.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    /**
     * 传入经纬度
     */
    private void loadFriendNearBy(String lat,String  lng) {
        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(SearchNewFriendActivity.this);
        params.addBodyParameter("token", sp.getString("token", null));
        params.addBodyParameter("lat", "");
        params.addBodyParameter("lng", "");
        String url = UrlUtils.POST_URL + UrlUtils.path_nearList;
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
                    java.lang.reflect.Type type = new TypeToken<MyNewFriends>() {
                    }.getType();
                    myFriends = gson.fromJson(result, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (myFriends.getCode() == 1) {

                    if (null != myFriends.getData()) {
                        for (int i = 0; i < myFriends.getData().size(); i++) {
                            UserInfos userInfos = new UserInfos();
                            userInfos.setUserid(myFriends.getData().get(i).getCloud_id());
                            userInfos.setUsername(myFriends.getData().get(i).getName());
                            userInfos.setUser_id_login(myFriends.getData().get(i).getId());
                            userInfos.setStatus("1");
                            if (myFriends.getData().get(i).getImg() != null)
                                userInfos.setPortrait(myFriends.getData().get(i).getImg());
                            friendsList.add(userInfos);
                        }
                        if (null != friendsList) {
                            myAdapter = new SearchMyFriendAdapter(friendsList, SearchNewFriendActivity.this);
                            mListSearch.setAdapter(myAdapter);
                        }
                        Log.e("Tag", "number" + friendsList);
                    }


                } else {
                    showShortToast(myFriends.getMsg());
                }

            }

        });
    }

    private void loadFriends() {
        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(SearchNewFriendActivity.this);
        params.addBodyParameter("token", sp.getString("token", null));
        if (null == userName) {

        } else {
            params.addBodyParameter("name", userName);
        }

        String url = UrlUtils.POST_URL + UrlUtils.path_search_friends;
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
                    java.lang.reflect.Type type = new TypeToken<MyNewFriends>() {
                    }.getType();
                    myFriends = gson.fromJson(result, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (myFriends.getCode() == 1) {

                    if (null != myFriends.getData()) {
                        for (int i = 0; i < myFriends.getData().size(); i++) {
                            UserInfos userInfos = new UserInfos();
                            userInfos.setUserid(myFriends.getData().get(i).getCloud_id());
                            userInfos.setUsername(myFriends.getData().get(i).getName());
                            userInfos.setUser_id_login(myFriends.getData().get(i).getId());
                            userInfos.setStatus("1");
                            if (myFriends.getData().get(i).getImg() != null)
                                userInfos.setPortrait(myFriends.getData().get(i).getImg());
                            friendsList.add(userInfos);
                        }
                        if (null != friendsList) {
                            myAdapter = new SearchMyFriendAdapter(friendsList, SearchNewFriendActivity.this);
                            mListSearch.setAdapter(myAdapter);
                        }
                        Log.e("Tag", "number" + friendsList);
                    }


                } else {
                    showShortToast(myFriends.getMsg());
                }

            }

        });
    }

    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
        if (mDialog != null)
            mDialog.dismiss();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.PERSONAL_REQUESTCODE) {
            Intent intent = new Intent();
            this.setResult(Constants.SEARCH_REQUESTCODE, intent);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    public void backTo(View view) {

        finish();
    }

    /**
     * Toast短显示
     *
     * @param msg
     */
    protected void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
