package info.guardianproject.mrapp;

import info.guardianproject.mrapp.media.AudioRecorderView;
import info.guardianproject.mrapp.media.MediaProjectManager;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;

import java.io.File;
import java.io.IOException;

import org.ffmpeg.android.MediaDesc;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.SeekBar;
import org.holoeverywhere.widget.SeekBar.OnSeekBarChangeListener;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

import com.animoto.android.views.DraggableGridView;
import com.animoto.android.views.OnRearrangeListener;
import com.efor18.rangeseekbar.RangeSeekBar;
import com.efor18.rangeseekbar.RangeSeekBar.OnRangeSeekBarChangeListener;

/**
 * 
 */
@SuppressLint("ValidFragment") // FIXME don't do this
public class OrderClipsFragment extends Fragment {
    private final static String TAG = "OrderClipsFragment";
   
    public ViewPager mAddClipsViewPager;
    View mView = null;
    private EditorBaseActivity mActivity;
    Button mPlayButton, mButtonAddNarration, mButtonDeleteNarration;
    private ImageView mImageViewMedia;
    private PreviewVideoView mPreviewVideoView = null;
    private LinearLayout mLLControlBar = null;
    private SeekBar mSeekBar = null;
    RangeSeekBar<Integer> mRangeSeekBar = null;
    ViewGroup mRangeSeekBarContainer = null;
    public MediaProjectManager mMPM;
    private Handler mHandlerPub;
    int mCurrentClipIdx = 0;
    AudioRecorderView mAudioNarrator = null;
    private boolean mKeepRunningPreview = false;
    boolean mTrimMode = false;
    
    private File mFileAudioNarration = null;
    
    int mPhotoEssaySlideLength = -1;//5 seconds

    /**
     * The sortable grid view that contains the clips to reorder on the
     * Order tab
     */
    protected DraggableGridView mOrderClipsDGV;

    	
    private void init ()
    {
        mActivity = (EditorBaseActivity)getActivity();
        mMPM = mActivity.mMPM;
        mHandlerPub = ((SceneEditorActivity)mActivity).mHandlerPub;
    }

    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

    	init ();
    	
    	int layout = getArguments().getInt("layout");
        View view = inflater.inflate(layout, null);
        
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(view.getContext().getApplicationContext());
         mPhotoEssaySlideLength = Integer.parseInt(settings.getString("pslideduration", AppConstants.DEFAULT_SLIDE_DURATION+""));
        
        mOrderClipsDGV = (DraggableGridView) view.findViewById(R.id.DraggableGridView01);

        mImageViewMedia = (ImageView) view.findViewById(R.id.imageView1);

        mPreviewVideoView = (PreviewVideoView) view.findViewById(R.id.previewVideoView);
        
        mLLControlBar = (LinearLayout) view.findViewById(R.id.llControlBar);
        
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
        
        mRangeSeekBar = new RangeSeekBar<Integer>(0, 99, getActivity());

        mRangeSeekBarContainer = (ViewGroup) view.findViewById(R.id.llRangeSeekBar);
        mRangeSeekBarContainer.addView(mRangeSeekBar);
        
        mRangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {

            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue,
                    Integer maxValue) {
            	
            	  min = bar.getSelectedMinValue().intValue();
                  max = bar.getSelectedMaxValue().intValue();
            	
            }

            int min = -1;
            int max = -1;
            @Override
            public void onStartTrackingTouch(RangeSeekBar<?> bar) {
                min = bar.getSelectedMinValue().intValue();
                max = bar.getSelectedMaxValue().intValue();
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar<?> bar) {
                saveTrim();
                
                lastPlayed = -1;
               
                if (min != bar.getSelectedMinValue().intValue()) {
                    // they were dragging the first handle
                    previewClip(mCurrentClipIdx);
                } else {
                    Media media = mMPM.mScene.getMediaAsArray()[mCurrentClipIdx];

                	 previewClip(mCurrentClipIdx,media.getTrimmedStartTime()+media.getTrimmedDuration()-2);
                    // they were dragging the second handle
                    // FIXME try showing the last 1 second only to help picking the end point
                }
            }
        });
        
        if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY
        		|| mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO
        		)
        {
        
        	mButtonAddNarration = (Button)view.findViewById(R.id.buttonAddNarration);
	    	  if (mAudioNarrator == null)
	          {
	              String audioFile = "narration" + mMPM.mScene.getId() + ".wav";
	              mFileAudioNarration = new File(mMPM.getExternalProjectFolder(mMPM.mProject, mActivity.getBaseContext()),audioFile);
	              mAudioNarrator = new AudioRecorderView(mFileAudioNarration,getActivity());
	          }
	    	  
        	View viewRow = view.findViewById(R.id.rowNarration);
        	viewRow.setVisibility(View.VISIBLE);
        	
	        mButtonAddNarration.setEnabled(true);
	        
	        mButtonAddNarration.setOnClickListener(new OnClickListener ()
	        {
	            @Override
	            public void onClick(View v) {
	
	                if (!mAudioNarrator.isRecording())
	                    recordNarration();
	                else
	                    stopRecordNarration();
	            }
	        });
	        
	        
	        mButtonDeleteNarration= (Button)view.findViewById(R.id.buttonDeleteNarration);
	        if (mFileAudioNarration != null && mFileAudioNarration.exists())
	        	mButtonDeleteNarration.setEnabled(true);
	        
	        mButtonDeleteNarration.setOnClickListener(new OnClickListener ()
	        {
	            @Override
	            public void onClick(View v) {
	            	if (mFileAudioNarration != null)
	            	{
	            		mFileAudioNarration.delete();
	            		mButtonDeleteNarration.setEnabled(false);
	            		mFileAudioNarration = null;
	            	}
	            }
	        });
	      
        }
        
        mPlayButton = (Button) view.findViewById(R.id.buttonPlay);
        mPlayButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                
                if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO
                        || mMPM.mProject.getStoryType() == Project.STORY_TYPE_AUDIO)
                {
                
                	if (!mPreviewVideoView.isPlaying())
                	{
	                	 startPreviewPlayback();
                	}
                	else
                	{
                		stopPreviewPlayback();
                	}
                }
                else if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY
                    || mMPM.mProject.getStoryType() == Project.STORY_TYPE_PHOTO)
                {
                    
                    handlePhotoPlayToggle();
                    
                }
                
                // FIXME need to detect which clip user last clicked on
                // and start from there
                // FIXME need to know when mPreviewVideoView is done
                // playing so we can return the thumbnail
            }
        });
        
        mPreviewVideoView.setCompletionCallback(new Runnable() {
            @Override
            public void run() {
                mImageViewMedia.setVisibility(View.VISIBLE);
                mPreviewVideoView.setVisibility(View.GONE);
                mKeepRunningPreview = false;
                mPlayButton.setText(R.string.play_recording);
                showThumbnail(mCurrentClipIdx);
                
                if (mAudioNarrator != null)
                {
	                if (mAudioNarrator.isRecording())
	                    stopRecordNarration ();
	                else if (mAudioNarrator.isPlaying())
	                    mAudioNarrator.stopPlaying();
                }
            }
        });
        
        
       
        loadMedia();
        
        mImageViewMedia.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                previewClip(mCurrentClipIdx);
            }
        });
        
        showThumbnail(mCurrentClipIdx);
        
        
        return view;
    }
    
    private void recordNarration ()
    {
        mActivity.mdExported = null;
        
        //start the playback
        if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY)
            handlePhotoPlayToggle ();
        else if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO)
        	startPreviewPlayback();
        
        //start the audio recorder
        mAudioNarrator.startRecording();
        
        mButtonDeleteNarration.setEnabled(true);
        mButtonAddNarration.setText("Recording now... (speak!)");
    }
    
    private void stopRecordNarration ()
    {
        //stop the audio recorder
        mAudioNarrator.stopRecording();
        mButtonAddNarration.setText("Re-record narration?");
        
        //start the playback
        if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY)
            handlePhotoPlayToggle ();
        else if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO)
            stopPreviewPlayback();

    }
    
 
    
    
    private Thread mPhotoThread = null;
    
    private void handlePhotoPlayToggle ()
    {
        if (mKeepRunningPreview)
        {
        	stopPhotoPlayback();           
        }
        else
        {
            mSeekBar.setMax(mMPM.mScene.getMediaAsList().size()*mPhotoEssaySlideLength);
            mSeekBar.setProgress(0);
        
            mPlayButton.setText(R.string.stop_recording);
            mKeepRunningPreview = true;
            
            
         	if (mFileAudioNarration != null && mFileAudioNarration.exists())
            	 mAudioNarrator.startPlaying();
           
            
            mPhotoThread = new Thread ()
            {
            
                public void run ()
                {
                    
                    String[] pathArray = mMPM.mScene.getMediaAsPathArray();
                    
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
            
            mPhotoThread.start();
            
            
             
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
    
    private void stopPreviewPlayback ()
    {
      	if (mFileAudioNarration != null && mFileAudioNarration.exists())
         	 mAudioNarrator.stopPlaying();
      	
            mPlayButton.setText(R.string.play_recording);
            mKeepRunningPreview = false;
             mPreviewVideoView.stopPlayback();

             

         		mImageViewMedia.setVisibility(View.GONE);
         		mPreviewVideoView.setVisibility(View.VISIBLE);
    }
         	
    private void startPreviewPlayback ()
    {
        		mImageViewMedia.setVisibility(View.VISIBLE);
         		mPreviewVideoView.setVisibility(View.GONE);
         	
         		
            mPlayButton.setText(R.string.stop_recording);
            mKeepRunningPreview = true;
             mImageViewMedia.setVisibility(View.GONE);
             mPreviewVideoView.setVisibility(View.VISIBLE);
            
             // play
             mPreviewVideoView.setMedia(mMPM.mScene.getMediaAsArray());
             mPreviewVideoView.play();
            

             if (mFileAudioNarration != null && mFileAudioNarration.exists())
            	 mAudioNarrator.startPlaying();
            
         	 
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
    
    private void stopPhotoPlayback()
    {
    	mPlayButton.setText(R.string.play_recording);
    	mSeekBar.setProgress(0);
    	mKeepRunningPreview = false;
    	           
    	if (mPhotoThread != null)
    		mPhotoThread.interrupt();
    	           
    	if (mFileAudioNarration != null && mFileAudioNarration.exists())
    		mAudioNarrator.stopPlaying();
    }
    
    public void loadMedia ()
    {
        mOrderClipsDGV.removeAllViews();
        
        Media[] sceneMedias = mMPM.mScene.getMediaAsArray();

        for (int i = 0; i < sceneMedias.length; i++)
        {
            ImageView iv = new ImageView(getActivity());
            
            if (sceneMedias[i] != null) {
                Bitmap thumb = Media.getThumbnail(mActivity,sceneMedias[i],mActivity.mMPM.mProject);

                iv.setImageBitmap(thumb);
            } 
            else
            {
                iv.setImageDrawable(getResources().getDrawable(R.drawable.thumb_incomplete));
            }
            
            mOrderClipsDGV.addView(iv);
        }
        
        mOrderClipsDGV.setOnRearrangeListener(new OnRearrangeListener() {

            @Override
            public void onRearrange(int oldIndex, int newIndex) {
                
            	mMPM.mScene.moveMedia(oldIndex, newIndex);
                
            }
        });

        mOrderClipsDGV.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mTrimMode) {
                    Log.d(TAG, "item clicked");
                    mCurrentClipIdx = position;
          //          showThumbnail(position);
                    previewClip(position);
                }
            }
        });
      
    }
    
    private int lastPlayed = -1;
    
    private void previewClip(int position) {
    	previewClip(position, -1);
    }
    
    private void previewClip(int position, int startTime) {
        Media[] medias = mMPM.mScene.getMediaAsArray();
        if (medias[position] != null) {
            
            if (medias[position].getMimeType().startsWith("video"))
            {
            	if (mImageViewMedia.getVisibility() == View.VISIBLE)
            	{
            		mImageViewMedia.setVisibility(View.GONE);
            		mPreviewVideoView.setVisibility(View.VISIBLE);
            	}
            	
            	// play
            	if (mPreviewVideoView.isPlaying())
            		mPreviewVideoView.stopPlayback();
            
            	if (position != lastPlayed)
            	{
            	
	                Media[] mediaArray = {medias[position]};
	                mPreviewVideoView.setMedia(mediaArray);
	                mPreviewVideoView.invalidate();
	                
	                if (startTime != -1)
	                	mPreviewVideoView.seekTo(startTime);
	                
	                mPreviewVideoView.play();

	                lastPlayed = position;
            	}
            	else
            	{
            		lastPlayed = -1;
            	}
            }
            else if (medias[position].getMimeType().startsWith("audio"))
            {
            	if (mImageViewMedia.getVisibility() == View.VISIBLE)
            	{
            		mImageViewMedia.setVisibility(View.GONE);
            		mPreviewVideoView.setVisibility(View.VISIBLE);
            	}
            	
            	if (mPreviewVideoView.isPlaying())
            		mPreviewVideoView.stopPlayback();
            	
            	if (position != lastPlayed)
            	{
            		Media[] mediaArray = {medias[position]};
            		mPreviewVideoView.setMedia(mediaArray);
                	mPreviewVideoView.play();

                    lastPlayed = position;
            	}
            	else
            	{
            		lastPlayed = -1;
            	}
                
            }
            else
            {
                showThumbnail(position);
            }
        }
        
    }
    
    private void showThumbnail(int position) {
        Media[] medias = mMPM.mScene.getMediaAsArray();
        if (medias[position] != null) {
        	mImageViewMedia.setVisibility(View.VISIBLE);
            mPreviewVideoView.setVisibility(View.GONE);
            
            Bitmap thumb = Media.getThumbnail(mActivity,medias[position],mActivity.mMPM.mProject);
            		
            mImageViewMedia.setImageBitmap(thumb);
            mImageViewMedia.invalidate();
        }
    }
    
    /*
    private void renderPreview ()
    {

        Message msg = mHandlerPub.obtainMessage(888);
        msg.getData().putString("status",
                getActivity().getString(R.string.rendering_clips_));
        mHandlerPub.sendMessage(msg);

        try {
            mMPM.doExportMedia(mMPM.getExportMediaFile(), false, true);
            MediaDesc mdExported = mMPM.getExportMedia();
            File mediaFile = new File(mdExported.path);

            if (mediaFile.exists()) {

                Message message = mHandlerPub.obtainMessage(777);
                message.getData().putString("fileMedia", mdExported.path);
                message.getData().putString("mime", mdExported.mimeType);

                mHandlerPub.sendMessage(message);
            }
            else {
                Message msgErr = new Message();
                msgErr.what = -1;
                msgErr.getData().putString("err", "Media export failed");
                mHandlerPub.sendMessage(msgErr);
            }
        } 
        catch (Exception e) {
            Message msgErr = new Message();
            msgErr.what = -1;
            msgErr.getData().putString("err", e.getLocalizedMessage());
            mHandlerPub.sendMessage(msgErr);
            Log.e(AppConstants.TAG, "error posting", e);
        }
    }*/
    
    public void enableTrimMode(boolean enable) {
        if (enable) {
            mLLControlBar.setVisibility(View.GONE);
            mRangeSeekBarContainer.setVisibility(View.VISIBLE);
            mTrimMode = true;
            setupTrimUndo();
        } else {
            mLLControlBar.setVisibility(View.VISIBLE);
            mRangeSeekBarContainer.setVisibility(View.GONE);
            mTrimMode = false;
        }
    }
    
    public void saveTrim() {
        Media media = mMPM.mScene.getMediaAsArray()[mCurrentClipIdx];
        boolean dirty = false;
        
        if (media.getTrimStart() != mRangeSeekBar.getSelectedMinValue()) {
            media.setTrimStart(mRangeSeekBar.getSelectedMinValue());
            dirty = true;
        }
        
        if (media.getTrimEnd() != mRangeSeekBar.getSelectedMaxValue()) {
            media.setTrimEnd(mRangeSeekBar.getSelectedMaxValue());
            dirty = true;
        }
        
        if (dirty) media.save(); // FIXME move dirty into model classes save() method
    }
    
    private float trimStartUndo = -1f;
    private float trimEndUndo = -1f;
    
    public void setupTrimUndo() {
    
    	if (mMPM != null && mMPM.mScene != null)
    	{
	    	Media media = mMPM.mScene.getMediaAsArray()[mCurrentClipIdx];
	        
	    	if (media != null)
	    	{
	    		trimStartUndo = media.getTrimStart();
	    		trimEndUndo = media.getTrimEnd();
	    	}
    	}
    }
    
    public void undoSaveTrim() {
        Media media = mMPM.mScene.getMediaAsArray()[mCurrentClipIdx];
        
        if (trimStartUndo != -1) media.setTrimStart(trimStartUndo);
        if (trimEndUndo != -1) media.setTrimEnd(trimEndUndo);
        media.save();
    }
    
    public void loadTrim() {
    	
        Media media = mMPM.mScene.getMediaAsArray()[mCurrentClipIdx];
        
        if (media != null)
        { 	
	        mRangeSeekBar.setSelectedMinValue(Math.round(media.getTrimStart()));
	        if (media.getTrimEnd() > 0) {
	            mRangeSeekBar.setSelectedMaxValue(Math.round(media.getTrimEnd()));
	        } else {
	            mRangeSeekBar.setSelectedMaxValue(99);
	        }
        }
    }
    
    public void stopPlaybackOnTabChange()
    { 
        if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO || mMPM.mProject.getStoryType() == Project.STORY_TYPE_AUDIO)
        {       
        	if (mPreviewVideoView.isPlaying())
        	{
            	 stopPreviewPlayback();
        	}
        }
        else if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY || mMPM.mProject.getStoryType() == Project.STORY_TYPE_PHOTO)
        {
            if (mKeepRunningPreview)
            {
                stopPhotoPlayback();           
            }        
        }
    }
}