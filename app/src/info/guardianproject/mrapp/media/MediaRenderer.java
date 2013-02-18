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
    		 
    		if (mMediaClip.mMediaDescOriginal.mimeType.startsWith("image"))
    			mMediaClip.mMediaDescRendered = prerenderImage(mMediaClip.mMediaDescOriginal);
	    	else if (mMediaClip.mMediaDescOriginal.mimeType.startsWith("video"))
	    		mMediaClip.mMediaDescRendered = prerenderVideo(mMediaClip.mMediaDescOriginal, false);
	    	else if (mMediaClip.mMediaDescOriginal.mimeType.startsWith("audio"))
	    		mMediaClip.mMediaDescRendered = prerenderAudio(mMediaClip.mMediaDescOriginal);
	    	else //wave? audio?	    	
	    		mMediaClip.mMediaDescRendered = prerenderVideo(mMediaClip.mMediaDescOriginal, true);
	    	
    		
    	}
    	catch (Exception e)
    	{
    		Message msg = mHandler.obtainMessage(-1);
            msg.getData().putString("status","error: " + e.getMessage());

	         mHandler.sendMessage(msg);
    		Log.e(AppConstants.TAG, "error exporting",e);
    	}
	}
	
	private MediaDesc prerenderAudio (MediaDesc mediaIn) throws Exception
    {
    	FfmpegController ffmpegc = new FfmpegController (mContext);
    	
    	File outPath = createOutputFile(mediaIn.path,"mp4");
    	mMediaManager.applyExportSettings(mediaIn);
    	mediaIn.videoCodec = null;
    	mediaIn.mimeType = "audio/3gp";
    	
    	MediaDesc mediaOut = ffmpegc.convertToMP4Stream(mediaIn, outPath.getAbsolutePath(), mShellCallback);
    
    	return mediaOut;
    
   }
    private MediaDesc prerenderVideo (MediaDesc mediaIn, boolean preconvertMP4) throws Exception
    {
    	FfmpegController ffmpegc = new FfmpegController (mContext);
    	
    	File outPath = createOutputFile(mediaIn.path,"mp4");
    	mMediaManager.applyExportSettings(mediaIn);

    	MediaDesc mediaOut = ffmpegc.convertToMP4Stream(mediaIn, outPath.getAbsolutePath(), mShellCallback);
    
    	return mediaOut;
    
   }

    private MediaDesc prerenderImage (MediaDesc mediaIn) throws Exception
    {
    	
    	FfmpegController ffmpegc = new FfmpegController (mContext);
    	
    	int durationSecs = 5;
    	
    	File outPath = createOutputFile(mediaIn.path,"mp4");
    	
    	mMediaManager.applyExportSettings(mediaIn);
    	MediaDesc mediaOut = ffmpegc.convertImageToMP4(mediaIn, durationSecs, outPath.getAbsolutePath(), mShellCallback);
    
    	return prerenderVideo(mediaOut, false);
    
   }


    private File createOutputFile (String inpath, String fileext) throws IOException
    {
    	
		File saveFile = new File(inpath + "-stream." + fileext);
		saveFile.createNewFile();
		return saveFile;
    }
}
