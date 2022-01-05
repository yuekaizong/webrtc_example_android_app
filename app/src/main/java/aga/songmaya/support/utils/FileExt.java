package aga.songmaya.support.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.appspot.apprtc.CallFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class FileExt {
    public static final String TAG = FileExt.class.getSimpleName();

    public static final int REQUEST_CODE = 100;
    private Activity activity;
    public static final String FLAG = "[文件]";
    private static String receiveFilename;

    public FileExt(Activity activity) {
        this.activity = activity;
    }

    public void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    public static void openImage(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setType("image/*");
        activity.startActivity(intent);
    }

    public void pickFile(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        fragment.startActivityForResult(intent, REQUEST_CODE);
    }


    public static String genFileMsg(String path) {
        File file = new File(path);
        if (!file.exists())
            return null;
        String filename = file.getName();
        long size = file.length();
        return String.format("%s:%s:%s", FLAG, filename, size);
    }

    public static void extractFilename(String msg) {
        if (!msg.startsWith(FLAG)) return;
        String[] str = msg.split(":");
        if (str.length >= 1) {
            receiveFilename = str[0];
        }
    }

    public void onActivityResult(final CallFragment.OnCallEvents callEvents, int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            try {
                Uri uri = data.getData();
                String path = FileUtils.getPath(activity, uri);
                File file = new File(path);
                if (file.exists()) {
                    callEvents.onChatSend(genFileMsg(path));
                    byte[] content = org.apache.commons.io.FileUtils.readFileToByteArray(new File(path));
                    callEvents.onChatSend(content);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void save(final Activity activity, byte[] data) {
        String filename = activity.getCacheDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";

        try (OutputStream out = new FileOutputStream(filename)) {
            out.write(data);
            out.close();
            saveImage2Album(activity, new File(filename));
            Log.e(TAG, "save path:" + filename);
            activity.runOnUiThread(() -> {
                new AlertDialog.Builder(activity)
                        .setTitle("文件已保存到相册")
                        .setNegativeButton("查看", ((dialog, which) -> {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("*/*");//无类型限制
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            activity.startActivity(intent);
                        }))
                        .show();
            });
        } catch (Exception e) {
            Log.e(TAG, "save fail", e);
        }
    }

    public static void saveImage2Album(Context context, File mediaFile) {
        Uri uri = Uri.fromFile(mediaFile);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    mediaFile.getAbsolutePath(), mediaFile.getName(), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveVideo2Album(Context context, File file) {
        ContentResolver localContentResolver = context.getContentResolver();
        ContentValues localContentValues = getVideoContentValues(context, file, System.currentTimeMillis());
        Uri localUri = localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri));

    }

    public static ContentValues getVideoContentValues(Context paramContext, File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(MediaStore.Images.Media.TITLE, paramFile.getName());
        localContentValues.put(MediaStore.Images.Media.DISPLAY_NAME, paramFile.getName());
        localContentValues.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4");
        localContentValues.put(MediaStore.Images.Media.DATE_TAKEN, Long.valueOf(paramLong));
        localContentValues.put(MediaStore.Images.Media.DATE_MODIFIED, Long.valueOf(paramLong));
        localContentValues.put(MediaStore.Images.Media.DATE_ADDED, Long.valueOf(paramLong));
        localContentValues.put(MediaStore.Images.Media.DATA, paramFile.getAbsolutePath());
        localContentValues.put(MediaStore.Images.Media.SIZE, Long.valueOf(paramFile.length()));
        return localContentValues;
    }


}
