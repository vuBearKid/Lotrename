package link.vu1984.lotrename;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * 在自己的APP里，全部继承这个activity，以便统一管理
 */
public class VUActivity extends AppCompatActivity {


    public Point getWindowSize() {
        //DisplayMetrics metrics = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(metrics);//用上面这个好像更好一些
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);//getRealSize会准确些，getSize会减去ACTIONBAR后的尺寸。
        return point;
    }

    public View getContentView(){
        return getWindow().getDecorView().findViewById(android.R.id.content);
    }

}
