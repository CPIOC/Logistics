package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.ApiResult;
import com.cpic.taylor.logistics.RongCloudModel.Friends;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.RongCloudaAdapter.SearchFriendAdapter;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 2015/3/26.
 */
public class SearchFriendActivity extends BaseApiActivity {

    private SearchView mEtSearch;
    private ListView mListSearch;
    private AbstractHttpRequest<Friends> searchHttpRequest;
    private List<ApiResult> mResultList;
    private SearchFriendAdapter adapter;
    private LoadingDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_search);
        getSupportActionBar().setTitle(R.string.public_account_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        getSupportActionBar().hide();
        mEtSearch = (SearchView) findViewById(R.id.de_ui_search);
        Button mBtSearch = (Button) findViewById(R.id.de_search);
        mListSearch = (ListView) findViewById(R.id.de_search_list);
        mListSearch.setVisibility(View.GONE);
        mResultList = new ArrayList<ApiResult>();
        mDialog = new LoadingDialog(this);

        /*mBtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = mEtSearch.getText().toString();
                if (mDialog != null && !mDialog.isShowing())
                    mDialog.show();

                if (RongYunContext.getInstance() != null) {
                    searchHttpRequest = RongYunContext.getInstance().getDemoApi().searchUserByUserName(userName, SearchFriendActivity.this);
                }
            }
        });*/
        /*mEtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i== EditorInfo.IME_ACTION_DONE){
                    Toast.makeText(SearchFriendActivity.this,"0",Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });*/
        /**
         * 清除SearchView中的下划线
         */
        if (mEtSearch != null) {

            try {        //--拿到字节码

                Class<?> argClass = mEtSearch.getClass();

                //--指定某个私有属性,mSearchPlate是搜索框父布局的名字

                Field ownField = argClass.getDeclaredField("mSearchPlate");

                Field [] array=argClass.getDeclaredFields();
                for (int i = 0; i < array.length; i++) {

                    Log.e("Tag",""+array[i]);
                }


                //--暴力反射,只有暴力反射才能拿到私有属性

                ownField.setAccessible(true);

                View mView = (View) ownField.get(mEtSearch);

                //--设置背景

                mView.setBackgroundColor(Color.TRANSPARENT);

            } catch (Exception e) {

                e.printStackTrace();

            }
        }
        mEtSearch.setIconifiedByDefault(false);
        mEtSearch.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String userName = s;
                if (mDialog != null && !mDialog.isShowing())
                    mDialog.show();

                if (RongYunContext.getInstance() != null) {
                    searchHttpRequest = RongYunContext.getInstance().getDemoApi().searchUserByUserName(userName, SearchFriendActivity.this);
                }
                Intent intent=new Intent(SearchFriendActivity.this,SearchNewFriendActivity.class);
                intent.putExtra("userName",s);
                startActivity(intent);
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
                Intent in = new Intent(SearchFriendActivity.this, PersonalDetailActivity.class);
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
                        adapter = new SearchFriendAdapter(mResultList, SearchFriendActivity.this);
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
