package com.example.wallpaperapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    private final String TAG= "BootCompletedReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            //Intent toIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            Log.e(TAG,"received the bootCompleted broadcast");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, LocalDownloadService.class));
            } else {
                context.startService(new Intent(context, LocalDownloadService.class));
            }
        }
    }
}
