package com.example.cryptomoney;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class FileUtil {

    static public File getFileByUri(Activity activity, Uri uri) {
        File file = null;
        if (uri != null) {
            if (uri.getScheme() != null && uri.getScheme().toString().compareTo("content") == 0) {
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor musicCursor = activity.managedQuery(uri, proj, null, null, null);
                if (musicCursor != null) {
                    int actual_music_column_index = musicCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    musicCursor.moveToFirst();
                    String musicPath = musicCursor.getString(actual_music_column_index);
                    if (musicPath != null) {
                        file = new File(musicPath);
                    }
                }
            } else if (uri.getScheme() != null && uri.getScheme().compareTo("file") == 0) {
                try {
                    file = new File(new URI(uri.toString()));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    file = new File(uri.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    /**
     * 文件路径获取bitmap
     *
     * @param filePath 文件路径 + 文件名(全名)
     * @return
     */
    public static Bitmap getBitmap(String filePath) {
        if (filePath == null || filePath.length() == 0)
            return null;
        File file = new File(filePath);
        if (file == null || file.exists() == false)
            return null;
        Bitmap bitmap = null;
        InputStream is = null;
        try {
            is = new FileInputStream(file.getAbsolutePath());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(is, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

}
