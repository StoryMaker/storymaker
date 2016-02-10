package org.storymaker.app.media;

import timber.log.Timber;

import org.storymaker.app.AppConstants;
import org.storymaker.app.R;
import org.storymaker.app.StoryMakerApp;
import org.storymaker.app.Utils;
import org.storymaker.app.db.StoryMakerDB;
import org.storymaker.app.media.exporter.MediaAudioExporter;
import org.storymaker.app.media.exporter.MediaVideoExporter;
import org.storymaker.app.media.exporter.MediaSlideshowExporter;
import org.storymaker.app.model.AudioClip;
import org.storymaker.app.model.Media;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.ProjectTable;
import org.storymaker.app.model.Scene;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.ffmpeg.android.MediaDesc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;

import scal.io.liger.StorageHelper;
import timber.log.Timber;

public class MediaProjectManager implements MediaManager {
	public final static String TAG = "MediaProjectManager";

    public final static String EXPORT_VIDEO_FILE_EXT = ".mp4";
//    public final static String EXPORT_AUDIO_FILE_EXT = ".m4a";//".ogg";//"".3gp";
    public final static String EXPORT_AUDIO_FILE_EXT = ".3gp";//".ogg";//"".3gp";

    public final static String EXPORT_PHOTO_FILE_EXT = ".jpg";
    public final static String EXPORT_ESSAY_FILE_EXT = ".mp4";
    
	private ArrayList<MediaClip> mMediaList = null;// FIXME refactor this to use it as a prop on project object
	
	private static File sFileExternDir; //where working files go
	//private File mMediaTmp;
	
	//private MediaHelper.MediaResult mMediaResult;
	public MediaHelper mMediaHelper;
	
	public Project mProject = null;
	public Scene mScene = null;
	
	private Context mContext = null;

	 private Activity mActivity;  // check for nulls, if called from the service this will be null
	
	private Handler mHandler;
	
	public int mClipIndex;  // FIXME hack to get clip we are adding media too into intent handler

	private SharedPreferences mSettings;
	
	private static boolean mUseInternal = false;
	
    public MediaProjectManager (Activity activity, Context context, Handler handler, int projectId) {
        this(activity, context, handler, (Project)(new ProjectTable()).get(context, projectId)); // FIXME ugly
    }
    
    public MediaProjectManager (Activity activity, Context context, Handler handler, Project project) {
        this(activity, context, handler, project, null);
    }
    
    public MediaProjectManager(Activity activity, Context context, Handler handler, Project project, Scene scene) {
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
        
    @SuppressLint("NewApi")
	private static synchronized void initExternalStorage (Context context)
    {   	
    	if (sFileExternDir == null){

			// file location now handled by helper class

			/*
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    		mUseInternal = settings.getBoolean("p_use_internal_storage",false);

    		boolean isStorageEmulated = false;
    		
    		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB){
    			isStorageEmulated = Environment.isExternalStorageEmulated();
    		}
    		
    		if (mUseInternal && !isStorageEmulated){
    			sFileExternDir = new File (context.getFilesDir(), AppConstants.FOLDER_PROJECTS_NAME);
    		}
    		else{
    			sFileExternDir = new File(Environment.getExternalStorageDirectory(), AppConstants.FOLDER_PROJECTS_NAME);
    		}
            Timber.d("sFileExternDir:" + sFileExternDir.getAbsolutePath());
            try {
                Timber.d("sFileExternDir.getCanonicalPath():" + sFileExternDir.getCanonicalPath());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            */

			sFileExternDir = new File(StorageHelper.getActualStorageDirectory(context), AppConstants.FOLDER_PROJECTS_NAME);

			Timber.d("sFileExternDir:" + sFileExternDir.getAbsolutePath());

			sFileExternDir.mkdirs();
    	}
    }
    
    public static File getRenderPath (Context context)
    {

	    // simplifying this method to ensure cleanup happens as intended
		// StorageHelper method should respect internal/external storage settings

		File fileRenderTmpDir = new File(StorageHelper.getActualStorageDirectory(context), "render");

		return fileRenderTmpDir;
    }
    
    public static File getExternalProjectFolder (Project project, Context context)
    {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    	
    	initExternalStorage (context);
    	
    	String folderName = "no_date"; // not certain how to best handle this
    	if (project.getCreatedAt() != null)
    	    folderName = sdf.format(project.getCreatedAt());
    	File fileProject = new File(sFileExternDir,folderName);
    	fileProject.mkdirs();
    	
    	return fileProject;
    }
    
    public static File getExternalProjectFolderOld (Project project, Context context)
    {
    	initExternalStorage (context);
    	
    	String folderName = project.getId()+"";
    	File fileProject = new File(sFileExternDir,folderName);
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
    
    public MediaDesc doExportMedia (File fileExport, boolean doCompress, boolean doOverwrite) throws Exception
    {
        MediaDesc mMediaDescOut = null;
    	 Message msg = mHandler.obtainMessage(0);
         msg.getData().putString("status","cancelled");
         ArrayList<Media> mList = mProject.getMediaAsList();
         ArrayList<MediaDesc> alMediaIn = new ArrayList<MediaDesc>();

         Utils.Proc.killZombieProcs(mContext);

		 double storageSpace = checkStorageSpace();
         //if not enough space
		 if (storageSpace > 0)
         {
			 // throw exception to pass info up the stack
			 throw new Exception(String.format(mContext.getString(R.string.error_storage_space), storageSpace));
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
    	    		MediaDesc mediaDesc = new MediaDesc();
    	    		mediaDesc.mimeType = media.getMimeType();
    	    		mediaDesc.path = new File(media.getPath()).getCanonicalPath();

    	    		if (media.getTrimStart() > 0) {
    	    		    mediaDesc.startTime = "" + media.getTrimmedStartTimeFloat() / 1000F;
                        mediaDesc.duration = "" + media.getTrimmedDuration() / 1000F;
    	    		} else if ((media.getTrimEnd() < 99) && media.getTrimEnd() > 0) {
    	    		    mediaDesc.duration = "" + media.getTrimmedDuration() / 1000F;
    	    		}

					mediaDesc.audioVolume = media.getVolume();

    	    		if (doCompress)
    	    			applyExportSettings(mediaDesc);

    	    		applyExportSettingsResolution(mediaDesc);

    	    		alMediaIn.add(mIdx, mediaDesc);
    	    		mIdx++;
	    	    }
	    	}

		    mMediaDescOut = new MediaDesc();

	    	if (doCompress)
	    		applyExportSettings(mMediaDescOut);
	    	else //this is the default audio codec settings
	    	{
	    		mMediaDescOut.audioCodec = "aac";
		    	mMediaDescOut.audioBitrate = 64;
	    	}

	    	applyExportSettingsResolution(mMediaDescOut);

	    	//override for now
	    	mMediaDescOut.mimeType = AppConstants.MimeTypes.MP4;

		    mMediaDescOut.path = fileExport.getCanonicalPath();

		    if ((!fileExport.exists()) || doOverwrite)
		    {
		    	if (fileExport.exists()) {
                    fileExport.delete();
                }

		    	fileExport.getParentFile().mkdirs();

			    //there can be only one renderer now - MP4Stream !!
                MediaVideoExporter mEx = new MediaVideoExporter(mContext, mHandler, alMediaIn, fileRenderTmp, mMediaDescOut);

//			    	if (audioPath != null)
                for (AudioClip audioClip: mProject.getAudioClipsAsList()) {
                    // TODO truncate and pad the font of and set volume on this clip
                    String tempPath = audioClip.getPath();
                    MediaDesc audioTrack = new MediaDesc();
                    audioTrack.path = tempPath;
                    audioTrack.startTime = audioClip.getStartTime(); // negative time will be padded in export
                    audioTrack.duration = audioClip.getDuration();
					audioTrack.audioVolume = audioClip.getVolume();

                    // TODO put volume in here
                    mEx.addAudioTrack(audioTrack);
                }
                mEx.export();
		    }
         } else if (mProject.getStoryType() == Project.STORY_TYPE_AUDIO) {
        	int mIdx = 0; 
        	for (Media media : mList)
 	    	{
        	    if (media != null)
        	    {
     	    		MediaDesc mDesc = new MediaDesc();
     	    		mDesc.mimeType = media.getMimeType();
     	    		mDesc.path = media.getPath();
     	    		

    	    		if (media.getTrimStart() > 0) {
    	    		    mDesc.startTime = "" + media.getTrimmedStartTimeFloat() / 1000F;
                        mDesc.duration = "" + media.getTrimmedDuration() / 1000F;
    	    		} else if ((media.getTrimEnd() < 99) && media.getTrimEnd() > 0) {
    	    		    mDesc.duration = "" + media.getTrimmedDuration() / 1000F;
    	    		}
     	    		
     	    		if (doCompress)
     	    			applyExportSettings(mDesc);
     	    		
     	    		applyExportSettingsResolution(mDesc);
     	    		
     	    		alMediaIn.add(mIdx++,mDesc);
        	    }
 	    	}
 	
 		    mMediaDescOut = new MediaDesc ();
// 		    mOut.mimeType = AppConstants.MimeTypes.OGG;
 //		    mOut.mimeType = AppConstants.MimeTypes.MP4_AUDIO;
		    mMediaDescOut.mimeType = AppConstants.MimeTypes.THREEGPP_AUDIO;
 		    
 		   // if (doCompress)
 		   applyExportSettingsAudio(mMediaDescOut);
 		   
 		   applyExportSettingsResolution(mMediaDescOut);
 		    
 		    mMediaDescOut.path = fileExport.getCanonicalPath();
 		    
 		   if ((!fileExport.exists()) || doOverwrite)
		    {

		    	if (fileExport.exists())
		    		fileExport.delete();
		    	
		    	fileExport.getParentFile().mkdirs();

 	 		    MediaAudioExporter mEx = new MediaAudioExporter(mContext, mHandler, alMediaIn, fileRenderTmp, mMediaDescOut);
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
  	    				 mMediaDescOut = new MediaDesc ();
  	    				mMediaDescOut.path = fileExport.getCanonicalPath();
  	    				mMediaDescOut.mimeType = AppConstants.MimeTypes.JPEG;
  	    				
  	    				applyExportSettingsResolution(mMediaDescOut);
  	    				
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
    	    		
    	    		applyExportSettingsResolution(mDesc);
    	    		
    	    		alMediaIn.add(mDesc);
	    	    }
	    	}
	
		    mMediaDescOut = new MediaDesc ();
		    
		    if (doCompress)
		    applyExportSettings(mMediaDescOut);
		    
		    applyExportSettingsResolution(mMediaDescOut);
		   
		    mMediaDescOut.path = fileExport.getCanonicalPath();
		    mMediaDescOut.mimeType = AppConstants.MimeTypes.MP4;
		    

		    int slideDuration = Integer.parseInt(mSettings.getString("pslideduration", AppConstants.DEFAULT_SLIDE_DURATION+""));

		    String audioPath = null;
    		
    		File fileAudio = new File(getExternalProjectFolder(mProject, mContext),"narration" + mScene.getId() + ".wav");
    		
    		if (fileAudio.exists())
    			audioPath = fileAudio.getCanonicalPath();
    		else
    		{
    			// fileAudio = new File(mContext.getExternalFilesDir(null),"narration" + mScene.getId() + ".wav");
				fileAudio = new File(StorageHelper.getActualStorageDirectory(mContext),"narration" + mScene.getId() + ".wav");

				if (fileAudio.exists())
        			audioPath = fileAudio.getCanonicalPath();
    		}
    		
    		if ((!fileExport.exists()) || doOverwrite)
   		    {

		    	if (fileExport.exists())
		    		fileExport.delete();
		    	
		    	fileExport.getParentFile().mkdirs();
			    
			    MediaSlideshowExporter mEx = new MediaSlideshowExporter(mContext, mHandler, alMediaIn, fileRenderTmp, audioPath, slideDuration, mMediaDescOut);
			    
			    mEx.export();
   		    }
         }
         
         deleteRecursive(fileRenderTmp, true);
         return mMediaDescOut;
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
    	mdout.videoBitrate = Integer.parseInt(mSettings.getString("p_video_bitrate", AppConstants.DEFAULT_VIDEO_BITRATE+""));
    	mdout.videoFps = mSettings.getString("p_video_framerate", AppConstants.DEFAULT_FRAME_RATE);
    	mdout.videoCodec = mSettings.getString("p_video_codec","mpeg4");
    	
    	mdout.audioBitrate = Integer.parseInt(mSettings.getString("p_audio_bitrate", AppConstants.DEFAULT_AUDIO_BITRATE+""));
    	mdout.audioCodec = mSettings.getString("p_audio_codec", AppConstants.DEFAULT_AUDIO_CODEC);
    	
    	mdout.width = Integer.parseInt(mSettings.getString("p_video_width", AppConstants.DEFAULT_WIDTH+""));
    	mdout.height = Integer.parseInt(mSettings.getString("p_video_height", AppConstants.DEFAULT_HEIGHT+""));
    }

    
    public void applyExportSettingsAudio (MediaDesc mdout)
    {
    	mdout.videoCodec = null; 
    	mdout.audioCodec =  mSettings.getString("p_audio_codec","aac");
    	mdout.audioBitrate = Integer.parseInt(mSettings.getString("p_audio_bitrate", AppConstants.DEFAULT_AUDIO_BITRATE+""));;
    	mdout.format = "3gp";
    }
    
    /***
     Method to convert video/images within resolution boundaries:
      1)Stock Media Player (1920x1088 MAX)
      2)MPEG-1 (4095x4095 MAX) 
    ***/
    public void applyExportSettingsResolution (MediaDesc mdout)
    {
    	int videoRes = Integer.parseInt(mSettings.getString("p_video_resolution", "0"));
    	
    	switch (videoRes)
    	{
	        case 1080:	
        		mdout.width = 1920;
        		mdout.height = 1080;
            	break;
	        case 720:
        		mdout.width = 1280;
        		mdout.height = 720;
        		break;
	        case 480:  
        		mdout.width = 720;
    			mdout.height = 480;
             	break;
	        case 360: 
        		mdout.width = 640;
        		mdout.height = 360;
        		break;
	        default:
        		mdout.width = Integer.parseInt(mSettings.getString("p_video_width", AppConstants.DEFAULT_WIDTH+""));
        		mdout.height = Integer.parseInt(mSettings.getString("p_video_height", AppConstants.DEFAULT_HEIGHT+""));
                break;
    	}
    }
    
    public Context getContext()
    {
    	return this.mContext;
    }

	// return space required to pass info up the stack
	public double checkStorageSpace()
    {
    	ArrayList<Media> mList = this.mProject.getMediaAsList();
    	Long totalBytesRequired= 0l;
    	
 		//first check that all of the input images are accessible		
 		for (Media media : mList)
 		{		
			try 
			{
	 			if (media == null || media.getPath() == null)
	 			{}
	 			else if (!new File(media.getPath()).exists())
	 			{
	 				throw new java.io.FileNotFoundException();			
	 			}
	 			else
	 			{
	 				File currentFile = new File(media.getPath());
	 				totalBytesRequired += (long)currentFile.length();
	 			} 
			} 
			catch (java.io.FileNotFoundException fnfe) 
			{
				Timber.e(fnfe, "Input image does not exist or is not readable" + ": " + media.getPath());
			}			
 		}
 		
 		//get memory path
        String memoryPath;
 		if(mUseInternal)
 		{	
 			memoryPath = Environment.getDataDirectory().getPath();
 		}
 		else
 		{
 			memoryPath = Environment.getExternalStorageDirectory().getPath();
 		}
 		
 		//get memory
 		StatFs stat = new StatFs(memoryPath);
 		Long totalBytesAvailable = (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();

    	//if not enough storage
		// FIXME we should raise this error via a intent, not a straight toast so it can be handled if the activity is hidden
 		if(totalBytesRequired > totalBytesAvailable)
 		{
 			double totalMBRequired = totalBytesRequired /(double)(1024*1024);
 			
 			// FIXME we need to warn the user here via a message sent to the current activity since this could be called from a service now
			// can't create toast message here because instances created by renderer have a null value for activity
 			return totalMBRequired;
 		}
    	  	
    	return 0;
    }
    
    public static boolean migrateProjectFiles(Project project, Context context)
    {
    	File oldDir = getExternalProjectFolderOld(project, context);
    	File newDir = getExternalProjectFolder(project, context);
    	
    	String oldString = "";
    	String newString = "";
    	
    	try{
            oldString = oldDir.getCanonicalPath();
            newString = newDir.getCanonicalPath();
    	} catch (Exception e) {
    		Timber.e("exception ocurred while getting project path: " + e.getMessage());
            return false;
    	}
    	
        if (oldDir.exists() && newDir.exists())
    	{
    	    File[] oldFiles = oldDir.listFiles();
            
            for (File oldFile : oldFiles)
            {
                try{
                    String oldPath = oldFile.getCanonicalPath();
                    String newPath = oldPath.replace(oldString, newString);
                    File newFile = new File(newPath);
                    oldFile.renameTo(newFile);
                } catch (Exception e) {
                    Timber.e("exception ocurred while moving files: " + e.getMessage());
                    return false;
                }
            }
    	}
    	else if (!oldDir.exists())
    	{
    		Timber.e(oldString + " (old directory) doesn't exist");
    		return false;
    	}
    	else if (!newDir.exists())
    	{
    	    Timber.e(newString + " (new directory) doesn't exist"); // created by get external dir method
            return false;
    	}
    	else
    	{
    	    Timber.e("an unexpected error has ocurred");
            return false;
    	}
    	
    	return true;
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
