package com.example.xyzreader.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by dgnc on 3/25/2016.
 */
public class AspectRatioImageViewer extends ImageView{

    private float mAspectRatio = 0.6f;

    public AspectRatioImageViewer(Context context) {
        super(context);
    }

    public AspectRatioImageViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AspectRatioImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
        //  requestLayout();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
   /*     int aspectRatioHeight=(int) (MeasureSpec.getSize(widthMeasureSpec) * mAspectRatio);
        int aspectRatioHeightSpec=MeasureSpec.makeMeasureSpec(aspectRatioHeight,MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, aspectRatioHeightSpec);*/
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, (int) (measuredWidth / mAspectRatio));
    }
}
