package link.vu1984.lotrename;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Administrator on 2016/6/17.
 */
public class AboutDialogFragment extends DialogFragment {
    private static final String TAG = "AboutDialogFragment";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.about_dialog_fragment_layout,null);
        return dialogView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(),R.style.VUDialogFragment);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        VULog.e(TAG,lp.x+"X"+lp.y);//-1

        WindowManager windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        dialogWindow.setGravity(Gravity.RIGHT | Gravity.TOP);
//        lp.x = 100; // 新位置X坐标
//        lp.y = 100; // 新位置Y坐标
//        lp.width = (int)(point.x * 0.5); // 不起作用
//        lp.height = (int )(point.y * 0.5); // 不起作用
//        lp.alpha = 0.5f; // 透明度
//        dialogWindow.setAttributes(lp);   //mDialog.onWindowAttributesChanged(lp);

        //VULog.e(TAG,lp.width+"X"+lp.height);

        return dialog;
    }
}
