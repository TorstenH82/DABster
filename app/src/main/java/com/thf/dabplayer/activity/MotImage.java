package com.thf.dabplayer.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.thf.dabplayer.utils.C0162a;

/* renamed from: com.ex.dabplayer.pad.activity.MotImage */
/* loaded from: classes.dex */
public class MotImage extends ImageView {
    public static final int MAX_BRIGHTNESS = 100;
    private static final float MAX_DIM_FACTOR = 1.0f;
    public static final int MIN_BRIGHTNESS = 0;
    private static final float NO_DIM_FACTOR = 0.0f;
    public static final int SRC_DAB_MOT_SLS = 2;
    private static final int SRC_DEFAULT = 0;
    public static final int SRC_STATION_LOGO = 1;
    private Drawable mDefaultDrawable;
    private float mDimFactor;
    private int mMaxHeight;
    private float mMaxScaleFactor;
    private int mMaxWidth;
    private int mSource;

    public MotImage(Context context) {
        this(context, null);
    }

    public MotImage(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MotImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MotImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        this.mMaxWidth = 0;
        this.mMaxHeight = 0;
        initMotImage(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initMotImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mDefaultDrawable = getDrawable().mutate();
        this.mMaxScaleFactor = 2.0f;
    }

    @Override // android.widget.ImageView, android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        redraw();
    }

    @Override // android.view.View
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mMaxWidth == 0 && this.mMaxHeight == 0) {
            int w = right - left;
            int h = bottom - top;
            if (w > 0 && h > 0) {
                this.mMaxWidth = w;
                this.mMaxHeight = h;
            }
        }
    }

    public final int getSource() {
        return this.mSource;
    }

    public final int getBrightness() {
        return 100 - ((int) (this.mDimFactor * 100.0f));
    }

    public void setDefaultImage() {
        setImageDrawable(this.mDefaultDrawable);
        this.mSource = 0;
        invalidate();
    }

    public void setImage(@NonNull Drawable drawable, int source) {
        if (source != 0) {
            setImageDrawable(drawable);
            this.mSource = source;
            invalidate();
        }
    }

    public void setMaxScaleFactor(float scaleFactor) {
        if (scaleFactor != this.mMaxScaleFactor) {
            this.mMaxScaleFactor = scaleFactor;
            invalidate();
        }
    }

    public void setBrightness(int brightness) {
        if (this.mSource != 0) {
            float dimFactor = (100.0f - brightness) / 100.0f;
            if (dimFactor >= NO_DIM_FACTOR && dimFactor <= 1.0f && this.mDimFactor != dimFactor) {
                this.mDimFactor = dimFactor;
                if (dimFactor == NO_DIM_FACTOR) {
                    clearColorFilter();
                    return;
                }
                int c = 255 - ((int) (255.0f * dimFactor));
                setColorFilter(Color.rgb(c, c, c), PorterDuff.Mode.MULTIPLY);
                invalidate();
            }
        }
    }

    public void setMaxDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (this.mMaxWidth != width || this.mMaxHeight != height) {
            this.mMaxWidth = width;
            this.mMaxHeight = height;
            getParent().requestLayout();
        }
    }

    private void redraw() {
        Drawable drawable;
        int maxWidth = this.mMaxWidth;
        int maxHeight = this.mMaxHeight;
        float scaleFactor = this.mMaxScaleFactor;
        float heightRatio = 1.0f;
        int intrHeight = 0;
        int intrWidth = 0;
        if (maxWidth > 0 && maxHeight > 0 && (drawable = getDrawable()) != null) {
            intrHeight = drawable.getIntrinsicHeight();
            intrWidth = drawable.getIntrinsicWidth();
            if (intrWidth <= 0 || intrHeight <= 0) {
                C0162a.m9a("MOT is damaged");
                return;
            }
            SharedPreferences settingsPreferences = getContext().getSharedPreferences(SettingsActivity.prefname_settings, 0);
            if (settingsPreferences.getBoolean(SettingsActivity.pref_key_background_boxes, false)) {
                maxHeight -= 8;
                maxWidth -= 8;
            }
            float widthRatio = maxWidth / intrWidth;
            heightRatio = maxHeight / intrHeight;
            if (widthRatio <= heightRatio) {
                heightRatio = widthRatio;
            }
            if (heightRatio > scaleFactor) {
                heightRatio = scaleFactor;
            }
            if (this.mSource == 0) {
                heightRatio = 1.0f;
            }
        }
        setScaleX(heightRatio);
        setScaleY(heightRatio);
        setAdjustViewBounds(true);
        if (!isInEditMode()) {
            C0162a.m9a("MotImage " + intrWidth + "x" + intrHeight + " scaled to " + Math.round(intrWidth * heightRatio) + "x" + Math.round(intrHeight * heightRatio) + " (" + heightRatio + " x), max " + maxWidth + "x" + maxHeight + ", dim " + this.mDimFactor);
        }
    }

    @NonNull
    public Bitmap getBitmap() {
        Bitmap bitmap;
        Drawable drawable = getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
