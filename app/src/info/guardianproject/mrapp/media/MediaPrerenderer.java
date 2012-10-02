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

public class MediaPrerenderer implements Runnable
{

	private Handler mHandler;
	private MediaClip  mMediaClip;
	private Context mContext;
	private File mFileExternDir;
	
    private int current, total;

	public MediaPrerenderer (Context context, Handler handler, MediaClip mediaClip, File fileExternDir)
	{
		mHandler = handler;
		mMediaClip = mediaClip;
		mContext = context;
		mFileExternDir = fileExternDir;
	}
	
	public void run ()
	{
    	try
    	{
    		 Message msg = mHandler.obtainMessage(0);
	            msg.getData().putString("status","Importing media in the background");

		        mHandler.sendMessage(msg);
		        
    		if (mMediaClip.mMediaDescOriginal.mimeType.startsWith("image"))
    			mMediaClip.mMediaDescRendered = prerenderImage(mMediaClip.mMediaDescOriginal);
	    	else if (mMediaClip.mMediaDescOriginal.mimeType.startsWith("video"))
	    		mMediaClip.mMediaDescRendered = prerenderVideo(mMediaClip.mMediaDescOriginal, false);
	    	else //wave? audio?	    	
	    		mMediaClip.mMediaDescRendered = prerenderVideo(mMediaClip.mMediaDescOriginal, true);
	    	
    		
    		File fileMediaOut = new File(mMediaClip.mMediaDescRendered.path);
    		
	         if (!fileMediaOut.exists() || (fileMediaOut.length() == 0))
	         {
	        	msg = mHandler.obtainMessage(0);
		        msg.getData().putString("status","Error occured with media pre-render");
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
	

    private MediaDesc prerenderVideo (MediaDesc mediaIn, boolean preconvertMP4) throws Exception
    {
    	FfmpegController ffmpegc = new FfmpegController (mContext);
    	
		File fileOutPath = createOutputFile("mp4"); 

    	MediaDesc mediaOut = ffmpegc.convertToMP4Stream(mediaIn, fileOutPath.getAbsolutePath(), preconvertMP4, new ShellCallback() {

			@Override
			public void shellOut(String line) {
				
				if (!line.startsWith("frame"))
					Log.d(AppConstants.TAG, line);
				
				
				int idx1;
				String newStatus = null;
				int progress = 0;
				
				
				if ((idx1 = line.indexOf("Duration:"))!=-1)
				{
					int idx2 = line.indexOf(",", idx1);
					String time = line.substring(idx1+10,idx2);
					
					int hour = Integer.parseInt(time.substring(0,2));
					int min = Integer.parseInt(time.substring(3,5));
					int sec = Integer.parseInt(time.substring(6,8));
					
					total = (hour * 60 * 60) + (min * 60) + sec;
					
					newStatus = line;
					progress = 0;
				}
				else if ((idx1 = line.indexOf("time="))!=-1)
				{
					int idx2 = line.indexOf(" ", idx1);
					String time = line.substring(idx1+5,idx2);
					newStatus = line;
					
					int hour = Integer.parseInt(time.substring(0,2));
					int min = Integer.parseInt(time.substring(3,5));
					int sec = Integer.parseInt(time.substring(6,8));
					
					current = (hour * 60 * 60) + (min * 60) + sec;
					
					progress = (int)( ((float)current) / ((float)total) *100f );
				}
				
				if (newStatus != null)
				{
				 Message msg = mHandler.obtainMessage(1);
		         msg.getData().putInt("progress", progress);
		         msg.getData().putString("status", newStatus);		         
		         mHandler.sendMessage(msg);
				}
			}
    	});
    
    	return mediaOut;
    
   }

    
    private MediaDesc prerenderImage (MediaDesc mediaIn) throws Exception
    {
    	
    	FfmpegController ffmpegc = new FfmpegController (mContext);
    	
    	mediaIn.videoFps = "29.97";
    	mediaIn.width = 1280;
    	mediaIn.height = 720;
    	
    	int durationSecs = 5;
    	
    	File outPath = createOutputFile("mp4");
    	
    	MediaDesc mediaOut = ffmpegc.convertImageToMP4(mediaIn, durationSecs, outPath.getAbsolutePath(), new ShellCallback() {

			@Override
			public void shellOut(String line) {
				
				
				if (!line.startsWith("frame"))
					Log.d(AppConstants.TAG, line);
				
				
				int idx1;
				String newStatus = null;
				int progress = 0;
				
				if ((idx1 = line.indexOf("Duration:"))!=-1)
				{
					int idx2 = line.indexOf(",", idx1);
					String time = line.substring(idx1+10,idx2);
					
					int hour = Integer.parseInt(time.substring(0,2));
					int min = Integer.parseInt(time.substring(3,5));
					int sec = Integer.parseInt(time.substring(6,8));
					
					total = (hour * 60 * 60) + (min * 60) + sec;
					
					newStatus = line;
					progress = 0;
				}
				else if ((idx1 = line.indexOf("time="))!=-1)
				{
					int idx2 = line.indexOf(" ", idx1);
					String time = line.substring(idx1+5,idx2);
					newStatus = line;
					
					int hour = Integer.parseInt(time.substring(0,2));
					int min = Integer.parseInt(time.substring(3,5));
					int sec = Integer.parseInt(time.substring(6,8));
					
					current = (hour * 60 * 60) + (min * 60) + sec;
					
					progress = (int)( ((float)current) / ((float)total) *100f );
				}
				
				if (newStatus != null)
				{
				 Message msg = mHandler.obtainMessage(1);
		         msg.getData().putInt("progress", progress);
		         msg.getData().putString("status", newStatus);		         
		         mHandler.sendMessage(msg);
				}
			}
    	});
    
    	return prerenderVideo(mediaOut, false);
    
   }


    private File createOutputFile (String fileext) throws IOException
    {
		File saveFile = File.createTempFile("output", '.' + fileext, mFileExternDir);	
		return saveFile;
    }
}
