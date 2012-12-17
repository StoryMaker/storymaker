package info.guardianproject.mrapp.media;

import java.io.File;
import java.util.ArrayList;

import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import info.guardianproject.mrapp.AppConstants;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MediaExporter implements Runnable {

	private Handler mHandler;
	private Context mContext;
	
	private ArrayList<MediaDesc> mMediaList;
	private MediaDesc mOut;
	
    private int current, total;

	public MediaExporter (Context context, Handler handler, ArrayList<MediaDesc> mediaList, MediaDesc out)
	{
		mHandler = handler;
		mContext = context;
		mOut = out;
		mMediaList = mediaList;
	}
	
	
	public void run ()
	{
    	try
    	{
    		String outputType = mOut.mimeType;
    		
    		concatMediaFiles(mMediaList, mOut);
    		
    		Message msg = mHandler.obtainMessage(0);
	         mHandler.sendMessage(msg);
    		File fileTest = new File(mOut.path);
	         if (fileTest.exists() && fileTest.length() > 0)
	         {
	    		MediaScannerConnection.scanFile(
	     				mContext,
	     				new String[] {mOut.path},
	     				new String[] {outputType},
	     				null);
	    
	    		msg = mHandler.obtainMessage(4);
	            msg.getData().putString("path",mOut.path);
	            
	            mHandler.sendMessage(msg);
	         }
	         else
	         {
	        		msg = mHandler.obtainMessage(0);
		            msg.getData().putString("status","Something went wrong with media export");

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
	
	 private void concatMediaFiles (ArrayList<MediaDesc> listMediaDesc, MediaDesc mdout) throws Exception
	    {
	    	  	
	    	boolean mediaNeedConvert = false;
	    	
	    	FfmpegController ffmpegc = new FfmpegController (mContext);
	    	
	    	ffmpegc.concatAndTrimFilesMP4Stream(listMediaDesc, mdout, mediaNeedConvert, new ShellCallback() {

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
						
						//newStatus = line;
						
						progress = 0;
					}
					else if ((idx1 = line.indexOf("time="))!=-1)
					{
						int idx2 = line.indexOf(" ", idx1);
						String time = line.substring(idx1+5,idx2);
						//newStatus = line;
						
						int hour = Integer.parseInt(time.substring(0,2));
						int min = Integer.parseInt(time.substring(3,5));
						int sec = Integer.parseInt(time.substring(6,8));
						
						current = (hour * 60 * 60) + (min * 60) + sec;
						
						progress = (int)( ((float)current) / ((float)total) *100f );
					}
					else if (line.startsWith("cat"))
					{
					    newStatus = "Combining clips...";
					}
					else if (line.startsWith("Input"))
					{
					    //12-18 02:48:07.187: D/StoryMaker(10508): Input #0, mov,mp4,m4a,3gp,3g2,mj2, from '/storage/sdcard0/DCIM/Camera/VID_20121211_140815.mp4':
					    idx1 = line.indexOf("'");
					    int idx2 = line.indexOf('\'', idx1+1);
					    newStatus = "Rendering clip: " + line.substring(idx1, idx2);
					}
					    
					
					if (newStatus != null)
					{
					 Message msg = mHandler.obtainMessage(1);
			         msg.getData().putInt("progress", progress);
			         msg.getData().putString("status", newStatus);		         
			         mHandler.sendMessage(msg);
					}
				}

				@Override
				public void processComplete(int exitValue) {
					// TODO Auto-generated method stub
					
				}
	    	});
	    
	    
	   }
	    
	    
}
