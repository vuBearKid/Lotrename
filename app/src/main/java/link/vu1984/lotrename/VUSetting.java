package link.vu1984.lotrename;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Administrator on 2016/7/31.
 */
public class VUSetting {
    final public static String SETTING_FILENAME = "setting";
    final public static String HISTORY_DIR = "historyDir";

    private static VUSetting mSelf;
    private Context mContext;
    private static HashMap<String,String> mSetting = new HashMap<>();
    static {//框架自带属性
        mSetting.put(HISTORY_DIR,null);
    }

    private VUSetting(Context context){
        mContext = context;
    }

    public static VUSetting getInstance(Context context){
        if(mSelf != null){
            return mSelf;
        }else{
            mSelf = new VUSetting(context);
            mSelf.loadSetting();
            return mSelf;
        }
    }

    public void  setSetting(String key, String value){
        mSetting.put(key,value);
    }

    public String getSetting(String key){
        return mSetting.get(key);
    }

    public void saveSetting(){
        SharedPreferences.Editor editor = mContext.getSharedPreferences(SETTING_FILENAME,Context.MODE_PRIVATE).edit();
        Iterator<HashMap.Entry<String, String>> iterator = mSetting.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<String, String> entry = iterator.next();
            editor.putString(entry.getKey(),entry.getValue());
        }
        editor.commit();
    }

    public void loadSetting(){
        SharedPreferences preferences = mContext.getSharedPreferences(SETTING_FILENAME,Context.MODE_PRIVATE);
        Iterator<HashMap.Entry<String, String>> iterator = mSetting.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<String, String> entry = iterator.next();
            mSetting.put(entry.getKey(),preferences.getString(entry.getValue(),null));
        }
    }



}
