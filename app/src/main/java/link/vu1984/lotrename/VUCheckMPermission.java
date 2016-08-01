package link.vu1984.lotrename;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 *
 */
public class VUCheckMPermission {
    private static final String TAG  = "VUCheckMPermission";
    public static final int PERMISSION_REQUEST_CODE  = 78;

    private Context mContext;

    public VUCheckMPermission(Context context){
        mContext = context;
    }



    public boolean checkPermission(String permission){
        return ContextCompat.checkSelfPermission(mContext,permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkPermissions(String[] permissions){
        for (String permission :permissions){
            if(!checkPermission(permission)){
                return false;
            }
        }
        return true;

    }

    public static boolean checkIsM(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return  true;
        }
        return false;
    }

    public static void requestPermissions(Activity activity,String[] permissions) {
        ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
    }

    public static boolean isPermissionResultGranted(int grantResult){
        return grantResult == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity,String permission){
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,permission);
    }


    public void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
        intent.setData(uri);
        mContext.startActivity(intent);
    }


}
