package info.guardianproject.mrapp.media;

import info.guardianproject.mrapp.AppConstants;
import info.guardianproject.mrapp.SceneEditorActivity;
import info.guardianproject.mrapp.StoryMakerApp;
import info.guardianproject.mrapp.media.exporter.MediaAudioExporter;
import info.guardianproject.mrapp.media.exporter.MediaVideoExporter;
import info.guardianproject.mrapp.media.exporter.MediaSlideshowExporter;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Scene;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

public class MediaProjectManager implements MediaManager {
	

    public final static String EXPORT_VIDEO_FILE_EXT = ".mp4";
//    public final static String EXPORT_AUDIO_FILE_EXT = ".m4a";//".ogg";//"".3gp";
    public final static String EXPORT_AUDIO_FILE_EXT = ".3gp";//".ogg";//"".3gp";

    public final static String EXPORT_PHOTO_FILE_EXT = ".jpg";
    public final static String EXPORT_ESSAY_FILE_EXT = ".mp4";
    
	private ArrayList<MediaClip> mMediaList = null;// FIXME refactor this to use it as a prop on project object
	
	private static File mFileExternDir; //where working files go
	//private File mMediaTmp;
	private MediaDesc mOut;
	
	//private MediaHelper.MediaResult mMediaResult;
	public MediaHelper mMediaHelper;
	
	public Project mProject = null;
	public Scene mScene = null;
	
	private Context mContext = null;

	private Activity mActivity;
	private Handler mHandler;
	
	public int mClipIndex;  // FIXME hack to get clip we are adding media too into intent handler

	private SharedPreferences mSettings;
	
	private static boolean mUseInternal = false;
	
    public MediaProjectManager (Activity activity, Context context, Intent intent, Handler handler, int pid) {
        this(activity, context, intent, handler, Project.get(context, pid));
    }
    
    public MediaProjectManager (Activity activity, Context context, Intent intent, Handler handler, Project project) {
        this(activity, context, intent, handler, project, null);
    }
    
    public MediaProjectManager (Activity activity, Context context, Intent intent, Handler handler, Project project, Scene scene) {
        mActivity = activity;
        mContext = context;
        mHandler = handler;
        
        mProject = project;
        if (scene == null) {
            mScene = project.getScenesAsArray()[0];
        } else {
            mScene = scene;
        }
        

        mSettings = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());

    }
    
    public void initProject()
    {
        int clipCount = 0;
        for (Scene s : mProject.getScenesAsArray()) {
            clipCount += s.getClipCount();
        } 
        mMediaList = new ArrayList<MediaClip>(clipCount);
        for (int i = 0; i < clipCount; i++) {
            mMediaList.add(null);
        }
    
        mMediaHelper = new MediaHelper (mActivity, mHandler);
        
        initExternalStorage(mContext);
    }
        
    public void addAllProjectMediaToEditor() {
        Media[] _medias = mScene.getMediaAsArray();
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
    
    public void handleResponse (Intent intent, File fileCapturePath) throws IOException
    {                
        MediaDesc result = mMediaHelper.handleIntentLaunch(intent);
        
        if (result == null && fileCapturePath != null)
        {
        	result = new MediaDesc();
        	result.path = fileCapturePath.getCanonicalPath();
        	result.mimeType = mMediaHelper.getMimeType(result.path);
        }
        
    	if (result != null && result.path != null && result.mimeType != null)
    	{
    		try
    		{
    			addMediaFile(mClipIndex, result.path, result.mimeType);
    			// FIXME use media type as definied in json
    			mScene.setMedia(mClipIndex, "FIXME", result.path, result.mimeType);
    			mScene.save();
    			((SceneEditorActivity)mActivity).refreshClipPager();
    		}
			catch (IOException ioe)
			{
				Log.e(AppConstants.TAG,"error adding media result",ioe);
			}
        }
        
    }
    
    public void deleteCurrentClip ()
    {
    	
    //	mScene.setMedia(mClipIndex, "FIXME", null, null);
    	Media media = mScene.getMediaAsList().get(mClipIndex);
    	media.delete();
    	((SceneEditorActivity)mActivity).refreshClipPager();
    }

   
    private static synchronized void initExternalStorage (Context context)
    {
    	
    	if (mFileExternDir == null)
    	{
    	
    		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    		 
    		mUseInternal = settings.getBoolean("p_use_internal_storage",false);
        	
    		if (mUseInternal)
    			mFileExternDir = context.getDir(AppConstants.FOLDER_PROJECTS_NAME,Context.MODE_WORLD_WRITEABLE|Context.MODE_WORLD_READABLE);
    		else
    			mFileExternDir = new File(context.getExternalFilesDir(null),AppConstants.FOLDER_PROJECTS_NAME);
    		
	    	mFileExternDir.mkdirs();
	    	
    	}
    }
    
    public static File getRenderPath (Context context)
    {

		 File fileRenderTmpDir = null;
		 
		 if (mUseInternal)
			 fileRenderTmpDir = context.getDir("render", Context.MODE_PRIVATE);
		 else
			 fileRenderTmpDir = new File(context.getExternalFilesDir(null),"render");
		 
		 return fileRenderTmpDir;
    }
    
    public static File getExternalProjectFolder (Project project, Context context)
    {
    	initExternalStorage (context);
    	
    	String folderName = project.getId()+"";
    	File fileProject = new File(mFileExternDir,folderName);
    	
    	fileProject.mkdirs();
    	
    	return fileProject;
    }
    
    
    public File getExportMediaFile ()
    {

        String fileName = mProject.getId()+"";
        
        try
        {

        	fileName = java.net.URLEncoder.encode(mProject.getTitle(),"UTF-8").toString();
        	
            String timeStamp = new java.text.SimpleDateFormat("ddMMyyyyHHmmss").format(new java.util.Date ());
        	fileName += timeStamp;
        }
        catch (Exception e){}
        
        
        //default to "project" folder
    	File fileExport = null;
	    
    	
    	if (mProject.getStoryType() == Project.STORY_TYPE_VIDEO)
        {
    		fileExport = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), fileName + EXPORT_VIDEO_FILE_EXT);
		    
        }    
        else if (mProject.getStoryType() == Project.STORY_TYPE_AUDIO)
        {

        	fileExport = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS), fileName + EXPORT_AUDIO_FILE_EXT);
		    
        }
        else if (mProject.getStoryType() == Project.STORY_TYPE_PHOTO)
        {
       	 			
			//	there should be only one
			fileExport = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName + EXPORT_PHOTO_FILE_EXT);
 	    	
        }
        else if (mProject.getStoryType() == Project.STORY_TYPE_ESSAY)
        {
       	
		    fileExport = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), fileName + EXPORT_ESSAY_FILE_EXT);
		    
        }
        else
        {
		    fileExport = new File(Environment.getExternalStoragePublicDirectory(null), fileName + EXPORT_ESSAY_FILE_EXT);

        }
    	
    	return fileExport;
    }
    
    public void doExportMedia (File fileExport, boolean doCompress, boolean doOverwrite) throws Exception
    {
    	 Message msg = mHandler.obtainMessage(0);
         msg.getData().putString("status","cancelled");
         ArrayList<Media> mList = mProject.getMediaAsList();
         ArrayList<MediaDesc> alMediaIn = new ArrayList<MediaDesc>();

         //is storage ready
         ((StoryMakerApp)mActivity.getApplication()).isExternalStorageReady();
         ((StoryMakerApp)mActivity.getApplication()).killZombieProcs();

 		//first check that all of the input images are accessible
 		
 		for (Media media : mList)
 		{
 			if (media == null || media.getPath() == null)
 			{
 				//throw new IOException("Input media object is null");
 			}
 			else if (!new File(media.getPath()).exists())
 			{
 				throw new java.io.FileNotFoundException("Input image does not exist or is not readable" + ": " + media.getPath());
 				
 			}
 			
 		}

		 File fileRenderTmpDir = getRenderPath(mContext);

		 File fileRenderTmp = new File(fileRenderTmpDir,new Date().getTime() + "");
		 fileRenderTmp.mkdirs();
		 
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
    	    		mDesc.path = new File(media.getPath()).getCanonicalPath();
    	    		
    	    		if (media.getTrimStart() > 0) {
    	    		    mDesc.startTime = "" + media.getTrimmedStartTime() / 1000F;
                        mDesc.duration = "" + media.getTrimmedDuration() / 1000F;
    	    		} else if ((media.getTrimEnd() < 99) && media.getTrimEnd() > 0) {
    	    		    mDesc.duration = "" + media.getTrimmedDuration() / 1000F;
    	    		}
    	    		
    	    		if (doCompress)
    	    			applyExportSettings(mDesc);
    	    		
    	    		alMediaIn.add(mIdx, mDesc);
    	    		mIdx++;
	    	    }
	    	}
	
		    mOut = new MediaDesc ();
	    	
	    	if (doCompress)	
	    		applyExportSettings(mOut);
	    	else
	    	{
	    		mOut.audioCodec = "aac";
		    	mOut.audioBitrate = 64;
	    	}
	    	
	    	//override for now
	    	mOut.mimeType = AppConstants.MimeTypes.MP4;

		    mOut.path = fileExport.getCanonicalPath();
		    
		    String audioPath = null;
		    
		    File fileAudio = new File(getExternalProjectFolder(mProject, mContext),"narration" + mScene.getId() + ".wav");
    		
    		if (fileAudio.exists())
    			audioPath = fileAudio.getCanonicalPath();
    		else
    		{
    			fileAudio = new File(mContext.getExternalFilesDir(null),"narration" + mScene.getId() + ".wav");
    			if (fileAudio.exists())
        			audioPath = fileAudio.getCanonicalPath();
    		}
		    
		    if ((!fileExport.exists()) || doOverwrite)
		    {
		    	if (fileExport.exists())
		    		fileExport.delete();
		    	
		    	fileExport.getParentFile().mkdirs();
			    	
			    //there can be only one renderer now - MP4Stream !!
			    	MediaVideoExporter mEx = new MediaVideoExporter(mContext, mHandler, alMediaIn, fileRenderTmp, mOut);
			    	
			    	if (audioPath != null)
			    	{
			    		MediaDesc audioTrack = new MediaDesc();
			    		audioTrack.path = audioPath;
			    		mEx.addAudioTrack(audioTrack);
			    	}
			    	
			    	mEx.export();
			    
		    }
	   
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
     	    		

    	    		if (media.getTrimStart() > 0) {
    	    		    mDesc.startTime = "" + media.getTrimmedStartTime() / 1000F;
                        mDesc.duration = "" + media.getTrimmedDuration() / 1000F;
    	    		} else if ((media.getTrimEnd() < 99) && media.getTrimEnd() > 0) {
    	    		    mDesc.duration = "" + media.getTrimmedDuration() / 1000F;
    	    		}
     	    		
     	    		if (doCompress)
     	    			applyExportSettings(mDesc);
     	    		
     	    		alMediaIn.add(mIdx++,mDesc);
        	    }
 	    	}
 	
 		    mOut = new MediaDesc ();
// 		    mOut.mimeType = AppConstants.MimeTypes.OGG;
 //		    mOut.mimeType = AppConstants.MimeTypes.MP4_AUDIO;
		    mOut.mimeType = AppConstants.MimeTypes.THREEGPP_AUDIO;
 		    
 		   // if (doCompress)
 		   applyExportSettingsAudio(mOut);
 		    
 		    mOut.path = fileExport.getCanonicalPath();
 		    
 		   if ((!fileExport.exists()) || doOverwrite)
		    {

		    	if (fileExport.exists())
		    		fileExport.delete();
		    	
		    	fileExport.getParentFile().mkdirs();

 	 		    MediaAudioExporter mEx = new MediaAudioExporter(mContext, mHandler, alMediaIn, fileRenderTmp, mOut);
 	 		    mEx.export();
		    }
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

  	  		    	
  	    				fileExport.getParentFile().mkdirs();
  	  			    
  	    				fileExport.createNewFile();
  	    				IOUtils.copy(new FileInputStream(fileSrc),new FileOutputStream(fileExport));
  	    				 mOut = new MediaDesc ();
  	    				mOut.path = fileExport.getCanonicalPath();
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
    	    		
    	    		File fileSrc = new File(media.getPath());
    	    		
    	    		
    	    		File fileTmp = new File(fileRenderTmp,fileSrc.getName());
    	    		if (!fileTmp.exists())
    	    		{
    	    			fileTmp.getParentFile().mkdirs();
    	    			fileTmp.createNewFile();
    	    			IOUtils.copy(new FileInputStream(fileSrc),new FileOutputStream(fileTmp));
    	    		}
    	    		
    	    		mDesc.path = fileTmp.getCanonicalPath();
    	    		
    	    		if (doCompress)
    	    			applyExportSettings(mDesc);
    	    		alMediaIn.add(mDesc);
	    	    }
	    	}
	
		    mOut = new MediaDesc ();
		    
		    if (doCompress)
		    applyExportSettings(mOut);
		   
		    mOut.path = fileExport.getCanonicalPath();
		    mOut.mimeType = AppConstants.MimeTypes.MP4;
		    

		    int slideDuration = Integer.parseInt(mSettings.getString("pslideduration", AppConstants.DEFAULT_SLIDE_DURATION+""));

		    String audioPath = null;
    		
    		File fileAudio = new File(getExternalProjectFolder(mProject, mContext),"narration" + mScene.getId() + ".wav");
    		
    		if (fileAudio.exists())
    			audioPath = fileAudio.getCanonicalPath();
    		else
    		{
    			fileAudio = new File(mContext.getExternalFilesDir(null),"narration" + mScene.getId() + ".wav");
    			if (fileAudio.exists())
        			audioPath = fileAudio.getCanonicalPath();
    		}
    		
    		if ((!fileExport.exists()) || doOverwrite)
   		    {

		    	if (fileExport.exists())
		    		fileExport.delete();
		    	
		    	fileExport.getParentFile().mkdirs();
			    
			    MediaSlideshowExporter mEx = new MediaSlideshowExporter(mContext, mHandler, alMediaIn, fileRenderTmp, audioPath, slideDuration, mOut);
			    
			    mEx.export();
   		    }
         }
         
         deleteRecursive(fileRenderTmp, true);
		 
         
    }
    
    void deleteRecursive(File fileOrDirectory, boolean onExit) throws IOException {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
            	deleteRecursive(child, onExit);

        if (!onExit)
        {
        	fileOrDirectory.delete();
        }
        else
        	fileOrDirectory.deleteOnExit();
    }
    
    
    public void applyExportSettings (MediaDesc mdout)
    {
    	
    	mdout.videoBitrate = Integer.parseInt(mSettings.getString("p_video_bitrate", AppConstants.DEFAULT_VIDEO_BITRATE+""));;
    	mdout.audioBitrate = Integer.parseInt(mSettings.getString("p_audio_bitrate", AppConstants.DEFAULT_AUDIO_BITRATE+""));;
    	mdout.videoFps = mSettings.getString("p_video_framerate", AppConstants.DEFAULT_FRAME_RATE);
    	mdout.width = Integer.parseInt(mSettings.getString("p_video_width", AppConstants.DEFAULT_WIDTH+""));
    	mdout.height = Integer.parseInt(mSettings.getString("p_video_height", AppConstants.DEFAULT_HEIGHT+""));
    	
    	mdout.videoCodec = mSettings.getString("p_video_codec","mpeg4");
    	
    }

    
    public void applyExportSettingsAudio (MediaDesc mdout)
    {
    	mdout.videoCodec = null; 
    	mdout.audioCodec =  mSettings.getString("p_audio_codec","aac");
    	mdout.audioBitrate = Integer.parseInt(mSettings.getString("p_audio_bitrate", AppConstants.DEFAULT_AUDIO_BITRATE+""));;
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
    	
    	

    	/*
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
		{*/
			//its the first clip and/or the previous item is the same type as this, or this is not an audio clip
    		
			MediaClip mClip = new MediaClip();
			mClip.mMediaDescOriginal = mdesc;
			
			mMediaList.set(clipIndex, mClip);
			
			((SceneEditorActivity)mActivity).refreshClipPager(); // FIXME we should handle this by emitting a change event directly

		//}
		
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
    //		File fileExportProjectDir = new File(mFileExportDir,mProject.getId()+"");

    		MediaRenderer mRenderer = new MediaRenderer(mContext, (MediaManager)this, mHandler, mClip, mFileExternDir, shellCallback);
    		// Convert to video
    		Thread thread = new Thread (mRenderer);
    		thread.setPriority(Thread.NORM_PRIORITY);
    		thread.start();
	
    }
    
    	
	
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
	}*/
	
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
