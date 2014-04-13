package com.wabbit.libraries;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by Bogdan Tirca on 27.03.2014.
 */
public class ScrollViewPhoto  extends ScrollView{
    private GestureDetector mGestureDetector;

    private boolean mIsFling;
    private OnEndScrollListener mOnEndScrollListener;

    public ScrollViewPhoto(Context context) {
        this(context, null, 0);
    }

    public ScrollViewPhoto(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollViewPhoto(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGestureDetector = new GestureDetector(context, new YScrollDetector());
        setFadingEdgeLength(0);
    }

    //Anim
    private ValueAnimator anim = ValueAnimator.ofFloat(0, 1);

    private View header;
    private RelativeLayout.LayoutParams params;
    private float normalSize, largeSize, offset;
    private float overScroll = 0, overScrollDefault;
    boolean isToTop = true;
    public void setHeader(View header, float normalSize, float largeSize){
        this.header = header;
        this.normalSize = normalSize;
        this.largeSize = largeSize;
        this.offset = largeSize - normalSize;
        this.params = (RelativeLayout.LayoutParams)header.getLayoutParams();

        anim.removeAllUpdateListeners();
        anim.setDuration(450);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                overScroll = overScrollDefault * (1 - animation.getAnimatedFraction());
                updatePhoto();
            }
        });

        updatePhoto();
    }
    public void updatePhoto(){
        params.height = (int)(normalSize + overScroll);
        header.setLayoutParams(params);
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        if(overScroll == 0) {
            super.onScrollChanged(x, y, oldX, oldY);
            if (y <= 2) {
                isToTop = true;
            } else {
                isToTop = false;
            }
        }

//        Log.d("scroll changed", oldY + " " + y);
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent ev){
        return super.dispatchTouchEvent(ev);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        super.onInterceptTouchEvent(ev);
        return  mGestureDetector.onTouchEvent(ev);
    }
    @Override
    public boolean onTouchEvent (MotionEvent ev){
        int action = ev.getAction();
        if(ev.getPointerCount() > 1)
            return true;
        switch (action){
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if(overScroll > 0) {
                    overScrollDefault = overScroll;
                    anim.start();
                }
                break;
            default:
                break;
        }
        mGestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    // Return false if we're scrolling in the x direction
    class YScrollDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d("scroll detector", distanceX + " " + distanceY);
            if(isToTop){
//                DecelerateInterpolator i = new DecelerateInterpolator();
                overScroll = overScroll - distanceY * 0.6f;
                if(overScroll > offset)
                    overScroll = offset;
                if(overScroll < 0)
                    overScroll = 0;
                if(anim.isRunning())
                    anim.end();
                updatePhoto();
            }
            if(Math.abs(distanceY) > Math.abs(distanceX)) {
                return true;
            }
            return false;
        }
    }

    public OnEndScrollListener getOnEndScrollListener() {
        return mOnEndScrollListener;
    }

    public void setOnEndScrollListener(OnEndScrollListener mOnEndScrollListener) {
        this.mOnEndScrollListener = mOnEndScrollListener;
    }

    public interface OnEndScrollListener {
        public void onEndScroll();
    }

}
