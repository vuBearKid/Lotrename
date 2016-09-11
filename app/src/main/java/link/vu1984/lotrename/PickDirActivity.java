package link.vu1984.lotrename;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 与CLASS RenamePopupWindow 相互依存
 */

public class PickDirActivity extends VUActivity {
    // TODO: 2016/6/7 很奇诡，当CHECKBOX都没选上后，LIST好像会自己刷新

    private static final String TAG = "PickDirActivity";

    private PickDirActivity mContext;
    private VUSetting mSetting;

    public Toolbar toolbar;
    public Toolbar toolbarBottom;
    private boolean isCheckAll = false;
    public FloatingActionButton renameFab;
    public boolean isRenameFabNeeded = false;

    private SwipeRefreshLayout swipeRefreshLayout;

    private File currentDir;
    private ListView fileListView;
    private FileItemAdapter fileItemAdapter;

    private List<FileItem> filesItems = new ArrayList<FileItem>();

    //批量重命名窗口状态
    private RenamePopupWindow renameWindow;
    public int currentRename = RenamePopupWindow.RENAME_PREFIX;
    public String renameExtension = null;
    public String renameReplaceFrom = null;
    public String renameReplaceTo = null;
    public boolean renameReplaceIsRegex = false;
    public String renamePrefix = null;
    public String renamePrefixSequence = "2";//getString(R.string.file_name_prefix_sequence_default);
    public boolean renamePrefixAutoZero = true;

    //排序
    public static boolean isDesc = false;
    public static final int ORDER_BY_NAME = 0;
    public static final int ORDER_BY_DATE = 1;
    public static final int ORDER_BY_LENGTH = 2;
    public static final int ORDER_BY_EXT = 3;
    public int orderBy = ORDER_BY_NAME;
    private String[] orderArray;
    private int whichOrder = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_dir_activity);
        mContext = this;


        //设置初始化
        mSetting = VUSetting.getInstance(mContext);
        if (mSetting.getSetting(VUSetting.HISTORY_DIR) == null) {
            mSetting.setSetting(VUSetting.HISTORY_DIR, VUApplication.extStoragePath);
        }
        if (mSetting.getSetting("currentRename") != null)
            currentRename =Integer.valueOf(mSetting.getSetting("currentRename"));
        if (mSetting.getSetting("renameExtension") != null)
            renameExtension = mSetting.getSetting("renameExtension");
        if (mSetting.getSetting("renameReplaceFrom") != null)
            renameReplaceFrom = mSetting.getSetting("renameReplaceFrom");
        if(mSetting.getSetting("renameReplaceTo") != null)
            renameReplaceTo = mSetting.getSetting("renameReplaceTo");
        if (mSetting.getSetting("renameReplaceIsRegex") != null)
            renameReplaceIsRegex = Boolean.valueOf(mSetting.getSetting("renameReplaceIsRegex"));
        if (mSetting.getSetting("renamePrefixAutoZero") != null)
            renamePrefixAutoZero = Boolean.valueOf(mSetting.getSetting("renamePrefixAutoZero"));
        if (mSetting.getSetting("renamePrefix") != null)
            renamePrefix = mSetting.getSetting("renamePrefix");
        if (mSetting.getSetting("renamePrefixSequence") != null)
            renamePrefixSequence = mSetting.getSetting("renamePrefixSequence");

        VULog.e(TAG, "onCreate codeChanged ? 33333"); // TODO: 2016/6/12 看代码改变没

        renameWindow = new RenamePopupWindow(mContext, currentRename);

        //toolbar初始化
        toolbar = (Toolbar) findViewById(R.id.toolbar_top);
        toolbar.inflateMenu(R.menu.menu_toolbar);//android.support.v7.view.menu.ListMenuItemView
        //toolbar.setLogo(R.mipmap.ic_launcher_bear);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher_bear);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.order_by:
                        showOrderDialog();
                        break;
                    /*case R.id.refresh_list:
                        refreshList(currentDir);
                        break;*/
                    case R.id.app_help:
                        Intent intent = new Intent(mContext, HelpActivity.class);
                        mContext.startActivity(intent);
                        break;
                    case R.id.app_about:
                        AboutDialogFragment aboutDialog = new AboutDialogFragment();
                        aboutDialog.show(getFragmentManager(), "AboutDialog");

                        break;
                    default:
                        VULog.e(TAG, "out of control");
                }
                return false;
            }
        });


        toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);
        toolbarBottom.inflateMenu(R.menu.menu_toolbar_bottom);
        toolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.check_all:
                        goCheckAll();
                        break;
                    case R.id.rename_file:
                        if(renameWindow == null) break;
                        renameWindow.showWindow(toolbarBottom.findViewById(item.getItemId()));
                        renameFab.hide();
                        break;
                    default:
                        VULog.e(TAG, "out of control");
                }
                return false;
            }
        });

        //

        currentDir = getExistDir(new File(mSetting.getSetting(VUSetting.HISTORY_DIR)));
        toolbar.setTitle(currentDir.getName());
        toolbar.setSubtitle(currentDir.getParent());
        //对filesItems赋予数据
        setFilesItemsData(currentDir);

        fileListView = (ListView) findViewById(R.id.file_list);
        fileItemAdapter = new FileItemAdapter(this, R.layout.file_listitem, filesItems);
        fileListView.setAdapter(fileItemAdapter);

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!filesItems.get(position).isDir) {
                    ((CheckBox) view.findViewById(R.id.file_check)).setChecked(!filesItems.get(position).isCheck);
                    return;
                }
                if (filesItems.get(position).file == null) {//反回上层目录 //
                    refreshList(currentDir.getParentFile());
                } else {
                    refreshList((filesItems.get(position)).file);
                }

                showOrHideToolbarBottom();
            }
        });
        fileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (filesItems.get(position).isDir) {
                    return true;
                }
                VUApplication.openFile(filesItems.get(position).file);
                return true;
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.list_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.toolbarBackgroundColor);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource( R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList(currentDir);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        renameFab = (FloatingActionButton) findViewById(R.id.rename_sure_fab);
        renameFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameWindow.showAlertDialog(true);
            }
        });
        renameFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast tempToast = Toast.makeText(mContext, getString(R.string.rename), Toast.LENGTH_SHORT);
                tempToast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, VUtil.dip2px(mContext, 20));
                tempToast.show();
                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveSetting();

        if(quitToast != null){
            quitToast.cancel();
        }
        VULog.e(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        VULog.e(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VULog.e(TAG, "onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();

        //android 6.0 权限检查
        if(VUCheckMPermission.checkIsM()){
            VUCheckMPermission permissionChecker = new VUCheckMPermission(this);
            String[] permissions = new String[]{
                    //Manifest.permission.READ_EXTERNAL_STORAGE,//根据官方文档只要请求写权限,implicity会到到读的权限
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            if(!permissionChecker.checkPermissions(permissions)){
                permissionChecker.requestPermissions(this,permissions);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case VUCheckMPermission.PERMISSION_REQUEST_CODE:
                boolean isAllPermissionGranted = true;
                for (int i = 0; i < permissions.length; i++){
                    if(!VUCheckMPermission.isPermissionResultGranted(grantResults[i])){//permission没得到许可
                        VULog.e(TAG,"没得到许可:"+permissions[i]);
                        if(VUCheckMPermission.shouldShowRequestPermissionRationale(this,permissions[i])){//可再提示
                            VULog.e(TAG,"可再提示");
                            this.finish();
                        }else{//用户选择不再提示
                            //不许可，又不再提示，只能88了
                            showPermissionPromptDialog();
                            VULog.e(TAG,"不可再提示");
                        }
                        isAllPermissionGranted = false;
                        break;

                        //只要一个没得到许可就退出
                    }else{
                        VULog.e(TAG,"得到许可:"+permissions[i]);
                    }
                }
                //都得到许可
                if(isAllPermissionGranted) refreshList(currentDir);

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    private void showPermissionPromptDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.VUAlertDialog);
        builder.setTitle(getString(R.string.permission_title));
        builder.setMessage(getString(R.string.permission_prompt));
        builder.setPositiveButton(getString(R.string.permission_i_got_it), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                PickDirActivity.this.finish();
            }
        });
        builder.show();
    }

    private void showOrderDialog() {
        //android.R.drawable.alert_light_frame
        if (orderArray == null)
            orderArray = new String[]{
                    getString(R.string.order_by_name),
                    getString(R.string.order_by_name_desc),
                    getString(R.string.order_by_date),
                    getString(R.string.order_by_date_desc),
                    getString(R.string.order_by_length),
                    getString(R.string.order_by_length_desc),
                    getString(R.string.order_by_ext),
            };
        AlertDialog orderDialog = new AlertDialog.Builder(this, R.style.VUAlertDialog)
                .setTitle(getString(R.string.choose_order_mode))
                .setSingleChoiceItems(orderArray, whichOrder, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        whichOrder = which;
                        switch (which) {
                            case 0:
                                orderBy = ORDER_BY_NAME;
                                isDesc = false;
                                break;
                            case 1:
                                orderBy = ORDER_BY_NAME;
                                isDesc = true;
                                break;
                            case 2:
                                orderBy = ORDER_BY_DATE;
                                isDesc = false;
                                break;
                            case 3:
                                orderBy = ORDER_BY_DATE;
                                isDesc = true;
                                break;
                            case 4:
                                orderBy = ORDER_BY_LENGTH;
                                isDesc = false;
                                break;
                            case 5:
                                orderBy = ORDER_BY_LENGTH;
                                isDesc = true;
                                break;
                            case 6:
                                orderBy = ORDER_BY_EXT;
                                isDesc = false;
                                break;
                        }
                        refreshList(currentDir);
                        isCheckAll = false;//refreshList是重读数据的
                        dialog.cancel();
                    }
                }).create();
        orderDialog.show();
    }


    private void goCheckAll() {
        for (FileItem i : filesItems) {
            if (i.canBeChecked) {
                i.isCheck = !isCheckAll;
            }
            if (!i.isCheck) {
                i.renamePreview = null;
            }
        }
        isCheckAll = !isCheckAll;
        fileItemAdapter.notifyDataSetChanged();

        if (isCheckAll) {
            VULog.e(TAG, "goCheckAll(CheckAll)");
            renamePreview(currentRename);
            //VULog.e(TAG,"renamePreview");
        }
        showOrHideToolbarBottom();
    }


    private void refreshList(File dir) {
        if (dir == null || !dir.isDirectory()) {//参数不合法,不是目标或空
            return;
        }
        currentDir = getExistDir(dir);
        mSetting.setSetting(VUSetting.HISTORY_DIR, currentDir.getAbsolutePath());//记录当前目录
        String tempTitle = currentDir.getName();
        String tempSubtitle = currentDir.getParent();
        if (VUtil.isStringEmpty(tempTitle, false)) {
            tempTitle = getString(R.string.root_dir);
        }
        toolbar.setTitle(tempTitle);
        toolbar.setSubtitle(tempSubtitle);
        setFilesItemsData(currentDir);
        fileItemAdapter.notifyDataSetChanged();
        isCheckAll = false;
        showOrHideToolbarBottom();

    }


    private void setFilesItemsData(File dir) {
        if (dir == null || !dir.isDirectory()) {//参数不合法
            return;
        }
        //清除旧数据
        filesItems.clear();

        //添加数据到listView的数据里
        File[] files = dir.listFiles();
        if (files == null) { //空目录
            files = new File[]{};
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (File file : files) {
            boolean isDir = false;
            boolean isCheck = false;
            boolean canBeCheck = false;
            long fileLength = 0;
            if (file.isDirectory()) {
                isDir = true;
                fileLength = -1;
            } else {
                isDir = false;
                fileLength = file.length();
            }

            //详细信息
            StringBuilder tempStr = new StringBuilder();
            //文件最后修改时间
            tempStr.append(dateFormat.format(new Date(file.lastModified())));
            //可读可写
            tempStr.append(" ");
            if (file.canRead()) {
                tempStr.append(getString(R.string.can_read));
            }
            if (file.canWrite()) {
                tempStr.append("/");
                tempStr.append(getString(R.string.can_write));
                if (!isDir) {
                    canBeCheck = true;
                }
            } else {
                canBeCheck = false;
            }
            //添加到列表数据
            filesItems.add(new FileItem(file, file.getName(), tempStr.toString(), isDir, isCheck, file.lastModified(), fileLength, canBeCheck));
        }

        sortData();

        //第一条数据必须为返回上一层目录,为了好排序放后面来了
        FileItem parentDir = new FileItem(null, "..", getString(R.string.back_to_parent_dir), true, false, 0, -1, false);
        filesItems.add(parentDir);
        Collections.rotate(filesItems, 1);
    }

    public void sortData() {
        if (filesItems.isEmpty()) return;
        switch (orderBy) {
            case ORDER_BY_NAME:
                Collections.sort(filesItems);
                break;
            case ORDER_BY_DATE:
                Collections.sort(filesItems, new Comparator<FileItem>() {
                    @Override
                    public int compare(FileItem lhs, FileItem rhs) {
                        if (lhs.lastModified == rhs.lastModified) return 0;
                        if (lhs.lastModified > rhs.lastModified) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
                break;
            case ORDER_BY_LENGTH:
                Collections.sort(filesItems, new Comparator<FileItem>() {
                    @Override
                    public int compare(FileItem lhs, FileItem rhs) {
                        if (lhs.length == rhs.length) return 0;
                        if (lhs.length > rhs.length) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
                break;
            case ORDER_BY_EXT://fileName.compareToIgnoreCase
                Collections.sort(filesItems, new Comparator<FileItem>() {
                    @Override
                    public int compare(FileItem lhs, FileItem rhs) {
                        return lhs.nameAndExt[1].compareToIgnoreCase(rhs.nameAndExt[1]);
                    }
                });
                break;
            default:
                VULog.e(TAG, "sort list out of control");
        }
        if (isDesc) {
            //FileItem temp = filesItems.get(0);
            //filesItems.remove(0);
            //filesItems.add(temp);
            Collections.reverse(filesItems);
        }
    }

    public void renamePreview(int currentRename) { // TODO: 2016/6/13  这重命名规则改了，对应的renameFile也要改
        final int colorAccent = ContextCompat.getColor(this, R.color.colorAccent);
        final int curRename = currentRename;
        //showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (curRename) {
                    case RenamePopupWindow.RENAME_EXTENSION:
                        if (VUtil.isStringEmpty(renameExtension, true)) {
                            isRenameFabNeeded = false;
                            break;
                        }
                        Pattern p = Pattern.compile("\\.[a-zA-Z0-9]+$");
                        String replacement = "." + renameExtension;
                        for (FileItem i : filesItems) {
                            if (i.isCheck) {
                                Matcher m = p.matcher(i.fileName);
                                String tempStr = "";
                                if (m.matches()) {//如果文件名是 .ext
                                    tempStr = i.fileName + replacement;
                                } else {
                                    int hasMatch = 0;
                                    while (m.find()) {
                                        hasMatch++;
                                    }
                                    if (hasMatch > 0) {
                                        tempStr = m.replaceFirst(replacement);
                                    } else {
                                        tempStr = i.fileName + replacement;
                                    }
                                }
                                i.renamePreview = new SpannableString(tempStr);

                                //上色
                                i.renamePreview.setSpan(new ForegroundColorSpan(colorAccent), 0, tempStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                        isRenameFabNeeded = true;
                        break;
                    case RenamePopupWindow.RENAME_REPLACE:
                        if (VUtil.isStringEmpty(renameReplaceTo, true) && VUtil.isStringEmpty(renameReplaceFrom, true)) {
                            isRenameFabNeeded = false;
                            break;
                        }

                        String strFrom;
                        String strTo;
                        if (VUtil.isStringEmpty(renameReplaceTo, true)) {//看作删除对应字符
                            strTo = "";
                        } else {
                            strTo = renameReplaceTo;//.trim();
                        }

                        boolean isRename = false;
                        if (VUtil.isStringEmpty(renameReplaceFrom, true)) {//看作想重命名
                            isRename = true;
                            strFrom = "";
                        } else {
                            strFrom = renameReplaceFrom;//.trim();
                        }
                        Pattern p1 = Pattern.compile(strFrom);

                        for (FileItem i : filesItems) {
                            if (i.isCheck) {
                                String[] filenameAndExt = VUtil.getFileNameAndExtension(i.fileName);
                                String newString;
                                if (isRename) {//目标为空，看作想重命名
                                    newString = strTo;
                                } else {
                                    if (renameReplaceIsRegex) {//正则式的替换
                                        newString = p1.matcher(filenameAndExt[0]).replaceAll(strTo);
                                    } else {//简单字符替换
                                        newString = filenameAndExt[0].replace(strFrom, strTo);
                                    }
                                }
                                newString += filenameAndExt[1];//加上扩展名
                                i.renamePreview = new SpannableString(newString);
                                //上色
                                i.renamePreview.setSpan(new ForegroundColorSpan(colorAccent), 0, newString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                        isRenameFabNeeded = true;
                        break;
                    case RenamePopupWindow.RENAME_PREFIX:
                        if (VUtil.isStringEmpty(renamePrefix, true)) {
                            VULog.e(TAG,"VUtil.isStringEmpty(renamePrefix, true))");
                            isRenameFabNeeded = false;
                            break;
                        }

                        //补0
                        int checkedAmount = 0;
                        DecimalFormat decimalFormat = new DecimalFormat("0");
                        if (renamePrefixAutoZero) {
                            //checkedAmount = getItemCheckedCount();
                            //int digitLong = (checkedAmount + "").length();
                            int digitLong = Integer.valueOf(renamePrefixSequence);
                            StringBuilder digitFormat = new StringBuilder();
                            for (int i = 0; i < digitLong; i++) {
                                digitFormat.append("0");
                            }
                            decimalFormat.applyPattern(digitFormat.toString());
                        }

                        int increaseFlag = 1;
                        for (FileItem i : filesItems) {

                            if (i.isCheck) {
                                String[] filenameAndExt = VUtil.getFileNameAndExtension(i.fileName);
                                String newString = renamePrefix.trim();
                                newString = newString.replace("{#}", decimalFormat.format(increaseFlag));//替换（添加）序号
                                increaseFlag++;

                                //特别定制，图片长宽高
                                if (newString.indexOf("{w}") > -1 || newString.indexOf("{h}") > -1) {
                                    String mimeType = VUFile.getMimeType(i.fileName);
                                    if ((mimeType.split("/"))[0].equalsIgnoreCase("image")) {
                                        String imageH = null, imageW = null;
                                        try {
                                            ExifInterface exif = new ExifInterface(i.file.getAbsolutePath());
                                            imageW = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                                            imageH = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                                        } catch (IOException e) {
                                            VULog.w(TAG,e.getMessage());
                                            imageW = null;imageH=null;
                                        }
                                        if (imageW == null ||Integer.valueOf(imageW)==0) {
                                            BitmapFactory.Options options = new BitmapFactory.Options();
                                            options.inJustDecodeBounds = true;
                                            BitmapFactory.decodeFile(i.file.getAbsolutePath(), options);
                                            imageH = options.outHeight + "";
                                            imageW = options.outWidth + "";
                                        }
                                        //添加长宽
                                        newString = newString.replace("{h}", imageH);
                                        newString = newString.replace("{w}", imageW);
                                    }
                                }

                                newString = newString.replace("{self}", filenameAndExt[0]);//替换原文件名
                                newString += filenameAndExt[1];//加上扩展名

                                i.renamePreview = new SpannableString(newString);
                                //上色
                                i.renamePreview.setSpan(new ForegroundColorSpan(colorAccent), 0, newString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                        isRenameFabNeeded = true;
                        break;
                    default:
                }
                if(isRenameFabNeeded){
                    Message message = new Message();
                    message.what = PREVIEW_DONE;
                    handler.sendMessage(message);
                }
            }
        }).start();


    }

    private int successCount = 0;
    private int failureCount = 0;
    public void renameFile(int currentRename) { // TODO: 2016/6/13  这重命名规则改了，对应的renamePreview也要改
        showProgressDialog();
        final int CR = currentRename;
        //在新进程里重命名
        new Thread(new Runnable() {
            @Override
            public void run() {
                //try {Thread.sleep(2000);} catch (Exception e) {e.printStackTrace();}
                switch (CR) {
                    case RenamePopupWindow.RENAME_EXTENSION:
                        if (VUtil.isStringEmpty(renameExtension, true)) return;
                        Pattern p = Pattern.compile("\\.[a-zA-Z0-9]+$");
                        String replacement = "." + renameExtension;
                        for (FileItem i : filesItems) {
                            if (i.isCheck) {
                                Matcher m = p.matcher(i.fileName);
                                String tempStr = "";
                                if (m.matches()) {//如果文件名是 .ext,全匹配
                                    tempStr = i.fileName + replacement;
                                } else {
                                    int hasMatch = 0;
                                    while (m.find()) {
                                        hasMatch++;
                                    }
                                    if (hasMatch > 0) {//有匹配
                                        tempStr = m.replaceFirst(replacement);
                                    } else {//没匹配
                                        tempStr = i.fileName + replacement;
                                    }
                                }
                                File toFile = new File(i.file.getParent() + VUApplication.DIR_SEPARATOR + tempStr);
                                if (toFile.exists()) {
                                    failureCount++;
                                } else {
                                    if (i.file.renameTo(toFile)) {//有相同名字相文件时会旧的删除
                                        successCount++;
                                    } else {
                                        failureCount++;
                                    }
                                }
                            }
                        }
                        break;
                    case RenamePopupWindow.RENAME_REPLACE:
                        if (VUtil.isStringEmpty(renameReplaceTo, true) && VUtil.isStringEmpty(renameReplaceFrom, true)) {
                            return;
                        }

                        String strFrom;
                        String strTo;
                        if (VUtil.isStringEmpty(renameReplaceTo, true)) {//看作删除对应字符
                            strTo = "";
                        } else {
                            strTo = renameReplaceTo;//.trim();
                        }

                        boolean isRename = false;
                        if (VUtil.isStringEmpty(renameReplaceFrom, true)) {//看作想重命名
                            isRename = true;
                            strFrom = "";
                        } else {
                            strFrom = renameReplaceFrom;//.trim();
                        }
                        Pattern p1 = Pattern.compile(strFrom);

                        for (FileItem i : filesItems) {
                            if (i.isCheck) {
                                String[] filenameAndExt = VUtil.getFileNameAndExtension(i.fileName);
                                String newString;
                                if (isRename) {//目标为空，看作想重命名
                                    newString = strTo;
                                } else {
                                    if (renameReplaceIsRegex) {//正则式的替换
                                        newString = p1.matcher(filenameAndExt[0]).replaceAll(strTo);
                                    } else {//简单字符替换
                                        newString = filenameAndExt[0].replace(strFrom, strTo);
                                    }
                                }
                                newString += filenameAndExt[1];//加上扩展名
                                File toFile = new File(i.file.getParent() + VUApplication.DIR_SEPARATOR + newString);
                                if (toFile.exists()) {
                                    failureCount++;
                                } else {
                                    if (i.file.renameTo(toFile)) {//有相同名字相文件时会旧的删除
                                        successCount++;
                                    } else {
                                        failureCount++;
                                    }
                                }
                            }
                        }
                        break;
                    case RenamePopupWindow.RENAME_PREFIX:
                        if (VUtil.isStringEmpty(renamePrefix, true)) return;

                        //补0
                        int checkedAmount = 0;
                        DecimalFormat decimalFormat = new DecimalFormat("0");
                        if (renamePrefixAutoZero) {
                            //checkedAmount = getItemCheckedCount();
                            //int digitLong = (checkedAmount + "").length();
                            int digitLong = Integer.valueOf(renamePrefixSequence);
                            StringBuilder digitFormat = new StringBuilder();
                            for (int i = 0; i < digitLong; i++) {
                                digitFormat.append("0");
                            }
                            decimalFormat.applyPattern(digitFormat.toString());
                        }

                        int increaseFlag = 1;
                        for (FileItem i : filesItems) {
                            if (i.isCheck) {
                                String[] filenameAndExt = VUtil.getFileNameAndExtension(i.fileName);
                                String newString = renamePrefix.trim();
                                newString = newString.replace("{#}", decimalFormat.format(increaseFlag));//替换（添加）序号
                                increaseFlag++;

                                //特别定制，图片长宽高
                                if (newString.indexOf("{w}") > -1 || newString.indexOf("{h}") > -1) {
                                    String mimeType = VUFile.getMimeType(i.fileName);
                                    if ((mimeType.split("/"))[0].equalsIgnoreCase("image")) {
                                        String imageH = null, imageW = null;
                                        try {
                                            ExifInterface exif = new ExifInterface(i.file.getAbsolutePath());
                                            imageW = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                                            imageH = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                                        } catch (IOException e) {
                                            VULog.w(TAG,e.getMessage());
                                            imageW = null;imageH=null;
                                        }
                                        if (imageW == null ||Integer.valueOf(imageW)==0) {
                                            BitmapFactory.Options options = new BitmapFactory.Options();
                                            options.inJustDecodeBounds = true;
                                            BitmapFactory.decodeFile(i.file.getAbsolutePath(), options);
                                            imageH = options.outHeight + "";
                                            imageW = options.outWidth + "";
                                        }
                                        //添加长宽
                                        newString = newString.replace("{h}", imageH);
                                        newString = newString.replace("{w}", imageW);
                                    }
                                }

                                newString = newString.replace("{self}", filenameAndExt[0]);//替换原文件名
                                newString += filenameAndExt[1];//加上扩展名


                                File toFile = new File(i.file.getParent() + VUApplication.DIR_SEPARATOR + newString);
                                if (toFile.exists()) {
                                    failureCount++;
                                } else {
                                    if (i.file.renameTo(toFile)) {//有相同名字相文件时会旧的删除
                                        successCount++;
                                    } else {
                                        failureCount++;
                                    }
                                }
                            }
                        }
                        break;
                    default:
                }
                //通知活动rename done;
                Message message = new Message();
                message.what = RENAME_DONE;
                Bundle replyData = new Bundle();
                replyData.putInt("success", successCount);
                replyData.putInt("failure", failureCount);
                message.setData(replyData);
                handler.sendMessage(message);

                successCount = 0;
                failureCount = 0;
            }
        }).start();

    }

    public static final int RENAME_DONE = 1;
    public static final int PREVIEW_DONE = 2;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RENAME_DONE:
                    VULog.e(TAG, "RENAME_DONE");
                    if (renameWindow != null) {
                        renameWindow.dismiss();
                    }
                    refreshList(currentDir);
                    //showOrHideToolbarBottom();
                    Bundle replyData = msg.getData();
                    String feedback = getString(R.string.rename_done) + getString(R.string.rename_success) + replyData.getInt("success") + " " + getString(R.string.rename_failure) + replyData.getInt("failure");
                    hideProgressDialog();
                    Toast.makeText(mContext, feedback, Toast.LENGTH_LONG).show();
                    break;
                case PREVIEW_DONE:

                    if (renameWindow != null) {
                        renameWindow.dismiss();
                    }
                    isRenameFabNeeded = true;
                    renameFab.show();
                    fileItemAdapter.notifyDataSetChanged();

                    //VULog.e(TAG, "PREVIEW_DONE");
                    //hideProgressDialog();

                    break;
                default:
            }
        }
    };

    public void cleanPreview() {
        for (int i = 0; i < filesItems.size(); i++) {
            filesItems.get(i).renamePreview = null;

        }
        fileItemAdapter.notifyDataSetChanged();
    }

    public int getItemCheckedCount() {
        int tempCount = 0;
        if (filesItems.isEmpty()) return tempCount;
        for (int i = 0; i < filesItems.size(); i++) {
            if (filesItems.get(i).isCheck) {
                tempCount++;
            }
        }
        return tempCount;
    }

    public void showOrHideToolbarBottom() {
        TextView checkInfo = (TextView) toolbarBottom.findViewById(R.id.checked_info);
        int tempCount = getItemCheckedCount();
        if (tempCount > 0) {
            mContext.toolbarBottom.setVisibility(View.VISIBLE);
            checkInfo.setText(tempCount + " " + getString(R.string.item_is_selected));
            if (isRenameFabNeeded) renameFab.show();
        } else {
            renameFab.hide();
            mContext.toolbarBottom.setVisibility(View.GONE);

        }
    }

    //hideInputMethod(new EditText[]{inputExtension,inputPrefix,inputReplaceFrom,inputReplaceTo});
    public void hideInputMethod(EditText[] ets) {
        VULog.e(TAG, "hideInputMethod");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        for (EditText et : ets) {
            if (et.getWindowToken() != null) {
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
            }
        }
    }

    private ProgressDialog proDialog;

    public void showProgressDialog() {
        if (proDialog != null) return;
        proDialog = new ProgressDialog(mContext);
        proDialog.setMessage(getString(R.string.please_wait));
        proDialog.setIndeterminate(false);
        proDialog.setCanceledOnTouchOutside(false);
        proDialog.setCancelable(false);
        proDialog.show();
    }

    public void hideProgressDialog() {
        if (proDialog != null) {
            proDialog.dismiss();
            proDialog = null;
        }

    }

    private void saveSetting() {
        mSetting.setSetting("currentRename", currentRename+"");
        mSetting.setSetting("renameExtension", renameExtension);
        mSetting.setSetting("renameReplaceFrom", renameReplaceFrom);
        mSetting.setSetting("renameReplaceTo", renameReplaceTo);
        mSetting.setSetting("renameReplaceIsRegex", Boolean.toString(renameReplaceIsRegex));
        mSetting.setSetting("renamePrefixAutoZero", Boolean.toString(renamePrefixAutoZero));
        mSetting.setSetting("renamePrefix", renamePrefix);
        mSetting.setSetting("renamePrefixSequence", renamePrefixSequence);
        mSetting.saveSetting();
    }

    private File getExistDir(File dir){
        if (dir.exists()) {
            return dir;
        } else {
            return getExistDir(dir.getParentFile());
        }
    }

    private long firstTimeDown = 0;

    private Toast quitToast;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTimeDown = System.currentTimeMillis();
                if (secondTimeDown - firstTimeDown > 1500) {//间隔时间过长
                    quitToast = Toast.makeText(this, getString(R.string.exit_app_hint), Toast.LENGTH_SHORT);
                    quitToast.show();
                    firstTimeDown = secondTimeDown;//更新点击时间
                    return true;
                } else {
                    finish();
                    //System.exit(0);
                }
                break;
            default:
        }
        return super.onKeyDown(keyCode, event);
    }
}