package aga.songmaya.support.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class PermissionWrapper {

    private static final int REQUEST_PERMISSIONS_CODE = 101;

    private static String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private Activity activity;

    public PermissionWrapper(Activity activity) {
        this.activity = activity;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void exec() {
        if (checkPermission(activity)) {

        } else {
            activity.requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
        }
    }


    private boolean checkPermission(Context context) {
        boolean granted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                granted = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
                if (!granted) {
                    break;
                }
            }
        }
        return granted;
    }

}
