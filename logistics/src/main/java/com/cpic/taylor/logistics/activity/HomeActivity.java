package com.cpic.taylor.logistics.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudActivity.MainActivity;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudModel.MyFriends;
import com.cpic.taylor.logistics.base.BaseActivity;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.fragment.HomeLineFragment;
import com.cpic.taylor.logistics.fragment.HomePoliceFragment;
import com.cpic.taylor.logistics.fragment.HomeRoadFragment;
import com.cpic.taylor.logistics.utils.ExampleUtil;
import com.cpic.taylor.logistics.utils.ProgressDialogHandle;
import com.cpic.taylor.logistics.utils.RoundImageView;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by Taylor on 2016/5/4.
 */
public class HomeActivity extends BaseActivity implements Handler.Callback {

    // 记录上次点击返回键的时间
    private long lastTime;

    private DrawerLayout layout;
    private ImageView ivMine;
    private TextView tvChat;
    private RadioGroup rgroup;
    private RadioButton lastButton, rbtn_road;
    // Fragment的管理类
    private FragmentManager mManager;
    // Fragment的事务类
    private FragmentTransaction mTrans;
    // 管理Fragment的List集合
    private List<Fragment> mFragList;
    public String curFragmentTag = "";

    /**
     * 侧滑部分
     */
    private LinearLayout linIcon;
    private RoundImageView ivIcon;
    private EditText etName, etCarNum, etCarType;
    private ImageView ivCarInfo;

    private static final int CAMERA = 0;
    private static final int PHOTO = 1;
    private static final int INFO_CAMERA = 2;
    private static final int INFO_PHOTO = 3;
    private Uri cameraUri;
    private File cameraPic;
    private PopupWindow pw;
    private int screenWidth;
    private String path;// 图片路径
    private String path1;// 图片路径
    private TextView tvCamera, tvPhoto, tvBack;
    private Intent intent;

    private HttpUtils post;
    private RequestParams params;
    private Dialog dialog;
    /**
     * 融云好友
     */
    MyFriends myFriends;
    ArrayList<UserInfos> friendsList = new ArrayList<UserInfos>();
    private Handler mHandler;
    private int HANDLER_LOGIN_SUCCESS = 1;
    private int HANDLER_LOGIN_FAILURE = 2;
    private int HANDLER_LOGIN_HAS_FOCUS = 3;
    private int HANDLER_LOGIN_HAS_NO_FOCUS = 4;
    private boolean isFirst = false;

    private static final int USER_ICON = 0;
    private static final int NAME = 1;
    private static final int CAR_NUM = 2;
    private static final int CAR_TYPE = 3;
    private static final int CAR_INFO = 4;

    private SharedPreferences sp;

    /**
     * jpush
     *
     * @param savedInstanceState
     */
    public static boolean isForeground = false;

    @Override
    protected void getIntentData(Bundle savedInstanceState) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
    }

    @Override
    protected void loadXml() {

        setContentView(R.layout.activity_home);
    }

    @Override
    protected void initView() {
        layout = (DrawerLayout) findViewById(R.id.activity_home_drawerlayout);
        ivMine = (ImageView) findViewById(R.id.activity_home_iv_mine);
        tvChat = (TextView) findViewById(R.id.activity_home_chat);
        rgroup = (RadioGroup) findViewById(R.id.activity_home_rgroup);
        lastButton = (RadioButton) findViewById(R.id.activity_home_rbtn_line);
        rbtn_road = (RadioButton) findViewById(R.id.activity_home_rbtn_line_road);

        /**
         * 侧滑控件
         */
        linIcon = (LinearLayout) findViewById(R.id.layout_icon);
        etName = (EditText) findViewById(R.id.layout_et_name);
        etCarNum = (EditText) findViewById(R.id.layout_et_car_num);
        etCarType = (EditText) findViewById(R.id.layout_et_car_type);
        ivCarInfo = (ImageView) findViewById(R.id.layout_iv_carinfo);
        ivIcon = (RoundImageView) findViewById(R.id.layout_iv_icon);
        dialog = ProgressDialogHandle.getProgressDialog(HomeActivity.this, null);
    }

    @Override
    protected void initData() {
        initFragment();

        sp = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
        etName.setText(sp.getString("name", ""));
        etCarNum.setText(sp.getString("plate_number", ""));
        etCarType.setText(sp.getString("car_models", ""));
        Glide.with(HomeActivity.this).load(sp.getString("img", "")).placeholder(R.mipmap.empty_photo).fitCenter().into(ivIcon);
        Glide.with(HomeActivity.this).load(sp.getString("driving_license", "")).placeholder(R.mipmap.empty_photo).fitCenter().into(ivCarInfo);

        registerMessageReceiver();
        init();

        mHandler = new Handler(HomeActivity.this);
        sp = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
        getFriendsFuction();

    }

    /**
     * 从服务器获取好友列表
     */

    private void getFriendsFuction() {

        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
        params.addBodyParameter("token", sp.getString("token", null));
        String url = UrlUtils.POST_URL + UrlUtils.path_friendslist;
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
                    java.lang.reflect.Type type = new TypeToken<MyFriends>() {
                    }.getType();
                    myFriends = gson.fromJson(result, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (myFriends.getCode() == 1) {

                    RongYunContext.getInstance().deleteUserInfos();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != myFriends.getdata()) {
                                for (int i = 0; i < myFriends.getdata().size(); i++) {
                                    UserInfos userInfos = new UserInfos();
                                    userInfos.setUserid(myFriends.getdata().get(i).getCloud_id());
                                    userInfos.setUsername(myFriends.getdata().get(i).getName());
                                    userInfos.setStatus("1");
                                    if (myFriends.getdata().get(i).getImg() != null)
                                        userInfos.setPortrait(myFriends.getdata().get(i).getImg());
                                    friendsList.add(userInfos);
                                }
                            }


                            if (friendsList != null) {
                                for (UserInfos friend : friendsList) {
                                    UserInfos f = new UserInfos();
                                    f.setUserid(friend.getUserid());
                                    f.setUsername(friend.getUsername());
                                    f.setPortrait(friend.getPortrait());
                                    f.setStatus(friend.getStatus());
                                    RongYunContext.getInstance().insertOrReplaceUserInfos(f);
                                }
                            }
                        }
                    });

                } else {
                    showShortToast(myFriends.getmsg());
                }

            }

        });

    }

    public void setHomeRoad() {
        rbtn_road.setChecked(true);
    }

    // 初始化 JPush。如果已经初始化，但没有登录成功，则执行重新登录。
    private void init() {
        JPushInterface.init(getApplicationContext());
    }

    private void initFragment() {
        // TODO Auto-generated method stub
        mFragList = new ArrayList<Fragment>();
        mFragList.add(new HomeLineFragment());
        mFragList.add(new HomeRoadFragment());
        mFragList.add(new HomePoliceFragment());

        mManager = getSupportFragmentManager();
        mTrans = mManager.beginTransaction();
        lastButton.setChecked(true);
        mTrans.add(R.id.activity_home_framelayout, mFragList.get(0), "0");
        mTrans.show(mFragList.get(0));
        mTrans.commit();
    }

    @Override
    protected void registerListener() {
        ivMine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.openDrawer(Gravity.LEFT);
            }
        });
        tvChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        rgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 当前选中的radioButton
                RadioButton selectRbtn = (RadioButton) findViewById(checkedId);

                int index = Integer.parseInt(selectRbtn.getTag().toString());
                int lastIndex = Integer.parseInt(lastButton.getTag().toString());

                Fragment mFragment = mManager.findFragmentByTag(index + "");

                mTrans = mManager.beginTransaction();

                if (mFragment == null) {
                    mTrans.add(R.id.activity_home_framelayout, mFragList.get(index), "" + index);
                }

                // 设置界面隐藏与显示，避免一次性加载所有界面
                mTrans.show(mFragList.get(index));
                mTrans.hide(mFragList.get(lastIndex));
                mTrans.commit();

                lastButton = selectRbtn;
            }
        });

        /**
         * 侧滑部分
         */
        etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                if (!b && !etName.getText().toString().equals(sp.getString("name", ""))) {
                    changeInfo(NAME);
                }
            }
        });
        etCarNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b && !etCarNum.getText().toString().equals(sp.getString("plate_number", ""))) {
                    changeInfo(CAR_NUM);
                }
            }
        });
        etCarType.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b && !etCarType.getText().toString().equals(sp.getString("car_models", ""))) {
                    changeInfo(CAR_TYPE);
                }
            }
        });
        linIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow(view, CAMERA, PHOTO, true);
            }
        });
        ivCarInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow(view, INFO_CAMERA, INFO_PHOTO, false);
            }
        });


    }

    /**
     * 修改个人信息
     *
     * @param status
     */
    public void changeInfo(int status) {
        sp = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
        String token = sp.getString("token", "");
        post = new HttpUtils();
        params = new RequestParams();

        String url = UrlUtils.POST_URL + UrlUtils.path_modifyInfo;
        if (status == USER_ICON) {
            params.addBodyParameter("img", new File(path));
        } else if (status == NAME) {
            params.addBodyParameter("name", etName.getText().toString());
        } else if (status == CAR_NUM) {
            params.addBodyParameter("plate_number", etCarNum.getText().toString());
        } else if (status == CAR_TYPE) {
            params.addBodyParameter("car_models", etCarType.getText().toString());
        } else if (status == CAR_INFO) {
            params.addBodyParameter("driving_license", new File(path1));
            Log.i("oye", "请求" + path1);
        }
        params.addBodyParameter("token", token);

        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                super.onStart();
                if (dialog != null) {
                    dialog.show();
                }

            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {

                if (dialog != null) {
                    dialog.dismiss();
                }
                JSONObject obj = JSONObject.parseObject(responseInfo.result);
                int code = obj.getIntValue("code");
                if (code == 1) {
                    showShortToast("修改成功");
                } else {
                    showShortToast(obj.getString("msg"));
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                showShortToast("修改失败,请检查网络连接");
            }
        });
    }

    @Override
    public void onBackPressed() {
//        // 获取本次点击的时间
//        long currentTime = System.currentTimeMillis();
//        long dTime = currentTime - lastTime;
//
//        if (dTime < 2000) {
//            finish();
//        } else {
//            showShortToast("再按一次退出程序");
//            lastTime = currentTime;
//        }

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
        intent.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(intent);


    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void showPopupWindow(View v, final int type1, final int type2, final boolean isUser) {
        View view = View.inflate(HomeActivity.this, R.layout.popupwindow_1, null);
        tvCamera = (TextView) view.findViewById(R.id.btn_camera);
        tvPhoto = (TextView) view.findViewById(R.id.btn_photo);
        tvBack = (TextView) view.findViewById(R.id.btn_back);
        tvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFromCamera(type1, isUser);
                pw.dismiss();
            }
        });

        tvPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFromPhoto(type2);
                pw.dismiss();
            }
        });
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
            }
        });

        pw = new PopupWindow(view, screenWidth * 99 / 100, LinearLayout.LayoutParams.WRAP_CONTENT);
        pw.setFocusable(true);
        WindowManager.LayoutParams params = HomeActivity.this.getWindow()
                .getAttributes();
        HomeActivity.this.getWindow().setAttributes(params);

        pw.setBackgroundDrawable(new ColorDrawable());
        pw.setOutsideTouchable(true);

        pw.setAnimationStyle(R.style.pw_anim_style);

        pw.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

        pw.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = HomeActivity.this
                        .getWindow().getAttributes();
                params.alpha = 1f;
                HomeActivity.this.getWindow().setAttributes(params);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/userIcon.jpg";
            if (!cameraUri.getPath().isEmpty()) {
                Bitmap temp = BitmapFactory.decodeFile(cameraUri.getPath());
                Bitmap bitmap = big(temp, 60, 60);
                ivIcon.setImageBitmap(bitmap);
                changeInfo(USER_ICON);
            }

            // upLoadUserIcon(new File(Environment.getExternalStorageDirectory()
            // .getAbsolutePath() + "/usericon.PNG"));
        } else if (requestCode == PHOTO) {
            if (data != null) {
                Uri uri = data.getData();
                // 因为相册出返回的uri路径是ContentProvider开放的路径，不是直接的sd卡具体路径
                // 因此无法通过decodeFile方法解析图片
                // 必须通过ContentResolver对象读取图片
                ContentResolver cr = HomeActivity.this.getContentResolver();
                try {
                    Bitmap b = MediaStore.Images.Media.getBitmap(cr, uri);
                    Bitmap bitmap = big(b, 60, 60);
                    bitmap.getByteCount();
                    ivIcon.setImageBitmap(bitmap);
                    // 这里开始的第二部分，获取图片的路径：
                    String[] proj = {MediaStore.Images.Media.DATA};
                    // 好像是android多媒体数据库的封装接口，具体的看Android文档
                    Cursor cursor = HomeActivity.this.managedQuery(uri, proj, null, null, null);
                    // 按我个人理解 这个是获得用户选择的图片的索引值
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                    cursor.moveToFirst();
                    // 最后根据索引值获取图片路径
                    path = cursor.getString(column_index);
                    changeInfo(USER_ICON);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (requestCode == INFO_CAMERA) {
            path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/carInfo.jpg";
            Log.i("oye", "相册" + path1);
            if (!cameraUri.getPath().isEmpty()) {
                Bitmap temp = BitmapFactory.decodeFile(cameraUri.getPath());
                Bitmap bitmap = big(temp, 60, 60);
                ivCarInfo.setImageBitmap(bitmap);
                changeInfo(CAR_INFO);
            }
        } else if (requestCode == INFO_PHOTO) {
            if (data != null) {
                Uri uri = data.getData();
                // 因为相册出返回的uri路径是ContentProvider开放的路径，不是直接的sd卡具体路径
                // 因此无法通过decodeFile方法解析图片
                // 必须通过ContentResolver对象读取图片
                ContentResolver cr = HomeActivity.this.getContentResolver();
                try {
                    Bitmap b = MediaStore.Images.Media.getBitmap(cr, uri);
                    Bitmap bitmap = big(b, 60, 60);
                    bitmap.getByteCount();
                    ivCarInfo.setImageBitmap(bitmap);
                    // 这里开始的第二部分，获取图片的路径：
                    String[] proj = {MediaStore.Images.Media.DATA};
                    // 好像是android多媒体数据库的封装接口，具体的看Android文档
                    Cursor cursor = HomeActivity.this.managedQuery(uri, proj, null, null, null);
                    // 按我个人理解 这个是获得用户选择的图片的索引值
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                    cursor.moveToFirst();
                    // 最后根据索引值获取图片路径
                    path1 = cursor.getString(column_index);
                    changeInfo(CAR_INFO);
                    // Log.i("oye", path);
                    // 上传头像
                    // upLoadUserIcon(new File(path));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 相机调用
     */
    private void getFromCamera(int type1, boolean isUser) {
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (isUser) {
            cameraPic = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/userIcon.jpg");
        } else {
            cameraPic = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/carInfo.jpg");
        }

        cameraUri = Uri.fromFile(cameraPic);
        // 指定照片拍摄后的存储位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(intent, type1);
    }

    /**
     * 调取相册
     */
    private void getFromPhoto(int type2) {
        intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra("return-data", true);
        startActivityForResult(intent, type2);
    }


    public Bitmap big(Bitmap b, float x, float y) {
        int w = b.getWidth();
        int h = b.getHeight();
        float sx = (float) x / w;// 要强制转换，不转换我的在这总是死掉。
        float sy = (float) y / h;
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy); // 长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(b, 0, 0, w, h, matrix, true);
        return resizeBmp;
    }


    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isclose", false);
        editor.commit();
    }


    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
        sp = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isclose", true);
        editor.commit();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


    //for receive customer msg from jpush server
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == HANDLER_LOGIN_FAILURE) {


        } else if (msg.what == HANDLER_LOGIN_SUCCESS) {

//            if (mDialog != null)
//                mDialog.dismiss();
            if (dialog != null) {
                dialog.dismiss();
            }
            /**
             * 融云登录成功
             */
            // WinToast.toast(LoginActivity.this, R.string.login_success);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else if (msg.what == HANDLER_LOGIN_HAS_FOCUS) {

        } else if (msg.what == HANDLER_LOGIN_HAS_NO_FOCUS) {

        }

        return false;
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                String messge = intent.getStringExtra(KEY_MESSAGE);
                String extras = intent.getStringExtra(KEY_EXTRAS);
                StringBuilder showMsg = new StringBuilder();
                showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                if (!ExampleUtil.isEmpty(extras)) {
                    showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                }
            }
        }
    }

}
