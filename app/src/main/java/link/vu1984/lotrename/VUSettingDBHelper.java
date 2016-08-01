package link.vu1984.lotrename;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 只针对app设置的数据库，数据库名和版本都类中定义好了
 */
public class VUSettingDBHelper extends SQLiteOpenHelper {
    private static final String TAG  = "VUSettingDBHelper";

    /**
     * 记录过去用过的版本：
     * version: 1 ,
     */
    private static final int DB_VERSION = 1;

    private static final String DB_NAME  = "Settings.db";
    public static final String TABLE_NAME  = "settings";
    private static final String TABLE_SETTINGS_SQL  =
            "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ( " +
            "key CHAR(50) PRIMARY KEY, " +
            "value TEXT );";

    private Context mContext;


    public VUSettingDBHelper(Context context) {
        //super(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
        super(context, DB_NAME, null, DB_VERSION);//数据库版本类里定义，用的地方不需要考虑
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        switch (DB_VERSION){
            case 1:
                db.execSQL(TABLE_SETTINGS_SQL);
                break;
            case 2:
                break;
        }

        VULog.i(TAG,"DB onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion){
            case 1:
                if(newVersion == 1) break;
                //升级到version 2的代码写在下面
            case 2:
                if(newVersion == 2) break;
            case 3://最后这个case应该等于DB_VERSION，并不作任何操作。
                if(newVersion == 3) break;
            default:
        }
    }


}
