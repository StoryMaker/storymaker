package org.codeforafrica.timby.listeningpost;

import java.io.File;
import java.util.ArrayList;

import org.codeforafrica.timby.listeningpost.R;
import org.codeforafrica.timby.listeningpost.media.AudioRecorderView;
import org.codeforafrica.timby.listeningpost.media.MediaProjectManager;
import org.codeforafrica.timby.listeningpost.model.Media;
import org.codeforafrica.timby.listeningpost.model.Project;
import org.holoeverywhere.widget.SeekBar;
import org.holoeverywhere.widget.SeekBar.OnSeekBarChangeListener;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

/**
 * 
 */
public class ReviewFragment extends Fragment {
    private final static String TAG = "OrderClipsFragment";
    public ViewPager mAddClipsViewPager;
    View mView = null;
    private EditorBaseActivity mActivity;
    Button mPlayButton;//, mButtonAddNarration, mButtonPlayNarration;
    private ImageView mImageViewMedia;
    private PreviewVideoView mPreviewVideoView = null;
//    private LinearLayout mLLControlBar = null;
    private SeekBar mSeekBar = null;
//    RangeSeekBar<Integer> mRangeSeekBar = null;
//    ViewGroup mRangeSeekBarContainer = null;
    public MediaProjectManager mMPM;
    int mCurrentClipIdx = 0;
    AudioRecorderView mAudioNarrator = null;
    private boolean mKeepRunningPreview = false;
    boolean mTrimMode = false;
    ArrayList<Media> mMediaList;
    
    private File mFileAudioNarration = null;
    
    int mPhotoEssaySlideLength = -1;//5 seconds

    public ReviewFragment()
    {
    }
    
    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mActivity = (EditorBaseActivity) getActivity();
        mMPM = mActivity.mMPM;
        mMediaList = mMPM.mProject.getMediaAsList();
        View view = inflater.inflate(R.layout.fragment_story_review, null);
        
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(view.getContext().getApplicationContext());
         mPhotoEssaySlideLength = Integer.parseInt(settings.getString("pslideduration", AppConstants.DEFAULT_SLIDE_DURATION+""));
        
        mImageViewMedia = (ImageView) view.findViewById(R.id.imageView1);
        mPreviewVideoView = (PreviewVideoView) view.findViewById(R.id.previewVideoView);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar1);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                if (fromUser)
                    mPreviewVideoView.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
            
        });
        
        if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY)
        {
            
            if (mAudioNarrator == null)
            {
                // FIXME make this work for project review tab
                String audioFile = "narration" + mMPM.mScene.getId() + ".wav";
                mFileAudioNarration = new File(mMPM.getExternalProjectFolder(mMPM.mProject,mActivity.getBaseContext()),audioFile);
                mAudioNarrator = new AudioRecorderView(mFileAudioNarration,getActivity());
            }
        }
        
        if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY
                || mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO)
        {
        	
	        mPlayButton = (Button) view.findViewById(R.id.buttonPlay);
	        mPlayButton.setOnClickListener(new OnClickListener() {
	
	            @Override
	            public void onClick(View v) {
	                
	                playNarration(true);
	                
	            }
	        });
        }
        
        mPreviewVideoView.setCompletionCallback(new Runnable() {
            @Override
            public void run() {
                mImageViewMedia.setVisibility(View.VISIBLE);
                mPreviewVideoView.setVisibility(View.GONE);
                mKeepRunningPreview = false;
                mPlayButton.setText(R.string.play_recording);
                showThumbnail(mCurrentClipIdx);
            }
        });
        
        mImageViewMedia.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                previewClip(mCurrentClipIdx);
            }
        });

        showThumbnail(mCurrentClipIdx);
        
        return view;
    }
    
    private void stopRecordNarration ()
    {
        //stop the audio recorder
        mAudioNarrator.stopRecording();
//        mButtonAddNarration.setText("Re-record narration?");
        
        //start the playback
        if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY)
            handlePhotoPlayToggle ();
        else if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO)
            handleVideoAudioPlayToggle();

    }
    
    private void playNarration (boolean start)
    {
        
        if (mAudioNarrator != null) { 
            if (start) {
                mAudioNarrator.startPlaying();
            } else {
                mAudioNarrator.stopPlaying();
            }
        }
        
        //start the playback
        if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY)
            handlePhotoPlayToggle ();
        else
            handleVideoAudioPlayToggle();
    }
    
    
    private void handlePhotoPlayToggle ()
    {
        if (mKeepRunningPreview)
        {
            mPlayButton.setText(R.string.play_recording);
            mSeekBar.setProgress(0);
            mKeepRunningPreview = false;
        }
        else
        {
            mSeekBar.setMax(mMediaList.size()*mPhotoEssaySlideLength);
            mSeekBar.setProgress(0);
        
            mPlayButton.setText(R.string.stop_recording);
            mKeepRunningPreview = true;
            
            Thread thread = new Thread ()
            {
            
                public void run ()
                {
                    
                    String[] pathArray = mMPM.mProject.getMediaAsPathArray();
                    
                    for (int i = 0; i < pathArray.length && mKeepRunningPreview; i++)
                    {
                        Message msg = new Message();
                        msg.what = 1;
                        msg.getData().putString("path", pathArray[i]);
                        msg.getData().putInt("idx", i);
                        hImageUpdater.sendMessage(msg);
                        
                        try {Thread.sleep(mPhotoEssaySlideLength * 1000);}catch(Exception e){}
                    }
                    
                    mKeepRunningPreview = false;
                    hImageUpdater.sendEmptyMessage(-1);
                    
                }
                
            };
            
            thread.start();
            
            
             
        }

    }
    
    
    Handler hImageUpdater = new Handler ()
    {

        @Override
        public void handleMessage(Message msg) {                
            super.handleMessage(msg);
            
            switch (msg.what)
            {
                case -1: //stop playback
                mPlayButton.setText(R.string.play_recording);
                if (mAudioNarrator != null)
                    if (mAudioNarrator.isRecording())
                        stopRecordNarration ();
                    else if (mAudioNarrator.isPlaying())
                        mAudioNarrator.stopPlaying();
                
                    mSeekBar.setProgress(mSeekBar.getMax());
                
                break;
                
                case 1: //update image view from path
                
                    String path = msg.getData().getString("path");
                    int idx = msg.getData().getInt("idx");
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    Bitmap bmp = BitmapFactory.decodeFile(path, options);
                    mImageViewMedia.setImageBitmap(bmp);
                    mSeekBar.setProgress(idx*mPhotoEssaySlideLength);
                break;
                
                default:
            }
        }
        
    };
    
    private void handleVideoAudioPlayToggle ()
    {
         if (mPreviewVideoView.isPlaying())
         {
            mPlayButton.setText(R.string.play_recording);
            mKeepRunningPreview = false;
             mPreviewVideoView.stopPlayback();
            
         }
         else
         {
            mPlayButton.setText(R.string.stop_recording);
            mKeepRunningPreview = true;
             mImageViewMedia.setVisibility(View.GONE);
             mPreviewVideoView.setVisibility(View.VISIBLE);
             
             // play
             mPreviewVideoView.setMedia(mMediaList);
             mPreviewVideoView.play();
             
             new Thread ()
             {
                public void run() {
                    
                    mSeekBar.setMax(mPreviewVideoView.getDuration());
                     mSeekBar.setProgress(mPreviewVideoView.getCurrentPosition());
                     
                     if (mKeepRunningPreview)
                         mSeekBar.postDelayed(this, 1000);
                        

                }
             }.start();
         }
    }
    
    private void previewClip(int position) {
        if (mMediaList.get(position) != null) {
            
            if (mMediaList.get(position).getMimeType().startsWith("video"))
            {
                mImageViewMedia.setVisibility(View.GONE);
                mPreviewVideoView.setVisibility(View.VISIBLE);
                // play
                mPreviewVideoView.stopPlayback();
                Media[] mediaArray = {mMediaList.get(position)};
                mPreviewVideoView.setMedia(mediaArray);
                mPreviewVideoView.play();
                
                //mSeekBar.setMax(mPreviewVideoView.getDuration());
                
            }
            else if (mMediaList.get(position).getMimeType().startsWith("audio"))
            {
                mImageViewMedia.setVisibility(View.GONE);
                mPreviewVideoView.setVisibility(View.VISIBLE);
                // play
                mPreviewVideoView.stopPlayback();
                Media[] mediaArray = {mMediaList.get(position)};
                mPreviewVideoView.setMedia(mediaArray);
                mPreviewVideoView.play();
                
                //mSeekBar.setMax(mPreviewVideoView.getDuration());
                
            }
            else
            {
                showThumbnail(position);
            }
        }
    }
    
    private void showThumbnail(int position) {
        if (mMediaList.get(position) != null) {
        	Bitmap bmp = Media.getThumbnail(mActivity,mMediaList.get(position),mActivity.mMPM.mProject);
            mImageViewMedia.setImageBitmap(bmp);
        }
    }
}