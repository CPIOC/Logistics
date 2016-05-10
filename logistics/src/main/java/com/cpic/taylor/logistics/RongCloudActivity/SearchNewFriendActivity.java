package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.ApiResult;
import com.cpic.taylor.logistics.RongCloudModel.Friends;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.RongCloudaAdapter.SearchFriendAdapter;
import com.cpic.taylor.logistics.base.RongYunContext;
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
    private LoadingDialog mDialog;
    private LinearLayout routeMembersLl,nearByMembersLl,searchFriendLl;
    private String userName;
    private TextView searchNewFriendTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_search);
        getSupportActionBar().setTitle(R.string.public_account_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        getSupportActionBar().hide();
        searchNewFriendTitle= (TextView) findViewById(R.id.search_activity_title);
        searchNewFriendTitle.setText("新的朋友");
        routeMembersLl= (LinearLayout) findViewById(R.id.layout_add);
        nearByMembersLl= (LinearLayout) findViewById(R.id.layout_chat_group);
        searchFriendLl= (LinearLayout) findViewById(R.id.search_friend_ll);
        routeMembersLl.setVisibility(View.GONE);
        nearByMembersLl.setVisibility(View.GONE);
        searchFriendLl.setVisibility(View.GONE);
        mEtSearch = (SearchView) findViewById(R.id.de_ui_search);
        mEtSearch.setVisibility(View.GONE);
        Button mBtSearch = (Button) findViewById(R.id.de_search);
        mBtSearch.setVisibility(View.GONE);
        mListSearch = (ListView) findViewById(R.id.de_search_list);
        mResultList = new ArrayList<ApiResult>();
        userName=getIntent().getStringExtra("userName");
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
                UserInfo userInfo = new UserInfo(mResultList.get(position).getId(), mResultList.get(position).getUsername(), Uri.parse(mResultList.get(position).getPortrait()));
                in.putExtra("USER", userInfo);
                in.putExtra("USER_SEARCH", true);
                startActivityForResult(in, Constants.SEARCH_REQUESTCODE);
            }
        });
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
}
