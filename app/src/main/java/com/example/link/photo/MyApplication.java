package com.example.link.photo;

import android.app.Application;
import com.example.link.photo.MainActivity.MyHandler;

/**
 * Created by link on 11/18/14.
 */
public class MyApplication extends Application {
    private MyHandler mHandler;

    public void setHandler(MyHandler handler) {
        mHandler = handler;
    }

    public MyHandler getHandler() {
        return mHandler;
    }
}
