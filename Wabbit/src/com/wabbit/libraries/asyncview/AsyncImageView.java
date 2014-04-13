package com.wabbit.libraries.asyncview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.wabbit.libraries.cache.BitmapLruCache;

import java.lang.ref.WeakReference;


public class AsyncImageView extends ImageView {

    private static final int[] ATTRS_DIMEN_ANDROID = {android.R.attr.layout_width, android.R.attr.layout_height};

    public BitmapLruCache mCache;
    private WeakReference<BitmapWorkerTask> mBitmapWorkerRef;

    private Animation mAppearAnimation;

    public AsyncImageView(Context context) {
        super(context);
        init(context, null);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context,  AttributeSet attrs) {
        if (!isInEditMode()) {
//            mAppearAnimation = new AlphaAnimation(0.f, 1.f);
//            mAppearAnimation.setDuration(300);
//            mAppearAnimation.setRepeatCount(0);
//            mAppearAnimation.setAnimationListener(new AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                    setAlphaCompat(1.f);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                }
//            });
        }
        else{
            // TODO else show placeholder
        }
    }

    public void setCache(BitmapLruCache pCache){
        mCache = pCache;
    }
    public void setImageUrl(String url) {
        if (url != null && cancelPotentialWork(url)) {
            final BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(this);
            mBitmapWorkerRef = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
            bitmapWorkerTask.execute(url, mCache);
        }
    }

//    @SuppressLint("NewApi")
//    @SuppressWarnings("deprecation")
//    private static Point getScreenDimensions(Context context) {
//        Point size = new Point();
//
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//
//        if (Build.VERSION.SDK_INT >= 13) {
//            display.getSize(size);
//        } else {
//            size.x = display.getWidth();
//            size.y = display.getHeight();
//        }
//
//        return size;
//    }

    @SuppressLint("NewApi")
    private void setAlphaCompat(float alpha) {

        if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 16) {
            setAlpha(alpha);
        } else if (Build.VERSION.SDK_INT >= 16) {
            setImageAlpha((int) (alpha * 255));
        } else {
            //Compatibility version: no alpha
            if (alpha == 0.f) {
                setVisibility(View.INVISIBLE);
            } else {
                setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean cancelPotentialWork(String url) {

        BitmapWorkerTask task = getBitmapWorkerTask();
        if (task != null) {

            if (url != task.url) {
                // Cancel previous task
                task.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    public BitmapWorkerTask getBitmapWorkerTask() {

        return mBitmapWorkerRef == null? null: mBitmapWorkerRef.get();
    }

    public void setImageDrawable(Drawable bm, boolean fromMemory) {
        if (fromMemory) {
            setImageDrawable(bm);
        } else {
            BitmapDrawable emptyDrawable = new BitmapDrawable(getResources());
            TransitionDrawable fadeInDrawable =
                    new TransitionDrawable(new Drawable[] { emptyDrawable, bm });
            setImageDrawable(fadeInDrawable);
            fadeInDrawable.startTransition(200);
        }
//        startAnimation(mAppearAnimation);
    }
}