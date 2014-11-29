package com.example.link.photo;

import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by link on 11/14/14.
 */
public class UploadThread extends Thread {
    private static final String TAG = "UploadThread";
    private DataSave mData;
    private String oauthToken;
    private String oauthTokenSecret;
    private String uploadLocate = "http://api-content.dfs.kuaipan.cn/1/fileops/upload_locate";
    private String createFolder = "http://openapi.kuaipan.cn/1/fileops/create_folder";
    private Tools mTools;
    private String uploadFilePath;

    public UploadThread(DataSave data, String filePath) {
        mData = data;
        oauthToken = mData.GetOauthToken();
        Log.d(TAG, "OauthToken: " + oauthToken);
        oauthTokenSecret = mData.GetOauthTokenSecret();
        Log.d(TAG, "OauthTokenSecret: " + oauthTokenSecret);
        mTools = new Tools();
        uploadFilePath = filePath;
    }

    @Override
    public void run() {
        super.run();
        File uploadFile = new File(uploadFilePath);
        String fileName = uploadFile.getName();
        String filePath = uploadFile.getAbsolutePath();
        //Part 1: Get upload_locate url
        oauthTokenSecret = mData.customSecretKey + mData.GetOauthTokenSecret();

        String oauthPara = mTools.GetOauthPara(oauthToken);
        String baseString = mTools.BaseString("GET&", uploadLocate, oauthPara);
        String urlSignature = mTools.SignatureUrl(baseString, oauthTokenSecret);
        String finalUrl = uploadLocate + "?" + oauthPara + "&" + "oauth_signature=" + urlSignature;

        String response = mTools.loadUrl(finalUrl);
        String uploadUrl = mTools.handleResponse(response, "url") + "1/fileops/upload_file";
        Log.i(TAG, "Get uploadUrl: " + uploadUrl);

        //Part 2: Upload file
        oauthPara = mTools.GetOauthPara(oauthToken);
        String extendPara = UploadExtendPara(fileName);

        baseString = mTools.BaseString("POST&", uploadUrl, oauthPara + extendPara);
        urlSignature = mTools.SignatureUrl(baseString, oauthTokenSecret);
        finalUrl = uploadUrl + "?" + oauthPara + "&oauth_signature=" + urlSignature + extendPara;
        Log.i(TAG, "upload file URL: " + finalUrl);

        filePost(finalUrl, uploadFilePath);
    }

    private String UploadExtendPara(String filePath) {
        return "&overwrite=True" + "&path=" + filePath + "&root=kuaipan";
    }

    private void filePost(String url, String file) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        String boundary = "-----------------" + System.currentTimeMillis();
        httppost.setHeader("Content-type", "multipart/form-data; boundary=" + boundary);

        File imageFile = new File(file);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.setBoundary(boundary);

        builder.addTextBody("name", "file");
        builder.addTextBody("filename", "ok.jpg");
        builder.addBinaryBody("file", imageFile);

        HttpEntity entity = builder.build();

        httppost.setEntity(entity);

        try {
            HttpResponse httpResponse = httpclient.execute(httppost);

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.d(TAG, "Upload complete!");
            }

            String response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
            Log.d(TAG, "got response: " + response);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }

    public static byte[] getBytesFromFile(File file) {
        byte[] ret = null;
        try {
            if (file == null) {
                // log.error("helper:the file is null!");
                return null;
            }
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) != -1) {
                out.write(b, 0, n);
            }
            in.close();
            out.close();
            ret = out.toByteArray();
        } catch (IOException e) {
            // log.error("helper:get bytes from file process error!");
            e.printStackTrace();
        }
        return ret;
    }
}