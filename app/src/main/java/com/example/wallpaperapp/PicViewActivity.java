package com.example.wallpaperapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PicViewActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private RelativeLayout parentView;

    private ViewPager viewPager;
    private ZoomImageView zoomImageView;
    private ZoomImageView[] imgsViewArray;
    private List<WallPaper> listWallPaper;
    private List<String> listAllNames=new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_view);

        Intent intent = getIntent();
        int positionStart = intent.getIntExtra("wallpaper_position", 0);
        String[] wallpaperName = intent.getStringArrayExtra("wallpaper_nameArray");

        listAllNames = Arrays.asList(wallpaperName);
        final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize=5;
        listWallPaper = Tools.loadWallpaperWithBitmap(this,listAllNames,bitmapOptions);

        parentView=findViewById(R.id.relative_layout);
        viewPager = findViewById(R.id.view_pager);

        viewPager.setOnPageChangeListener(this);

        imgsViewArray = new ZoomImageView[listWallPaper.size()];
        for (int i = 0; i < imgsViewArray.length; i++) {
            zoomImageView = new ZoomImageView(this);
            zoomImageView.setImageBitmap(listWallPaper.get(i).getWallPaperBitmap());
            imgsViewArray[i] = zoomImageView;
        }
        viewPager.setAdapter(new ContentAdapter(listWallPaper));
        viewPager.setCurrentItem(positionStart);
    }


    public class ContentAdapter extends PagerAdapter {

        private List<WallPaper> mListWallPaper;

        public ContentAdapter(List<WallPaper> list){
            mListWallPaper = list;
        }

        @Override
        public int getCount() {
            return listWallPaper.size()  ;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imgsViewArray[position]);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            container.addView(imgsViewArray[position],0);
            imgsViewArray[position].setScaleType(ImageView.ScaleType.MATRIX);
            ImageViewTouchListener imageViewTouchListener = new ImageViewTouchListener(getBaseContext(),position,mListWallPaper,parentView,imgsViewArray[position]);
            imgsViewArray[position].setOnTouchListener(imageViewTouchListener);
            return imgsViewArray[position];
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
