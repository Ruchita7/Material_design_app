package com.example.xyzreader.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * This custom image view class helps to define aspect ratio and set height based on measured dimensions
 */
public class AspectRatioImageViewer extends ImageView {
    private float mAspectRatio = 1.5f;

    /**
     * @param context
     */
    public AspectRatioImageViewer(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public AspectRatioImageViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @param context
     * @param attrs
     */
    public AspectRatioImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param aspectRatio
     */
    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
    }

    /**
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, (int) (measuredWidth / mAspectRatio));
    }
}
