package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudModel.ApiResult;
import com.cpic.taylor.logistics.RongCloudModel.Friends;
import com.cpic.taylor.logistics.RongCloudModel.MyNewFriends;
import com.cpic.taylor.logistics.RongCloudModel.SameRoutineFriends;
import com.cpic.taylor.logistics.RongCloudModel.SameRoutineFriendsData;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudaAdapter.SearchFriendAdapter;
import com.cpic.taylor.logistics.RongCloudaAdapter.SearchFriendNearByAdapter;
import com.cpic.taylor.logistics.RongCloudaAdapter.SearchMyFriendAdapter;
import com.cpic.taylor.logistics.activity.LoginActivity;
import com.cpic.taylor.logistics.bean.setRoute;
import com.cpic.taylor.logistics.utils.CloseActivityClass;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.sea_monster.network.AbstractHttpRequest;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 2015/3/26.
 */
public class SearchNewFriendActivity extends BaseActivity {

    private SearchView mEtSearch;
    private ListView mListSearch;
    private AbstractHttpRequest<Friends> searchHttpRequest;
    private List<ApiResult> mResultList;
    private SearchFriendAdapter adapter;
    private SearchMyFriendAdapter myAdapter;
    private SearchFriendNearByAdapter myAdapterNear;
    private LinearLayout routeMembersLl, nearByMembersLl, searchFriendLl;
    private String userName = null;
    private TextView searchNewFriendTitle;
    private Handler mHandler;
    private HttpUtils post;
    private RequestParams params;
    private SharedPreferences sp;
    MyNewFriends myFriends;
    private View under_line;
    private setRoute msetRoute;
    ArrayList<UserInfos> friendsList = new ArrayList<UserInfos>();
    private SameRoutineFriends sameRoutineFriends;
    private ArrayList<SameRoutineFriendsData> sameRoutineFriendsData;

    /**
     * 代表附近的人或者路线上的人
     */
    private String stringType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CloseActivityClass.activityList.add(this);
        setContentView(R.layout.de_ac_search);


        getSupportActionBar().hide();
        under_line = findViewById(R.id.under_line);
        searchNewFriendTitle = (TextView) findViewById(R.id.search_activity_title);
        searchNewFriendTitle.setText("新的朋友");
        routeMembersLl = (LinearLayout) findViewById(R.id.layout_add);
        nearByMembersLl = (LinearLayout) findViewById(R.id.layout_chat_group);
        searchFriendLl = (LinearLayout) findViewById(R.id.search_friend_ll);
        routeMembersLl.setVisibility(View.GONE);
        nearByMembersLl.setVisibility(View.GONE);
        searchFriendLl.setVisibility(View.GONE);
        under_line.setVisibility(View.GONE);
        mEtSearch = (SearchView) findViewById(R.id.de_ui_search);
        mEtSearch.setVisibility(View.GONE);
        Button mBtSearch = (Button) findViewById(R.id.de_search);
        mBtSearch.setVisibility(View.GONE);
        mListSearch = (ListView) findViewById(R.id.de_search_list);
        mResultList = new ArrayList<ApiResult>();
        userName = getIntent().getStringExtra("userName");
        stringType = getIntent().getStringExtra("type");
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
        if (null != userName) {
            loadFriends();
        }
        if (null != stringType) {
            if (stringType.equals("near_by")) {
                sp = PreferenceManager.getDefaultSharedPreferences(SearchNewFriendActivity.this);
//                Log.i("oye",sp.getString("now_latitude", "")+"------"+sp.getString("now_longitude", ""));
                loadFriendNearBy(sp.getString("now_latitude", ""), sp.getString("now_longitude", ""));

            } else if (stringType.equals("same_route")) {
                sp = PreferenceManager.getDefaultSharedPreferences(SearchNewFriendActivity.this);
                loadFriendSameRoute(sp.getString("start", ""), sp.getString("end", ""));
            }
        }


    }


    /**
     * 传入起止地点
     */
    private void loadFriendSameRoute(String start, String end) {
        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(SearchNewFriendActivity.this);
        params.addBodyParameter("token", sp.getString("token", null));
        params.addBodyParameter("start", start);
        params.addBodyParameter("end", end);
        String url = UrlUtils.POST_URL + UrlUtils.path_setRoute;
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
                    java.lang.reflect.Type type = new TypeToken<setRoute>() {
                    }.getType();
                    msetRoute = gson.fromJson(result, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (msetRoute.getCode() == 1) {

                    if (null != msetRoute.getData()) {
                        for (int i = 0; i < msetRoute.getData().size(); i++) {
                            UserInfos userInfos = new UserInfos();
                            userInfos.setUserid(msetRoute.getData().get(i).getCloud_id());
                            userInfos.setUsername(msetRoute.getData().get(i).getUser_name());
                            userInfos.setUser_id_login(msetRoute.getData().get(i).getCloud_id());
                            userInfos.setStatus("1");
                            if (msetRoute.getData().get(i).getImg() != null)
                                userInfos.setPortrait(msetRoute.getData().get(i).getImg());
                            if (userInfos.getUserid().equals(sp.getString("cloud_id", ""))) {

                            } else {
                                friendsList.add(userInfos);
                            }

                        }
                        if (null != friendsList) {
                            myAdapter = new SearchMyFriendAdapter(friendsList, SearchNewFriendActivity.this);
                            mListSearch.setAdapter(myAdapter);
                        }
                    }


                } else if (msetRoute.getCode() == 2) {
                    Toast.makeText(SearchNewFriendActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SearchNewFriendActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 10);
                } else {
                    showShortToast(msetRoute.getMsg());
                }

            }

        });
    }

    /**
     * 传入经纬度
     */
    private void loadFriendNearBy(String lat, String lng) {
        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(SearchNewFriendActivity.this);
        params.addBodyParameter("token", sp.getString("token", null));
        params.addBodyParameter("lat", lat);
        params.addBodyParameter("lng", lng);
//        Log.i("oye",lat+"------"+lng);
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
                    java.lang.reflect.Type type = new TypeToken<SameRoutineFriends>() {
                    }.getType();
                    sameRoutineFriends = gson.fromJson(result, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (sameRoutineFriends.getCode() == 1) {

                    if (null != sameRoutineFriends.getData()) {
                        for (int i = 0; i < sameRoutineFriends.getData().size(); i++) {
                            UserInfos userInfos = new UserInfos();
                            userInfos.setUserid(sameRoutineFriends.getData().get(i).getCloud_id());
                            userInfos.setUsername(sameRoutineFriends.getData().get(i).getName());
                            userInfos.setUser_id_login(sameRoutineFriends.getData().get(i).getUser_id());
                            userInfos.setStatus("1");
                            if (sameRoutineFriends.getData().get(i).getImg() != null)
                                userInfos.setPortrait(sameRoutineFriends.getData().get(i).getImg());
                            if (userInfos.getUserid().equals(sp.getString("cloud_id", ""))) {

                            } else {
                                friendsList.add(userInfos);
                            }
                        }
                        sameRoutineFriendsData = sameRoutineFriends.getData();
                        if (null != friendsList) {
                            myAdapterNear = new SearchFriendNearByAdapter(friendsList, SearchNewFriendActivity.this, sameRoutineFriendsData);
                            mListSearch.setAdapter(myAdapterNear);
                        }
                    }
                } else if (sameRoutineFriends.getCode() == 2) {
                    Toast.makeText(SearchNewFriendActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SearchNewFriendActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 10);
                } else {
                    showShortToast(sameRoutineFriends.getMsg());
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
                            if (userInfos.getUserid().equals(sp.getString("cloud_id", ""))) {

                            } else {
                                friendsList.add(userInfos);
                            }
                        }
                        if (null != friendsList) {
                            myAdapter = new SearchMyFriendAdapter(friendsList, SearchNewFriendActivity.this);
                            mListSearch.setAdapter(myAdapter);
                        }
                        userName = null;

                    }


                } else if (myFriends.getCode() == 2) {
                    Toast.makeText(SearchNewFriendActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(SearchNewFriendActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 10);
                } else {
                    showShortToast(myFriends.getMsg());
                }

            }

        });
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


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


}
