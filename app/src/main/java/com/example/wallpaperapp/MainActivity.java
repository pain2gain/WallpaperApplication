package com.example.wallpaperapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**wallpaper对于recyclerview适配器*/
    private WallPaperAdapter mAdapter;
    /**recyclerView */
    private RecyclerView mRecyclerView;
    /**wallpaper的List容器*/
    private List<WallPaper> listWallPaper =new ArrayList();
    /**所有的需要加载的壁纸的名字*/
    private static List<String> listAllNames=new ArrayList();
    /**加载好的的壁纸的名字，可以传递给其他activity，顺序表示现实的阿顺序*/
    private static String[] wallpaperName;
    /**每次加载的图片数量*/
    private final int loadItemAmount = 8;
    /**需要申请的权限*/
    private static String[]  REQUEST_PERMISSION= {Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                    Manifest.permission.INTERNET,
                                                    Manifest.permission.RECEIVE_BOOT_COMPLETED};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //删除原本的actionbar
        ActionBar actionTopBar =getSupportActionBar();
        if (actionTopBar!=null){
            actionTopBar.hide();
        }
        //获取权限，这里只用write_external_storage 来判断有没有权限，获取以上全部的权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    REQUEST_PERMISSION, 1);
        }

        //启动local service，用于定时下载图片和更新数据库
        startService(new Intent(this,LocalDownloadService.class));

        //要加载的图片
        final List<String> listWallPaperGot;
        listAllNames = Tools.initWallPaper(this);
        wallpaperName=listAllNames.toArray(new String[listAllNames.size()]);
        listWallPaperGot = Tools.getWallpaperToLoad(listAllNames,loadItemAmount);

        //bitmap 缩小五倍，并且将要加载的wallpaper加入listWallpaper中
        final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize=5;
        listWallPaper = Tools.loadWallpaperWithBitmap(this,listWallPaperGot,bitmapOptions);

        //创建recyclerview 瀑布流的布局管理
        mRecyclerView = findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new WallPaperAdapter(listWallPaper,this,wallpaperName);
        mRecyclerView.setAdapter(mAdapter);

        //Swipe组件 提供上滑刷新功能
        final SwipeRefreshLayout mRefreshLayout;
        mRefreshLayout =findViewById(R.id.swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefresh(getBaseContext(),listWallPaper,wallpaperName,mAdapter,mRefreshLayout));

        final ImageDownLoader imageDownLoader = new ImageDownLoader(this);
        //监听recyclerView滚动事件，每次添加8个，并且将List更新，剩下没有加载的图片名
        mRecyclerView.addOnScrollListener(new ScrollToLoadListener(listWallPaper) {
           @Override
            public void onLoadMore(){

               //另外开一个线程通知recyclerView数据改变了
               Handler handler = new Handler();
               final Runnable runnable = new Runnable() {
                   public void run() {
                       mAdapter.notifyDataSetChanged();
                   }
               };
               //下载的文件在cache/picscache/文件中，获取图片名称
               File file = Tools.createFileDir(getBaseContext(),"picscache");
               String[] picName = file.list();

               if (listAllNames.size()>0){
                   //当assets预置的图片还有时
                   List<String> listAddedNames = Tools.getWallpaperToLoad(listAllNames,loadItemAmount);
                   List<WallPaper> tempWallpaper = Tools.loadWallpaperWithBitmap(getBaseContext(),listAddedNames,bitmapOptions);
                   for (int i=0;i<tempWallpaper.size();i++){
                       listWallPaper.add(tempWallpaper.get(i));
                   }
                   handler.post(runnable);
               }else if (listAllNames.size()==0){
                   //加载picscache下载的图片
                 /*  for (int i =picName.length-1;i>0;i--){
                       Bitmap tempBitmap =imageDownLoader.getBitmapCache(picName[i]);
                       WallPaper wallPaper = new WallPaper(picName[i],tempBitmap,file.getAbsolutePath());
                      /* Boolean isNewItemAdded = false;
                       for (int j=0;j<listWallPaper.size();j++){
                           WallPaper wp = listWallPaper.get(j);
                           if (!wp.equals(wallPaper)) {
                               isNewItemAdded=true;
                               listWallPaper.add(wallPaper);
                           }
                       }
                       if (isNewItemAdded) handler.post(runnable);*/
                /*      listWallPaper.add(wallPaper);
                   }
                   handler.post(runnable);**/
               }
           }
        });
    }
    //设置headview和FootView
   /* private void setHeaderView(RecyclerView view){
        View header = LayoutInflater.from(this).inflate(R.layout.header, view, false);
        mAdapter.setHeaderView(header);
    }

    private void setFooterView(RecyclerView view){
        View footer = LayoutInflater.from(this).inflate(R.layout.footer, view, false);
        mAdapter.setFooterView(footer);
    }
*/
}

