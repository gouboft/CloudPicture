package com.example.link.photo;

/**
 * Created by link on 11/10/14.
 */
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private Intent serviceIntent;
    private IPhotoService myService = null;
    private MyHandler myHandler;
    private WebView mWebView;
    private String str = "Hello";

    private boolean mIsBound = false;
    private final int MSG_AUTH_URL = 10000;
    private final int MSG_ACCESS_TOKEN = 10001;
    private NetworkThread networkThread;

    private String tempOauthToken = null;

    private DataSave mData;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mData = new DataSave();

        myHandler = new MyHandler();
        mWebView = (WebView) findViewById(R.id.webView);


        serviceIntent = new Intent(this, PhotoService.class);
        startService(serviceIntent);

        networkThread = new NetworkThread(true, myHandler, mData);
        networkThread.start();
    }

    public void SetTempOauthToken(String temp) {
        tempOauthToken = temp;
    }
    public void click(View v) {
        int key = v.getId();
        switch (key) {
            case R.id.button1:
                //doBindService();
                TakePicture();
                break;
            case R.id.button2:
                //doUnbindService();
                if (serviceIntent != null)
                    stopService(serviceIntent);
                break;
            default:
                break;
        }

    }

    private void TakePicture() {
        Intent intent = new Intent();
        intent.setAction("com.example.link.photo");
        sendBroadcast(intent);
    }

    private void doBindService() {
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(serviceConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "MainActivity take picture");
            myService = IPhotoService.Stub.asInterface(service);
            try {
                myService.TakePicture();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }

    };

    private final class MyWebViewClient extends WebViewClient{
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
                networkThread = null;
                myHandler.sendEmptyMessage(MSG_ACCESS_TOKEN);
            }
            Log.d(TAG, "html: " + html);
        }
    }

    public class MyHandler extends Handler {
        public MyHandler() {
        }

        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUTH_URL:
                    mWebView.getSettings().setJavaScriptEnabled(true);
                    mWebView.addJavascriptInterface(new InJavaScriptLocalObj(), "handler");
                    mWebView.setWebViewClient(new MyWebViewClient());
                    mWebView.loadUrl(msg.obj.toString());
                    break;
                case MSG_ACCESS_TOKEN:
                    networkThread = new NetworkThread(false, myHandler, mData);
                    networkThread.start();
                    break;
                default:
                    Log.e(TAG, "Unknown Message!");
            }
        }
    }
}
