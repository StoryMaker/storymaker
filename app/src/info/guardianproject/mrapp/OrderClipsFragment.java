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
    int layout;
    public ViewPager mAddClipsViewPager;
    View mView = null;
    private EditorBaseActivity mActivity;
    Button mPlayButton, mButtonAddNarration, mButtonPlayNarration;
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

    public OrderClipsFragment()
    {
    	
    }
    
    public OrderClipsFragment(int layout, EditorBaseActivity activity)
            throws IOException, JSONException {
        this.layout = layout;
        mActivity = activity;
        mMPM = activity.mMPM;
        mHandlerPub = ((SceneEditorActivity)mActivity).mHandlerPub;
    }

    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

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
                // TODO Auto-generated method stub
                
            }
            
        });
        
        mRangeSeekBar = new RangeSeekBar<Integer>(0, 99, getActivity());

        mRangeSeekBarContainer = (ViewGroup) view.findViewById(R.id.llRangeSeekBar);
        mRangeSeekBarContainer.addView(mRangeSeekBar);
        
        mRangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {

            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue,
                    Integer maxValue) {
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
                previewClip(mCurrentClipIdx);
//                if (min != bar.getSelectedMinValue().intValue()) {
//                    // they were dragging the first handle
//                    previewClip(mCurrentClipIdx);
//                } else {
//                    // they were dragging the second handle
//                    // FIXME try showing the last 1 second only to help picking the end point
//                }
            }
        });
        
        if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY)
        {
            
            if (mAudioNarrator == null)
            {
                String audioFile = "narration" + mMPM.mScene.getId() + ".wav";
                mFileAudioNarration = new File(mMPM.getProjectFolder(mMPM.mProject),audioFile);
                mAudioNarrator = new AudioRecorderView(mFileAudioNarration,getActivity());
            }
        }
        
        mButtonAddNarration = (Button)view.findViewById(R.id.buttonAddNarration);
        if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY
        		|| mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO
        		)
        {
        	
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
	        
	        mButtonPlayNarration= (Button)view.findViewById(R.id.buttonPlayNarration);
	        if (mFileAudioNarration != null && mFileAudioNarration.exists())
	            mButtonPlayNarration.setEnabled(true);
	        
	        mButtonPlayNarration.setOnClickListener(new OnClickListener ()
	        {
	            @Override
	            public void onClick(View v) {
	                playNarration(!mAudioNarrator.isPlaying());
	            }
	        });
	        
	        mPlayButton = (Button) view.findViewById(R.id.buttonPlay);
	        mPlayButton.setOnClickListener(new OnClickListener() {
	
	            @Override
	            public void onClick(View v) {
	                
	                if (mMPM.mProject.getStoryType() == Project.STORY_TYPE_VIDEO
	                        || mMPM.mProject.getStoryType() == Project.STORY_TYPE_AUDIO)
	                {
	                   handleVideoAudioPlayToggle();
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
            handleVideoAudioPlayToggle();
        
        //start the audio recorder
        mAudioNarrator.startRecording();
        
        mButtonPlayNarration.setEnabled(true);
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
            handleVideoAudioPlayToggle();

    }
    
    private void playNarration (boolean start)
    {
        
        if (start)
            mAudioNarrator.startPlaying();
        else
            mAudioNarrator.stopPlaying();
        
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
            mSeekBar.setMax(mMPM.mScene.getMediaAsList().size()*mPhotoEssaySlideLength);
            mSeekBar.setProgress(0);
        
            mPlayButton.setText(R.string.stop_recording);
            mKeepRunningPreview = true;
            
            Thread thread = new Thread ()
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
             mPreviewVideoView.setMedia(mMPM.mScene.getMediaAsArray());
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
    
    public void loadMedia ()
    {
        mOrderClipsDGV.removeAllViews();
        
        Media[] sceneMedias = mMPM.mScene.getMediaAsArray();

        for (int i = 0; i < sceneMedias.length; i++)
        {
            ImageView iv = new ImageView(getActivity());
            
            if (sceneMedias[i] != null) {
                iv.setImageBitmap(mActivity.getThumbnail(sceneMedias[i]));
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
                mMPM.mScene.swapMediaIndex(oldIndex, newIndex);
                mActivity.mdExported= null;
            }
        });

        mOrderClipsDGV.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mTrimMode) {
                    Log.d(TAG, "item clicked");
                    mCurrentClipIdx = position;
                    showThumbnail(position);
    //                previewClip(position);
                }
            }
        });
      
    }
    
    private void previewClip(int position) {
        Media[] medias = mMPM.mScene.getMediaAsArray();
        if (medias[position] != null) {
            
            if (medias[position].getMimeType().startsWith("video"))
            {
                mImageViewMedia.setVisibility(View.GONE);
                mPreviewVideoView.setVisibility(View.VISIBLE);
                // play
                mPreviewVideoView.stopPlayback();
                Media[] mediaArray = {medias[position]};
                mPreviewVideoView.setMedia(mediaArray);
                mPreviewVideoView.play();
                
                //mSeekBar.setMax(mPreviewVideoView.getDuration());
                
            }
            else if (medias[position].getMimeType().startsWith("audio"))
            {
                mImageViewMedia.setVisibility(View.GONE);
                mPreviewVideoView.setVisibility(View.VISIBLE);
                // play
                mPreviewVideoView.stopPlayback();
                Media[] mediaArray = {medias[position]};
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
        Media[] medias = mMPM.mScene.getMediaAsArray();
        if (medias[position] != null) {
            mImageViewMedia.setImageBitmap(mActivity.getThumbnail(medias[position]));
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
    
    private int trimStartUndo = -1;
    private int trimEndUndo = -1;
    public void setupTrimUndo() {
        Media media = mMPM.mScene.getMediaAsArray()[mCurrentClipIdx];
        
        trimStartUndo = media.getTrimStart();
        trimEndUndo = media.getTrimEnd();
    }
    
    public void undoSaveTrim() {
        Media media = mMPM.mScene.getMediaAsArray()[mCurrentClipIdx];
        
        if (trimStartUndo != -1) media.setTrimStart(trimStartUndo);
        if (trimEndUndo != -1) media.setTrimEnd(trimEndUndo);
        media.save();
    }
    
    public void loadTrim() {
        Media media = mMPM.mScene.getMediaAsArray()[mCurrentClipIdx];
        mRangeSeekBar.setSelectedMinValue(media.getTrimStart());
        if (media.getTrimEnd() > 0) {
            mRangeSeekBar.setSelectedMaxValue(media.getTrimEnd());
        } else {
            mRangeSeekBar.setSelectedMaxValue(99);
        }
    }
}