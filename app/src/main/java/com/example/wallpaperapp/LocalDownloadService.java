package com.example.wallpaperapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class LocalDownloadService extends Service {

   // private MyBinder mBinder;

   /* private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);

            try{
                Log.e("LocalServiceHttp","Connected with "+iMyAidlInterface.getServiceName());
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(LocalDownloadService.this,"连接断开，重启 remoteService",Toast.LENGTH_LONG);
            startService(new Intent(LocalDownloadService.this,RemoteServiceHttp.class));
            bindService(new Intent(LocalDownloadService.this,RemoteServiceHttp.class),connection, Context.BIND_IMPORTANT);
        }
    };

    */
    @Override
    public IBinder onBind(Intent intent) {
       /* mBinder = new MyBinder();
        return mBinder;*/
        return null;
    }

    @Override
    public void onCreate(){

        super.onCreate();
        String channelID = "31415";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelID, "channelName", NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel(channel);
        Notification notification = new NotificationCompat.Builder(this, channelID)
                                    .build();
        startForeground(93333, notification);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this,"LocalServiceHttp 启动", Toast.LENGTH_LONG).show();
        //startService(new Intent(LocalDownloadService.this,RemoteServiceHttp.class));
        //bindService(new Intent(LocalDownloadService.this,RemoteServiceHttp.class),connection,Context.BIND_IMPORTANT);

 /*       TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Looper.prepare();
                    try {
                        String[] picasNames = getAssets().list("pics");
                        int randomNumber = (int)Math.ceil(Math.random()*picasNames.length);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize=5;
                        Bitmap bitmap = Tools.getBitmapFromAssets(getBaseContext(),picasNames[randomNumber],options);
                        Tools.saveImageAsWallpaper(bitmap,getBaseContext());
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    Looper.loop();
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask,5*1000,5*1000);
*/
        int jobid = 996;
        JobInfo.Builder builder_download = new JobInfo.Builder(jobid, new ComponentName(this, JobSchedulerService.class));
        //jobScheduler 至少循环15mins执行
        builder_download.setPeriodic(10*60*1000);
        //任何网络环境都去执行
        builder_download.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder_download.setPersisted(true);
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder_download.build());

        return START_STICKY;
    }
    @Override
    public void onDestroy(){
        Log.e("destroy","destroy");
        super.onDestroy();
    }


  /*  private class MyBinder extends IMyAidlInterface.Stub{
        @Override
        public String getServiceName() throws RemoteException {
            return LocalDownloadService.class.getName();
        }
    }
*/

}
