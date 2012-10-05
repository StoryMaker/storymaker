package info.guardianproject.mrapp.media;

import info.guardianproject.mrapp.AppConstants;

import java.io.File;
import java.io.IOException;

import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MediaMerger implements Runnable
{

	private Handler mHandler;
	private MediaClip  mMediaClipVideo, mMediaClipAudio;
	private Context mContext;
	private File mFileExternDir;
	private ShellCallback mShellCallback;
	private MediaManager mMediaManager;
	
	public MediaMerger (Context context, MediaManager mediaManager, Handler handler, MediaClip mediaClipVideo, MediaClip mediaClipAudio, File fileExternDir, ShellCallback shellCallback)
	{
		mHandler = handler;
		mMediaClipVideo = mediaClipVideo;
		mMediaClipAudio = mediaClipAudio;
		
		mContext = context;
		mFileExternDir = fileExternDir;
		mShellCallback = shellCallback;
		mMediaManager = mediaManager;
	}
	
	public void run ()
	{
    	try
    	{
    		 Message msg = mHandler.obtainMessage(0);
	            msg.getData().putString("status","Merging audio/video in the background");

		        mHandler.sendMessage(msg);
		        
	    		mMediaClipVideo.mMediaDescRendered = doMerge(mMediaClipVideo.mMediaDescOriginal, mMediaClipAudio.mMediaDescOriginal);
	    	
    		
    		File fileMediaOut = new File(mMediaClipVideo.mMediaDescRendered.path);
    		
	         if (!fileMediaOut.exists() || (fileMediaOut.length() == 0))
	         {
	        	msg = mHandler.obtainMessage(0);
		        msg.getData().putString("status","Error occured with media pre-render");
		        mHandler.sendMessage(msg);
			 }
	         else
	         {
	        	 msg = mHandler.obtainMessage(0);
			        msg.getData().putString("status","Success - media rendered!");
			        mHandler.sendMessage(msg);
	         }
    	}
    	catch (Exception e)
    	{
    		Message msg = mHandler.obtainMessage(0);
            msg.getData().putString("status","error: " + e.getMessage());

	         mHandler.sendMessage(msg);
    		Log.e(AppConstants.TAG, "error exporting",e);
    	}
	}
	
    private MediaDesc doMerge (MediaDesc videoIn, MediaDesc audioIn) throws Exception
    {
    	FfmpegController ffmpegc = new FfmpegController (mContext);
    	
		File fileOutPath = createOutputFile("mp4"); 
    	mMediaManager.applyExportSettings(videoIn);

    	videoIn.videoBitrate = -1;
    	videoIn.videoCodec = "copy";
    	audioIn.audioBitrate = 128;
    	audioIn.audioCodec = "aac";
    	
    	MediaDesc mediaOut = ffmpegc.combineAudioAndVideo(videoIn, audioIn, fileOutPath.getAbsolutePath(), mShellCallback);
    
    	return mediaOut;
    
   }

    private File createOutputFile (String fileext) throws IOException
    {
		File saveFile = File.createTempFile("output", '.' + fileext, mFileExternDir);	
		return saveFile;
    }
}
