package com.example.wallpaperapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PicViewActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private RelativeLayout parentView;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_view);

        Intent intent = getIntent();
        int positionStart = intent.getIntExtra("wallpaper_position", 0);
        String[] wallpaperName = intent.getStringArrayExtra("wallpaper_nameArray");

        parentView=findViewById(R.id.relative_layout);
        viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(),wallpaperName,parentView));
        viewPager.setCurrentItem(positionStart);
    }


    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        String[] wallpaperNames;
        RelativeLayout topView;
        public ViewPagerAdapter(FragmentManager fm,String[] wpn,RelativeLayout relativeLayout) {
            super(fm);
            wallpaperNames = wpn;
            topView =relativeLayout;
        }

        @Override
        public Fragment getItem(int position) {
            EmptyFragment fragment = new EmptyFragment(wallpaperNames,position,topView);

            Log.e("zrg", "getItem: 当前位置position=" + position);
            return fragment;
        }

        @Override
        public int getCount() {
            return wallpaperNames.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.e("zrg", "instantiateItem: 当前位置position=" + position);
            return super.instantiateItem(container, position);
        }


        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public void setTitles(String[] titles) {
            notifyDataSetChanged();
        }
    }


    public static class EmptyFragment extends BaseLazyFragment {
        TextView mTvClick;
        ConstraintLayout parentView;
        RelativeLayout topView;
        String[] wallpaperName;
        private List<WallPaper> listWallPaper;
        private List<String> listAllNames=new ArrayList();
        int position;

        public EmptyFragment(String[] wpn, int p,RelativeLayout relativeLayout) {
            wallpaperName = wpn;
            position = p;
            topView = relativeLayout;
        }


        @Override
        public void onCreate( Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
            parentView =(ConstraintLayout) inflater.inflate(R.layout.fragment_empty, container, false);
            mTvClick = parentView.findViewById(R.id.tv_click);
            mIsprepared = true;
            lazyLoad();
            return parentView;
        }

        @Override
        protected void lazyLoad() {
            if (!mIsprepared || !mIsVisible || mHasLoadedOnce) {
                return;
            }
            parentView.removeView(mTvClick);
            String[] temp = new String[]{wallpaperName[position]};
            listAllNames = Arrays.asList(temp);
            listWallPaper = Tools.loadWallpaperWithBitmap(getContext(),listAllNames,null);

            WallPaper mWallPaper = listWallPaper.get(0);

            ZoomImageView zoomImageView = new ZoomImageView(getContext());
            zoomImageView.setImageBitmap(mWallPaper.getWallPaperBitmap());

            ImageViewTouchListener imageViewTouchListener = new ImageViewTouchListener(getContext(),position,mWallPaper,topView,zoomImageView);
            zoomImageView.setOnTouchListener(imageViewTouchListener);
            parentView.addView(zoomImageView);
            mHasLoadedOnce = true;

        }
    }

    @Override
        public void onPageScrollStateChanged(int arg0){
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {

        }

}
