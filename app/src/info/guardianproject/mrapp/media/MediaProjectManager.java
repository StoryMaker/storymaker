package info.guardianproject.mrapp.media;

import info.guardianproject.mrapp.AppConstants;
import info.guardianproject.mrapp.MediaOutputPreferences;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.R.id;
import info.guardianproject.mrapp.R.layout;
import info.guardianproject.mrapp.R.menu;
import info.guardianproject.mrapp.R.string;
import info.guardianproject.mrapp.SceneEditorActivity;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.ui.MediaView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.MediaUtils;
import org.ffmpeg.android.ShellUtils.ShellCallback;
import org.ffmpeg.android.filters.DrawBoxVideoFilter;
import org.ffmpeg.android.filters.DrawTextVideoFilter;
import org.ffmpeg.android.filters.FadeVideoFilter;
import org.ffmpeg.android.filters.VideoFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MediaProjectManager implements MediaManager {
	
	private ArrayList<MediaClip> mMediaList = null;// FIXME refactor this to use it as a prop on project object
	
	private File mFileExternDir;
	
	//private File mMediaTmp;
	private MediaDesc mOut;
	
	//private MediaHelper.MediaResult mMediaResult;
	public MediaHelper mMediaHelper;
	
	public Project mProject = null;
	
	private Context mContext = null;

	private Activity mActivity;
	private Handler mHandler;
	
	public int mClipIndex;  // FIXME hack to get clip we are adding media too into intent handler

    public MediaProjectManager (Activity activity, Context context, Intent intent, Handler handler, int pid) {
    
        this(activity, context, intent, handler, Project.get(context, pid));
    }
    
    public MediaProjectManager (Activity activity, Context context, Intent intent, Handler handler, Project project) {
        mActivity = activity;
        mContext = context;
        mHandler = handler;
        
        mProject = project;
        
        initProject();
    }
    
    private void initProject()
    {
        mMediaList = new ArrayList<MediaClip>(mProject.getClipCount());
        
        for (int i = 0; i < mProject.getClipCount(); i++)
            mMediaList.add(null); 
        
        mMediaHelper = new MediaHelper (mActivity, mHandler);
        
        initExternalStorage();

        Media[] _medias = mProject.getMediaAsArray();
        for (Media media: _medias) {
        	if (media != null) {
                try
                {
                    addMediaFile(media.getClipIndex(), media.getPath(), media.getMimeType());
                }
                catch (IOException ioe)
                {
                    Log.e(AppConstants.TAG,"error adding media from saved project", ioe);
                }
        	}
        }
    
        
    }
    

    public MediaDesc getExportMedia ()
    {
    	return mOut;
    }
    
    public void handleResponse (Intent intent, File fileCapturePath)
    {                
        MediaDesc result = mMediaHelper.handleIntentLaunch(intent);
        
        if (result == null && fileCapturePath != null)
        {
        	result = new MediaDesc();
        	result.path = fileCapturePath.getAbsolutePath();
        	result.mimeType = mMediaHelper.getMimeType(result.path);
        }
        
    	if (result != null && result.path != null && result.mimeType != null)
    	{
    		try
    		{
    			addMediaFile(mClipIndex, result.path, result.mimeType);
    			mProject.setMedia(mClipIndex, "FIXME", result.path, result.mimeType);
    			mProject.save();
    			((SceneEditorActivity)mActivity).refreshClipPager();
    		}
			catch (IOException ioe)
			{
				Log.e(AppConstants.TAG,"error adding media result",ioe);
			}
        }
        
    }

   
    private void initExternalStorage ()
    {
    	String extState = Environment.getExternalStorageState();
    	
    	if (extState.equals(Environment.MEDIA_MOUNTED) || extState.equals(Environment.MEDIA_SHARED))
    	{
			//fileExternDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    		mFileExternDir = new File(mContext.getExternalFilesDir(null),AppConstants.FILE_MEDIAFOLDER_NAME);    		
    	}
    	else
    	{
    		mFileExternDir = new File(Environment.getDataDirectory(),AppConstants.FILE_MEDIAFOLDER_NAME);    		
    	}
    	
    	mFileExternDir.mkdirs();
    }
    
    private final static String EXPORT_VIDEO_FILE_EXT = ".mp4";
    private final static String EXPORT_AUDIO_FILE_EXT = ".3gp";
    private final static String EXPORT_PHOTO_FILE_EXT = ".jpg";
    private final static String EXPORT_ESSAY_FILE_EXT = ".mp4";
    
    public void doExportMedia (String fileName, boolean doCompress) throws IOException
    {
    	 Message msg = mHandler.obtainMessage(0);
         msg.getData().putString("status","cancelled");

         ArrayList<Media> mList = mProject.getMediaAsList();
         ArrayList<MediaDesc> alMediaIn = new ArrayList<MediaDesc>();
         
         //for video, render the sequence together
         if (mProject.getStoryType() == Project.STORY_TYPE_VIDEO)
         {
        	int mIdx = 0;
	    	for (Media media : mList)
	    	{
	    	    if (media != null)
	    	    {
    	    		MediaDesc mDesc = new MediaDesc();
    	    		mDesc.mimeType = media.getMimeType();
    	    		mDesc.path = media.getPath();
    	    		
    	        	applyExportSettings(mDesc);
    	    		alMediaIn.add(mIdx, mDesc);
    	    		mIdx++;
	    	    }
	    	}
	
		    mOut = new MediaDesc ();
	    	mOut.mimeType = AppConstants.MimeTypes.MP4;

		    if (doCompress)
		    	applyExportSettings(mOut);
		    
		    File fileExport = new File(mFileExternDir, fileName + EXPORT_VIDEO_FILE_EXT);
		    fileExport.delete();
		    fileExport.createNewFile();
		    mOut.path = fileExport.getAbsolutePath();
		    
		   MediaVideoAudioExporter mEx = new MediaVideoAudioExporter(mContext, mHandler, alMediaIn, mOut);
		   mEx.run();
	   
         }    
         else if (mProject.getStoryType() == Project.STORY_TYPE_AUDIO)
         {
        	int mIdx = 0; 
        	for (Media media : mList)
 	    	{
        	    if (media != null)
        	    {
     	    		MediaDesc mDesc = new MediaDesc();
     	    		mDesc.mimeType = media.getMimeType();
     	    		mDesc.path = media.getPath();
     	        	applyExportSettings(mDesc);
     	    		alMediaIn.add(mIdx++,mDesc);
        	    }
 	    	}
 	
 		    mOut = new MediaDesc ();
 		    mOut.mimeType = AppConstants.MimeTypes.THREEGPP_AUDIO;

 		    applyExportSettingsAudio(mOut);
 		    
 		    File fileExport = new File(mFileExternDir, fileName + EXPORT_AUDIO_FILE_EXT);
 		    fileExport.delete();
 		    fileExport.createNewFile();
 		    mOut.path = fileExport.getAbsolutePath();
 		    
 		   MediaVideoAudioExporter mEx = new MediaVideoAudioExporter(mContext, mHandler, alMediaIn, mOut);
 		   mEx.run();
         }
         else if (mProject.getStoryType() == Project.STORY_TYPE_PHOTO)
         {
        	 for (Media media : mList)
  	    	{
        		 if (media == null)
        			 continue;
        		 
  	    		MediaDesc mDesc = new MediaDesc();
  	    		mDesc.mimeType = media.getMimeType();
  	    		mDesc.path = media.getPath();
  	        	
  	    		if (mDesc.path != null)
  	    		{
  	    			File fileSrc = new File (mDesc.path);
  	    			
  	    			if (fileSrc.exists())
  	    			{
  	    				//	there should be only one
  	    				File fileExport = new File(mFileExternDir, fileName + EXPORT_PHOTO_FILE_EXT);
  	    				fileExport.createNewFile();
  	    				IOUtils.copy(new FileInputStream(fileSrc),new FileOutputStream(fileExport));
  	    				 mOut = new MediaDesc ();
  	    				mOut.path = fileExport.getAbsolutePath();
  	    				mOut.mimeType = AppConstants.MimeTypes.JPEG;
  	    				
  	    				break;
  	    			}
  	    			
  	    		}

  	 		    
  	    	}
         }
         else if (mProject.getStoryType() == Project.STORY_TYPE_ESSAY)
         {
        	
	    	for (Media media : mList)
	    	{
	    	    if (media != null)
	    	    {
    	    		MediaDesc mDesc = new MediaDesc();
    	    		mDesc.mimeType = media.getMimeType();
    	    		mDesc.path = media.getPath();
    	        	applyExportSettings(mDesc);
    	    		alMediaIn.add(mDesc);
	    	    }
	    	}
	
		    mOut = new MediaDesc ();
		    applyExportSettings(mOut);
		    
		    File fileExport = new File(mFileExternDir, fileName + EXPORT_ESSAY_FILE_EXT);
		    fileExport.delete();
		    fileExport.createNewFile();
		    mOut.path = fileExport.getAbsolutePath();
		    
		    int slideDuration = 5; //where to set this?
    		File fileAudio = new File(Environment.getExternalStorageDirectory(),"narration" + mProject.getId() + ".wav");
    		
		   MediaSlideshowExporter mEx = new MediaSlideshowExporter(mContext, mHandler, alMediaIn,fileAudio.getAbsolutePath(), slideDuration, mOut);
		   mEx.run();
         }
    }
    
/*
    private File createOutputFile (String fileext) throws IOException
    {
		File saveFile = File.createTempFile("output", '.' + fileext, mFileExternDir);	
		return saveFile;
    }
    */
    
    public void applyExportSettings (MediaDesc mdout)
    {
    	//look this up from prefs?
    	mdout.videoCodec = "libx264";
    	mdout.videoBitrate = 1500;
    	mdout.audioBitrate = 128;
    	mdout.videoFps = "29.97";
    	mdout.width = 720;
    	mdout.height = 480;
    	
    }

    public void applyExportSettingsAudio (MediaDesc mdout)
    {
    	//look this up from prefs?
    	mdout.videoCodec = null; 
    	mdout.audioCodec = "aac";
    	mdout.audioBitrate = 128;
    	mdout.format = "3gp";
    }
    
    
    
    private void addMediaFile (int clipIndex, String path, String mimeType) throws IOException
    {
    	MediaDesc mdesc = new MediaDesc ();
    	mdesc.path = path;
    	mdesc.mimeType = mimeType;
    	
    	while ((clipIndex+1) > mMediaList.size())
    	{
    	    mMediaList.add(null);
    	}

		if (mimeType.startsWith("audio") && mMediaList.get(clipIndex) != null 
		        && (!mMediaList.get(mMediaList.size()-1).mMediaDescOriginal.mimeType.equals(mimeType)))
		{
			MediaClip mClipVideo =  mMediaList.get(clipIndex);
			
			MediaClip mClipAudio = new MediaClip();
			mClipAudio.mMediaDescOriginal = mdesc;
		
				ShellCallback sc = null;
	    		MediaMerger mm = new MediaMerger(mContext, (MediaManager)this, mHandler, mClipVideo, mClipAudio, mFileExternDir, sc);
	    		// Convert to video
	    		Thread thread = new Thread (mm);
	    		thread.setPriority(Thread.NORM_PRIORITY);
	    		thread.start();
		
			
		}
		else
		{
			//its the first clip and/or the previous item is the same type as this, or this is not an audio clip
    		
			MediaClip mClip = new MediaClip();
			mClip.mMediaDescOriginal = mdesc;
			
			mMediaList.set(clipIndex, mClip);
			
			((SceneEditorActivity)mActivity).refreshClipPager(); // FIXME we should handle this by emitting a change event directly

		}
		
		mOut = null;
    	
//		if (mimeType.startsWith("audio") && mediaList.size() > 0 && (!mediaList.get(mediaList.size()-1).mMediaDescOriginal.mimeType.equals(mimeType)))
//		{
//			MediaClip mClipVideo =  mediaList.get(mediaList.size()-1);
//			
//			MediaClip mClipAudio = new MediaClip();
//			mClipAudio.mMediaDescOriginal = mdesc;
//		
//			try {
//				ShellCallback sc = null;
//	    		MediaMerger mm = new MediaMerger(mContext, (MediaManager)this, mHandler, mClipVideo, mClipAudio, fileExternDir, sc);
//	    		// Convert to video
//	    		Thread thread = new Thread (mm);
//	    		thread.setPriority(Thread.NORM_PRIORITY);
//	    		thread.start();
//			} catch (Exception e) {
//				updateStatus("error merging video and audio");
//				Log.e(AppConstants.TAG,"error merging video and audio",e);
//			}
//			
//			
//		}
//		else
//		{
//			//its the first clip and/or the previous item is the same type as this, or this is not an audio clip
//    		
//			MediaClip mClip = new MediaClip();
//			mClip.mMediaDescOriginal = mdesc;
//			mediaList.add(clipIndex, mClip);
//			
//			int mediaId = mediaList.size()-1;
//			
//			MediaView mView = addMediaView(mClip, mediaId);
//			
//			prerenderMedia (mClip, mView); 
//		}
//		
//		mOut = null;
    	
    }
    
    
    /*
    public void prerenderMedia (MediaClip mClip, ShellCallback shellCallback)
    {
    	
    	
    		MediaRenderer mRenderer = new MediaRenderer(mContext, (MediaManager)this, mHandler, mClip, mFileExternDir, shellCallback);
    		// Convert to video
    		Thread thread = new Thread (mRenderer);
    		thread.setPriority(Thread.NORM_PRIORITY);
    		thread.start();
	
    }*/
    
    	
	
	private void copyFile ()
	{
		// TODO prompt user for storage location?
		 if (mOut != null && mOut.path != null) {
			 File inFile = new File(mOut.path);
			 FileChannel in;
			 try {
				 in = new FileInputStream(inFile).getChannel();
				 FileChannel out = new FileOutputStream(new File("/sdcard/"
						 + inFile.getName())).getChannel();
				 in.transferTo(0, in.size(), out);
			 } catch (FileNotFoundException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
			 } catch (IOException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
			 }
		 }
	}
	
//	private void showAddMediaDialog (final Activity activity)
//	{
//		
//		final CharSequence[] items = {"Open Gallery","Open File","Choose Shot","Record Video", "Record Audio", "Take Photo"};
//
//		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//		builder.setTitle("Choose medium");
//		builder.setItems(items, new DialogInterface.OnClickListener() {
//		    public void onClick(DialogInterface dialog, int item) {
//		        
//		    	switch (item) {
//		    		case 0:
//		    			mMediaHelper.openGalleryChooser("*/*");
//		    			break;
//		    		case 1:
//		    			mMediaHelper.openFileChooser();
//		    			break;
//		    		case 2:
//		    			showOverlayCamera(activity);
//		    			break;
//		    		case 3:
//		    			mMediaTmp = mMediaHelper.captureVideo(fileExternDir);
//
//		    			break;
//		    		case 4:
//		    			mMediaTmp = mMediaHelper.captureAudio(fileExternDir);
//
//		    			break;
//		    		case 5:
//		    			mMediaTmp = mMediaHelper.capturePhoto(fileExternDir);
//		    			break;
//		    		default:
//		    			//do nothing!
//		    	}
//		    	
//		    	
//		    }
//		});
//		
//		AlertDialog alert = builder.create();
//		alert.show();
//	}


	
	
	
	
}
