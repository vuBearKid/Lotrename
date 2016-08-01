package link.vu1984.lotrename;

import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

/**
 * 与CLASS PickDirActivity 相互依存
 * 写个类的目点是：不想让PickDirActivity太乱
 */
public class RenamePopupWindow {
    private static String TAG = "RenamePopupWindow";
    private PickDirActivity mContext;

    public final static int RENAME_EXTENSION = 0;
    public final static int RENAME_REPLACE = 1;
    public final static int RENAME_PREFIX = 2;
    public PopupWindow renamePopupWindow;
    public int currentRename = RENAME_PREFIX;

    private View renameLayout;
    private Button buttonExtension;
    private Button buttonReplace;
    private Button buttonPrefix;
    private LinearLayout bodyExtension;
    private LinearLayout bodyReplace;
    private LinearLayout bodyPrefix;
    private LinearLayout renameDialog;
    private TextView dialogTitle;
    private EditText inputExtension;
    private EditText inputReplaceFrom;
    private EditText inputReplaceTo;
    private CheckBox replaceIsRegex;
    private EditText inputPrefix;
    private EditText inputPrefixSequence;
    private CheckBox prefixAutoZero;



    public RenamePopupWindow(PickDirActivity context, final int currentRenameFlag){
        mContext = context;
        currentRename = currentRenameFlag;
        renameLayout = mContext.getLayoutInflater().inflate(R.layout.rename_dialog, null);
        renameDialog = (LinearLayout) renameLayout.findViewById(R.id.rename_dialog);
        buttonExtension = (Button) renameLayout.findViewById(R.id.rename_extension);
        buttonReplace = (Button) renameLayout.findViewById(R.id.rename_replace);
        buttonPrefix = (Button) renameLayout.findViewById(R.id.rename_prefix);
        bodyExtension = (LinearLayout) renameLayout.findViewById(R.id.rename_extension_body);
        bodyReplace = (LinearLayout) renameLayout.findViewById(R.id.rename_replace_body);
        bodyPrefix = (LinearLayout) renameLayout.findViewById(R.id.rename_prefix_body);
        dialogTitle = (TextView) renameLayout.findViewById(R.id.rename_dialog_title);

        inputExtension = (EditText) renameLayout.findViewById(R.id.rename_extension_input);
        inputReplaceFrom = (EditText) renameLayout.findViewById(R.id.rename_replace_from);
        inputReplaceTo  = (EditText) renameLayout.findViewById(R.id.rename_replace_to);
        replaceIsRegex = (CheckBox) renameLayout.findViewById(R.id.rename_replace_regex);
        inputPrefix = (EditText) renameLayout.findViewById(R.id.rename_prefix_input);
        inputPrefixSequence = (EditText) renameLayout.findViewById(R.id.rename_prefix_sequence);
        prefixAutoZero = (CheckBox) renameLayout.findViewById(R.id.rename_prefix_auto_zero);


        buttonExtension.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentRename == RENAME_EXTENSION){//buttonExtension变确定按键
                    showAlertDialog(!(VUtil.isStringEmpty(mContext.renameExtension,true)));
                    return;
                }
                currentRename = RENAME_EXTENSION;
                mContext.currentRename = currentRename;//更新activity中的currentRename，保存状态
                initPopupBody();
                mContext.cleanPreview();
            }
        });
        buttonReplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentRename == RENAME_REPLACE){//变确定按键
                    showAlertDialog(!(VUtil.isStringEmpty(mContext.renameReplaceTo, true) && VUtil.isStringEmpty(mContext.renameReplaceFrom, true)));
                    return;
                }
                currentRename = RENAME_REPLACE;
                mContext.currentRename = currentRename;
                initPopupBody();
                mContext.cleanPreview();
            }
        });
        buttonPrefix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentRename == RENAME_PREFIX){//变确定按键
                    showAlertDialog(!(VUtil.isStringEmpty(mContext.renamePrefix,true)));
                    return;
                }
                currentRename = RENAME_PREFIX;
                mContext.currentRename = currentRename;
                initPopupBody();
                mContext.cleanPreview();

            }
        });
        inputExtension.setFilters(new InputFilter[]{new VUinputFilter.NoNewLinerFilter(),new VUinputFilter.RegexFilter("[^a-zA-Z0-9]+")});
        inputExtension.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //VULog.e(TAG,"1beforeTextChanged CharSequence:"+s+" start:"+start+" count:"+count+" after:"+after);
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //VULog.e(TAG,"2onTextChanged CharSequence:"+s+" start:"+start+" count:"+count+" before:"+before);
            }
            @Override
            public void afterTextChanged(Editable s) {
                //VULog.e(TAG,"3afterTextChanged Editable:"+s.toString());
                mContext.renameExtension  = s.toString();

            }
        });

        inputReplaceFrom.setFilters(new InputFilter[]{new VUinputFilter.NoNewLinerFilter()});
        inputReplaceTo.setFilters(new InputFilter[]{new VUinputFilter.NoNewLinerFilter(),new VUinputFilter.RegexFilter("[\\\\\\?/:*|<>]+")});
        inputReplaceFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                mContext.renameReplaceFrom = s.toString();
            }
        });
        inputReplaceTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                mContext.renameReplaceTo = s.toString();
            }
        });
        replaceIsRegex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mContext.renameReplaceIsRegex = isChecked;
            }
        });



        inputPrefix.setFilters(new InputFilter[]{new VUinputFilter.NoNewLinerFilter(),new VUinputFilter.RegexFilter("[\\\\\\?/:*|<>]+")});
        inputPrefix.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                mContext.renamePrefix  = s.toString();
                //VULog.e(TAG,"afterTextChanged:"+mContext.renamePrefix);
            }
        });
        inputPrefixSequence.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                mContext.renamePrefixSequence  = s.toString();
                //VULog.e(TAG,"afterTextChanged:"+mContext.renamePrefix);
            }
        });
        prefixAutoZero.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mContext.renamePrefixAutoZero = isChecked;
            }
        });



        Point point = mContext.getWindowSize();
        int tempW = (int) (point.x * 0.7);
        int tempH = (int) (point.y * 0.5);

        renamePopupWindow =  new PopupWindow(renameLayout,tempW,tempH,true);

        //setBackgroundDrawable，setOutsideTouchable，这样点击window外或返回键，窗体才会消失
        renamePopupWindow.setBackgroundDrawable(new Drawable() {
            @Override
            public void draw(Canvas canvas) {

            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return 0;
            }
        });
        renamePopupWindow.setOutsideTouchable(true);
        renamePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //Toast.makeText(mContext,"renamePopupWindow Dismiss",Toast.LENGTH_SHORT).show();
                if (isManualDismiss) return;//当调用RenamePopupWindow.dismiss()不再继续

                //mContext.hideInputMethod(new EditText[]{inputExtension,inputPrefix,inputReplaceFrom,inputReplaceTo});
                //根据currentRename判断，应预览那个
                mContext.renamePreview(currentRename);

            }
        });
        initPopupBody();
    }


    public void showWindow(View view){
        renamePopupWindow.showAsDropDown(view);
    }

    private boolean isManualDismiss = false;
    public void dismiss(){
        isManualDismiss = true;
        renamePopupWindow.dismiss();
        isManualDismiss = false;
    }

    public void initPopupBody(){
        switch (currentRename) {
            case RENAME_EXTENSION:
                dialogTitle.setText(mContext.getString(R.string.file_name_extension_title));
                buttonExtension.setText(mContext.getString(R.string.file_name_submit));
                buttonReplace.setText(mContext.getString(R.string.file_name_replace));
                buttonPrefix.setText(mContext.getString(R.string.file_name_prefix));
                bodyExtension.setVisibility(View.VISIBLE);
                bodyReplace.setVisibility(View.GONE);
                bodyPrefix.setVisibility(View.GONE);
                //为了不把EditText的HINT去掉，加IF
                if (mContext.renameExtension != null) inputExtension.setText(mContext.renameExtension);

                break;
            case RENAME_REPLACE:
                dialogTitle.setText(mContext.getString(R.string.file_name_replace_title));
                buttonExtension.setText(mContext.getString(R.string.file_name_extension));
                buttonReplace.setText(mContext.getString(R.string.file_name_submit));
                buttonPrefix.setText(mContext.getString(R.string.file_name_prefix));
                bodyExtension.setVisibility(View.GONE);
                bodyReplace.setVisibility(View.VISIBLE);
                bodyPrefix.setVisibility(View.GONE);
                if (mContext.renameReplaceFrom != null) inputReplaceFrom.setText(mContext.renameReplaceFrom);
                if (mContext.renameReplaceTo != null) inputReplaceTo.setText(mContext.renameReplaceTo);
                replaceIsRegex.setChecked(mContext.renameReplaceIsRegex);
                break;
            case RENAME_PREFIX:
                dialogTitle.setText(mContext.getString(R.string.file_name_prefix_title));
                buttonExtension.setText(mContext.getString(R.string.file_name_extension));
                buttonReplace.setText(mContext.getString(R.string.file_name_replace));
                buttonPrefix.setText(mContext.getString(R.string.file_name_submit));
                bodyExtension.setVisibility(View.GONE);
                bodyReplace.setVisibility(View.GONE);
                bodyPrefix.setVisibility(View.VISIBLE);
                //因为一开始就有例子在那里，所以要初始化
                if (mContext.renamePrefix != null) {
                    inputPrefix.setText(mContext.renamePrefix);
                } else {
                    mContext.renamePrefix = mContext.getString(R.string.file_name_prefix_hint);
                }
                if (mContext.renamePrefixSequence != null) inputPrefixSequence.setText(mContext.renamePrefixSequence);
                prefixAutoZero.setChecked(mContext.renamePrefixAutoZero);
                break;
            default:
        }

    }

    /**
     *
     * @param toShow 如果为true则显示dialog,false反之
     */
    public void showAlertDialog(boolean toShow){
        if (!toShow) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.VUAlertDialog);
        builder.setTitle(mContext.getString(R.string.rename_warning_title));
        builder.setMessage(mContext.getString(R.string.rename_warning_info));
        builder.setPositiveButton(mContext.getString(R.string.rename_sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mContext.renameFile(currentRename);
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.rename_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }
}
