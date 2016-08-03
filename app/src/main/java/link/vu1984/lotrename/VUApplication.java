package link.vu1984.lotrename;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * 这个类会在app打开时就初始化，可以在这里定义些全局变量，需要在AndroidManifest.xml注册
 * <application
 * android:name="full.package.name.VUApplication">
 * ......
 * ......
 * </application>
 */
public class VUApplication extends Application {
    public static final String TAG = "VUApplication";
    private static Context mContext;
    private static AssetManager mAssetManager;
    private static Charset mCharset =Charset.defaultCharset();

    private static final String VERSION = "1";
    public static final String DIR_SEPARATOR = File.separator;
    public static final String BASE_DIR = "vuapp";//all your app should be placed in this DIR

    public static String appDir = null; //specify app's DIR,should be named with packageName
    public static String extStoragePath = null; //shared data to place in
    public static String PrivateExtStoragePath = null; //private data to place in
    public static String tempDir = "temp";
    public static String tempDirPath = null; //temp shared data to place in
    //public static WindowManager windowManager;
    //public static Display display;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //windowManager = (WindowManager)context.getSystemService(context.WINDOW_SERVICE);
        //display = windowManager.getDefaultDisplay();


        appDir = mContext.getPackageName();
        extStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        tempDirPath = extStoragePath + DIR_SEPARATOR + BASE_DIR + DIR_SEPARATOR + appDir + DIR_SEPARATOR + tempDir;
        PrivateExtStoragePath = mContext.getExternalFilesDir(null).getPath();

    }


    public static Context getContext() {
        if (mContext != null) {
            return mContext;
        } else {
            return null;
        }
    }

    public static AssetManager getAssetManager(){
        if(mAssetManager == null) mAssetManager = mContext.getAssets();
        return mAssetManager;
    }

    public static void setCharset(String charsetName){
        mCharset = Charset.forName(charsetName);
    }

    public static void openFile(File file){
        //VULog.e(TAG,VUFile.getMimeType(file.getName()));
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),VUFile.getMimeType(file.getName()));
        mContext.startActivity(intent);
    }

    /**
     * 注意这文件的换行符会被替换
     *
     * @param filePath 相对assets的路径名
     * @return
     */
    public static String getAssetsTxt(String filePath){
        StringBuilder returnStr= new StringBuilder();
        getAssetManager();
        try {
            InputStream is = mAssetManager.open(filePath);
            InputStreamReader isr = new InputStreamReader(is,mCharset);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null){
                returnStr.append(line);
                returnStr.append('\n');
            }
            br.close();
            isr.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        returnStr.deleteCharAt(returnStr.length()-1);//删除最后一个换行
        return returnStr.toString();
    }

    public static  Point getWindowSize() {
        //DisplayMetrics metrics = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(metrics);//用上面这个好像更好一些
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);//getRealSize会准确些，getSize会减去ACTIONBAR后的尺寸。
        return point;
    }
}
