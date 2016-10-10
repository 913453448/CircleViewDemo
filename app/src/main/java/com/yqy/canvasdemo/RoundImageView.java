package com.yqy.canvasdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.cisetech.circleviewdemo.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * @author EX_YINQINGYANG
 * @version [Android PABank C01, @2016-10-08]
 * @date 2016-10-08
 * @description
 */
public class RoundImageView extends ImageView {
    /**
     * 圆角ImageView圆角的半径大小
     */
    private int mRadius=dp2px(10);
    /**
     * 圆形类型
     */
    private int TYPE_CIRCLE=0;
    /**
     * 圆角类型
     */
    private int TYPED_ROUND=1;
    /**
     * 图片类型
     */
    private int mType=TYPE_CIRCLE;
    /**
     * 图片缩放模式
     */
    private ScaleType mScaleType;
    /**
     * 缓存bitmap
     */
    private WeakReference<Bitmap>mWeakReference;
    /**
     * 模板Bitmap
     */
    private Bitmap mMaskBitmap;
    /**
     * 画笔
     */
    private Paint mPaint;
    /**
     * shape paint
     */
    private Paint shapePaint;
    /**
     * 画笔Xfermode
     */
    private Xfermode mXfermode=new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
    public RoundImageView(Context context) {
        this(context,null);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainStyleAttr(context,attrs,defStyleAttr);
        mScaleType=getScaleType();
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        shapePaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    }

    private void obtainStyleAttr(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a=context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundImageView,defStyleAttr,0);
        mRadius=a.getDimensionPixelSize(R.styleable.RoundImageView_borderRadius,mRadius);
        mType=a.getInteger(R.styleable.RoundImageView_type,mType);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = mWeakReference==null?null:mWeakReference.get();
        if(bitmap==null || bitmap.isRecycled()){
            //获取一下设置的图片资源
            Drawable drawable=getDrawable();
            if(drawable!=null){
                //创建一个空白画布，用来画模板跟原图
                bitmap=Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);
                Matrix matrix=null;
                Canvas dstCanvas=new Canvas(bitmap);
                dstCanvas.save();
                if (getScaleType()==ScaleType.FIT_XY){
                    drawable.setBounds(0,0,getWidth(),getHeight());
                    matrix=null;
                }else{
                    matrix=new Matrix();
                    configureBounds(drawable,matrix);
                }
                if(matrix!=null){
                    dstCanvas.concat(matrix);
                }
                drawable.draw(dstCanvas);
                dstCanvas.restore();
                //画模板
                if(mMaskBitmap==null||mMaskBitmap.isRecycled()){
                    mMaskBitmap=getShapeBitmap();
                }
                dstCanvas.drawBitmap(mMaskBitmap,0,0,mPaint);
                mPaint.setXfermode(null);
            }
        }
        //最后把我们准备好的Bitmap画在canvas上
        canvas.drawBitmap(bitmap,0,0,null);
    }

    /**
     * 根据Shape类型创建ShapeBitmap
     */
    private Bitmap getShapeBitmap() {
        Bitmap bitmap=Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas =new Canvas(bitmap);
        if(TYPE_CIRCLE==mType){
            canvas.drawCircle(canvas.getWidth()/2,canvas.getHeight()/2,canvas.getWidth()/2,shapePaint);
        } else{
            canvas.drawRoundRect(new RectF(0,0,canvas.getWidth(),canvas.getHeight()),mRadius,mRadius,shapePaint);
        }
        return bitmap;
    }
    private void configureBounds(Drawable drawable, Matrix matrix) {
        ScaleType mScaleType = getScaleType();
        //获取图片的宽高
        int dwidth = drawable.getIntrinsicWidth();
        int dheight = drawable.getIntrinsicHeight();
        int vwidth =getWidth();
        int vheight = getHeight();
        if (ScaleType.MATRIX == mScaleType) {
            /////
        } else if (ScaleType.CENTER_CROP == mScaleType) {
            float scale;
            float dx = 0, dy = 0;

            if (dwidth * vheight > vwidth * dheight) {
                scale = (float) vheight / (float) dheight;
                dx = (vwidth - dwidth * scale) * 0.5f;
            } else {
                scale = (float) vwidth / (float) dwidth;
                dy = (vheight - dheight * scale) * 0.5f;
            }

            matrix.setScale(scale, scale);
            matrix.postTranslate(Math.round(dx), Math.round(dy));
        } else if (ScaleType.CENTER_INSIDE == mScaleType) {
            float scale;
            float dx;
            float dy;

            if (dwidth <= vwidth && dheight <= vheight) {
                scale = 1.0f;
            } else {
                scale = Math.min((float) vwidth / (float) dwidth,
                        (float) vheight / (float) dheight);
            }
            dx = Math.round((vwidth - dwidth * scale) * 0.5f);
            dy = Math.round((vheight - dheight * scale) * 0.5f);
            matrix.setScale(scale, scale);
            matrix.postTranslate(dx, dy);
        } else {
            matrix.setRectToRect(new RectF(drawable.getBounds()), new RectF(0, 0, getWidth(), getHeight()), scaleTypeToScaleToFit(mScaleType));
        }
    }
    private Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType mScaleType) {
        Class mClass=ImageView.class;
        try {
            Method method = mClass.getDeclaredMethod("scaleTypeToScaleToFit", new Class[]{ScaleType.class});
            method.setAccessible(true);
            if(method!=null){
                Matrix.ScaleToFit fit = (Matrix.ScaleToFit) (method.invoke(null, new Object[]{mScaleType}));
                return fit;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Matrix.ScaleToFit.FILL;
    }
    /**
     * dp2px
     * @param value
     * @return px
     */
    private int dp2px(int value) {
        return (int) (value*getContext().getResources().getDisplayMetrics().density+0.5f);
    }
}