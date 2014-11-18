package com.example.link.photo;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by link on 11/13/14.
 */
public class OauthThread extends Thread{
    private static final String TAG = "OauthThread";
    private String SecretKey;

    private MainActivity.MyHandler mHandler;
    private boolean mIsRequestToken;
    private Tools mTools;
    private final int MSG_AUTHORIZE = 10000;
    private final int MSG_OAUTH_FINISH = 10002;

    private String tempOauth;

    private final String requestTokenUrl = "https://openapi.kuaipan.cn/open/requestToken";
    private String accessTokenUrl = "https://openapi.kuaipan.cn/open/accessToken";

    private DataSave mData;



    public OauthThread(boolean isRequestToken, MainActivity.MyHandler handler, DataSave data) {
        mIsRequestToken = isRequestToken;
        mHandler = handler;
        mData = data;
        mTools = new Tools();

    }

    @Override
    public void run() {
        super.run();
        String response;

        if (mIsRequestToken) {
            //requestToken
            String extendUrl = getExtendUrlString(null);
            String baseString = mTools.BaseString("GET&", requestTokenUrl, extendUrl);
            String urlSignature = mTools.SignatureUrl(baseString, mData.customSecretKey);
            String finalUrl = requestTokenUrl + "?" + extendUrl + "&" + "oauth_signature=" + urlSignature;
            response = mTools.loadUrl(finalUrl);

            //authorize
            handleResponse(mIsRequestToken, response);
            String authUrl = "https://www.kuaipan.cn/api.php?ac=open&op=authorise&oauth_token=" + tempOauth;
            mHandler.obtainMessage(MSG_AUTHORIZE, authUrl).sendToTarget();
        } else {
            //accessToken
            tempOauth = mData.GetTempOauthToken();
            SecretKey = mData.customSecretKey + mData.GetTempOauthTokenSecret();
            String extendUrl = getExtendUrlString(tempOauth);
            String baseString = mTools.BaseString("GET&", accessTokenUrl, extendUrl);
            String urlSignature = mTools.SignatureUrl(baseString, SecretKey);
            String finalUrl = accessTokenUrl + "?" + extendUrl + "&" + "oauth_signature=" + urlSignature;

            response = mTools.loadUrl(finalUrl);
            handleResponse(false, response);

            mHandler.sendEmptyMessage(MSG_OAUTH_FINISH);
        }
    }


    private void handleResponse(boolean isRequestToken, String response) {
        try {
            JSONObject object = new JSONObject(response);

            if (isRequestToken) {
                tempOauth = object.getString("oauth_token");
                mData.SetTempOauthToken(object.getString("oauth_token"));
                mData.SetTempOauthTokenSecret(object.getString("oauth_token_secret"));
            } else {
                mData.SetUserId(object.getString("user_id"));
                mData.SetOauthToken(object.getString("oauth_token"));
                mData.SetOauthTokenSecret(object.getString("oauth_token_secret"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                    "&oauth_token=" + oauthToken +
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
}
