/**
 * 
 */
package info.guardianproject.mrapp;

import java.io.File;

import info.guardianproject.mrapp.model.Media;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * @author josh
 *
 */
public class PreviewVideoView extends VideoView implements MediaPlayer.OnCompletionListener {
	private final static String TAG = "PreviewVideoView";
	protected int mCurrentMedia = 0;
	protected String[] mPathArray;
	//MediaPlayer mp;
	protected Runnable mCompletionCallback = null;
	
	public PreviewVideoView(Context context) {
		super(context);
		setOnCompletionListener(this);
		
	}
	
	public PreviewVideoView(Context context, AttributeSet attrs) {
		 
		super( context, attrs );
		setOnCompletionListener(this);
	}
		 
	public PreviewVideoView(Context context, AttributeSet attrs, int defStyle) {
		 
		super( context, attrs, defStyle );
		setOnCompletionListener(this);
	}
		 
	
	public void setMedia(String[] pathArray) {
		mPathArray = pathArray;
		mCurrentMedia = 0;
	}
	
	public void setCompletionCallback(Runnable runnable) {
		mCompletionCallback = runnable;
	}
	
	public void play() {
		for (; mCurrentMedia <= mPathArray.length ; mCurrentMedia++) {
			if (mCurrentMedia == mPathArray.length) {
				mCurrentMedia = 0;
				if (mCompletionCallback != null) {
					mCompletionCallback.run();
				}
				break;
			} else {
				String path = mPathArray[mCurrentMedia];
				if (path != null) {
					File file = new File(path);
					if (file.exists()) {
						this.setVideoPath(path);
						this.start();
						break;
					}
				}
			}
		}
	}
	
	public void play(int startFrom) {
		// TODO if we are already playing, stop, load the new video and start.
		if (startFrom >= 0 && startFrom < mPathArray.length) {
			mCurrentMedia = startFrom;
			this.play();
			
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mCurrentMedia < mPathArray.length) {
			mCurrentMedia++;
			play();
		} else {
			mCurrentMedia = 0;
			if (mCompletionCallback != null) {
				mCompletionCallback.run();
			}
		}
	}
}
