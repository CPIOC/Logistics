package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.ApiResult;
import com.cpic.taylor.logistics.RongCloudModel.Friends;
import com.cpic.taylor.logistics.RongCloudUtils.Constants;
import com.cpic.taylor.logistics.RongCloudWidget.LoadingDialog;
import com.cpic.taylor.logistics.RongCloudaAdapter.SearchFriendAdapter;
import com.cpic.taylor.logistics.utils.CloseActivityClass;
import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;

import java.util.ArrayList;
import java.util.List;

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
    private LinearLayout layout_chat_group;
    private LinearLayout layout_add;
    private EditText mEtSearchEt;
    private ImageView mBtnClearSearchText;
    private LinearLayout mLayoutClearSearchText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_search);
        CloseActivityClass.activityList.add(this);
        getSupportActionBar().setTitle(R.string.public_account_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        getSupportActionBar().hide();
        mEtSearch = (SearchView) findViewById(R.id.de_ui_search);
        mEtSearchEt = (EditText) findViewById(R.id.et_search);
        mBtnClearSearchText = (ImageView) findViewById(R.id.btn_clear_search_text);
        mLayoutClearSearchText = (LinearLayout) findViewById(R.id.layout_clear_search_text);
        Button mBtSearch = (Button) findViewById(R.id.de_search);
        mListSearch = (ListView) findViewById(R.id.de_search_list);
        mListSearch.setVisibility(View.GONE);
        mResultList = new ArrayList<ApiResult>();
        mDialog = new LoadingDialog(this);
        layout_add= (LinearLayout) findViewById(R.id.layout_add);
        layout_chat_group= (LinearLayout) findViewById(R.id.layout_chat_group);
        /**
         * 搜索附近的人
         */
        layout_chat_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(SearchFriendActivity.this,SearchNewFriendActivity.class);
                intent.putExtra("type","near_by");
                startActivity(intent);

            }
        });

        /**
         * 搜索路线上的人
         */
        layout_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(SearchFriendActivity.this,SearchNewFriendActivity.class);
                intent.putExtra("type","same_route");
                startActivity(intent);
            }
        });
        mEtSearchEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int textLength = mEtSearchEt.getText().length();
                if (textLength > 0) {
                    mLayoutClearSearchText.setVisibility(View.VISIBLE);
                } else {
                    mLayoutClearSearchText.setVisibility(View.GONE);
                }
            }
        });

        mBtnClearSearchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtSearchEt.setText("");
                mLayoutClearSearchText.setVisibility(View.GONE);
            }
        });
        mEtSearchEt.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    String userName = mEtSearchEt.getText().toString().trim();
                    if (mDialog != null && !mDialog.isShowing())
                        mDialog.show();

                    Intent intent=new Intent(SearchFriendActivity.this,SearchNewFriendActivity.class);
                    intent.putExtra("userName",userName);
                    startActivity(intent);
                    return true;
                }
                if(keyCode == KeyEvent.KEYCODE_BACK){
                    finish();
                    return true;
                }
                return false;
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

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog != null)
            mDialog.dismiss();
    }
}
