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

public class MediaRenderer implements Runnable
{

	private Handler mHandler;
	private MediaClip  mMediaClip;
	private Context mContext;
	private File mFileExternDir;
	private ShellCallback mShellCallback;
	private MediaManager mMediaManager;
	
    private int current, total;

	public MediaRenderer (Context context, MediaManager mediaManager, Handler handler, MediaClip mediaClip, File fileExternDir, ShellCallback shellCallback)
	{
		mHandler = handler;
		mMediaClip = mediaClip;
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
	            msg.getData().putString("status","Rendering media in the background");

		        mHandler.sendMessage(msg);
		        
    		if (mMediaClip.mMediaDescOriginal.mimeType.startsWith("image"))
    			mMediaClip.mMediaDescRendered = prerenderImage(mMediaClip.mMediaDescOriginal);
	    	else if (mMediaClip.mMediaDescOriginal.mimeType.startsWith("video"))
	    		mMediaClip.mMediaDescRendered = prerenderVideo(mMediaClip.mMediaDescOriginal, false);
	    	else if (mMediaClip.mMediaDescOriginal.mimeType.startsWith("audio"))
	    		mMediaClip.mMediaDescRendered = prerenderAudio(mMediaClip.mMediaDescOriginal);
	    	else //wave? audio?	    	
	    		mMediaClip.mMediaDescRendered = prerenderVideo(mMediaClip.mMediaDescOriginal, true);
	    	
    		
    		File fileMediaOut = new File(mMediaClip.mMediaDescRendered.path);
    		
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
	
	private MediaDesc prerenderAudio (MediaDesc mediaIn) throws Exception
    {
    	FfmpegController ffmpegc = new FfmpegController (mContext);
    	
		File fileOutPath = createOutputFile("mp4"); 
    	mMediaManager.applyExportSettings(mediaIn);
    	mediaIn.videoCodec = null;
    	mediaIn.mimeType = "audio/wav";
    	
    	MediaDesc mediaOut = ffmpegc.convertToMP4Stream(mediaIn, fileOutPath.getAbsolutePath(), true, mShellCallback);
    
    	return mediaOut;
    
   }
    private MediaDesc prerenderVideo (MediaDesc mediaIn, boolean preconvertMP4) throws Exception
    {
    	FfmpegController ffmpegc = new FfmpegController (mContext);
    	
		File fileOutPath = createOutputFile("mp4"); 
    	mMediaManager.applyExportSettings(mediaIn);

    	MediaDesc mediaOut = ffmpegc.convertToMP4Stream(mediaIn, fileOutPath.getAbsolutePath(), preconvertMP4, mShellCallback);
    
    	return mediaOut;
    
   }

    private MediaDesc prerenderImage (MediaDesc mediaIn) throws Exception
    {
    	
    	FfmpegController ffmpegc = new FfmpegController (mContext);
    	
    	int durationSecs = 5;
    	
    	File outPath = createOutputFile("mp4");
    	mMediaManager.applyExportSettings(mediaIn);
    	MediaDesc mediaOut = ffmpegc.convertImageToMP4(mediaIn, durationSecs, outPath.getAbsolutePath(), mShellCallback);
    
    	return prerenderVideo(mediaOut, false);
    
   }


    private File createOutputFile (String fileext) throws IOException
    {
		File saveFile = File.createTempFile("output", '.' + fileext, mFileExternDir);	
		return saveFile;
    }
}
