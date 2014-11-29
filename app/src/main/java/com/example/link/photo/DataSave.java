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

    public String customSecretKey = "qa3ZaNTBHMsvjvMb&";

    public DataSave() {
        tempOauthToken = "";
        tempOauthTokenSecret = "";
        OauthToken = "";
        OauthTokenSecret = "";
        UserId = "";
    }

    public void SetTempOauthToken(String string) {
        tempOauthToken = string;
    }

    public void SetTempOauthTokenSecret(String string) {
        tempOauthTokenSecret = string;
    }

    public void SetOauthToken(String string) {
        OauthToken = string;
    }

    public void SetOauthTokenSecret(String string) {
        OauthTokenSecret = string;
    }

    public void SetUserId(String string) {
        UserId = string;
    }

    public String GetTempOauthToken() {
        return tempOauthToken;
    }

    public String GetTempOauthTokenSecret() {
        return tempOauthTokenSecret;
    }

    public String GetOauthToken() {
        return OauthToken;
    }

    public String GetOauthTokenSecret() {
        return OauthTokenSecret;
    }

    public String GetUserId() {
        return UserId;
    }
}

