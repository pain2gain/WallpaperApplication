package com.example.wallpaperapp;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Tools {

    /**获取assets下pics文件夹中的图片*/
    public static List<String> initWallPaper(Context context) {
        List<String> list_image = new ArrayList<>();
        String[] array_image = null;
        try {
            array_image = context.getAssets().list("pics");
            for (String item : array_image) {
                list_image.add(item);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return list_image;
    }

    /**将图片保存到本地，并且返回wallpaper的容器*/
    public static List<WallPaper> loadWallpaperWithBitmap( final Context context, final List<String> list_image, BitmapFactory.Options options) {
        List<WallPaper> listWallPaper = new ArrayList<>();
        for (int i = 0; i < list_image.size(); ++i) {
            Bitmap bitmap = getBitmapFromAssets(context,list_image.get(i),options);

            //保存未压缩的bitmap以jpeg格式到本地
            final String fileName = list_image.get(i);
            final String dir =context.getCacheDir().getAbsolutePath()+File.separator+"pics";
            new Thread( new Runnable(){
                @Override
                public void run() {
                    saveImage2Local(fileName,context,dir,false);
                }
            }).start();
            //初始化wallpaper
            WallPaper pi = new WallPaper(list_image.get(i), bitmap,dir+File.separator+fileName + ".jpg");
            listWallPaper.add(pi);
        }
        return listWallPaper;
    }

    /**获取要加载的图片*/
    public static List<String> getWallpaperToLoad(List<String> list, int loadItemAmount) {

        List<String> itemAdded = new ArrayList<>();
        if (list.size() < loadItemAmount) {
            int loopAmount = list.size();
            for (int i = 0; i < list.size(); i++) {
                itemAdded.add(list.get(i));
            }
            list.subList(0, loopAmount).clear();
        } else if (list.size() >= loadItemAmount) {
            for (int i = 0; i < loadItemAmount; i++) {
                itemAdded.add(list.get(i));
            }
            list.subList(0, loadItemAmount).clear();
        }
        return itemAdded;
    }

    /**将图片保存到本地*/
    public static void saveImage2Local(String fileName, Context context,String outputPath,boolean saveWallpaper) {
        File file = new File(outputPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        File pic = new File(file.getAbsolutePath() +File.separator+ fileName + ".jpg");
        try {
            if (!pic.exists()) {
                pic.createNewFile();
            }else if(pic.exists() && saveWallpaper){
                Toast.makeText(context, "图片文件已存在", Toast.LENGTH_SHORT).show();
            }else return;
            FileOutputStream out = new FileOutputStream(pic);
            Bitmap bitmap = getBitmapFromAssets(context,fileName,null);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            //通知系统有新的图片加入
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            //如果是点击保存图片的按钮，就显示Toast
            if (saveWallpaper){
                Toast.makeText(context, "图片文件保存成功", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            if (saveWallpaper){
                Toast.makeText(context, "图片文件保存失败", Toast.LENGTH_SHORT).show();
            }
            e.printStackTrace();
        }
    }

    public static void saveImageAsWallpaper(Bitmap bitmap, Context context){
        try {
            WallpaperManager wpm = WallpaperManager.getInstance(context);
            wpm.setBitmap(bitmap);
            Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "设置失败", Toast.LENGTH_SHORT).show();
        }
    }

    public static Bitmap getBitmapFromAssets(Context context,String name,BitmapFactory.Options opntions){
        InputStream inputStream=null;
        Bitmap bitmap = null;
        try {
            String temp = "pics" + File.separator +name;
            inputStream = context.getAssets().open(temp);
            if (opntions==null){
                bitmap = BitmapFactory.decodeStream(inputStream);
            }else{
                bitmap = BitmapFactory.decodeStream(inputStream,null,opntions);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**检查网络状况*/
    public static boolean checkNetworkConnect(Context con) {
        ConnectivityManager cwjManager = (ConnectivityManager) con
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            if (cwjManager.getActiveNetworkInfo() != null
                    && cwjManager.getActiveNetworkInfo().isAvailable()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 创建目录 PicsCache */
    public static File createFileDir(Context context, String dirName) {
        String filePath = context.getCacheDir().getPath() + File.separator + dirName;
        File destDir = new File(filePath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return destDir;
    }
    /** 递归 获取缓存文件数量*/
    public static long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles();
                if (subFiles != null) {
                    int num = subFiles.length;
                    for (int i = 0; i < num; i++) {
                        size += getFileSize(subFiles[i]);
                    }
                }
            } else {
                size += file.length();
            }
        }
        return size;
    }
    /** 删除文件 是否保留路径文件夹*/
    public static void delFile(File file, boolean delThisPath) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                int num = subFiles.length;
                // 删除子目录和文件
                for (int i = 0; i < num; i++) {
                    delFile(subFiles[i], true);
                }
            }
        }
        if (delThisPath) {
            file.delete();
        }
    }
    /**保存Bitmap到指定文件中*/
    public static void saveBitmap(File dir, String fileName, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        File file = new File(dir, fileName+".jpg");
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**判断图片 是否存在*/
    public static Boolean isFileExists(File dir, String fileName){
        return new File(dir,fileName).exists();
    }

    /**
     * 清理缓存图片，保持一定的个数
     */

  /*  public static void deleteCachePics(Context context, String dirName){
        String dirPath = context.getCacheDir().getPath() + File.separator + dirName;
        File file = new File(dirPath);
        int accountFile = (int)getFileSize(file);
        if(accountFile>20){
            List<Integer> integers = getRandomNumber(accountFile,10);
            for (int i=0;i<accountFile;i++){
                if(integers.contains(i)){
                    file.listFiles()[i].delete();
                }
            }
        }

    }

    public static List<Integer> getRandomNumber(int range, int account){
        if (range <=0 || account<=0) return null;

        List<Integer> numberChoose =new ArrayList();

        while (numberChoose.size()==account){
            int temp = (int) Math.ceil(Math.random() * range);
            if (numberChoose.contains(temp)) {
                numberChoose.add((int) Math.ceil(Math.random() * range));
            }
        }
        return numberChoose;
    }*/
}
