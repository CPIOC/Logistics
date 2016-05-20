package com.cpic.taylor.logistics.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.base.BaseActivity;
import com.cpic.taylor.logistics.utils.ProgressDialogHandle;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

/**
 * Created by Taylor on 2016/5/4.
 */
public class ForgetPwdActivity extends BaseActivity{

    private Button btnNext;
    private Intent intent;
    private ImageView ivBack;
    private EditText etMobile,etCode,etPwd,etAgain;
    private TextView tvGetCode;

    private HttpUtils post;
    private RequestParams params;
    private Dialog dialog;

    private int count = 30;

    private TimeCount time;

    @Override
    protected void getIntentData(Bundle savedInstanceState) {

    }

    @Override
    protected void loadXml() {
        setContentView(R.layout.activity_forget);
    }

    @Override
    protected void initView() {
        btnNext = (Button) findViewById(R.id.activity_forget_btn_next);
        ivBack = (ImageView) findViewById(R.id.activity_forget_iv_back);
        etMobile = (EditText) findViewById(R.id.activity_forget_et_mobile);
        etPwd = (EditText) findViewById(R.id.activity_forget_et_pwd);
        etAgain = (EditText) findViewById(R.id.activity_forget_et_again);
        etCode = (EditText) findViewById(R.id.activity_forget_et_code);
        dialog = ProgressDialogHandle.getProgressDialog(ForgetPwdActivity.this,null);
        tvGetCode = (TextView) findViewById(R.id.activity_forget_tv_getcode);
        time = new TimeCount(60000, 1000);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void registerListener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tvGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = etMobile.getText().toString();
                if ("".equals(str)) {
                    showLongToast("手机号码不得为空");
                    return;
                }
                if (str.length() != 11) {
                    showLongToast("手机号码格式不正确");
                    return;
                }
                getCode();
                time.start();

            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (etMobile == null || "".equals(etMobile.getText().toString())){
                   showShortToast("手机号码不得为空");
                   return;
               }
                if (etCode == null || "".equals(etCode.getText().toString())){
                    showShortToast("验证码不得为空");
                    return;
                }
                if (etPwd == null || "".equals(etPwd.getText().toString())){
                    showShortToast("密码不得为空");
                    return;
                }
                if (etMobile == null || "".equals(etMobile.getText().toString())){
                    showShortToast("请再次输入密码");
                    return;
                }
                if (etPwd.getText().equals(etAgain.getText().toString())){
                    showShortToast("两次输入的密码不一致");
                    return;
                }
                changePwdAction();

            }
        });
    }

    private void getCode(){
        post = new HttpUtils();
        params = new RequestParams();
        params.addBodyParameter("mobile",etMobile.getText().toString());
        String url = UrlUtils.POST_URL+ UrlUtils.path_code;
        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                super.onStart();
                if (dialog != null){
                    dialog.show();
                }

            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (dialog != null){
                    dialog.dismiss();
                }
                JSONObject obj = JSONObject.parseObject(responseInfo.result);
                int code = obj.getIntValue("code");
                if (code == 1){
                    Toast.makeText(ForgetPwdActivity.this,"获取验证码成功，请注意查收短信",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ForgetPwdActivity.this,obj.getString("msg"),Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (dialog != null){
                    dialog.dismiss();
                }
                Toast.makeText(ForgetPwdActivity.this,"获取验证码失败，请检查网络连接",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changePwdAction() {
        params = new RequestParams();
        post = new HttpUtils();
        String url = UrlUtils.POST_URL+UrlUtils.path_forgotPwd;
        params.addBodyParameter("mobile",etMobile.getText().toString());
        params.addBodyParameter("code",etCode.getText().toString());
        params.addBodyParameter("password",etPwd.getText().toString());

        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {

            @Override
            public void onStart() {
                super.onStart();
                if (dialog != null){
                    dialog.show();
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (dialog != null){
                    dialog.dismiss();
                }
                JSONObject obj = JSONObject.parseObject(responseInfo.result);
                int code = obj.getIntValue("code");
                if (code == 1){
                    showShortToast("修改密码成功");
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ForgetPwdActivity.this);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("mobile",etMobile.getText().toString());
                    editor.putString("pwd",etPwd.getText().toString());
                    editor.commit();
                    finish();
                }else{
                    showShortToast("修改失败");
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (dialog != null){
                    dialog.dismiss();
                }
                showShortToast("修改失败，请检查网络连接");
            }
        });

    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            tvGetCode.setText("获取验证码");
            tvGetCode.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
            tvGetCode.setClickable(false);// 防止重复点击
            tvGetCode.setText(millisUntilFinished / 1000 + "s" + "重新验证");


        }
    }
}
