package com.example.link.photo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.util.Date;
import java.util.Random;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import junit.framework.Assert;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PhotoService extends Service {
    private static final String TAG = "LocalService";

    private Camera mCamera;
    private boolean isRepeat = false;
    private int interval = 5; //second

    private AlarmManager am = null;
    private NotificationManager mNM;

    private int NOTIFICATION = R.string.local_service_started;
    private final String secretKey = "qa3ZaNTBHMsvjvMb&";

    private String finalUrl;

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class PhotoBinder extends Binder {
        public PhotoService getService() {
            return PhotoService.this;
        }

    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        mCamera = openCamera();

        if (isRepeat)
            initRepeat();
        else
            initSingle();

        try {
            String httpUrl = getHttpUrl();
            NetworkThread networkThread = new NetworkThread(httpUrl);
            networkThread.start();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

    private String getHttpUrl() throws UnsupportedEncodingException {
        Random random = new Random();
        int randomInt = random.nextInt();
        if (randomInt < 0)
            randomInt = -randomInt;
        String baseURLEncode = null;
        String extendUrlEncode = null;
        String signatureEncode = null;

        String baseUrl = "https://openapi.kuaipan.cn/open/requestToken";
        String extendUrl = "oauth_consumer_key=xc4YmYkbZLfiH3eU" + "&" +
                "oauth_nonce=" + randomInt + "&" +
                "oauth_signature_method=HMAC-SHA1" + "&" +
                "oauth_timestamp=" + System.currentTimeMillis() + "&" +
                "oauth_version=1.0";
        try {
            baseURLEncode = URLEncoder.encode(baseUrl, "UTF-8");
            extendUrlEncode = URLEncoder.encode(extendUrl, "UTF-8");
            Log.d(TAG, "base: " + baseURLEncode);
            Log.d(TAG, "extendUrlEncode: " + extendUrlEncode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        assert baseURLEncode != null && extendUrlEncode != null;
        String baseString = "GET&" + baseURLEncode + "&" + extendUrlEncode;

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(secretKey.getBytes("UTF-8"), mac.getAlgorithm());
            mac.init(secret);
            byte[] digest = mac.doFinal(baseString.getBytes());
            String result = Base64.encodeToString(digest, Base64.DEFAULT);

            signatureEncode = URLEncoder.encode(result, "UTF-8");
            Log.d(TAG, "Base64 result Encode: " + signatureEncode);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        finalUrl = baseUrl + "?" + extendUrl + "&" + "oauth_signature=" + signatureEncode;
        Log.d(TAG, "finalUrl = " + finalUrl);
        return finalUrl;
    }

    private Camera openCamera() {
        Camera cam = Camera.open(0);
        return cam;
    }

    private void initSingle() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.link.photo");
        registerReceiver(alarmReceiver, filter);
    }

    private void initRepeat() {
        am = (AlarmManager) getSystemService(ALARM_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.vegetables_source.alarm");
        registerReceiver(alarmReceiver, filter);

        Intent intent = new Intent();
        intent.setAction("com.vegetables_source.alarm");
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);

        // 马上开始，每5秒触发一次
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * interval, pi);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mNM.cancel(NOTIFICATION);

        if (am != null)
            cancelAlertManager();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    IPhotoService.Stub mBinder = new IPhotoService.Stub() {
        @Override
        public void TakePicture() throws RemoteException {
        }
    };

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        CharSequence text = getText(R.string.local_service_started);

        Notification notification = new Notification(R.drawable.stat_running,
                text, System.currentTimeMillis());

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        notification.setLatestEventInfo(this,
                getText(R.string.local_service_label), text, contentIntent);

        mNM.notify(NOTIFICATION, notification);
    }

    private void cancelAlertManager() {
        Intent intent = new Intent();
        intent.setAction("com.vegetables_source.alarm");
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        am.cancel(pi);

        // 注销广播
        unregisterReceiver(alarmReceiver);
    }

    BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.vegetables_source.alarm".equals(intent.getAction())
                    || "com.example.link.photo".equals(intent.getAction())) {
                if (mCamera != null) {
                    SurfaceView dummy = new SurfaceView(getBaseContext());
                    try {
                        mCamera.setPreviewDisplay(dummy.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mCamera.startPreview();

                    mCamera.takePicture(null, null, new PhotoHandler(
                            getApplicationContext()));
                }
            }
        }
    };

}