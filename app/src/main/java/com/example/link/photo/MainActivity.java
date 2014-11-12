package com.example.link.photo;

/**
 * Created by link on 11/10/14.
 */
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private Intent serviceIntent;
    private IPhotoService myService = null;

    private String str = "Hello";

    private boolean mIsBound = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceIntent = new Intent(this, PhotoService.class);
        startService(serviceIntent);
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

}
