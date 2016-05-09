package com.cpic.taylor.logistics.RongCloudActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cpic.taylor.logistics.R;


/**
 * Created by Administrator on 2015/3/20.
 */
@SuppressLint("SetJavaScriptEnabled")
public class DocumentActivity extends BaseActionBarActivity {

    private WebView mWebView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_document);

        getSupportActionBar().setTitle(R.string.dv_document);

        mWebView = (WebView) findViewById(R.id.document_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setSupportZoom(true);

        MyWebViewClient mMyWebViewClient = new MyWebViewClient();
        mMyWebViewClient.onPageFinished(mWebView, "http://docs.rongcloud.cn/api/android/imkit/index.html");
        mMyWebViewClient.shouldOverrideUrlLoading(mWebView, "http://docs.rongcloud.cn/api/android/imkit/index.html");
        mMyWebViewClient.onPageFinished(mWebView, "http://docs.rongcloud.cn/api/android/imkit/index.html");
        mWebView.setWebViewClient(mMyWebViewClient);
    }

    class MyWebViewClient extends WebViewClient {

        ProgressDialog progressDialog;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//网页页面开始加载的时候
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(DocumentActivity.this);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                mWebView.setEnabled(false);// 当加载网页的时候将网页进行隐藏
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {//网页加载结束的时候
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
                mWebView.setEnabled(true);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) { //网页加载时的连接的网址
            view.loadUrl(url);
            return false;
        }
    }
}
