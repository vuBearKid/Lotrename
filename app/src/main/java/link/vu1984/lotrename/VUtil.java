package link.vu1984.lotrename;

import android.content.Context;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *这个文件用来写自己的Util类
 */
public class VUtil {
    private VUtil() {
    }

    //空白字符也算空
    public static boolean isStringEmpty(CharSequence str, boolean WhitespaceToo) {
        if (str == null || str.length()==0) {
            return true;
        }
        if (WhitespaceToo) {
            Pattern p = Pattern.compile("^\\s+$");
            Matcher m = p.matcher(str);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 分解文件名为扩展名和文件名
     *
     * @param filename 文件全名
     * @return String[0]文件名,String[1]扩展名包括"."（没有时为空字符串）
     */
    public static String[] getFileNameAndExtension(String filename){
        String[] file = new String[]{"", ""};
        filename = filename.trim();
        int stringMaxIndex = filename.length()-1;

        if(stringMaxIndex < 0) return file;//filename为空字符串

        char dot = '.';
        int dotIndex = filename.lastIndexOf(dot);
        //VULog.e("caocao",dotIndex+"dotIndex");
        if(dotIndex < 0 || dotIndex == 0 || dotIndex == stringMaxIndex){//没有扩展名 ".ex"  "ext." ".ext." "ext"
            file[0] = filename;
            return file;
        }
        if(dotIndex > 0 && dotIndex < stringMaxIndex){
            file[0] = filename.substring(0,dotIndex);//不包括dotIndex的字符
            file[1] = filename.substring(dotIndex);//包括dotIndex的字符
        }
        return file;
    }
    /* getFileNameAndExtension unit test
    String[] temp = VUtil.getFileNameAndExtension(".abc");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension("abc");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension(".abc.bcd.mp4");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension("你好.mp3");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension("你 不好.");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension(".");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension("..");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension("a");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension(".b");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    */


    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }


    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


}


/**
 *为了方便app"上市"，不显示LOG，LEVEL=NOTHING
 */
class VULog {
    private VULog() {
    }

    //the original level of android.util.Log
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;

    //shutdown the log
    public static final int NOTHING = 6;

    //all below the LEVEL message will be showed,need to chang it in the code manually
    public static final int LEVEL = ERROR;


    public static void v(String tag, String msg) {
        if (LEVEL <= VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (LEVEL <= DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (LEVEL <= INFO) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (LEVEL <= WARN) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (LEVEL <= ERROR) {
            Log.e(tag, msg);
        }
    }

    /*public static void codeChangedAlert(String tag, String msg){
        if (LEVEL <= ERROR) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Log.e(tag, dateFormat.format(new Date(System.currentTimeMillis()))+"@"+msg);
        }
    }*/
}


class VUFile {

    private static HashMap<String, String> mimeType = new HashMap<String, String>(64,0.25f);

    private VUFile(){}
    static {
        mimeType.put(".3gp","video/3gpp");
        mimeType.put(".apk", "application/vnd.android.package-archive");
        mimeType.put(".asf", "video/x-ms-asf");
        mimeType.put(".avi", "video/x-msvideo");
        mimeType.put(".bin", "application/octet-stream");
        mimeType.put(".bmp", "image/bmp");
        mimeType.put(".c", "text/plain");
        mimeType.put(".class", "application/octet-stream");
        mimeType.put(".conf", "text/plain");
        mimeType.put(".cpp", "text/plain");
        mimeType.put(".doc", "application/msword");
        mimeType.put(".exe", "application/octet-stream");
        mimeType.put(".gif", "image/gif");
        mimeType.put(".gtar", "application/x-gtar");
        mimeType.put(".gz", "application/x-gzip");
        mimeType.put(".h", "text/plain");
        mimeType.put(".htm", "text/html");
        mimeType.put(".html", "text/html");
        mimeType.put(".jar", "application/java-archive");
        mimeType.put(".java", "text/plain");
        mimeType.put(".jpeg", "image/jpeg");
        mimeType.put(".jpg", "image/jpeg");
        mimeType.put(".js", "application/x-javascript");
        mimeType.put(".log", "text/plain");
        mimeType.put(".m3u", "audio/x-mpegurl");
        mimeType.put(".m4a", "audio/mp4a-latm");
        mimeType.put(".m4b", "audio/mp4a-latm");
        mimeType.put(".m4p", "audio/mp4a-latm");
        mimeType.put(".m4u", "video/vnd.mpegurl");
        mimeType.put(".m4v", "video/x-m4v");
        mimeType.put(".mov", "video/quicktime");
        mimeType.put(".mp2", "audio/x-mpeg");
        mimeType.put(".mp3", "audio/x-mpeg");
        mimeType.put(".mp4", "video/mp4");
        mimeType.put(".mpc", "application/vnd.mpohun.certificate");
        mimeType.put(".mpe", "video/mpeg");
        mimeType.put(".mpeg", "video/mpeg");
        mimeType.put(".mpg", "video/mpeg");
        mimeType.put(".mpg4", "video/mp4");
        mimeType.put(".mpga", "audio/mpeg");
        mimeType.put(".msg", "application/vnd.ms-outlook");
        mimeType.put(".ogg", "audio/ogg");
        mimeType.put(".pdf", "application/pdf");
        mimeType.put(".png", "image/png");
        mimeType.put(".pps", "application/vnd.ms-powerpoint");
        mimeType.put(".ppt", "application/vnd.ms-powerpoint");
        mimeType.put(".prop", "text/plain");
        mimeType.put(".rar", "application/x-rar-compressed");
        mimeType.put(".rc", "text/plain");
        mimeType.put(".rmvb", "audio/x-pn-realaudio");
        mimeType.put(".rtf", "application/rtf");
        mimeType.put(".sh", "text/plain");
        mimeType.put(".tar", "application/x-tar");
        mimeType.put(".tgz", "application/x-compressed");
        mimeType.put(".txt", "text/plain");
        mimeType.put(".wav", "audio/x-wav");
        mimeType.put(".wma", "audio/x-ms-wma");
        mimeType.put(".wmv", "audio/x-ms-wmv");
        mimeType.put(".wps", "application/vnd.ms-works");
        //mimeType.put(".xml", "text/xml");
        mimeType.put(".xml", "text/plain");
        mimeType.put(".z", "application/x-compress");
        mimeType.put(".zip", "application/zip");
        mimeType.put("", "*/*");
    }

    public static String getMimeType(String filename){
        String tempMimeType;
        String[] file = getFileNameAndExtension(filename);
        tempMimeType = mimeType.get(file[1].toLowerCase());
        if(tempMimeType == null){
            tempMimeType = "*/*";
        }
        return tempMimeType;

    }

    /**
     * 分解文件名为扩展名和文件名
     *
     * @param filename 文件全名
     * @return String[0]文件名,String[1]扩展名包括"."（没有时为空字符串）
     */
    public static String[] getFileNameAndExtension(String filename){
        String[] file = new String[]{"", ""};
        filename = filename.trim();
        int stringMaxIndex = filename.length()-1;

        if(stringMaxIndex < 0) return file;//filename为空字符串

        char dot = '.';
        int dotIndex = filename.lastIndexOf(dot);
        //VULog.e("caocao",dotIndex+"dotIndex");
        if(dotIndex < 0 || dotIndex == 0 || dotIndex == stringMaxIndex){//没有扩展名 ".ex"  "ext." ".ext." "ext"
            file[0] = filename;
            return file;
        }
        if(dotIndex > 0 && dotIndex < stringMaxIndex){
            file[0] = filename.substring(0,dotIndex);//不包括dotIndex的字符
            file[1] = filename.substring(dotIndex);//包括dotIndex的字符
        }
        return file;
    }
    /* getFileNameAndExtension unit test
    String[] temp = VUtil.getFileNameAndExtension(".abc");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension("abc");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension(".abc.bcd.mp4");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension("你好.mp3");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension("你 不好.");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension(".");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension("..");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension("a");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    temp = VUtil.getFileNameAndExtension(".b");
    VULog.e(TAG,temp[0]+"|"+temp[1]);
    */
}
