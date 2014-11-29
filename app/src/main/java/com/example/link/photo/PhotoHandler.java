package com.example.link.photo;

/**
 * Created by link on 11/10/14.
 */

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoHandler implements PictureCallback {
    private static final String TAG = "PhotoHandler";
    private DataSave mData;

    private final int MSG_UPLOAD_PICTURE = 10003;

    public PhotoHandler(String token, String secret) {
        mData = new DataSave();
        mData.SetOauthToken(token);
        mData.SetOauthTokenSecret(secret);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "Picture_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);
        Log.d(TAG, "filename is " + filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        UploadThread uploadThread = new UploadThread(mData, filename);
        uploadThread.start();
    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "ServiceCamera");
    }
}