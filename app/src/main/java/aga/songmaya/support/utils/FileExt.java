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
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.appspot.apprtc.CallFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class FileExt {
    public static final String TAG = FileExt.class.getSimpleName();

    public static final int REQUEST_CODE = 100;
    private Activity activity;
    public static final String FLAG = "[文件]";
    private static String receiveFilename;
    private static long receiveFileSize;
    private static long receiveFileCount = 0;
    private static int fileReceiveState = 0;

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
        receiveFileCount = 0;
        fileReceiveState = 1;
        String[] str = msg.split(":");
        if (str.length >= 1) {
            receiveFilename = str[1];
        }
        if (str.length >= 2) {
            receiveFileSize = Long.parseLong(str[2]);
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
/*                    byte[] content = org.apache.commons.io.FileUtils.readFileToByteArray(new File(path));
                    callEvents.onChatSend(content);*/

                    FileInputStream stream = new FileInputStream(file);
                    byte[] buffer = new byte[16 * 1024];
                    while (stream.read(buffer) != -1) {
                        callEvents.onChatSend(buffer);
                    }
                    IOUtils.close(stream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void save(final Activity activity, byte[] data) {
        try {
            String filepath = FileUtils.getDiskCacheDir(activity) + File.separator + System.currentTimeMillis() + ".unknown";
            if (!TextUtils.isEmpty(receiveFilename)) {
                filepath = FileUtils.getDiskCacheDir(activity) + File.separator + receiveFilename;
            }
            Log.d(TAG, "save filepath "+filepath);
            File file = new File(filepath);

            if (fileReceiveState == 1) {
                Files.write(Paths.get(filepath), data);
            } else {
                Files.write(Paths.get(filepath), data, StandardOpenOption.APPEND);
            }
            fileReceiveState = 2;
            receiveFileCount += data.length;
            Log.d(TAG, String.format("receive file size=%s, count=%s", receiveFileSize, receiveFileCount));


            if (receiveFileCount >= receiveFileSize) {
                receiveFileCount = 0;
                fileReceiveState = 0;
                if (FileUtils.isImage(filepath)) {
                    saveImage2Album(activity, file);
                } else if (FileUtils.isVideo(filepath)) {
                    saveVideo2Album(activity, file);
                }

                Log.e(TAG, "save path:" + filepath);
                activity.runOnUiThread(() -> {
                    new AlertDialog.Builder(activity)
                            .setTitle("查看接收到的文件")
                            .setNegativeButton("查看", ((dialog, which) -> {
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("*/*");//无类型限制
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                activity.startActivity(intent);
                            }))
                            .setPositiveButton("取消", null)
                            .show();
                });
            }
        } catch (Exception e) {
            receiveFileCount = 0;
            fileReceiveState = 0;
            Log.e(TAG, "save:", e);
        }
//        try (OutputStream out = new FileOutputStream(filepath)) {
//            out.write(data);
//            out.close();
//            saveImage2Album(activity, new File(filepath));
//            Log.e(TAG, "save path:" + filepath);
//            activity.runOnUiThread(() -> {
//                new AlertDialog.Builder(activity)
//                        .setTitle("文件已保存到相册")
//                        .setNegativeButton("查看", ((dialog, which) -> {
//                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                            intent.setType("*/*");//无类型限制
//                            intent.addCategory(Intent.CATEGORY_OPENABLE);
//                            activity.startActivity(intent);
//                        }))
//                        .show();
//            });
//        } catch (Exception e) {
//            Log.e(TAG, "save fail", e);
//        }
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
