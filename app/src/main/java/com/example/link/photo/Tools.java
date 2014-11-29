package com.example.link.photo;

import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by link on 11/14/14.
 */
public class Tools {
    private static final String TAG = "Tools";
    private boolean DEBUG = true;

    public String loadUrl(String httpUrl) {
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
                InputStreamReader response;
                if (urlConn.getResponseCode() != HttpStatus.SC_OK) {
                    Log.e(TAG, "Http Connection Error, Error Code: " + urlConn.getResponseCode());
                    response = new InputStreamReader(urlConn.getErrorStream());
                } else
                    response = new InputStreamReader(urlConn.getInputStream());

                // 为输出创建BufferedReader
                BufferedReader buffer = new BufferedReader(response);
                String inputLine = null;

                //使用循环来读取获得的数据
                while (((inputLine = buffer.readLine()) != null)) {
                    //我们在每一行后面加上一个"\n"来换行
                    resultData += inputLine + "\n";
                }

                if ( !resultData.equals("")) {
                    if(DEBUG) Log.d(TAG, "resultData: " + resultData);
                } else {
                    Log.e(TAG, "resultDate is null");
                }
                //关闭InputStreamReader
                response.close();
                //关闭http连接
                urlConn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Url is NULL!");
        }
        return resultData;
    }

    public String handleResponse(String response, String key) {
        String tempStr = null;
        try {
            JSONObject object = new JSONObject(response);

            tempStr = object.getString(key);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tempStr;
    }


    public String BaseString(String httpMtd, String baseUrl, String extendUrl) {
        String baseURLEncode = null;
        String extendUrlEncode = null;
        if(DEBUG) Log.d(TAG, "baseUrl: " + baseUrl);
        if(DEBUG) Log.d(TAG, "extendUrl: " + extendUrl);
        try {
            baseURLEncode = URLEncoder.encode(baseUrl, "UTF-8");
            extendUrlEncode = URLEncoder.encode(extendUrl, "UTF-8");
            if(DEBUG) Log.d(TAG, "baseUrlEncode: " + baseURLEncode);
            if(DEBUG) Log.d(TAG, "extendUrlEncode: " + extendUrlEncode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return  httpMtd + baseURLEncode + "&" + extendUrlEncode;
    }

    public String SignatureUrl(String baseString, String secretKey) {
        if(DEBUG) Log.d(TAG, "secretKey: " + secretKey);
        String signatureEncode = null;
        try {
            SecretKeySpec secret = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secret);
            byte[] digest = mac.doFinal(baseString.getBytes("UTF-8"));
            String result = Base64.encodeToString(digest, Base64.DEFAULT);
            if(DEBUG) Log.d(TAG, "Base64 result: " + result);

            //We would be REMOVE the last char in result
            signatureEncode = URLEncoder.encode(result.substring(0, result.length() - 1), "UTF-8");
            if(DEBUG) Log.d(TAG, "Base64 result Encode: " + signatureEncode);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return signatureEncode;
    }

    public String GetOauthPara(String oauthToken) {
        String oauthPara = null;
        Random random = new Random();
        int randomInt = random.nextInt();
        if (randomInt < 0)
            randomInt = -randomInt;
        if (oauthToken != null) {
            oauthPara = "oauth_consumer_key=xc4YmYkbZLfiH3eU" +
                    "&oauth_nonce=" + String.valueOf(randomInt) +
                    "&oauth_signature_method=HMAC-SHA1" +
                    "&oauth_timestamp=" + System.currentTimeMillis() +
                    "&oauth_token=" + oauthToken +
                    "&oauth_version=1.0";
        }
        return oauthPara;
    }
}
