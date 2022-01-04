package aga.songmaya.support.utils;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;


public class FileExt {
    public static final String TAG = FileExt.class.getSimpleName();

    public static final int REQUEST_CODE = 100;
    private Activity activity;

    public FileExt(Activity activity) {
        this.activity = activity;
    }

    public void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    public void pickFile(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        fragment.startActivityForResult(intent, REQUEST_CODE);
    }


    public byte[] onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            try {
                Uri uri = data.getData();
                String path = FileUtils.getPath(activity, uri);
                byte[] content = org.apache.commons.io.FileUtils.readFileToByteArray(new File(path));
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void save(Activity activity, byte[] data) {
        String filename = activity.getCacheDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";

        try (OutputStream out = new FileOutputStream(filename)) {
            out.write(data);
            out.close();
            Log.e(TAG, "save path:" + filename);
        } catch (Exception e) {
            Log.e(TAG, "save fail", e);
        }
    }


}
