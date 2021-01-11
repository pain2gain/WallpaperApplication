package com.example.wallpaperapp;


import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.Toast;


public class ZoomImageView extends ImageView{

    Matrix matrix;
    PointF viewSize;                    //imageview 的大小
    PointF imageSize;                   //图片大小
    PointF scaleSize = new PointF();    //缩放后的图片大小
    float scaleOriginal ;

    private PointF bitmapOriginPoint = new PointF(); //imageView中的bitmap 的xy的坐标


    public ZoomImageView(Context context) {
        super(context);
        init();
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZoomImageView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setScaleType(ScaleType.MATRIX);
        matrix = new Matrix();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        viewSize = new PointF(width,height);

        Drawable drawable = getDrawable();
        imageSize = new PointF(drawable.getMinimumWidth(),drawable.getMinimumHeight());
        showCenter();
    }

    /**将图片放大居中展示*/
    public void showCenter(){
        float scalex = viewSize.x/imageSize.x;
        float scaley = viewSize.y/imageSize.y;

        scaleOriginal = scalex<scaley?scalex:scaley;
        scaleImage(new PointF(scaleOriginal,scaleOriginal));

        if (scalex<scaley){
            translationImage(new PointF(0,viewSize.y/2 - scaleSize.y/2));
            bitmapOriginPoint.x = 0;
            bitmapOriginPoint.y = viewSize.y/2 - scaleSize.y/2;
        }else {
            translationImage(new PointF(viewSize.x/2 - scaleSize.x/2,0));
            bitmapOriginPoint.x = viewSize.x/2 - scaleSize.x/2;
            bitmapOriginPoint.y = 0;
        }
    }
    public void translationImage(PointF pointF){
        matrix.postTranslate(pointF.x,pointF.y);
        setImageMatrix(matrix);
    }

    public void scaleImage(PointF scaleXY){
        matrix.setScale(scaleXY.x,scaleXY.y);
        scaleSize.set(scaleXY.x * imageSize.x,scaleXY.y * imageSize.y);
        setImageMatrix(matrix);
    }

}
