package org.codeforafrica.listeningpost.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class BigImageLabelView extends View {

	private String mText;
	private Paint mTextPaint;
	private int mAscent;
	private Bitmap mImage;
	private int mFontSize = 42;
	private int mFontColor = Color.WHITE;
	private int mBgColor = Color.DKGRAY;
	private Rect bounds;
	private int mTextWidth;
	private int mTextHeight;
	
	public BigImageLabelView(Context context, String title, Bitmap image, int fontColor, int bgColor) {
		super(context);
		
		mText = title;
		mImage = image;
		mFontColor = fontColor;
		
		mBgColor = bgColor;
		setBackgroundColor(bgColor);
		
		initViewGfx();
	}
	
	private void initViewGfx() {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
  
        mTextPaint.setTextSize(mFontSize);
        mTextPaint.setColor(mFontColor);
        setPadding(3, 12, 3, 3);
        mAscent = (int) mTextPaint.ascent();
        mTextPaint.setAlpha(150);
        computeBounds ();
        mTextWidth = (int)mTextPaint.measureText(mText, 0, mText.length());
        
         
	}
	
	private void computeBounds ()
	{
		int boundsHeight = getPaddingTop();

        int boundsWidth = getWidth() - this.getPaddingLeft() - this.getPaddingRight();
		
        if (mImage != null)
        {
        	float imageRatio = ((float)mImage.getHeight())/((float)mImage.getWidth());
        	boundsHeight = (int)(((float)boundsWidth) * imageRatio) - this.getPaddingTop() - this.getPaddingBottom();
		
        	bounds = new Rect(getPaddingLeft(),getPaddingTop(),boundsWidth, boundsHeight);
        }
        else
        {
        	bounds = new Rect(getPaddingLeft(),getPaddingTop(),getWidth()-getPaddingLeft()*2, getHeight()-getPaddingTop()*2);
        }
     }

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		
		if (mImage != null)
		{
			
			 computeBounds ();
			canvas.drawBitmap(mImage, null, bounds, mTextPaint);
		}
		
		mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(mBgColor);
        mTextPaint.setAlpha(150);
        
		canvas.drawRect(getWidth()/2-mTextWidth/2, getHeight()/2-mAscent*3, getWidth()/2+mTextWidth/2, getHeight()/2+mAscent*2, mTextPaint);

		mTextPaint.setColor(mFontColor);
		mTextPaint.setStyle(Paint.Style.STROKE);
        canvas.drawText(mText, getWidth()/2-mTextWidth/2, getHeight()/2-mAscent, mTextPaint);		
	}
	
	
	/**
     * Sets the text to display in this label
     * @param text The text to display. This will be drawn as one line.
     */
    public void setText(String text) {
        mText = text;
        requestLayout();
        invalidate();
        initViewGfx();
    }

    /**
     * Sets the text size for this label
     * @param size Font size
     */
    public void setTextSize(int size) {
    	mFontSize = size;
        requestLayout();
        invalidate();
        initViewGfx();
    }

    /**
     * Sets the text color for this label.
     * @param color ARGB value for the text
     */
    public void setTextColor(int color) {
        mFontColor = color;
        invalidate();
        initViewGfx();
    }

    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = (int) mTextPaint.measureText(mText) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    /**
     * Determines the height of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        mAscent = (int) mTextPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (-mAscent + mTextPaint.descent()) + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

	@Override
	public void invalidate() {

		super.invalidate();
		 computeBounds ();
	}


}
