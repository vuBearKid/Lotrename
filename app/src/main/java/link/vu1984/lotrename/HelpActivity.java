package link.vu1984.lotrename;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends VUActivity {
    private static final String TAG = "HelpActivity";
    private Context mContext;
    private View mContainer;

    public Toolbar toolbarTop;

    private HelpViewPager helpViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        mContext = this;

        mContainer = findViewById(R.id.app_help_container);

        toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        toolbarTop.setNavigationIcon(android.R.drawable.ic_menu_revert);
        toolbarTop.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"hiii",Toast.LENGTH_SHORT).show();
                ((VUActivity)mContext).finish();

            }
        });
        toolbarTop.setVisibility(View.GONE);

        helpViewPager = new HelpViewPager(new String[] {getString(R.string.help_resume),getString(R.string.help_rename),getString(R.string.help_preview),getString(R.string.help_regex)});




    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbarTop.setTitle("abc");
        helpViewPager.initCursor();


    }

    @Override
    protected void onPause() {
        super.onPause();
        helpViewPager.saveState();
    }

    public class HelpViewPager{
        private static final String TAG = "HelpViewPager";

        private View view1,view2,view3,view4;
        private List<View> viewList = new ArrayList<View>();
        private List<String> viewListTitle = new ArrayList<String>();
        private ViewPager viewPager;
        private ImageView cursor;
        private int cursorWidth; // 游标宽度
        private int offset; // 动画图片偏移量
        private int tabOffset;
        private int initIndex = 0;
        private int initX; //TranslateAnimation的参数 ，相对最开始来算的
        private int initY;
        private int currIndex;// 当前页卡编号

        private LinearLayout viewpagerTitle;

        public HelpViewPager(String[] StrArrTitle){
            viewPager = (ViewPager) findViewById(R.id.help_viewpager);
            LayoutInflater layoutInflater = getLayoutInflater();
            view1 = layoutInflater.inflate(R.layout.help_viewpager_layout1,(ViewGroup) mContainer,false);
            view2 = layoutInflater.inflate(R.layout.help_viewpager_layout2,(ViewGroup)mContainer,false);
            view3 = layoutInflater.inflate(R.layout.help_viewpager_layout3,(ViewGroup)mContainer,false);
            view4 = layoutInflater.inflate(R.layout.help_viewpager_layout4,(ViewGroup)mContainer,false);

            TextView regexTextView = (TextView)view4.findViewById(R.id.regex_help);
            regexTextView.setText(VUApplication.getAssetsTxt("regexhelp.txt"));


            viewList.add(view1);
            viewList.add(view2);
            viewList.add(view3);
            viewList.add(view4);

            viewListTitle.add(StrArrTitle[0]);
            viewListTitle.add(StrArrTitle[1]);
            viewListTitle.add(StrArrTitle[2]);
            viewListTitle.add(StrArrTitle[3]);

            HelpViewpagerAdapter pagerAdapter = new HelpViewpagerAdapter(viewList,viewListTitle);
            viewPager.setAdapter(pagerAdapter);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                private int positionOffsetPixelsState = 1;
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    //VULog.e(TAG," position:"+position+" positionOffset:"+positionOffset+" positionOffsetPixels:"+positionOffsetPixels );
                    //已经是第一页
                    if(position == 0 && positionOffsetPixelsState == 0 && positionOffsetPixels==0){
                        finish();
                    }
                    //已经是最后页
                    /*if(position == viewList.size()-1 && positionOffsetPixelsState == 0 && positionOffsetPixels==0){
                    }*/
                    positionOffsetPixelsState = positionOffsetPixels;
                }
                @Override
                public void onPageSelected(int position) {// TODO: 2016/6/16 一进入APP，没到这个活动，就执行了这个。
                    //VULog.e(TAG,"onPageSelected~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    Animation animation;
                    int xFromDelta;
                    int xToDelta;
                    xFromDelta = (currIndex - initIndex) * tabOffset;
                    xToDelta = (position - initIndex) * tabOffset;
                    //VULog.e(TAG," xFromDelta:"+xFromDelta+" xToDelta:"+xToDelta+" currIndex:"+currIndex+" initIndex:"+initIndex);
                    animation = new TranslateAnimation(xFromDelta, xToDelta, 0, 0);
                    currIndex = position;
                    animation.setFillAfter(true);// True:图片停在动画结束位置
                    animation.setDuration(300);
                    cursor.startAnimation(animation);

                }
                @Override
                public void onPageScrollStateChanged(int state) {
                    //VULog.e(TAG," state:"+state);
                }
            });

            //初始化title，要注意viewpagerTitle里的TextView应该与viewListTitle.size()一样，不然会有title丢失
            viewpagerTitle = (LinearLayout) findViewById(R.id.help_viewpager_title);
            for(int i = 0; i < viewpagerTitle.getChildCount();i++){
                TextView tempTextView = (TextView)viewpagerTitle.getChildAt(i);
                if (i>=viewListTitle.size()) {
                    viewpagerTitle.removeView(tempTextView);
                    continue;
                }
                String tempTitle = viewListTitle.get(i);
                tempTextView.setText(tempTitle);
                tempTextView.setOnClickListener(new TitleOnClickListener(i));
            }

        }
        public void saveState(){
            initIndex = currIndex;
        }

        public void initCursor(){
            // 初始化动画
            cursor = (ImageView) findViewById(R.id.help_viewpager_cursor);
            cursorWidth = BitmapFactory.decodeResource(getResources(), R.drawable.cursor_line)
                    .getWidth();// 获取图片宽度

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int screenW = dm.widthPixels;// 获取分辨率宽度
            offset = (screenW / viewList.size() - cursorWidth) / 2;// 计算偏移量
            tabOffset = 2 * offset + cursorWidth;

            currIndex = initIndex;
            initX = offset + initIndex * tabOffset;
            initY = 0;
            Matrix matrix = new Matrix();
            matrix.postTranslate(initX, initY);
            cursor.setImageMatrix(matrix);// 设置动画初始位置
            viewPager.setCurrentItem(currIndex);

        }

        public void sayHI(){
            Toast.makeText(mContext,"hi",Toast.LENGTH_SHORT).show();
        }

        public class HelpViewpagerAdapter extends PagerAdapter{
            private List<View> mViewList;
            private List<String> mViewListTitle;

            public HelpViewpagerAdapter(List<View> viewList,List<String> viewListTitle){
                mViewList = viewList;
                mViewListTitle = viewListTitle;
            }
            @Override
            public int getCount() {
//                VULog.e(TAG,"getCount");
                return mViewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
//                VULog.e(TAG,"isViewFromObject");
                return view == mViewList.get(Integer.parseInt(object.toString())) ;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
//                VULog.e(TAG,"destroyItem");
                container.removeView(mViewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
//                VULog.e(TAG,"instantiateItem");
                container.addView( mViewList.get(position));
                return position;
            }

        }
        public class TitleOnClickListener implements View.OnClickListener{
            private int mIndex;
            public TitleOnClickListener(int i){
                mIndex = i;
            }

            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(mIndex);
            }
        }
    }
}


