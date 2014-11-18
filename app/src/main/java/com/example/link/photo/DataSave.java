package com.example.link.photo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by link on 11/14/14.
 */
public class DataSave {
    private String tempOauthToken;
    private String tempOauthTokenSecret;
    private String OauthToken;
    private String OauthTokenSecret;
    private String UserId;
    private Context mContext;

    public String customSecretKey = "qa3ZaNTBHMsvjvMb&";

    public DataSave() {
        tempOauthToken = "";
        OauthToken = "";
        OauthTokenSecret = "";
    }

    public void SetTempOauthToken(String string) {
        tempOauthToken = string;
    }

    public void SetTempOauthTokenSecret(String string) {
        tempOauthTokenSecret = string;
    }

    public void SetOauthToken(String string) {
        OauthToken = string;
        PreferenceSetString("OauthToken", OauthToken);
    }

    public void SetOauthTokenSecret(String string) {
        OauthTokenSecret = string;
        PreferenceSetString("OauthTokenSecret", OauthTokenSecret);
    }

    public void SetUserId(String string) {
        UserId = string;
    }

    public void SetContext(Context ctx) {
        mContext = ctx;
    }

    public String GetTempOauthToken() {
        return tempOauthToken;
    }

    public String GetTempOauthTokenSecret() {
        return tempOauthTokenSecret;
    }

    public String GetOauthToken() {
        String str = PreferenceGetString("OauthToken");
        if (str != null)
            return str;
        return OauthToken;
    }

    public String GetOauthTokenSecret() {
        String str = PreferenceGetString("OauthTokenSecret");
        if (str != null)
            return str;
        return OauthTokenSecret;
    }

    public String GetUserId() {
        return UserId;
    }

    public void PreferenceSetString(String key, String value) {
        SharedPreferences sp = mContext.getSharedPreferences("SP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);

        editor.apply();
    }

    public String PreferenceGetString(String key) {
        SharedPreferences sp = mContext.getSharedPreferences("SP", Context.MODE_PRIVATE);

        return sp.getString(key, "");

    }

}

