/**
 * 
 */
package info.guardianproject.mrapp;

import java.io.File;
import java.util.ArrayList;

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
	private boolean isPlayingMultiple = false;
	
	private Handler mHandler = new Handler();
	private Runnable mTrimClipEndTask = new Runnable() {
	    public void run() {
	        PreviewVideoView.this.stopPlayback();
	        PreviewVideoView.this.playNext();
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
		 
    public void setMedia(ArrayList<Media> media) {
        mMediaArray = media.toArray(new Media[media.size()]);
        mCurrentMedia = 0;
    }
    
    public void setMedia(Media[] media) {
        mMediaArray = media;
        mCurrentMedia = 0;
    }
	
	public void setCompletionCallback(Runnable runnable) {
		mCompletionCallback = runnable;
	}
	
	public void play() {
		
		isPlayingMultiple = true;
		
		for (; mCurrentMedia <= mMediaArray.length ; mCurrentMedia++) {
		    if (mCurrentMedia == mMediaArray.length) { 
				mCurrentMedia = 0;
				if (mCompletionCallback != null) {
					mCompletionCallback.run();
				}
				break;
			} 
		    else if (mMediaArray[mCurrentMedia] == null) {
                //playNext(); // skip null media in playlist (commented out to fix issues/991 - Skipping Clip after Empty Clip Bug)
            }
            else {
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
        
        if (media.getDuration() == 0) {
            // old projects didn't save duration, we need it
            media.setDuration(getDuration());
            media.save();
        }
        
        if ((media.getTrimStart() > 0) && (media.getTrimStart() < 99)) {
            int startTime = media.getTrimmedStartTime();
            seekTo(startTime);
        }
        
        if  ((media.getTrimEnd() != 0) && (media.getTrimEnd() < 99)) {// && (media.getTrimStart() < media.getTrimEnd())) {
            mHandler.removeCallbacks(mTrimClipEndTask);
            int duration = media.getTrimmedDuration();
            mHandler.postDelayed(mTrimClipEndTask, duration);
        }

        start();
        // FIXME make sure to kill off the timer if we close the activity/stop
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
		if (isPlayingMultiple)
			playNext();
	}
	
	private void playNext() {
        mHandler.removeCallbacks(mTrimClipEndTask);
        if (mCurrentMedia < mMediaArray.length) {
            mCurrentMedia++;
            play();
        } else {
            mCurrentMedia = 0;
            if (mCompletionCallback != null) {
                mCompletionCallback.run();
            }
            isPlayingMultiple = false;
        }
	}
}
