package com.example.link.photo;

/**
 * Created by link on 11/10/14.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;


public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "MainActivity";
    private Intent serviceIntent;
    private IPhotoService myService = null;
    private MyHandler myHandler;
    private SharedPreferences mPrefs;

    private String str = "Hello";

    private boolean mIsBound = false;

    private final int MSG_AUTHORIZE = 10000;
    private final int MSG_ACCESS_TOKEN = 10001;
    private final int MSG_OAUTH_FINISH = 10002;
    private final int MSG_UPLOAD_PICTURE = 10003;
    private final int MSG_SET_OAU_TKN = 10004;
    private final int MSG_SET_OAU_TKN_SECRET = 10005;

    private OauthThread oauthThread;
    private boolean passOauth = false;
    private boolean isRepeat;
    private int interval;

    private DataSave mData;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        isRepeat = mPrefs.getBoolean("auto_take_pic_checkbox", false);
        interval = Integer.parseInt(mPrefs.getString("time_interval", "5"));


        MyApplication myApp = (MyApplication) getApplication();
        mContext = getApplicationContext();
        mData = myApp.getData();

        String OauthTokenSecret = PreferenceGetString("OauthTokenSecret");
        if (OauthTokenSecret != null) {
            Log.d(TAG, "OauthTokenSecret: " + OauthTokenSecret);
            mData.SetOauthTokenSecret(OauthTokenSecret);
        }
        String OauthToken = PreferenceGetString("OauthToken");
        if (OauthToken != null) {
            Log.d(TAG, "OauthToken: " + OauthToken);
            mData.SetOauthToken(OauthToken);
        }


        if (mData.GetOauthToken() != null && mData.GetOauthTokenSecret() != null)
            passOauth = true;

        myHandler = new MyHandler();
        myApp.setHandler(myHandler);

        doBindService();

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
        serviceIntent = new Intent("com.example.link.photo.IPhotoService");
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
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
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        myHandler = null;
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "MainActivity take picture");
            myService = IPhotoService.Stub.asInterface(service);
            try {
                myService.SetToken(mData.GetOauthToken(), mData.GetOauthTokenSecret());
                myService.SetRepeat(isRepeat);
                myService.SetTimeInterval(interval);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }

    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("auto_take_pic_checkbox")) {
            Log.d(TAG, "auto_take_pic_checkbox");
            try {
                isRepeat = mPrefs.getBoolean("auto_take_pic_checkbox", false);
                myService.SetRepeat(isRepeat);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (key.equals("time_interval")) {
            Log.d(TAG, "time_interval");
            interval = Integer.parseInt(sharedPreferences.getString("time_interval", "5"));
            try {
                myService.SetTimeInterval(interval);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public class MyHandler extends Handler {
        public MyHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUTHORIZE:
                    Intent intent = new Intent(MainActivity.this, KuaiPanSettings.class);
                    intent.putExtra("url", msg.obj.toString());
                    startActivity(intent);
                    break;
                case MSG_ACCESS_TOKEN:
                    oauthThread = new OauthThread(false, myHandler, mData);
                    oauthThread.start();
                    break;
                case MSG_OAUTH_FINISH:
                    oauthThread = null;
                    break;
                case MSG_UPLOAD_PICTURE:
                    UploadThread uploadThread = new UploadThread(mData, msg.obj.toString());
                    uploadThread.start();
                    break;
                case MSG_SET_OAU_TKN_SECRET:
                    PreferenceSetString("OauthTokenSecret", msg.obj.toString());
                    break;
                case MSG_SET_OAU_TKN:
                    PreferenceSetString("OauthToken", msg.obj.toString());
                    break;
                default:
                    Log.e(TAG, "Unknown Message!");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            //intent.putExtra();
            startActivity(intent);
            return true;
        } else if (id == R.id.action_kuai_pan_settings) {
            OauthThread oauthThread = new OauthThread(true, myHandler, mData);
            oauthThread.start();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void PreferenceSetString(String key, String value) {
        SharedPreferences sp = mContext.getSharedPreferences("oauth_data_save", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);

        editor.apply();
    }

    public String PreferenceGetString(String key) {
        SharedPreferences sp = mContext.getSharedPreferences("oauth_data_save", Context.MODE_PRIVATE);

        return sp.getString(key, "");

    }
}
