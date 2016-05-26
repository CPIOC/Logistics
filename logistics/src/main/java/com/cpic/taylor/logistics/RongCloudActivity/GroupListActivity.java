package com.cpic.taylor.logistics.RongCloudActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudModel.Friend;
import com.cpic.taylor.logistics.RongCloudModel.MyGroup;
import com.cpic.taylor.logistics.RongCloudModel.MyGroupData;
import com.cpic.taylor.logistics.activity.LoginActivity;
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

import java.util.ArrayList;

import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;

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
    private PopupWindow popuWindowDel;
    private GroupListAdapter groupListAdapter;
    private TextView cancelTv, delTv;
    View contentView = null;

    @Override
    protected void getIntentData(Bundle savedInstanceState) {

    }

    @Override
    protected void loadXml() {

    }

    @Override
    protected void initView() {

        setContentView(R.layout.group_list);
        CloseActivityClass.activityList.add(this);
        group_list_view = (ListView) findViewById(R.id.group_list_view);
        group_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                RongIM.getInstance().startDiscussionChat(GroupListActivity.this, myGroupDatas.get(i).getTarget_id(), myGroupDatas.get(i).getChat_name());
                finish();
            }
        });
        group_list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                onlineTel(view, i);
                return true;
            }
        });

    }

    /**
     * 删除群组提示弹框
     *
     * @param
     */
    public void onlineTel(View view, final int position) {

        if (popuWindowDel == null) {
            LayoutInflater mLayoutInflater = LayoutInflater.from(this);
            contentView = mLayoutInflater.inflate(R.layout.del_item_group_name, null);
            popuWindowDel = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ColorDrawable cd = new ColorDrawable(0x000000);
        popuWindowDel.setBackgroundDrawable(cd);
        // 产生背景变暗效果
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.6f;
        getWindow().setAttributes(lp);
        popuWindowDel.setOutsideTouchable(true);
        popuWindowDel.setFocusable(true);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        popuWindowDel.setWidth(display.getWidth() * 80 / 100);
        popuWindowDel.showAtLocation((View) view.getParent(), Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
        popuWindowDel.update();
        cancelTv = (TextView) contentView.findViewById(R.id.cancel_tv);
        delTv = (TextView) contentView.findViewById(R.id.yes_tv);
        cancelTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popuWindowDel.dismiss();
            }
        });
        delTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteData(myGroupDatas.get(position).getId(), position);
            }
        });
        popuWindowDel.setOnDismissListener(new PopupWindow.OnDismissListener() {

            // 在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);

            }
        });

    }

    @Override
    protected void initData() {

        loadData();

    }

    @Override
    protected void registerListener() {

    }

    public void backTo(View view) {
        finish();
    }

    public class GroupListAdapter extends BaseAdapter {

        ArrayList<MyGroupData> myGroupDataList;

        public GroupListAdapter(ArrayList<MyGroupData> myGroupDataList) {

            this.myGroupDataList = myGroupDataList;

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
            final ViewHolder vh;
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

            //vh.name.setText(myGroupDataList.get(position).chat_name);

            RongIM.getInstance().getRongIMClient().getDiscussion(myGroupDataList.get(position).getTarget_id(), new RongIMClient.ResultCallback<Discussion>() {
                @Override
                public void onSuccess(Discussion discussion) {

                    // mDiscussionName = discussion.getName();
                    Log.e("Tag", "mDiscussionName" + discussion.getName());
                    vh.name.setText(discussion.getName());
                    //addChatGroup(discussion.getName(),conversation.getTargetId());

                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });
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
                        myGroupDatas = (ArrayList<MyGroupData>) myGroup.getData();
                        groupListAdapter = new GroupListAdapter(myGroupDatas);
                        group_list_view.setAdapter(groupListAdapter);
                    }

                } else if (myGroup.getCode() == 2) {
                    Toast.makeText(GroupListActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(GroupListActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 10);
                } else {
                    showShortToast(myGroup.getMsg());
                }

            }

        });
    }

    public void deleteData(String chat_id, final int position) {
        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(GroupListActivity.this);
        params.addBodyParameter("token", sp.getString("token", null));
        params.addBodyParameter("chat_id", chat_id);
        String url = UrlUtils.POST_URL + UrlUtils.path_delete_meb;
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
                org.json.JSONObject jsonObj = null;
                try {
                    jsonObj = new org.json.JSONObject(result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if ("1".equals(String.valueOf(jsonObj.getInt("code")))) {
                        if (null != myGroupDatas) {
                            deleteDiscussionRecord(myGroupDatas.get(position).getTarget_id());
                            myGroupDatas.remove(position);
                        }

                        if (null != groupListAdapter)
                            groupListAdapter.notifyDataSetChanged();
                        if (null != popuWindowDel)
                            popuWindowDel.dismiss();

                        showShortToast("删除成功");

                    } else if ("2".equals(String.valueOf(jsonObj.getInt("code")))) {
                        Toast.makeText(GroupListActivity.this, "身份验证失败，请重新登陆", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(GroupListActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }, 10);
                    } else {
                        showShortToast(jsonObj.getString("msg"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });
    }

    private void deleteDiscussionRecord(final String targetId) {
        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null)
            RongIM.getInstance().getRongIMClient().quitDiscussion(targetId, new RongIMClient.OperationCallback() {
                @Override
                public void onSuccess() {
                    RongIM.getInstance().getRongIMClient().removeConversation(Conversation.ConversationType.DISCUSSION, targetId, new RongIMClient.ResultCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {


                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });
    }

}
