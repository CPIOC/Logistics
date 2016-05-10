package com.cpic.taylor.logistics.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
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

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudActivity.MainActivity;
import com.cpic.taylor.logistics.base.BaseActivity;
import com.cpic.taylor.logistics.fragment.HomeLineFragment;
import com.cpic.taylor.logistics.fragment.HomePoliceFragment;
import com.cpic.taylor.logistics.fragment.HomeRoadFragment;
import com.cpic.taylor.logistics.utils.RoundImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Taylor on 2016/5/4.
 */
public class HomeActivity extends BaseActivity {

    // 记录上次点击返回键的时间
    private long lastTime;

    private DrawerLayout layout;
    private ImageView ivMine;
    private TextView tvChat;
    private RadioGroup rgroup;
    private RadioButton lastButton;
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
    private TextView tvCamera, tvPhoto, tvBack;
    private Intent intent;

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

        /**
         * 侧滑控件
         */
        linIcon = (LinearLayout) findViewById(R.id.layout_icon);
        etName = (EditText) findViewById(R.id.layout_et_name);
        etCarNum = (EditText) findViewById(R.id.layout_et_car_num);
        etCarType = (EditText) findViewById(R.id.layout_et_car_type);
        ivCarInfo = (ImageView) findViewById(R.id.layout_iv_carinfo);
        ivIcon = (RoundImageView) findViewById(R.id.layout_iv_icon);
    }

    @Override
    protected void initData() {
        initFragment();
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

            }
        });
        etCarNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

            }
        });
        etCarType.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

            }
        });
        linIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow(view,CAMERA,PHOTO);
            }
        });
        ivCarInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow(view,INFO_CAMERA,INFO_PHOTO);
            }
        });


    }

    @Override
    public void onBackPressed() {
        // 获取本次点击的时间
        long currentTime = System.currentTimeMillis();
        long dTime = currentTime - lastTime;

        if (dTime < 2000) {
            finish();
        } else {
            showShortToast("再按一次退出程序");
            lastTime = currentTime;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void showPopupWindow(View v,final int type1,final int type2) {
        View view = View.inflate(HomeActivity.this, R.layout.popupwindow_1, null);
        tvCamera = (TextView) view.findViewById(R.id.btn_camera);
        tvPhoto = (TextView) view.findViewById(R.id.btn_photo);
        tvBack = (TextView) view.findViewById(R.id.btn_back);
        tvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFromCamera(type1);
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
            path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/userIcon.jpg";
            if (!cameraUri.getPath().isEmpty()) {
                Bitmap temp = BitmapFactory.decodeFile(cameraUri.getPath());
                Bitmap bitmap = big(temp, 60, 60);
                ivIcon.setImageBitmap(bitmap);
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
                    String[] proj = { MediaStore.Images.Media.DATA };
                    // 好像是android多媒体数据库的封装接口，具体的看Android文档
                    Cursor cursor = HomeActivity.this.managedQuery(uri, proj, null, null, null);
                    // 按我个人理解 这个是获得用户选择的图片的索引值
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                    cursor.moveToFirst();
                    // 最后根据索引值获取图片路径
                    path = cursor.getString(column_index);
                    // Log.i("oye", path);
                    // 上传头像
                    // upLoadUserIcon(new File(path));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }else if (requestCode == INFO_CAMERA){
            path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/carInfo.jpg";
            if (!cameraUri.getPath().isEmpty()) {
                Bitmap temp = BitmapFactory.decodeFile(cameraUri.getPath());
                Bitmap bitmap = big(temp, 60, 60);
                ivCarInfo.setImageBitmap(bitmap);
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
                    String[] proj = { MediaStore.Images.Media.DATA };
                    // 好像是android多媒体数据库的封装接口，具体的看Android文档
                    Cursor cursor = HomeActivity.this.managedQuery(uri, proj, null, null, null);
                    // 按我个人理解 这个是获得用户选择的图片的索引值
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                    cursor.moveToFirst();
                    // 最后根据索引值获取图片路径
                    path = cursor.getString(column_index);
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
    private void getFromCamera(int type1) {
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraPic = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/userIcon.jpg");
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
}
