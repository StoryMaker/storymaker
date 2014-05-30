package info.guardianproject.mrapp.ui;

import org.holoeverywhere.widget.FrameLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class TouchlessFrame extends FrameLayout {
    public TouchlessFrame(Context context) {
        super(context);
    }

    public TouchlessFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public TouchlessFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    // This swallows touch events at the frame level so they don't get passed down to the views inside the fragment
    public boolean onInterceptTouchEvent (MotionEvent ev){
        return true;
    }
}
