package com.thf.dabplayer.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
//>import android.support.p000v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.view.ViewCompat;

/* renamed from: com.ex.dabplayer.pad.activity.ColorPickerView */
/* loaded from: classes.dex */
public class ColorPickerView extends View {
    private static final float BORDER_WIDTH_PX = 1.0f;
    private static final int PANEL_ALPHA = 2;
    private static final int PANEL_HUE = 1;
    private static final int PANEL_SAT_VAL = 0;
    private static float mDensity = 1.0f;
    private float ALPHA_PANEL_HEIGHT;
    private float HUE_PANEL_WIDTH;
    private float PALETTE_CIRCLE_TRACKER_RADIUS;
    private float PANEL_SPACING;
    private float RECTANGLE_TRACKER_OFFSET;
    private int mAlpha;
    private Paint mAlphaPaint;
    private RectF mAlphaRect;
    private Shader mAlphaShader;
    private String mAlphaSliderText;
    private Paint mAlphaTextPaint;
    private int mBorderColor;
    private Paint mBorderPaint;
    private float mDrawingOffset;
    private RectF mDrawingRect;
    private float mHue;
    private Paint mHuePaint;
    private RectF mHueRect;
    private Shader mHueShader;
    private Paint mHueTrackerPaint;
    private int mLastTouchedPanel;
    private OnColorChangedListener mListener;
    private float mSat;
    private Shader mSatShader;
    private Paint mSatValPaint;
    private RectF mSatValRect;
    private Paint mSatValTrackerPaint;
    private boolean mShowAlphaPanel;
    private int mSliderTrackerColor;
    private Point mStartTouchPoint;
    private float mVal;
    private Shader mValShader;

    /* renamed from: com.ex.dabplayer.pad.activity.ColorPickerView$OnColorChangedListener */
    /* loaded from: classes.dex */
    public interface OnColorChangedListener {
        void onColorChanged(int i);
    }

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.HUE_PANEL_WIDTH = 30.0f;
        this.ALPHA_PANEL_HEIGHT = 20.0f;
        this.PANEL_SPACING = 10.0f;
        this.PALETTE_CIRCLE_TRACKER_RADIUS = 5.0f;
        this.RECTANGLE_TRACKER_OFFSET = 2.0f;
        this.mAlpha = 255;
        this.mHue = 360.0f;
        this.mSat = 0.0f;
        this.mVal = 0.0f;
        this.mAlphaSliderText = "Alpha";
        this.mSliderTrackerColor = -14935012;
        this.mBorderColor = -9539986;
        this.mShowAlphaPanel = false;
        this.mLastTouchedPanel = 0;
        this.mStartTouchPoint = null;
        init();
    }

    private void init() {
        mDensity = getContext().getResources().getDisplayMetrics().density;
        this.PALETTE_CIRCLE_TRACKER_RADIUS *= mDensity;
        this.RECTANGLE_TRACKER_OFFSET *= mDensity;
        this.HUE_PANEL_WIDTH *= mDensity;
        this.ALPHA_PANEL_HEIGHT *= mDensity;
        this.PANEL_SPACING *= mDensity;
        this.mDrawingOffset = calculateRequiredOffset();
        initPaintTools();
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void initPaintTools() {
        this.mSatValPaint = new Paint();
        this.mSatValTrackerPaint = new Paint();
        this.mHuePaint = new Paint();
        this.mHueTrackerPaint = new Paint();
        this.mAlphaPaint = new Paint();
        this.mAlphaTextPaint = new Paint();
        this.mBorderPaint = new Paint();
        this.mSatValTrackerPaint.setStyle(Paint.Style.STROKE);
        this.mSatValTrackerPaint.setStrokeWidth(mDensity * 2.0f);
        this.mSatValTrackerPaint.setAntiAlias(true);
        this.mHueTrackerPaint.setColor(this.mSliderTrackerColor);
        this.mHueTrackerPaint.setStyle(Paint.Style.STROKE);
        this.mHueTrackerPaint.setStrokeWidth(mDensity * 2.0f);
        this.mHueTrackerPaint.setAntiAlias(true);
        this.mAlphaTextPaint.setColor(-14935012);
        this.mAlphaTextPaint.setTextSize(14.0f * mDensity);
        this.mAlphaTextPaint.setAntiAlias(true);
        this.mAlphaTextPaint.setTextAlign(Paint.Align.CENTER);
        this.mAlphaTextPaint.setFakeBoldText(true);
    }

    private float calculateRequiredOffset() {
        float offset = Math.max(this.PALETTE_CIRCLE_TRACKER_RADIUS, this.RECTANGLE_TRACKER_OFFSET);
        return 1.5f * Math.max(offset, 1.0f * mDensity);
    }

    private int[] buildHueColorArray() {
        int[] hue = new int[361];
        int count = 0;
        int i = hue.length - 1;
        while (i >= 0) {
            hue[count] = Color.HSVToColor(new float[]{i, 1.0f, 1.0f});
            i--;
            count++;
        }
        return hue;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mDrawingRect.width() > 0.0f && this.mDrawingRect.height() > 0.0f) {
            drawSatValPanel(canvas);
            drawHuePanel(canvas);
            drawAlphaPanel(canvas);
        }
    }

    private void drawSatValPanel(Canvas canvas) {
        RectF rect = this.mSatValRect;
        int rgb = Color.HSVToColor(new float[]{this.mHue, 1.0f, 1.0f});
        this.mBorderPaint.setColor(this.mBorderColor);
        canvas.drawRect(this.mDrawingRect.left, this.mDrawingRect.top, 1.0f + rect.right, 1.0f + rect.bottom, this.mBorderPaint);
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(1, null);
        }
        if (this.mValShader == null) {
            this.mValShader = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom, -1, (int) ViewCompat.MEASURED_STATE_MASK, Shader.TileMode.CLAMP);
        }
        this.mSatShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top, -1, rgb, Shader.TileMode.CLAMP);
        ComposeShader mShader = new ComposeShader(this.mValShader, this.mSatShader, PorterDuff.Mode.MULTIPLY);
        this.mSatValPaint.setShader(mShader);
        canvas.drawRect(rect, this.mSatValPaint);
        Point p = satValToPoint(this.mSat, this.mVal);
        this.mSatValTrackerPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
        canvas.drawCircle(p.x, p.y, this.PALETTE_CIRCLE_TRACKER_RADIUS - (1.0f * mDensity), this.mSatValTrackerPaint);
        this.mSatValTrackerPaint.setColor(-2236963);
        canvas.drawCircle(p.x, p.y, this.PALETTE_CIRCLE_TRACKER_RADIUS, this.mSatValTrackerPaint);
    }

    private void drawHuePanel(Canvas canvas) {
        RectF rect = this.mHueRect;
        this.mBorderPaint.setColor(this.mBorderColor);
        canvas.drawRect(rect.left - 1.0f, rect.top - 1.0f, rect.right + 1.0f, 1.0f + rect.bottom, this.mBorderPaint);
        if (this.mHueShader == null) {
            this.mHueShader = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom, buildHueColorArray(), (float[]) null, Shader.TileMode.CLAMP);
            this.mHuePaint.setShader(this.mHueShader);
        }
        canvas.drawRect(rect, this.mHuePaint);
        float rectHeight = (4.0f * mDensity) / 2.0f;
        Point p = hueToPoint(this.mHue);
        RectF r = new RectF();
        r.left = rect.left - this.RECTANGLE_TRACKER_OFFSET;
        r.right = rect.right + this.RECTANGLE_TRACKER_OFFSET;
        r.top = p.y - rectHeight;
        r.bottom = p.y + rectHeight;
        canvas.drawRoundRect(r, 2.0f, 2.0f, this.mHueTrackerPaint);
    }

    private void drawAlphaPanel(Canvas canvas) {
    }

    private Point hueToPoint(float hue) {
        RectF rect = this.mHueRect;
        float height = rect.height();
        Point p = new Point();
        p.y = (int) ((height - ((hue * height) / 360.0f)) + rect.top);
        p.x = (int) rect.left;
        return p;
    }

    private Point satValToPoint(float sat, float val) {
        RectF rect = this.mSatValRect;
        float height = rect.height();
        float width = rect.width();
        Point p = new Point();
        p.x = (int) ((sat * width) + rect.left);
        p.y = (int) (((1.0f - val) * height) + rect.top);
        return p;
    }

    private Point alphaToPoint(int alpha) {
        RectF rect = this.mAlphaRect;
        float width = rect.width();
        Point p = new Point();
        p.x = (int) ((width - ((alpha * width) / 255.0f)) + rect.left);
        p.y = (int) rect.top;
        return p;
    }

    private float[] pointToSatVal(float x, float y) {
        float x2;
        float y2;
        RectF rect = this.mSatValRect;
        float[] result = new float[2];
        float width = rect.width();
        float height = rect.height();
        if (x < rect.left) {
            x2 = 0.0f;
        } else if (x > rect.right) {
            x2 = width;
        } else {
            x2 = x - rect.left;
        }
        if (y < rect.top) {
            y2 = 0.0f;
        } else if (y > rect.bottom) {
            y2 = height;
        } else {
            y2 = y - rect.top;
        }
        result[0] = (1.0f / width) * x2;
        result[1] = 1.0f - ((1.0f / height) * y2);
        return result;
    }

    private float pointToHue(float y) {
        float y2;
        RectF rect = this.mHueRect;
        float height = rect.height();
        if (y < rect.top) {
            y2 = 0.0f;
        } else if (y > rect.bottom) {
            y2 = height;
        } else {
            y2 = y - rect.top;
        }
        return 360.0f - ((y2 * 360.0f) / height);
    }

    private int pointToAlpha(int x) {
        int x2;
        RectF rect = this.mAlphaRect;
        int width = (int) rect.width();
        if (x < rect.left) {
            x2 = 0;
        } else if (x > rect.right) {
            x2 = width;
        } else {
            x2 = x - ((int) rect.left);
        }
        return 255 - ((x2 * 255) / width);
    }

    @Override // android.view.View
    public boolean onTrackballEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean update = false;
        if (event.getAction() == 2) {
            switch (this.mLastTouchedPanel) {
                case 0:
                    float sat = this.mSat + (x / 50.0f);
                    float val = this.mVal - (y / 50.0f);
                    if (sat < 0.0f) {
                        sat = 0.0f;
                    } else if (sat > 1.0f) {
                        sat = 1.0f;
                    }
                    if (val < 0.0f) {
                        val = 0.0f;
                    } else if (val > 1.0f) {
                        val = 1.0f;
                    }
                    this.mSat = sat;
                    this.mVal = val;
                    update = true;
                    break;
                case 1:
                    float hue = this.mHue - (y * 10.0f);
                    if (hue < 0.0f) {
                        hue = 0.0f;
                    } else if (hue > 360.0f) {
                        hue = 360.0f;
                    }
                    this.mHue = hue;
                    update = true;
                    break;
                case 2:
                    if (!this.mShowAlphaPanel || this.mAlphaRect == null) {
                        update = false;
                        break;
                    } else {
                        int alpha = (int) (this.mAlpha - (x * 10.0f));
                        if (alpha < 0) {
                            alpha = 0;
                        } else if (alpha > 255) {
                            alpha = 255;
                        }
                        this.mAlpha = alpha;
                        update = true;
                        break;
                    }
            }
        }
        if (update) {
            if (this.mListener != null) {
                this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal}));
            }
            invalidate();
            return true;
        }
        return super.onTrackballEvent(event);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        boolean update = false;
        switch (event.getAction()) {
            case 0:
                this.mStartTouchPoint = new Point((int) event.getX(), (int) event.getY());
                update = moveTrackersIfNeeded(event);
                break;
            case 1:
                this.mStartTouchPoint = null;
                update = moveTrackersIfNeeded(event);
                break;
            case 2:
                update = moveTrackersIfNeeded(event);
                break;
        }
        if (update) {
            if (this.mListener != null) {
                this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal}));
            }
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean moveTrackersIfNeeded(MotionEvent event) {
        if (this.mStartTouchPoint == null) {
            return false;
        }
        int startX = this.mStartTouchPoint.x;
        int startY = this.mStartTouchPoint.y;
        if (this.mHueRect.contains(startX, startY)) {
            this.mLastTouchedPanel = 1;
            this.mHue = pointToHue(event.getY());
            return true;
        } else if (this.mSatValRect.contains(startX, startY)) {
            this.mLastTouchedPanel = 0;
            float[] result = pointToSatVal(event.getX(), event.getY());
            this.mSat = result[0];
            this.mVal = result[1];
            return true;
        } else if (this.mAlphaRect == null || !this.mAlphaRect.contains(startX, startY)) {
            return false;
        } else {
            this.mLastTouchedPanel = 2;
            this.mAlpha = pointToAlpha((int) event.getX());
            return true;
        }
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthAllowed = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightAllowed = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthAllowed2 = chooseWidth(widthMode, widthAllowed);
        int heightAllowed2 = chooseHeight(heightMode, heightAllowed);
        if (!this.mShowAlphaPanel) {
            height = (int) ((widthAllowed2 - this.PANEL_SPACING) - this.HUE_PANEL_WIDTH);
            if (height > heightAllowed2) {
                height = heightAllowed2;
                width = (int) (height + this.PANEL_SPACING + this.HUE_PANEL_WIDTH);
            } else {
                width = widthAllowed2;
            }
        } else {
            width = (int) ((heightAllowed2 - this.ALPHA_PANEL_HEIGHT) + this.HUE_PANEL_WIDTH);
            if (width > widthAllowed2) {
                width = widthAllowed2;
                height = (int) ((widthAllowed2 - this.HUE_PANEL_WIDTH) + this.ALPHA_PANEL_HEIGHT);
            } else {
                height = heightAllowed2;
            }
        }
        setMeasuredDimension(width, height);
    }

    private int chooseWidth(int mode, int size) {
        return (mode == Integer.MIN_VALUE || mode == 1073741824) ? size : getPrefferedWidth();
    }

    private int chooseHeight(int mode, int size) {
        return (mode == Integer.MIN_VALUE || mode == 1073741824) ? size : getPrefferedHeight();
    }

    private int getPrefferedWidth() {
        int width = getPrefferedHeight();
        if (this.mShowAlphaPanel) {
            width = (int) (width - (this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT));
        }
        return (int) (width + this.HUE_PANEL_WIDTH + this.PANEL_SPACING);
    }

    private int getPrefferedHeight() {
        int height = (int) (200.0f * mDensity);
        if (this.mShowAlphaPanel) {
            return (int) (height + this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT);
        }
        return height;
    }

    @Override // android.view.View
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mDrawingRect = new RectF();
        this.mDrawingRect.left = this.mDrawingOffset + getPaddingLeft();
        this.mDrawingRect.right = (w - this.mDrawingOffset) - getPaddingRight();
        this.mDrawingRect.top = this.mDrawingOffset + getPaddingTop();
        this.mDrawingRect.bottom = (h - this.mDrawingOffset) - getPaddingBottom();
        setUpSatValRect();
        setUpHueRect();
        setUpAlphaRect();
    }

    private void setUpSatValRect() {
        RectF dRect = this.mDrawingRect;
        float panelSide = dRect.height() - 2.0f;
        if (this.mShowAlphaPanel) {
            panelSide -= this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT;
        }
        float left = dRect.left + 1.0f;
        float top = dRect.top + 1.0f;
        float bottom = top + panelSide;
        float right = left + panelSide;
        this.mSatValRect = new RectF(left, top, right, bottom);
    }

    private void setUpHueRect() {
        RectF dRect = this.mDrawingRect;
        float left = (dRect.right - this.HUE_PANEL_WIDTH) + 1.0f;
        float top = dRect.top + 1.0f;
        float bottom = (dRect.bottom - 1.0f) - (this.mShowAlphaPanel ? this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT : 0.0f);
        float right = dRect.right - 1.0f;
        this.mHueRect = new RectF(left, top, right, bottom);
    }

    private void setUpAlphaRect() {
        if (!this.mShowAlphaPanel) {
        }
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.mListener = listener;
    }

    public void setBorderColor(int color) {
        this.mBorderColor = color;
        invalidate();
    }

    public int getBorderColor() {
        return this.mBorderColor;
    }

    public int getColor() {
        return Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal});
    }

    public void setColor(int color) {
        setColor(color, false);
    }

    public void setColor(int color, boolean callback) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);
        float[] hsv = new float[3];
        Color.RGBToHSV(red, green, blue, hsv);
        this.mAlpha = alpha;
        this.mHue = hsv[0];
        this.mSat = hsv[1];
        this.mVal = hsv[2];
        if (callback && this.mListener != null) {
            this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal}));
        }
        invalidate();
    }

    public float getDrawingOffset() {
        return this.mDrawingOffset;
    }

    public void setAlphaSliderVisible(boolean visible) {
        if (this.mShowAlphaPanel != visible) {
            this.mShowAlphaPanel = visible;
            this.mValShader = null;
            this.mSatShader = null;
            this.mHueShader = null;
            this.mAlphaShader = null;
            requestLayout();
        }
    }

    public void setSliderTrackerColor(int color) {
        this.mSliderTrackerColor = color;
        this.mHueTrackerPaint.setColor(this.mSliderTrackerColor);
        invalidate();
    }

    public int getSliderTrackerColor() {
        return this.mSliderTrackerColor;
    }

    public void setAlphaSliderText(int res) {
        String text = getContext().getString(res);
        setAlphaSliderText(text);
    }

    public void setAlphaSliderText(String text) {
        this.mAlphaSliderText = text;
        invalidate();
    }

    public String getAlphaSliderText() {
        return this.mAlphaSliderText;
    }
}
