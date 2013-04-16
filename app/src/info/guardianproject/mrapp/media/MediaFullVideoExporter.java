package info.guardianproject.mrapp.media;

import java.io.File;
import java.util.ArrayList;

import net.sourceforge.sox.SoxController;

import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import info.guardianproject.mrapp.AppConstants;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/*
 * cross fades audio and other niceities
 */
public class MediaFullVideoExporter implements Runnable {

	private Handler mHandler;
	private Context mContext;
	
	private ArrayList<MediaDesc> mMediaList;
	private MediaDesc mOut;
	
    private int current, total;

    private MediaAudioExporter maExport;
    private MediaDesc maOut;
    private FfmpegController ffmpegc;
    
    private ArrayList<MediaDesc> mAudioTracks;
    
	public MediaFullVideoExporter (Context context, Handler handler, ArrayList<MediaDesc> mediaList, MediaDesc out)
	{
		mHandler = handler;
		mContext = context;
		mOut = out;
		mMediaList = mediaList;
		
		mAudioTracks = new ArrayList<MediaDesc>();
    	
	}
	
	public void addAudioTrack (MediaDesc audioTrack)
	{
		mAudioTracks.add(audioTrack);
	}
	
	public void run ()
	{
    	try
    	{

    		//get lengths of all clips
    		
    		
    		ffmpegc = new FfmpegController (mContext);
    		
    		//first let's get the audio done
    		maOut = new MediaDesc();
    		maOut.path = mOut.path + ".wav";
    		
    		//maOut.audioCodec = "aac";
    		//maOut.audioBitrate = 256;
    		
    		maExport = new MediaAudioExporter (mContext, mHandler, mMediaList, maOut);
    		maExport.run();
    		
    		ArrayList<String> mAudioTracksPaths = new ArrayList<String>();
    		//now merge all audio tracks into main audio track
    		for (MediaDesc audioTrack : mAudioTracks)
    		{
    			ffmpegc.convertToWaveAudio(audioTrack, audioTrack.path+"-tmp.wav", MediaAudioExporter.SAMPLE_RATE, MediaAudioExporter.CHANNELS, sc);
    			mAudioTracksPaths.add(audioTrack.path+"-tmp.wav");
    			
    		}
    		mAudioTracksPaths.add(maOut.path);
    		
    		String finalAudioMix = maOut.path + "-mix.wav";

    		SoxController sxCon = new SoxController(mContext);
    		sxCon.combineMix(mAudioTracksPaths, finalAudioMix);
    		
    		if (!new File(finalAudioMix).exists())
    		{
    			throw new Exception("Audio rendering error");
    		}
    		
    		maOut.path = finalAudioMix;
    		
    		//now merge audio and video
    		String finalPath = mOut.path;
    		String finalAudioCodec = mOut.audioCodec;
    		
    		mOut.path = mOut.path + "-tmp.mp4";
    		mOut.audioCodec = null;
    		
    		String outputType = mOut.mimeType;
    		
    		/*
    		for (MediaDesc mdesc : mMediaList)
    		{
    			if (mdesc.startTime != null)
    				mdesc.startTime = "00:00:00.700"; //trim one second off the beginning of each clip
    		}*/
    		
        	ffmpegc.concatAndTrimFilesMPEG(mMediaList, mOut, true, sc);
        	
        	maOut.audioCodec = "aac";
        	maOut.audioBitrate = 128;
        	
    		ffmpegc.combineAudioAndVideo(mOut, maOut, finalPath, sc);

    		mOut.path = finalPath; //reset to initial
    		mOut.audioCodec = finalAudioCodec;
    		
    		//processing complete message
    		Message msg = mHandler.obtainMessage(0);
	         mHandler.sendMessage(msg);
	         

	         //now scan for media to add to gallery
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
	
	    
	 
	 private final ShellCallback sc = new ShellCallback() {

			@Override
			public void shellOut(String line) {
				
				
				if (!line.startsWith("frame"))
					Log.d(AppConstants.TAG, line);
				
				
				int idx1;
				String newStatus = null;
				int progress = -1;
				
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
				    

             Message msg = mHandler.obtainMessage(1);
             
				if (newStatus != null)
				    msg.getData().putString("status", newStatus);                 
             
				if (progress != -1)
				    msg.getData().putInt("progress", progress);
		       
				
			    mHandler.sendMessage(msg);
             
			}

			@Override
			public void processComplete(int exitValue) {
			
				 Message msg = mHandler.obtainMessage(1);
                 
				    msg.getData().putString("status", "file processing complete");                 
          
				    msg.getData().putInt("progress", 100);
		       
					
				    mHandler.sendMessage(msg);
				
			}
 	};
	    
}
