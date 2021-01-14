package com.example.wallpaperapp;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class JobSchedulerService extends JobService {

    private static final String TAG = "JobSchedulerService";

    int skipNumber = (int)Math.round(Math.random()*100);
    final String urlAPI = "https://service.picasso.adesk.com/v1/vertical/vertical?limit=5&skip="+skipNumber+"&adult=false&first=0&order=hot";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"on create");
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG,"on start job");
        doJobScheduler();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private  void doJobScheduler(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                //下载壁纸
                if(Tools.checkNetworkConnect(getApplicationContext())) {
                    Log.i(TAG,"start downloading pics");
                    new AsyncDownloadTask(getApplicationContext(), 500, 600).execute(urlAPI);
                }else{
                    Log.i(TAG,"failed to start downloading pics, no network");
                    Toast.makeText(getApplicationContext(),"无网络连接",Toast.LENGTH_SHORT).show();
                }
                //将wallpaper添加到数据库中
                String dirPics =getCacheDir().getAbsolutePath()+ File.separator+"pics"+File.separator;
                String dirPicscache =getCacheDir().getAbsolutePath()+ File.separator+"picscache"+File.separator;
                Log.i(TAG,"Add downloaded pics into DB");
                addWallpaper2Database(dirPics);
                addWallpaper2Database(dirPicscache);
                //更换壁纸
                Log.i(TAG,"start changing wallpaper");
                changeRandomWallpaper();
                Looper.loop();
            }
        }).start();
    }

    /**将图片添加到数据库中，名字，创建事件，路径*/
    private void addWallpaper2Database(String dir){
        File fileWallpaper = new File(dir);
        if (!fileWallpaper.exists()){
            Log.i(TAG,"Wallpaper to DB"+"No wallpaperFile exits");
            return;
        }
        DatabaseHelper dbHelper= new DatabaseHelper(this,"Wallpaper.db",null,6);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] arrayWallpaperInFile = fileWallpaper.list();
        if (arrayWallpaperInFile.length==0){
            Log.i(TAG,"Wallpaper to DB"+"No wallpaper exits");
            return;
        }

        if (arrayWallpaperInFile.length>0){
            for (int i=0;i<arrayWallpaperInFile.length;i++){
                ContentValues values = new ContentValues();

                String path = dir+arrayWallpaperInFile[i];
                String name =arrayWallpaperInFile[i];
                Long time = (new File(path)).lastModified();

                values.put("wallpaper_name",name);
                values.put("wallpaper_time",time);
                values.put("wallpaper_path",path);

                db.replace("wallpaper_table",null,values);
            }
        }
    }

    private void changeRandomWallpaper(){
        try {
            String[] picasNames = getAssets().list("pics");
            int randomNumber = (int)Math.ceil(Math.random()*picasNames.length);
            Bitmap bitmap = Tools.getBitmapFromAssets(this,picasNames[randomNumber],null);
            Tools.saveImageAsWallpaper(bitmap,this);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
