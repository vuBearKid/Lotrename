package link.vu1984.lotrename;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 设置类
 */
public class VUSetting {
    final public static String TAG = "VUSetting";
    final public static String SETTING_FILENAME = "setting";
    final public static String HISTORY_DIR = "historyDir";

    private static VUSetting mSelf;
    private Context mContext;
    private VUSettingDBHelper mDBHelper;
    private static HashMap<String,String> mSetting = new HashMap<>();
    static {//框架自带属性
        mSetting.put(HISTORY_DIR,null);
    }

    private VUSetting(Context context){
        mContext = context;
        mDBHelper = new VUSettingDBHelper(mContext);
        loadSetting();
    }

    public static VUSetting getInstance(Context context){
        if(mSelf != null){
            return mSelf;
        }else{
            mSelf = new VUSetting(context);
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
        SQLiteDatabase db =  mDBHelper.getWritableDatabase();

        String sql = "REPLACE INTO "+ VUSettingDBHelper.TABLE_NAME+" (key, value) VALUES(?, ?)";
        SQLiteStatement statement = db.compileStatement(sql);

        db.beginTransaction();
        for (Map.Entry<String, String> entry : mSetting.entrySet()) {
            //VULog.e(TAG,entry.getKey()+":"+entry.getValue());
            statement.bindString(1,entry.getKey());
            if(entry.getValue() == null){
                statement.bindNull(2);
            }else{
                statement.bindString(2,entry.getValue());
            }
            statement.execute();
            statement.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void loadSetting(){
        SQLiteDatabase db =  mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ VUSettingDBHelper.TABLE_NAME, null);
        if (cursor.moveToFirst()){
            do {
                mSetting.put(
                        cursor.getString(cursor.getColumnIndex("key")),
                        cursor.getString(cursor.getColumnIndex("value"))
                );
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }


    //在4.4环境下，由于非正常退出时，读取不到已经存在的SharedPreferences，导致设置丢失，故先放一边
    public void saveSettingPref(){
        SharedPreferences.Editor editor = mContext.getSharedPreferences(SETTING_FILENAME,Context.MODE_PRIVATE  ).edit();
        Iterator<HashMap.Entry<String, String>> iterator = mSetting.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<String, String> entry = iterator.next();
            editor.putString(entry.getKey(),entry.getValue());
        }
        editor.commit();
    }
    //在4.4环境下，由于非正常退出时，读取不到已经存在的SharedPreferences，导致设置丢失，故先放一边
    public void loadSettingPref(){
        SharedPreferences preferences = mContext.getSharedPreferences(SETTING_FILENAME,Context.MODE_PRIVATE);
        Iterator<HashMap.Entry<String, String>> iterator = mSetting.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<String, String> entry = iterator.next();
            mSetting.put(entry.getKey(),preferences.getString(entry.getValue(),null));
        }
    }

}
