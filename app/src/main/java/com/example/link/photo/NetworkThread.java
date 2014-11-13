package com.example.link.photo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by link on 11/13/14.
 */
public class NetworkThread extends Thread{
    private static final String TAG = "NetworkThread";
    private String mHttpUrl = null;

    public NetworkThread(String httpUrl) {
        mHttpUrl = httpUrl;
    }

    @Override
    public void run() {
        super.run();
        try {
            connectNetDisk(mHttpUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private int connectNetDisk(String httpUrl) throws UnsupportedEncodingException {
        String resultData = "";
        URL url = null;
        try {
            //构造一个URL对象
            url = new URL(httpUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException");
        }
        if (url != null) {
            try {
                //使用HttpURLConnection打开连接
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                //得到读取的内容(流)
                assert urlConn != null;

                InputStreamReader errorReturn = new InputStreamReader(urlConn.getErrorStream());

                // 为输出创建BufferedReader
                BufferedReader buffer = new BufferedReader(errorReturn);
                String inputLine = null;
                //使用循环来读取获得的数据
                while (((inputLine = buffer.readLine()) != null)) {
                    //我们在每一行后面加上一个"\n"来换行
                    resultData += inputLine + "\n";
                }

                if ( !resultData.equals("")) {
                    Log.d(TAG, "resultData: " + resultData);
                } else {
                    Log.d(TAG, "resultDate is null");
                }
                //关闭InputStreamReader
                errorReturn.close();
                //关闭http连接
                urlConn.disconnect();
                //设置显示取得的内容

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Url is NULL!");
        }
        return 0;
    }
}
