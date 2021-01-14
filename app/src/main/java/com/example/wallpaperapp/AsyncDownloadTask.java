package com.example.wallpaperapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class AsyncDownloadTask extends AsyncTask<String,Integer,String> {
    final static String TAG = "AsyncDownloadTask";
    Context mContext;

    private int width;
    int height;

    public AsyncDownloadTask(Context c, int w, int h) {
        mContext = c;
        width = w;
        height=h;
    }



    @Override
    protected void onPreExecute() {
        Log.i(TAG, "start downloading pics");
    }
    @Override
    protected String doInBackground(final String... params) {
                ImageDownLoader imageDownLoader = new ImageDownLoader(mContext);
                imageDownLoader.loadAllImages(params[0], width, height);

        return null;
    }

    /*@Override
    protected void onProgressUpdate(Integer... progresses) {
        Log.i(TAG,"Loading "+progresses[0]+"%");
    }*/

    @Override
    protected void onPostExecute(String s) {
        Log.i(TAG,"Loading finished");
    }

    @Override
    protected void onCancelled() {
        Log.i(TAG, "Loading canceled");
    }

}
