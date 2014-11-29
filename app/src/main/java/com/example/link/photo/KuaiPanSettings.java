package com.example.link.photo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.example.link.photo.MainActivity.MyHandler;


public class KuaiPanSettings extends Activity {
    private static final String TAG = "KuaiPanSettings";
    private MyHandler mHandler;
    private ViewHandler viewHandler;
    private final int MSG_ACCESS_TOKEN = 10001;
    private final int MSG_VIEW_MODIFY = 100;

    public KuaiPanSettings() {
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kuai_pan_settings);
        MyApplication mApp = (MyApplication) getApplication();
        mHandler = mApp.getHandler();
        viewHandler = new ViewHandler();

        Intent intent = getIntent();

        WebView mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new InJavaScriptLocalObj(), "handler");
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.loadUrl(intent.getStringExtra("url"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewHandler = null;
    }

    private final class MyWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:window.handler.show(document.body.innerHTML);");
            super.onPageFinished(view, url);
        }
    }

    private final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void show(String html) {
            int isExist = html.indexOf("登录并授权成功");
            if (isExist > 0) {
                mHandler.sendEmptyMessage(MSG_ACCESS_TOKEN);
                viewHandler.sendEmptyMessage(MSG_VIEW_MODIFY);

                Log.d(TAG, "html: " + html);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
            }
        }
    }

    public class ViewHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_VIEW_MODIFY:
                    TextView textView = (TextView) findViewById(R.id.text);
                    textView.setText("登录成功，2秒后自动关闭登录。");
            }
        }
    }
}
