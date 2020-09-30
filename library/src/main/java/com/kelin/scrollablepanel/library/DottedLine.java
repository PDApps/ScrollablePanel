package com.kelin.scrollablepanel.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES;

/**
 * This is a custom view to draw dotted, vertical lines.
 *
 * A regular shape drawable (like we use for horizontal lines) is usually not sufficient because
 * rotating such a (by default horizontal) line to be vertical does not recalculate the correct
 * with and height if they are set to match_parent or wrap_content.
 *
 * Furthermore, this view draws actual round dots, not those fake tiny square ones like shape
 * drawables do.
 *
 * A more elaborate version of this view would use custom attributes to set the color of the line
 * more dynamically, as well as the line shape, gap size, etc.
 */
public class DottedLine extends View {

    private boolean isDotted = false;
    private boolean isBold = false;

    private Paint mDashedPaint;
    private Paint mSolidPaint;
    private float normalLineWidth;
    private float boldLineWidth;

    public DottedLine(Context context) {
        super(context);
        init();
    }

    public DottedLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DottedLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public DottedLine(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        Resources res = getResources();
        int lineColor = getLineColor();
        normalLineWidth = res.getDimension(R.dimen.line_width);
        boldLineWidth = res.getDimension(R.dimen.bold_line_width);
        mDashedPaint = new Paint();
        float dashLength = res.getDimension(R.dimen.dash_length);
        mDashedPaint.setPathEffect(new DashPathEffect(new float[]{dashLength, dashLength}, 0));
        mDashedPaint.setColor(lineColor);

        // If we don't render in software mode, the dotted line becomes a solid line.
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mSolidPaint = new Paint();
        mSolidPaint.setColor(lineColor);
    }

    private @ColorInt
    int getLineColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.lineColor, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint;
        if (isDotted) {
            paint = mDashedPaint;
        } else {
            paint = mSolidPaint;
        }
        float lineWidth = normalLineWidth;
        if (isBold) {
            lineWidth = boldLineWidth;
        }
        paint.setStrokeWidth(lineWidth);
        float lineXCoordinate = getWidth() / 2f;
        canvas.drawLine(lineXCoordinate, 0, lineXCoordinate, getHeight(), paint);
    }

    public void setDotted(boolean dotted) {
        isDotted = dotted;
    }

    public void setColor(int color) {
        mDashedPaint.setColor(color);
        mSolidPaint.setColor(color);
    }

    public void setIsBold(boolean isBold) {
        this.isBold = isBold;
    }
}