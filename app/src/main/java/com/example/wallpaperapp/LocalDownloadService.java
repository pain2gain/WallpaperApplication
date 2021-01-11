package com.example.wallpaperapp;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class LocalDownloadService extends Service {

    private MyBinder mBinder;

    private ServiceConnection connection = new ServiceConnection() {
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
    @Override
    public IBinder onBind(Intent intent) {
        mBinder = new MyBinder();
        return mBinder;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this,"LocalServiceHttp 启动", Toast.LENGTH_LONG).show();
        startService(new Intent(LocalDownloadService.this,RemoteServiceHttp.class));
        bindService(new Intent(LocalDownloadService.this,RemoteServiceHttp.class),connection,Context.BIND_IMPORTANT);

        int jobid = 996;
        JobInfo.Builder builder_download = new JobInfo.Builder(jobid, new ComponentName(this, JobSchedulerService.class));
        //jobScheduler 至少循环15mins执行
        builder_download.setPeriodic(15*60*1000);
        //任何网络环境都去执行
        builder_download.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder_download.setPersisted(true);
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder_download.build());

        return START_STICKY;
    }

    private class MyBinder extends IMyAidlInterface.Stub{
        @Override
        public String getServiceName() throws RemoteException {
            return LocalDownloadService.class.getName();
        }
    }

}
