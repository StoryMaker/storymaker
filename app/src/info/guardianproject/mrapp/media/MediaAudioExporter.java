package info.guardianproject.mrapp.media;

import java.io.File;
import java.util.ArrayList;

import net.sourceforge.sox.CrossfadeCat;
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

public class MediaAudioExporter implements Runnable {

	private Handler mHandler;
	private Context mContext;
	
	private ArrayList<MediaDesc> mMediaList;
	private MediaDesc mOut;
	
    private int current, total;
    
    private ArrayList<Double> mDurations;
    
	 double fadeLen = 1.0f;
	 String fadeType = "t"; //triangle/linear
	 
	 public final static String SAMPLE_RATE = "44100";
	 public final static int CHANNELS = 1;
/*
 *  fade [type] fade-in-length [stop-time [fade-out-length]]
              Apply a fade effect to the beginning, end, or both of the audio.

              An  optional  type  can  be specified to select the shape of the fade curve: q for quarter of a sine wave, h for half a sine wave, t for linear (`triangular')
              slope, l for logarithmic, and p for inverted parabola.  The default is logarithmic.

              A fade-in starts from the first sample and ramps the signal level from 0 to full volume over fade-in-length seconds.  Specify  0  seconds  if  no  fade-in  is
              wanted.

              For  fade-outs,  the  audio  will be truncated at stop-time and the signal level will be ramped from full volume down to 0 starting at fade-out-length seconds
              before the stop-time.  If fade-out-length is not specified, it defaults to the same value as fade-in-length.  No fade-out is performed  if  stop-time  is  not
              specified.   If  the file length can be determined from the input file header and length-changing effects are not in effect, then 0 may be specified for stop-
              time to indicate the usual case of a fade-out that ends at the end of the input audio stream.

              All times can be specified in either periods of time or sample counts.  To specify time periods use the format hh:mm:ss.frac format.  To specify using  sample
              counts, specify the number of samples and append the letter `s' to the sample count (for example `8000s').

              See also the splice effect.

 */
	 
	public MediaAudioExporter (Context context, Handler handler, ArrayList<MediaDesc> mediaList, MediaDesc out)
	{
		mHandler = handler;
		mContext = context;
		mOut = out;
		mMediaList = mediaList;
	}
	
	public void setFadeLength (double fadeLen)
	{
		this.fadeLen = fadeLen;
	}
	
	public void setFadeType (String fadeType)
	{
		this.fadeType = fadeType;
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
	
	public ArrayList<Double> getDurations ()
	{
		return mDurations;
	}
	
	
	 private void concatMediaFiles (ArrayList<MediaDesc> listMediaDesc, MediaDesc mdout) throws Exception
	    {

		 //now add 1 second cross fade to each audio file and cat them together
		 SoxController sxCon = new SoxController(mContext);
		
		 int exportBitRate = mdout.audioBitrate;
		 String exportCodec = mdout.audioCodec;

		 String fadeLenStr = sxCon.formatTimePeriod(fadeLen);
		 
		 FfmpegController ffmpegc = new FfmpegController (mContext);
	    
		 ArrayList<MediaDesc> alAudio = new ArrayList<MediaDesc>();
		 
		 
		 //convert each input file to a WAV so we can use Sox to process
		 for (MediaDesc mediaIn : listMediaDesc)
		 {
		 
	    	MediaDesc audioOut = ffmpegc.convertToWaveAudio(mediaIn, mediaIn.path + ".wav",SAMPLE_RATE,CHANNELS, sc);
	    	alAudio.add(audioOut);
		 }
		
		 mDurations = new ArrayList<Double>();
		 
		 String fileOut = alAudio.get(0).path;

		 mDurations.add(new Double(sxCon.getLength(fileOut)));
		 
		 for (int i = 1; i < alAudio.size(); i++)
		 {		
			 String fileAdd = alAudio.get(i).path;
			 
			 mDurations.add(new Double(sxCon.getLength(fileAdd)));
			 
			 CrossfadeCat xCat = new CrossfadeCat(sxCon, fileOut, fileAdd, fadeLen, fileOut);
			 xCat.start();
		 
		 }
		 
		 //1 second fade in and fade out, t = triangle or linear
		 String fadeFileOut = sxCon.fadeAudio(fileOut, fadeType, fadeLenStr, "0", fadeLenStr);
		 
		 //now export the final file to our requested output format
		 MediaDesc mdFinalIn = new MediaDesc();
		 mdFinalIn.path = fadeFileOut;
		 
		 mdout.audioBitrate = exportBitRate;
		 mdout.audioCodec = exportCodec;
		 
		 MediaDesc exportOut = ffmpegc.convertTo3GPAudio(mdFinalIn, mdout, sc);
		 
	   }
	    
	 private ShellCallback sc = new ShellCallback() {

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
				    newStatus = "Combining audio clips...";
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
                 
				    msg.getData().putString("status", "audio clip processed...");                 
             
				    msg.getData().putInt("progress", 100);
		       
					
				    mHandler.sendMessage(msg);
				
			}
 	};
	    
}
