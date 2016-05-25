package com.cpic.taylor.logistics.activity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
public class RegisterActivity extends BaseActivity{

    private EditText etMobile,etCode,etPwd,etCarNum,etCarType;
    private Button btnRegister;
    private TextView tvGetCode;

    private HttpUtils post;
    private RequestParams params;
    private Dialog dialog;
    private SharedPreferences sp;

    private int count = 30;

    private TimeCount time;


    @Override
    protected void getIntentData(Bundle savedInstanceState) {

    }

    @Override
    protected void loadXml() {
        setContentView(R.layout.activity_register);
    }

    @Override
    protected void initView() {
        etMobile = (EditText) findViewById(R.id.activity_register_et_mobile);
        etCode = (EditText) findViewById(R.id.activity_register_et_code);
        etPwd = (EditText) findViewById(R.id.activity_register_et_pwd);
        btnRegister = (Button) findViewById(R.id.activity_register_btn_register);
        tvGetCode = (TextView) findViewById(R.id.activity_register_tv_getcode);
        etCarNum = (EditText) findViewById(R.id.activity_register_et_carnum);
        etCarType = (EditText) findViewById(R.id.activity_register_et_cartype);

        dialog = ProgressDialogHandle.getProgressDialog(RegisterActivity.this,null);
        time = new TimeCount(60000, 1000);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void registerListener() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etMobile.getText().toString() == null || etPwd.getText().toString() == null||etCode.getText().toString()==null
                        || "".equals(etMobile.getText().toString()) || "".equals(etPwd.getText().toString())
                         || "".equals(etCode.getText().toString())) {
                    showShortToast("用户名和密码不得为空");
                    return;
                }
                if (etCarType.getText().toString() == null || etCarNum.getText().toString() == null||"".equals(etCarType.getText().toString()) || "".equals(etCarNum.getText().toString())){
                    showShortToast("请输入您的车牌号和车型");
                    return;
                }

                registerAction();
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
                    Toast.makeText(RegisterActivity.this,"获取验证码成功，请注意查收短信",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(RegisterActivity.this,obj.getString("msg"),Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (dialog != null){
                    dialog.dismiss();
                }
                Toast.makeText(RegisterActivity.this,"获取验证码失败，请检查网络连接",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerAction() {
        post = new HttpUtils();
        params = new RequestParams();
        String url = UrlUtils.POST_URL+UrlUtils.path_register;
        params.addBodyParameter("mobile",etMobile.getText().toString());
        params.addBodyParameter("password",etPwd.getText().toString());
        params.addBodyParameter("code",etCode.getText().toString());
        params.addBodyParameter("plate_number",etCarNum.getText().toString());
        params.addBodyParameter("car_models",etCarType.getText().toString());

        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                super.onStart();
                if (dialog !=null){
                    dialog.show();
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (dialog !=null){
                    dialog.dismiss();
                }
                JSONObject obj = JSONObject.parseObject(responseInfo.result);
                int code = obj.getIntValue("code");
                if (code == 1){
                    showShortToast("注册成功");
                    sp = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("mobile",etMobile.getText().toString());
                    editor.putString("pwd",etPwd.getText().toString());
                    editor.commit();
                    finish();
                }else{
                    showShortToast(obj.getString("msg"));
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (dialog !=null){
                    dialog.dismiss();
                }
                showShortToast("注册失败，请检查网络连接");
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
