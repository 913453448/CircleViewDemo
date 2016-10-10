package com.cisetech.circleviewdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * author：yinqingy
 * date：2016-10-09 21:14
 * blog：http://blog.csdn.net/vv_bug
 * desc：
 */

public class CricleXfexrmode extends ImageView {
    /**
     * 默认圆角半径
     */
    public static final int DEFAULT_RADIUS = 10;
    /**
     * 圆形
     */
    public static final int TYPE_CIRCLE = 0;
    /**
     * 圆角
     */
    public static final int TYPE_ROUND = 1;
    /**
     * 图片的类型，圆形or圆角
     */
    private int mType = TYPE_CIRCLE;
    /**
     * 圆角半径
     */
    private int mRadius = DEFAULT_RADIUS;
    /**
     * 弱引用
     */
    private WeakReference<Bitmap> mWeakBitmap;
    private Paint mPaint;
    private Xfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
    private Bitmap mMaskBitmap;

    public CricleXfexrmode(Context context) {
        this(context, null);
    }

    public CricleXfexrmode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CricleXfexrmode(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainStyleAttr(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    }

    /**
     * 获取attr
     */
    private void obtainStyleAttr(Context context, AttributeSet set, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(set, R.styleable.CricleXfexrmode, defStyleAttr, 0);
        mRadius = a.getDimensionPixelSize(R.styleable.CricleXfexrmode_borderRadius, dp2px(DEFAULT_RADIUS));
        mType = a.getInteger(R.styleable.CricleXfexrmode_type, mType);
        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = mWeakBitmap == null ? null : mWeakBitmap.get();
        if (bitmap == null || bitmap.isRecycled()) {
            //获取Drawable
            Drawable drawable = getDrawable();
            if (drawable != null) {
                    bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                Matrix matrix=null;
                Canvas dstCanvas = new Canvas(bitmap);
                dstCanvas.save();
                if (getScaleType()==ScaleType.FIT_XY){
                    drawable.setBounds(0,0,getWidth(),getHeight());
                    matrix=null;
                }else{
                    matrix=new Matrix();
                    getMatric(drawable,matrix);
                }
                if(matrix!=null){
                    dstCanvas.concat(matrix);
                }
                drawable.draw(dstCanvas);
                dstCanvas.restore();
                if (mMaskBitmap == null || mMaskBitmap.isRecycled()) {
                    mMaskBitmap = getBitmap();
                }
                mPaint.reset();
                mPaint.setFilterBitmap(false);
                mPaint.setXfermode(mXfermode);
                dstCanvas.drawBitmap(mMaskBitmap, 0, 0, mPaint);
                mPaint.setXfermode(null);
                canvas.drawBitmap(bitmap, 0, 0, null);
                mWeakBitmap = new WeakReference<Bitmap>(bitmap);
            }
        } else if (bitmap != null) {
            mPaint.setXfermode(null);
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, mPaint);
        }
    }

    private Bitmap getBitmap() {
        Bitmap bitmap =null;
            bitmap=  Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        if (TYPE_CIRCLE == mType) {
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, canvas.getWidth() / 2, paint);
        } else {
            canvas.drawRoundRect(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), mRadius, mRadius, paint);
        }
        return bitmap;
    }

    @Override
    public void invalidate() {
        mWeakBitmap = null;
        if (mMaskBitmap != null) {
            mMaskBitmap.recycle();
            mMaskBitmap = null;
        }
        super.invalidate();
    }

    /**
     * dp2px
     * @param value
     * @return px
     */
    private int dp2px(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getContext().getResources().getDisplayMetrics());
    }
    private void getMatric(Drawable drawable, Matrix matrix) {
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
}
