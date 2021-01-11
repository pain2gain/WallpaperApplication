package com.example.wallpaperapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageDownLoader {

    /** 保存正在下载或等待下载的URL和相应失败下载次数（初始为0），防止多次下载 */
    private Hashtable<String, Integer> taskCollection;
    /** 缓存类 */
    private LruCache<String, Bitmap> lruCache;
    /** 线程池 */
    private ExecutorService threadPool;
    /** 缓存文件目录 （如无SD卡，则data目录下） */
    private File cacheFileDir;
    /** 缓存文件夹 */
    private static final String DIR_CACHE = "picscache";
    /** 缓存文件夹最大容量限制（10M） */
    private static final long DIR_CACHE_LIMIT = 10 * 1024 * 1024;
    /** 图片下载失败重试次数 */
    private static final int IMAGE_DOWNLOAD_FAIL_TIMES = 3;


    /**初始化*/
    public ImageDownLoader(Context context) {
        // 获取系统分配给每个应用程序的最大内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        // 给LruCache分配最大内存的1/8
        lruCache = new LruCache<String, Bitmap>(maxMemory / 8) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
        taskCollection = new Hashtable<String, Integer>();
        threadPool = Executors.newFixedThreadPool(10);
        cacheFileDir = Tools.createFileDir(context, DIR_CACHE);
    }

    /**添加Bitmap到LruCache缓存 */
    private void addLruCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            lruCache.put(key, bitmap);
        }
    }

    /**从 LruCache 缓存中获取Bitmap*/
    private Bitmap getBitmapFromMemCache(String key) {
        return lruCache.get(key);
    }

    /**下载APi中的所有的图片*/
    public void loadAllImages(final String url,final int width, final int height){

        HttpClient httpClient = new HttpClient();
        String jsonResult = httpClient.doGetForApi(url);
        //解析json格式的result，获得图片url
        String[] allPicUrl = httpClient.parseJson(jsonResult);
        for (int i=0;i<allPicUrl.length;i++){
            //下载单个图片
            loadImage(allPicUrl[i],width,height);
        }
    }

    /**
     * 异步下载图片，并按指定宽度和高度压缩图片
     * 图片下载完成后调用接口
     */
    public void loadImage(final String url, final int width, final int height) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap =downloadImage(url, width, height);
                // 将Bitmap 加入内存缓存
                addLruCache(url, bitmap);
                // 加入文件缓存前，需判断缓存目录大小是否超过限制，超过则清空缓存再加入
                long cacheFileSize = Tools.getFileSize(cacheFileDir);
                if (cacheFileSize > DIR_CACHE_LIMIT) {
                    Log.i("Log", cacheFileDir
                            + " size has exceed limit." + cacheFileSize);
                    Tools.delFile(cacheFileDir, false);
                    taskCollection.clear();
                }
                // 缓存文件名称（ 替换url中非字母和非数字的字符，防止系统误认为文件路径）
                String urlKey = url.replaceAll("[^\\w]", "");
                // 将Bitmap加入文件缓存
                Tools.saveBitmap(cacheFileDir, urlKey, bitmap);
            }
        };
        // 记录该url，防止滚动时多次下载，0代表该url下载失败次数
        taskCollection.put(url, 0);
        threadPool.execute(runnable);
    }

    /**
     * 获取Bitmap, 若内存缓存为空，则去文件缓存中获取
     */
      public Bitmap getBitmapCache(String url) {
        String urlKey =url;

        if (getBitmapFromMemCache(url) != null) {
            return getBitmapFromMemCache(url);
        } else if (Tools.isFileExists(cacheFileDir, urlKey)
                && Tools.getFileSize(new File(cacheFileDir, urlKey)) > 0) {
            // 从文件缓存中获取Bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(cacheFileDir.getPath()
                    + File.separator + urlKey);
            // 将Bitmap 加入内存缓存
            addLruCache(url, bitmap);
            return bitmap;
        }
        return null;
    }
    /**
     * 下载图片，并按指定高度和宽度压缩
     */
    private Bitmap downloadImage(String url, int width, int height) {
        Bitmap bitmap = null;
        HttpClient httpClient = new HttpClient();
        byte[] byteIn = httpClient.doGetForPicUrl(url);
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(byteIn, 0, byteIn.length, bmpFactoryOptions);
        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / height);
        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / width);
        if (heightRatio > 1 && widthRatio > 1) {
                bmpFactoryOptions.inSampleSize = heightRatio > widthRatio ? heightRatio
                        : widthRatio;
        }
        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeByteArray(byteIn, 0,
                        byteIn.length, bmpFactoryOptions);

        // 下载失败，再重新下载
        if (taskCollection.get(url) != null) {
            int times = taskCollection.get(url);
            if (bitmap == null
                    && times < IMAGE_DOWNLOAD_FAIL_TIMES) {
                times++;
                taskCollection.put(url, times);
                bitmap = downloadImage(url, width, height);
            }
        }
        return bitmap;
    }
}
