package com.example.link.photo;

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
import android.util.Log;
import android.view.SurfaceView;

import java.io.IOException;

public class PhotoService extends Service {
    private static final String TAG = "LocalService";

    private Camera mCamera;
    private boolean isRepeat = false;
    private int interval = 5; //second

    private String Token;
    private String Secret;

    private AlarmManager am = null;
    private NotificationManager mNM;

    private int NOTIFICATION = R.string.local_service_started;


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
    }

    private Camera openCamera() {
        Camera cam = Camera.open(0);
        return cam;
    }

    private void initSingle() {
        if (am != null)
            cancelAlertManager();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.link.photo");
        registerReceiver(alarmReceiver, filter);
    }

    private void initRepeat(int interval) {
        if (am != null)
            cancelAlertManager();
        am = (AlarmManager) getSystemService(ALARM_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.vegetables_source.alarm");
        registerReceiver(alarmReceiver, filter);

        Intent intent = new Intent();
        intent.setAction("com.vegetables_source.alarm");
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * interval, pi);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mNM.cancel(NOTIFICATION);
        if (alarmReceiver != null)
            unregisterReceiver(alarmReceiver);

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
        public void SetTimeInterval(int second) throws RemoteException {
            interval = second;
            if (isRepeat)
                initRepeat(interval);
            else
                initSingle();
        }

        @Override
        public void SetRepeat(boolean repeat) throws RemoteException {
            isRepeat = repeat;
            if (isRepeat)
                initRepeat(interval);
            else
                initSingle();
        }

        @Override
        public void SetToken(String token, String secret) throws RemoteException {
            Token = token;
            Secret = secret;
            Log.d(TAG, "mData Secret: " + Token + " " + Secret);
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

                    mCamera.takePicture(null, null, new PhotoHandler(Token, Secret));
                }
            }
        }
    };
}