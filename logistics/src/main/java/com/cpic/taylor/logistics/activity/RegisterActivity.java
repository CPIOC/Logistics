package com.cpic.taylor.logistics.activity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    private EditText etMobile,etCode,etPwd;
    private Button btnRegister;
    private TextView tvGetCode;

    private HttpUtils post;
    private RequestParams params;
    private Dialog dialog;
    private SharedPreferences sp;

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
        dialog = ProgressDialogHandle.getProgressDialog(RegisterActivity.this,null);
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

                registerAction();
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
}
