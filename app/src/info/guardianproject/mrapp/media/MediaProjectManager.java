package info.guardianproject.mrapp.media;

import info.guardianproject.mrapp.AppConstants;
import info.guardianproject.mrapp.MediaOutputPreferences;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.R.id;
import info.guardianproject.mrapp.R.layout;
import info.guardianproject.mrapp.R.menu;
import info.guardianproject.mrapp.R.string;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.ui.MediaView;
import info.guardianproject.mrapp.ui.OverlayCamera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

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
	
	private ArrayList<MediaClip> mediaList = new ArrayList<MediaClip>();
	private ArrayList<MediaView> mediaViewList = new ArrayList<MediaView>();
	
	private File fileExternDir;
	
	private File mMediaTmp;
	private MediaDesc mOut;
	
	private MediaHelper mMediaHelper;
	private MediaHelper.MediaResult mMediaResult;
	
	private Project mProject = null;
	
	private Context mContext = null;
	
    public MediaProjectManager (Context context) {
    
    	mContext = context;
        
        initExternalStorage();
        
    }
    
    public void handleResponse (Activity activity, Intent intent)
    {
        
        mMediaHelper = new MediaHelper (activity, mHandler);                
        
        MediaDesc result = mMediaHelper.handleIntentLaunch(intent);
        	
    	int pid = intent.getIntExtra("pid", -1);
        if (pid != -1) {
            mProject = Project.get(activity.getApplicationContext(), pid);
            Media[] _medias = mProject.getMediaAsArray();
            for (Media media: _medias) {
                try
                {
                    addMediaFile(media.getPath(), media.getMimeType());
                }
                catch (IOException ioe)
                {
                    Log.e(AppConstants.TAG,"error adding media from saved project", ioe);
                }
            }
        } // FIXME else what?
    	
    	if (result != null && result.path != null && result.mimeType != null)
    	{
    		try
    		{
    			addMediaFile(result.path, result.mimeType);
    			mProject.appendMedia(result.path, result.mimeType);
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
    		fileExternDir = mContext.getExternalFilesDir(null);
    		
    	}
    	else
    	{
    		fileExternDir = Environment.getDataDirectory();
    	}
    }
    
    
    private void doExportMedia ()
    {
    	 Message msg = mHandler.obtainMessage(0);
         msg.getData().putString("status","cancelled");

    	ArrayList<MediaDesc> listMediaDesc = new ArrayList<MediaDesc>();
    	for (MediaClip mClip : mediaList)
    		if (mClip.mMediaDescRendered != null)
    			listMediaDesc.add(mClip.mMediaDescRendered);
    		else
    			listMediaDesc.add(mClip.mMediaDescOriginal);

	    try
	    {
		    mOut = new MediaDesc ();
		    applyExportSettings(mOut);
		    mOut.path = createOutputFile("mp4").getAbsolutePath();
		    
		   MediaExporter mEx = new MediaExporter(mContext, mHandler, listMediaDesc, mOut);
		   
			// Convert to video
			Thread thread = new Thread (mEx);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
	    }
	    catch (IOException ioe)
	    {
	    	updateStatus("error creating file: " + ioe.getMessage());
	    	Log.e(AppConstants.TAG,"error creating file",ioe);
	       
	    }
    
    }
    

    private File createOutputFile (String fileext) throws IOException
    {
		File saveFile = File.createTempFile("output", '.' + fileext, fileExternDir);	
		return saveFile;
    }
    
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
    
    private void addMediaFile (String path, String mimeType) throws IOException
    {
    	MediaDesc mdesc = new MediaDesc ();
    	mdesc.path = path;
    	mdesc.mimeType = mimeType;
    	
		if (mimeType.startsWith("audio") && mediaList.size() > 0 && (!mediaList.get(mediaList.size()-1).mMediaDescOriginal.mimeType.equals(mimeType)))
		{
			MediaClip mClipVideo =  mediaList.get(mediaList.size()-1);
			
			MediaClip mClipAudio = new MediaClip();
			mClipAudio.mMediaDescOriginal = mdesc;
		
			try {
				ShellCallback sc = null;
	    		MediaMerger mm = new MediaMerger(mContext, (MediaManager)this, mHandler, mClipVideo, mClipAudio, fileExternDir, sc);
	    		// Convert to video
	    		Thread thread = new Thread (mm);
	    		thread.setPriority(Thread.NORM_PRIORITY);
	    		thread.start();
			} catch (Exception e) {
				updateStatus("error merging video and audio");
				Log.e(AppConstants.TAG,"error merging video and audio",e);
			}
		
			
		}
		else
		{
			//its the first clip and/or the previous item is the same type as this
    		
			MediaClip mClip = new MediaClip();
			mClip.mMediaDescOriginal = mdesc;
			mediaList.add(mClip);
			
			int mediaId = mediaList.size()-1;
			
			MediaView mView = addMediaView(mClip, mediaId);
			
			prerenderMedia (mClip, mView);
		}
		
		mOut = null;
    	
    }
    
    public void prerenderMedia (MediaClip mClip, ShellCallback shellCallback)
    {
    	
    	try {
    		MediaRenderer mRenderer = new MediaRenderer(mContext, (MediaManager)this, mHandler, mClip, fileExternDir, shellCallback);
    		// Convert to video
    		Thread thread = new Thread (mRenderer);
    		thread.setPriority(Thread.NORM_PRIORITY);
    		thread.start();
		} catch (Exception e) {
			Toast.makeText(mContext,"error converting video to mpeg",Toast.LENGTH_SHORT).show();
			Log.e(AppConstants.TAG,"error converting video to mpeg",e);
		}
	
    	
    }
    
    private MediaView addMediaView (MediaClip mediaClip, int mediaId) throws IOException
    {
    	File fileMediaClip = new File(mediaClip.mMediaDescOriginal.path);

    	 MediaView mediaView = null;
    	 MediaDesc mdesc = mediaClip.mMediaDescOriginal;
    	     	 
     	if (mdesc.mimeType.startsWith("image"))
     	{
     		
     		mediaView = new MediaView(mContext,(MediaManager)this, mediaClip,fileMediaClip.getName(), mMediaHelper.getBitmapThumb(fileMediaClip), Color.WHITE, Color.BLACK);

     	}
     	else if (mdesc.mimeType.startsWith("video"))
     	{
     		mediaView = new MediaView(mContext,(MediaManager)this,  mediaClip,fileMediaClip.getName(), MediaUtils.getVideoFrame(mdesc.path, 5), Color.WHITE, Color.BLACK);
     		
     	}
     	else if (mdesc.mimeType.startsWith("audio")) 
     	{
     		mediaView = new MediaView(mContext,(MediaManager)this,  mediaClip, fileMediaClip.getName(), null, Color.WHITE, Color.BLACK);

     	}
     	else 
     	{
     		mediaView = new MediaView(mContext,(MediaManager)this,  mediaClip, fileMediaClip.getName(),null, Color.WHITE, Color.BLACK);

     	}
    
     	mediaView.setId(mediaId);
     	
     
		return mediaView;
    }
    
    
    private void updateStatus (String msg)
    {
    	Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
    
    /*
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		
		if (resultCode == RESULT_OK)
		{
			if (requestCode == 777) //overlay camerae
			{
				//now launch real camera
				mMediaTmp = mMediaHelper.captureVideo(fileExternDir);
				

			}
			else
			{
				mMediaResult = mMediaHelper.handleResult(requestCode, resultCode, intent, mMediaTmp);
				
				try
				{
					if (mMediaResult != null)
						if (mMediaResult.path != null)
							addMediaFile(mMediaResult.path, mMediaResult.mimeType);
				}
				catch (IOException ioe)
				{
					Log.e(AppConstants.TAG,"error adding media result",ioe);
				}
				
			
				//if path is null, wait for the scanner callback in the mHandler
			}
		}
		
		
	}	*/
	
	
	public void showMediaPrefs (Activity activity)
	{
		Intent intent = new Intent(activity, MediaOutputPreferences.class);
		activity.startActivityForResult(intent,0);
		
	}

	private void playMedia ()
	{
		 mMediaHelper.playMedia(new File(mOut.path), MediaConstants.MIME_TYPE_VIDEO);

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
	}
	
	private void showAddMediaDialog (final Activity activity)
	{
		
		final CharSequence[] items = {"Open Gallery","Open File","Choose Shot","Record Video", "Record Audio", "Take Photo"};

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Choose medium");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        
		    	switch (item) {
		    		case 0:
		    			mMediaHelper.openGalleryChooser("*/*");
		    			break;
		    		case 1:
		    			mMediaHelper.openFileChooser();
		    			break;
		    		case 2:
		    			showOverlayCamera(activity);
		    			break;
		    		case 3:
		    			mMediaTmp = mMediaHelper.captureVideo(fileExternDir);

		    			break;
		    		case 4:
		    			mMediaTmp = mMediaHelper.captureAudio(fileExternDir);

		    			break;
		    		case 5:
		    			mMediaTmp = mMediaHelper.capturePhoto(fileExternDir);
		    			break;
		    		default:
		    			//do nothing!
		    	}
		    	
		    	
		    }
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showOverlayCamera (Activity activity)
	{
		Intent intent = new Intent(activity, OverlayCamera.class);
		activity.startActivityForResult(intent,777);
		
	}
	
	private Handler mHandler = new Handler()
	{
		 public void handleMessage(Message msg) {
			 
			 String status = msg.getData().getString("status");
			 
	            switch (msg.what) {
		            case 0: //status
	
		            	updateStatus(status);
		                   
	                    
	                 break;
	                case 1: //status
	                	updateStatus(status);
	                    break;
	               
	                case 4: //play video
	                	
	                	playMedia();
	                	break;
	                	
	                case 5:
	                		
	                		if (mMediaResult != null)
	                		{
	                			String path = msg.getData().getString("path");
	                			try
	                			{
	                				addMediaFile(path, mMediaResult.mimeType);
	                                mProject.appendMedia(path, mMediaResult.mimeType);
	                			}
	                			catch (IOException ioe)
	                			{
	                				Log.e(AppConstants.TAG,"error adding media result",ioe);
	                			}
	                		}
	                	break;
	                default:
	                    super.handleMessage(msg);
	            }
	        }
	};
	
	
}
