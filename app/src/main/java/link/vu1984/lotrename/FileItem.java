package link.vu1984.lotrename;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class FileItem implements Comparable<FileItem> {
    public String fileName;
    public long length; //定义目录为-1，因为需要计算，所以最好用到才算。
    public String fileInfo;
    public SpannableString renamePreview;
    public long lastModified;
    public boolean isDir = false;
    public boolean isCheck = false;
    public boolean canBeChecked = false;
    public File file;
    public String[] nameAndExt;

    public FileItem(File file, String fileName, String fileInfo, boolean isDir, boolean isCheck, long lastModified, long length, boolean canBeChecked) {
        this.file = file;
        this.fileName = fileName;
        this.fileInfo = fileInfo;
        this.isDir = isDir;
        this.isCheck = isCheck;
        this.lastModified = lastModified;
        this.length = length;
        this.canBeChecked = canBeChecked;
        this.nameAndExt = VUtil.getFileNameAndExtension(fileName);
    }


    @Override
    /**
     * @return <another 负数， >another 正数,  =another 0
     *
     */
    public int compareTo(FileItem another) {
        return this.fileName.compareToIgnoreCase(another.fileName);
    }
}

class FileItemAdapter extends ArrayAdapter<FileItem> {

    private static final String TAG = "FileItemAdapter";

    private int resId;
    private PickDirActivity mContext;

    public FileItemAdapter(Context context, int resource, List<FileItem> objects) {
        super(context, resource, objects);
        resId = resource;
        mContext = (PickDirActivity) getContext();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {//parent是listView
        View view;
        FileItem item = getItem(position);

        ViewHolder viewHolder = new ViewHolder();

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resId, parent, false);
            viewHolder.fileName = (TextView) view.findViewById(R.id.file_listitem_name);
            viewHolder.fileInfo = (TextView) view.findViewById(R.id.file_listitem_info);
            viewHolder.fileImage = (ImageView) view.findViewById(R.id.file_listitem_image);
            viewHolder.fileCheck = (CheckBox) view.findViewById(R.id.file_check);
            view.setTag(viewHolder);

        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.fileName.setText(item.fileName);
        if (!VUtil.isStringEmpty(item.renamePreview,true)){
            viewHolder.fileInfo.setText(item.renamePreview);
            //VULog.e(TAG,item.renamePreview.toString());
        }else{
            viewHolder.fileInfo.setText(item.fileInfo);
        }

        if (item.isDir) {
            viewHolder.fileImage.setImageResource(R.drawable.file_dir);
        } else {
            viewHolder.fileImage.setImageResource(R.drawable.file_default);
        }

        if (item.canBeChecked){
            viewHolder.fileCheck.setVisibility(View.VISIBLE);
        }else{
            viewHolder.fileCheck.setVisibility(View.GONE);
        }

        //必须放在item付值的的前面，以防还监控着的是前一个item
        //final int  pos = position;
        viewHolder.fileCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getItem(position).isCheck = isChecked;
                if(!isChecked) {//不选中清空预览
                    getItem(position).renamePreview = null;
                    //还原R.id.file_listitem_info
                    ((TextView)((View)buttonView.getParent()).findViewById(R.id.file_listitem_info)).setText(getItem(position).fileInfo);
                }else{
                    if(getItem(position).renamePreview == null)
                        mContext.renamePreview(mContext.currentRename);// TODO: 2016/6/12 可优化针对当前
                }
                mContext.showOrHideToolbarBottom();
                VULog.e(TAG,"position:"+position);
            }
        });
        viewHolder.fileCheck.setChecked(item.isCheck);

        return view;
    }

    public int getItemCheckedCount(){
        int tempCount = 0;
        for (int i = 0; i < getCount(); i++){
            if(getItem(i).isCheck){
                tempCount++;
            }
        }
        return tempCount;
    }

    class ViewHolder {
        public TextView fileName;
        public TextView fileInfo;
        public ImageView fileImage;
        public CheckBox fileCheck;

        public ViewHolder() {
        }
    }
}