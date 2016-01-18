package org.storymaker.app.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by admin on 12/11/15.
 */
public class SwipelessViewPager extends ViewPager {

    private boolean enabled;

    public SwipelessViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (this.enabled) {
//            return super.onTouchEvent(event);
//        }
//
//        return false;
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//        if (this.enabled) {
//            return super.onInterceptTouchEvent(event);
//        }
//
//        return false;
//    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}