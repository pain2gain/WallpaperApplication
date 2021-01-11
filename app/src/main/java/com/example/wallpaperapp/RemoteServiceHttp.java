package com.example.wallpaperapp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class RemoteServiceHttp extends Service {
    public RemoteServiceHttp() {
    }

    private MyBinder mBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            try {
                Log.e("RemoteServiceHttp", "connected with " + iMyAidlInterface.getServiceName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(RemoteServiceHttp.this,"链接断开，重新启动 LocalService",Toast.LENGTH_LONG).show();
            RemoteServiceHttp.this.startService(new Intent(RemoteServiceHttp.this,LocalDownloadService.class));
            RemoteServiceHttp.this.bindService(new Intent(RemoteServiceHttp.this,LocalDownloadService.class),connection,Context.BIND_IMPORTANT);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
       mBinder = new MyBinder();
       return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Toast.makeText(this,"RemoteServiceHttp 启动", Toast.LENGTH_LONG).show();
        bindService(new Intent(RemoteServiceHttp.this,LocalDownloadService.class),connection,Context.BIND_IMPORTANT);

        return START_STICKY;
    }

    private class MyBinder extends IMyAidlInterface.Stub{
            @Override
            public String getServiceName() throws RemoteException {
                return RemoteServiceHttp.class.getName();
            }

        }

}
