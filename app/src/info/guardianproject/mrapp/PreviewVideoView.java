/**
 * 
 */
package info.guardianproject.mrapp;

import java.io.File;

import info.guardianproject.mrapp.model.Media;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * @author josh
 *
 */
public class PreviewVideoView extends VideoView implements MediaPlayer.OnCompletionListener {
	private final static String TAG = "PreviewVideoView";
	protected int mCurrentMedia = 0;
	protected Media[] mMediaArray;
	//MediaPlayer mp;
	protected Runnable mCompletionCallback = null;
	
	private Handler mHandler = new Handler();
	private Runnable mTrimClipEndTask = new Runnable() {
	    public void run() {
	        PreviewVideoView.this.stopPlayback();
	        PreviewVideoView.this.doComplete();
	    }
	 };

	
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
		 
	
	public void setMedia(Media[] media) {
		mMediaArray = media;
		mCurrentMedia = 0;
	}
	
	public void setCompletionCallback(Runnable runnable) {
		mCompletionCallback = runnable;
	}
	
	public void play() {
		for (; mCurrentMedia <= mMediaArray.length ; mCurrentMedia++) {
			if (mCurrentMedia == mMediaArray.length) {
				mCurrentMedia = 0;
				if (mCompletionCallback != null) {
					mCompletionCallback.run();
				}
				break;
			} else {
			    Media media = mMediaArray[mCurrentMedia];
				String path = media.getPath();
				if (path != null) {
					File file = new File(path);
					if (file.exists()) {
                        setOnPreparedListener(new MediaPlayer.OnPreparedListener()  {
                            @Override
                            public void onPrepared(MediaPlayer mp) {                         
                                PreviewVideoView.this.doPlay();
                            }
                        });
                        this.setVideoPath(path);
						break;
					}
				}
			}
		}
	}
	
	private void doPlay() {
        Media media = mMediaArray[mCurrentMedia];

        int duration = getDuration();
        int trimStart = media.getTrimStart();
        int trimEnd = media.getTrimEnd();
        float startPercent = (trimStart + 1) / 100F;
        float endPercent = (trimEnd + 1) / 100F;
        int startTime = Math.round(startPercent * duration);
        int endTime = Math.round(endPercent * duration);
        int trimmedLength = endTime - startTime;
        
        if ((media.getTrimStart() > 0) && (media.getTrimStart() < 99)) {
            seekTo(startTime);
        }
        
        if  ((media.getTrimEnd() != 0) && (media.getTrimEnd() < 99)) {// && (media.getTrimStart() < media.getTrimEnd())) {
            mHandler.removeCallbacks(mTrimClipEndTask);
            mHandler.postDelayed(mTrimClipEndTask, trimmedLength);
        }

        start();
        // FIXME make sure to kill off the time if we close the activity/stop
	}
	
	public void play(int startFrom) {
		// TODO if we are already playing, stop, load the new video and start.
		if (startFrom >= 0 && startFrom < mMediaArray.length) {
			mCurrentMedia = startFrom;
			this.play();
			
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
	    doComplete();
	}
	
	private void doComplete() {
        mHandler.removeCallbacks(mTrimClipEndTask);
        if (mCurrentMedia < mMediaArray.length) {
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
