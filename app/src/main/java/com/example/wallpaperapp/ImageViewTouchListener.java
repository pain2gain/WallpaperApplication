package com.example.wallpaperapp;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class ImageViewTouchListener implements ImageView.OnTouchListener {


    private LinearLayout mLinearLayout;
    private RelativeLayout parentView;
    private Context mContext;
    private int position;
    //private List<WallPaper> mListWallPaper;
    private  WallPaper mWallPaper;
    private ZoomImageView zoomImageView;

    public ImageViewTouchListener(Context mc,int posi,WallPaper wp, RelativeLayout rlatv,ZoomImageView iv){
        mContext = mc;
        position = posi;
        mWallPaper = wp;
        parentView = rlatv;
        zoomImageView = iv;
    }

    //view和image的长宽参数设定
    private PointF viewSize;                    //imageview 的大小
    private PointF imageSize;
    private Matrix matrix;
    private PointF scaleSize = new PointF();
    private PointF originScale = new PointF();  //最初的宽度和高度的比例
    private PointF bitmapOriginPoint = new PointF(); //imageView中的bitmap 的xy的坐标


    //操作参数的设定
    private long doubleClickTimeSpan = 250; //双击时间间隔毫秒
    private long lastClickUptime = 0;       //初始化上次点击事件为0，这样可以避免第一次点击就方大
    private long longPressTime =500;       //长按时间
    PointF lastDownPoint = new PointF();   //上次点击的位置

    private int doubleClickZoom = 2;
    private PointF clickPoint = new PointF();  //点击的点

    //图片的缩放状态
    public class ZoomMode{
        public  final  static  int Ordinary=0;//普通
        public  final  static  int  ZoomIn=1;//双击放大
        public final static int TowFingerZoom = 2;//双指缩放
    }
    //放大比例
    private int zoomInMode = ZoomMode.Ordinary;
    private PointF tempPoint = new PointF();
    //最大缩放比例
    private float maxScrole = 5;
    //两点之间的距离
    private float doublePointDistance = 0;
    //双指缩放时候的中心点
    private PointF doublePointCenter = new PointF();
    //两指缩放的比例
    private float doubleFingerScrole = 0;
    //上次点击手指数量
    private int lastFingerNum = 0;
    //图片是否是最大比例
    private boolean isMaxScrole =false;

    //长按的handler
    final Handler longPressHandler = new Handler();
    Runnable longPressedRunnable = new Runnable() {
        public void run() {
            longPressAction();
        }
    };
    //标记边界
    private boolean isBoundArrived=false;


    public void setBaseParams() {

        viewSize = zoomImageView.viewSize;
        imageSize =new PointF(zoomImageView.getDrawable().getBounds().width(),zoomImageView.getDrawable().getBounds().height());

        float scalex = viewSize.x/imageSize.x;
        float scaley = viewSize.y/imageSize.y;
        matrix = new Matrix();
        float scale = scalex<scaley?scalex:scaley;
        originScale.set(scale,scale);
        scaleSize.set(scale * imageSize.x,scale * imageSize.y);
        //移动图片，并保存最初的图片左上角（即原点）所在坐标
        if (scalex<scaley){
            translationImage(new PointF(0,viewSize.y/2 - scaleSize.y/2));
            bitmapOriginPoint.x = 0;
            bitmapOriginPoint.y = viewSize.y/2 - scaleSize.y/2;
        }else {
            translationImage(new PointF(viewSize.x/2 - scaleSize.x/2,0));
            bitmapOriginPoint.x = viewSize.x/2 - scaleSize.x/2;
            bitmapOriginPoint.y = 0;
        }
        showCenter();
    }

    private void showCenter(){
            float scalex = viewSize.x/imageSize.x;
            float scaley = viewSize.y/imageSize.y;

            float scale = scalex<scaley?scalex:scaley;

            scaleImage(new PointF(scale,scale));

            if (scalex<scaley){
                translationImage(new PointF(0,viewSize.y/2 - scaleSize.y/2));
                bitmapOriginPoint.x = 0;
                bitmapOriginPoint.y = viewSize.y/2 - scaleSize.y/2;
            }else {
                translationImage(new PointF(viewSize.x/2 - scaleSize.x/2,0));
                bitmapOriginPoint.x = viewSize.x/2 - scaleSize.x/2;
                bitmapOriginPoint.y = 0;
            }
        originScale.set(scale,scale);
    }

    public void translationImage(PointF pointF){
        matrix.postTranslate(pointF.x,pointF.y);
        zoomImageView.setImageMatrix(matrix);
    }

    public void scaleImage(PointF scaleXY){
        matrix.setScale(scaleXY.x,scaleXY.y);
        scaleSize.set(scaleXY.x * imageSize.x,scaleXY.y * imageSize.y);
        zoomImageView.setImageMatrix(matrix);
    }



    @Override
    public boolean onTouch(View v, MotionEvent event){
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //手指按下事件

                if (zoomInMode != ZoomMode.Ordinary){
                    if (zoomImageView.getParent() instanceof ViewPager){
                        //请求父类不拦截
                        zoomImageView.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }

                if (event.getPointerCount()==1){
                    longPressHandler.postDelayed(longPressedRunnable, longPressTime);
                }
                clickPoint.set(event.getX(),event.getY());
                lastDownPoint = new PointF(event.getX(),event.getY());
                if (event.getPointerCount()==1){
                    if(System.currentTimeMillis()-lastClickUptime<=doubleClickTimeSpan){
                        setBaseParams();
                        if (zoomInMode == ZoomMode.Ordinary) {
                            //记录这个点，用来计算放大后的偏移量
                            tempPoint.set((clickPoint.x - bitmapOriginPoint.x) / scaleSize.x,
                                            (clickPoint.y-bitmapOriginPoint.y) / scaleSize.y);
                            //放大
                            scaleImage(new PointF(originScale.x * doubleClickZoom,
                                    originScale.y * doubleClickZoom));
                            //获取放大以后的图片左上点的坐标
                            getBitmapOffset();
                            //平移图片，使得被点击的点的位置不变。这里是计算缩放后被点击的xy坐标，与原始点击的位置的xy坐标值，计算出差值，然后做平移动作
                            translationImage(new PointF(clickPoint.x - (bitmapOriginPoint.x + tempPoint.x*scaleSize.x),
                                                        clickPoint.y - (bitmapOriginPoint.y + tempPoint.y * scaleSize.y)));
                            zoomInMode = ZoomMode.ZoomIn;
                            doubleFingerScrole = originScale.x*doubleClickZoom;
                        } else {
                            showCenter();
                            zoomInMode = ZoomMode.Ordinary;
                            doubleFingerScrole = originScale.x;
                        }
                    }
                }
                lastClickUptime = System.currentTimeMillis();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                //屏幕上已经有一个点按住 再按下一点时触发该事件
                longPressHandler.removeCallbacks(longPressedRunnable);
                //计算最初的两个手指之间的距离
                doublePointDistance = getDoubleFingerDistance(event);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                //屏幕上已经有两个点按住 再松开一点时触发该事件
                longPressHandler.removeCallbacks(longPressedRunnable);
                //记录此时的双指缩放比例
                if (zoomInMode==ZoomMode.Ordinary) {
                    doubleFingerScrole = zoomImageView.scaleOriginal / zoomImageView.imageSize.x;
                }else{
                    //当有一个手指离开屏幕后，就修改状态，这样如果双击屏幕就能恢复到初始大小
                    zoomInMode = ZoomMode.ZoomIn;
                    doubleFingerScrole = scaleSize.x/imageSize.x;
                    //判断缩放后的比例，如果小于最初的那个比例，就恢复到最初的大小
                    if (scaleSize.x<=viewSize.x && scaleSize.y<=viewSize.y){
                        zoomInMode = ZoomMode.Ordinary;
                        showCenter();
                    }
                }
                //记录此时屏幕触碰的点的数量
                lastFingerNum = 1;
                break;

            case MotionEvent.ACTION_MOVE:
                //手指移动时触发事件

                //与Action down时的位置比较，移动距离大于15时取消长按事件
                float currentX = event.getX();
                float currentY = event.getY();
                double distance = Math.sqrt(
                        (currentY - lastDownPoint.y) * (currentY - lastDownPoint.y) +
                                ((currentX - lastDownPoint.x) * (currentX - lastDownPoint.x)));
                if (distance > 20) {
                    longPressHandler.removeCallbacks(longPressedRunnable);
                }

                if (zoomInMode != ZoomMode.Ordinary ) {
                    if (isBoundArrived){
                        //请求父类不拦截
                        zoomImageView.getParent().requestDisallowInterceptTouchEvent(false);
                    }else zoomImageView.getParent().requestDisallowInterceptTouchEvent(true);

                    //如果是多指，计算中心点为假设的点击的点
                    currentX = 0;
                    currentY = 0;
                    //获取此时屏幕上被触碰的点有多少个
                    int pointCount = event.getPointerCount();
                    //计算出中间点所在的坐标
                    for (int i = 0; i < pointCount; i++) {
                        currentX += event.getX(i);
                        currentY += event.getY(i);
                    }
                    currentX /= pointCount;
                    currentY /= pointCount;
                    //当屏幕被触碰的点的数量变化时，将最新算出来的中心点看作是被点击的点
                    if (lastFingerNum != event.getPointerCount()) {
                        clickPoint.x = currentX;
                        clickPoint.y = currentY;
                        lastFingerNum = event.getPointerCount();
                    }
                    //将移动手指时，实时计算出来的中心点坐标，减去被点击点的坐标就得到了需要移动的距离
                    float moveX = currentX - clickPoint.x;
                    float moveY = currentY - clickPoint.y;
                    //计算边界，使得不能已出边界，但是如果是双指缩放时移动，因为存在缩放效果，
                    //所以此时的边界判断无效
                    float[] moveFloat = moveBorderDistance(moveX, moveY);
                    //移动图片
                    translationImage(new PointF(moveFloat[0], moveFloat[1]));
                    clickPoint.set(currentX, currentY);
                }

                //判断当前是两个手指接触到屏幕才处理缩放事件
                if (event.getPointerCount() == 2){
                    //如果此时缩放比例最大，就不操作，否则初始化参数
                    if (isMaxScrole && getDoubleFingerDistance(event) - doublePointDistance > 0 &&
                            zoomInMode!=ZoomMode.Ordinary) {
                        break;
                    }else {
                        setBaseParams();
                    }
                    //这里设置当双指缩放的的距离变化量大于10，并且当前不是在双指缩放状态下，
                    //就计算中心点，等一些初始化缩放的操作
                    if (Math.abs(getDoubleFingerDistance(event) - doublePointDistance )> 10
                            && zoomInMode != ZoomMode.TowFingerZoom ){
                        //计算两个手指之间的中心点，当作放大的中心点
                        doublePointCenter.set((event.getX(0) + event.getX(1))/2,
                                (event.getY(0) + event.getY(1))/2);
                        clickPoint.set(doublePointCenter);
                        //下面就和双击放大基本一样
                        getBitmapOffset();
                        tempPoint.set((clickPoint.x - bitmapOriginPoint.x)/scaleSize.x,
                                (clickPoint.y - bitmapOriginPoint.y)/scaleSize.y);
                        //设置进入双指缩放状态
                        zoomInMode = ZoomMode.TowFingerZoom;
                    }

                    //如果已经进入双指缩放状态，就直接计算缩放的比例，并进行位移
                    if (zoomInMode == ZoomMode.TowFingerZoom){
                        //用当前的缩放比例加上原始放大的比例 与此时双指间距离的缩放比例相乘，就得到对应的图片应该缩放的比例
                        float scrole =
                                (doubleFingerScrole+zoomImageView.scaleOriginal)*getDoubleFingerDistance(event)/doublePointDistance;
                        //当缩放比例大于最大缩放比例，且手指继续放大则设置标志
                        if ((scrole > zoomImageView.scaleOriginal * maxScrole) &&
                                getDoubleFingerDistance(event) - doublePointDistance > 0) {
                            isMaxScrole = true;
                        }
                        //这里也是和双击放大时一样的
                        tempPoint.set((clickPoint.x - bitmapOriginPoint.x)/scaleSize.x,
                                     (clickPoint.y - bitmapOriginPoint.y)/scaleSize.y);
                        scaleImage(new PointF(scrole,scrole));
                        getBitmapOffset();
                        translationImage(
                                new PointF(clickPoint.x - (bitmapOriginPoint.x + tempPoint.x*scaleSize.x),
                                        clickPoint.y - (bitmapOriginPoint.y + tempPoint.y*scaleSize.y))
                        );
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //手指松开时触发事件

                longPressHandler.removeCallbacks(longPressedRunnable);
                isMaxScrole=false;
                lastFingerNum=0;
                /*if (isLongtimePress(event,lastDownPoint,longPressTime)){
                    longPressAction();
                }*/
            break;

            case MotionEvent.ACTION_CANCEL:
                longPressHandler.removeCallbacks(longPressedRunnable);
                break;

            default:zoomImageView.showCenter();
        }
        return true;
    }

    //获取view中bitmap的坐标点，得到左上方点
    public void getBitmapOffset(){
        float[] value = new float[9];
        float[] offset = new float[2];
        Matrix imageMatrix = zoomImageView.getImageMatrix();
        imageMatrix.getValues(value);
        offset[0] = value[2];
        offset[1] = value[5];
        bitmapOriginPoint.set(offset[0],offset[1]);
    }

    public static float  getDoubleFingerDistance(MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return  (float)Math.sqrt(x * x + y * y) ;
    }

    public float[] moveBorderDistance(float moveX,float moveY){
        //计算bitmap的左上角坐标
        getBitmapOffset();

        //计算bitmap的右下角坐标
        float bitmapRightBottomX = bitmapOriginPoint.x + scaleSize.x;
        float bitmapRightBottomY = bitmapOriginPoint.y + scaleSize.y;

        if (bitmapOriginPoint.x + moveX > 0 || bitmapRightBottomX + moveX < viewSize.x){
            isBoundArrived=true;
        }else{
            isBoundArrived=false;
        }
        if (moveY > 0){
            //向下滑
            if (bitmapOriginPoint.y + moveY > 0){
                if (bitmapOriginPoint.y < 0){
                    moveY = -bitmapOriginPoint.y;
                }else {
                    moveY = 0;
                }
            }
        }else if (moveY < 0){
            //向上滑
            if (bitmapRightBottomY + moveY < viewSize.y){
                if (bitmapRightBottomY > viewSize.y){
                    moveY = -(bitmapRightBottomY - viewSize.y);
                }else {
                    moveY = 0;
                }
            }
        }

        if (moveX > 0){
            //向右滑
            if (bitmapOriginPoint.x + moveX > 0){
                if (bitmapOriginPoint.x < 0){
                    moveX = -bitmapOriginPoint.x;
                }else {
                    moveX = 0;
                }
            }
        }else if (moveX < 0){
            //向左滑
            if (bitmapRightBottomX + moveX < viewSize.x){
                if (bitmapRightBottomX > viewSize.x){
                    moveX = -(bitmapRightBottomX - viewSize.x);
                }else {
                    moveX = 0;
                }
            }
        }
        return new float[]{moveX,moveY};
    }


    public void longPressAction(){
        View.inflate(mContext, R.layout.popup_button, parentView);
        mLinearLayout=parentView.findViewById(R.id.popup_button_layout);
        Button popupButtonWallpaper = mLinearLayout.findViewById(R.id.popup_button_wallpaper);
        Button popupButtonSave = mLinearLayout.findViewById(R.id.popup_button_save);
        popupButtonWallpaper.setAlpha((float) 0.9);

        final WallPaper wallPaper = mWallPaper;

        popupButtonWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.saveImageAsWallpaper(wallPaper.getWallPaperBitmap(),mContext);
                parentView.removeView(mLinearLayout);
            }
        });

        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentView.removeView(mLinearLayout);
            }
        });

        popupButtonSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "wallpapers"+File.separator;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Tools.saveImage2Local(wallPaper.getName(),mContext,dir,true);
                        Looper.loop();
                    }
                }).start();
                parentView.removeView(mLinearLayout);
            }
        });
    }
    }
