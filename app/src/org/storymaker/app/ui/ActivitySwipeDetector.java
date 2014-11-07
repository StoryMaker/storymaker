package org.storymaker.app.ui;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ActivitySwipeDetector implements View.OnTouchListener {

    static final String logTag = "ActivitySwipeDetector";
    private SwipeInterface activity;
    static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY;

    public ActivitySwipeDetector(SwipeInterface activity){
        this.activity = activity;
    }

    public void onRightToLeftSwipe(View v){
        Log.i(logTag, "RightToLeftSwipe!");
        activity.right2left(v);
    }

    public void onLeftToRightSwipe(View v){
        Log.i(logTag, "LeftToRightSwipe!");
        activity.left2right(v);
    }

    public void onTopToBottomSwipe(View v){
        Log.i(logTag, "onTopToBottomSwipe!");
        activity.top2bottom(v);
    }

    public void onBottomToTopSwipe(View v){
        Log.i(logTag, "onBottomToTopSwipe!");
        activity.bottom2top(v);
    }

    public boolean onTouch(View v, MotionEvent event) {
    	
        switch(event.getAction()){
        case MotionEvent.ACTION_DOWN: {
            downX = event.getX();
            downY = event.getY();
            return true;
        }
        case MotionEvent.ACTION_UP: {
            upX = event.getX();
            upY = event.getY();

            float deltaX = downX - upX;
            float deltaY = downY - upY;

            // swipe horizontal?
            if(Math.abs(deltaX) > MIN_DISTANCE){
                // left or right
                if(deltaX < 0) { this.onLeftToRightSwipe(v); return true; }
                if(deltaX > 0) { this.onRightToLeftSwipe(v); return true; }
            }
            else {
                Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + MIN_DISTANCE);
            }

            // swipe vertical?
            if(Math.abs(deltaY) > MIN_DISTANCE){
                // top or down
                if(deltaY < 0) { this.onTopToBottomSwipe(v); return true; }
                if(deltaY > 0) { this.onBottomToTopSwipe(v); return true; }
            }
            else {
                Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + MIN_DISTANCE);
                v.performClick();
            }
        }
        }
        return false;
    }

}
