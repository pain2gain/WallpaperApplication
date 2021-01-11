package com.example.wallpaperapp;

import android.graphics.Bitmap;
import java.io.File;
import java.io.InputStream;

public class WallPaper {
    private String name;
    private Bitmap wallPaperUri;
    private String path;

    public WallPaper(String n , Bitmap uri,String s){
        this.name=n;
        this.wallPaperUri =uri;
        this.path=s;
    }
    public Bitmap getWallPaperBitmap(){
        return this.wallPaperUri;
    }
    public String getPath(){
        return  this.path;
    }
    public String getName(){
        return this.name;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof WallPaper))
            return false;
        WallPaper wp = (WallPaper) obj;
        return this.name.equals(wp.name);

    }
}
