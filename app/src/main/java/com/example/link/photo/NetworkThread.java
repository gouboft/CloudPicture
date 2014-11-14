package com.example.link.photo;

import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;

import org.json.JSONArray;
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
 * Created by link on 11/13/14.
 */
public class NetworkThread extends Thread{
    private static final String TAG = "NetworkThread";
    private String mHttpUrl = null;
    private String customSecretKey = "qa3ZaNTBHMsvjvMb&";
    private String tempSecretKey = null;

    private MainActivity.MyHandler mHandler;
    private boolean mIsRequestToken;
    private final int MSG_AUTH_URL = 10000;

    private String tempOauth;
    private String oauthToken;
    private String oauthTokenSecret;
    private String userId;

    private String requestTokenUrl = "https://openapi.kuaipan.cn/open/requestToken";
    private String accessTokenUrl = "https://openapi.kuaipan.cn/open/accessToken";

    private DataSave mData;



    public NetworkThread(boolean isRequestToken, MainActivity.MyHandler handler, DataSave data) {
        mIsRequestToken = isRequestToken;
        mHandler = handler;
        mData = data;
    }

    @Override
    public void run() {
        super.run();
        try {
            String httpUrl;
            String response;
            if (mIsRequestToken) {
                //requestToken
                httpUrl = getHttpUrl(requestTokenUrl, getExtendUrlString(null));
                response = requestUrl(httpUrl);

                //authorize
                handleResponse(mIsRequestToken, response);
                String authUrl = "https://www.kuaipan.cn/api.php?ac=open&op=authorise&oauth_token=" + tempOauth;
                mHandler.obtainMessage(MSG_AUTH_URL, authUrl).sendToTarget();
            } else {
                //accessToken
                tempOauth = mData.GetTempOauthToken();
                customSecretKey = customSecretKey + mData.GetTempOauthTokenSecret();
                Log.d(TAG, "customSecretKey: " + customSecretKey);
                httpUrl = getHttpUrl(accessTokenUrl, getExtendUrlString(tempOauth));
                response = requestUrl(httpUrl);
                handleResponse(false, response);
                Log.d(TAG, "oauthToken: " + oauthToken + "     oauthTokenSecret: " + oauthTokenSecret);
                mData.SetOauthToken(oauthToken);
                mData.SetOauthTokenSecret(oauthTokenSecret);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String handleResponse(boolean isRequestToken, String response) {
        try {
            JSONObject object = new JSONObject(response);

            if (isRequestToken) {
                tempOauth = object.getString("oauth_token");
                mData.SetTempOauthToken(object.getString("oauth_token"));
                mData.SetTempOauthTokenSecret(object.getString("oauth_token_secret"));
            } else {
                userId = object.getString("user_id");
                mData.SetOauthToken(object.getString("oauth_token"));
                mData.SetOauthTokenSecret(object.getString("oauth_token_secret"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String requestUrl(String httpUrl) throws UnsupportedEncodingException {
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
                InputStreamReader response = null;
                if (urlConn.getResponseCode() == 200) //200 is normal response, if others error occur
                    response = new InputStreamReader(urlConn.getInputStream());
                else
                    response = new InputStreamReader(urlConn.getErrorStream());

                // 为输出创建BufferedReader
                BufferedReader buffer = new BufferedReader(response);
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
                response.close();
                //关闭http连接
                urlConn.disconnect();
                //设置显示取得的内容

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Url is NULL!");
        }
        return resultData;
    }

    private String getHttpUrl(String baseUrl, String extendUrl) {
        String baseURLEncode = null;
        String extendUrlEncode = null;

        try {
            baseURLEncode = URLEncoder.encode(baseUrl, "UTF-8");
            extendUrlEncode = URLEncoder.encode(extendUrl, "UTF-8");
            Log.d(TAG, "base: " + baseURLEncode);
            Log.d(TAG, "extendUrlEncode: " + extendUrlEncode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String baseString = "GET&" + baseURLEncode + "&" + extendUrlEncode;

        String finalUrl = baseUrl + "?" + extendUrl + "&" + "oauth_signature=" + SignatureUrl(baseString);
        return finalUrl;
    }

    private String getExtendUrlString(String oauthToken) {
        String extendUrl = null;
        Random random = new Random();
        int randomInt = random.nextInt();
        if (randomInt < 0)
            randomInt = -randomInt;
        if (oauthToken != null) {
            extendUrl = "oauth_consumer_key=xc4YmYkbZLfiH3eU" +
                    "&oauth_nonce=" + String.valueOf(randomInt) +
                    "&oauth_signature_method=HMAC-SHA1" +
                    "&oauth_timestamp=" + System.currentTimeMillis() +
                    "&oauth_token=" + tempOauth +
                    "&oauth_version=1.0";
        } else {
            extendUrl = "oauth_consumer_key=xc4YmYkbZLfiH3eU" +
                    "&oauth_nonce=" + String.valueOf(randomInt) +
                    "&oauth_signature_method=HMAC-SHA1" +
                    "&oauth_timestamp=" + System.currentTimeMillis() +
                    "&oauth_version=1.0";
        }
        return extendUrl;
    }

    private String SignatureUrl(String str) {
        String signatureEncode = null;
        try {
            SecretKeySpec secret = new SecretKeySpec(customSecretKey.getBytes("UTF-8"), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secret);
            byte[] digest = mac.doFinal(str.getBytes("UTF-8"));
            String result = Base64.encodeToString(digest, Base64.DEFAULT);
            Log.d(TAG, "Base64 result: " + result);

            //We would be REMOVE the last char in result
            signatureEncode = URLEncoder.encode(result.substring(0, result.length() - 1), "UTF-8");
            Log.d(TAG, "Base64 result Encode: " + signatureEncode);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return signatureEncode;
    }
}
