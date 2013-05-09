package info.guardianproject.mrapp.media.exporter;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import net.sourceforge.sox.SoxController;

import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import info.guardianproject.mrapp.AppConstants;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

/*
 * cross fades audio and other niceities
 */
public class MediaVideoExporter extends MediaExporter {

	private Handler mHandler;
	private Context mContext;
	
	private ArrayList<MediaDesc> mMediaList;
	private MediaDesc mOut;
	
    private int current, total;

    private MediaAudioExporter maExport;
    private MediaDesc maOut;
    private FfmpegController ffmpegc;
    
    private ArrayList<MediaDesc> mAudioTracks;
    
    private int mAudioSampleRate = -1;
    
    private float mFadeLen = .5f;
    
    private File mFileProject;
    
	
	boolean mPreconvertClipsToMP4 = false;
	boolean mUseCatCmd = false;
	
	public MediaVideoExporter (Context context, Handler handler, ArrayList<MediaDesc> mediaList, File fileProject, MediaDesc out)
	{
		mHandler = handler;
		mContext = context;
		mOut = out;
		mMediaList = mediaList;
		mFileProject = fileProject;
		mAudioTracks = new ArrayList<MediaDesc>();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        mAudioSampleRate = Integer.parseInt(settings.getString("p_audio_samplerate", AppConstants.DEFAULT_AUDIO_SAMPLE_RATE));
    	
        mFadeLen = Float.parseFloat(settings.getString("p_audio_xfade_len",".5f"));
        
        mPreconvertClipsToMP4 = settings.getBoolean("p_preconvert_mp4", false);
     
        mUseCatCmd = settings.getBoolean("p_use_cat", false);
        
	}
	
	public void addAudioTrack (MediaDesc audioTrack)
	{
		mAudioTracks.add(audioTrack);
	}
	
	public void export ()
	{
    	try
    	{

    		//get lengths of all clips
    		
    		
    		ffmpegc = new FfmpegController (mContext, mFileProject);
    		
    		//first let's get the audio done
    		maOut = new MediaDesc();
    		maOut.path = new File(mFileProject, "tmp.wav").getCanonicalPath();
    		
    		Message msg = mHandler.obtainMessage(0);
            msg.getData().putString("status","Processing audio tracks...");
	        mHandler.sendMessage(msg);
    		
	        //export video clips and crossfade
    		maExport = new MediaAudioExporter (mContext, mHandler, mMediaList, mFileProject, maOut);
    		maExport.setFadeLength(mFadeLen);
    		maExport.export();
    		
    		
    		//now merge all audio tracks into main audio track
    		if (mAudioTracks.size() > 0)
    		{
	    		ArrayList<String> mAudioTracksPaths = new ArrayList<String>();
	    		int idxAudioTracks = 0;
	    		for (MediaDesc audioTrack : mAudioTracks)
	    		{

	        		msg = mHandler.obtainMessage(0);
	                msg.getData().putString("status","Processing audio track " + (idxAudioTracks+1) + "/" + mAudioTracks.size());
	    	        mHandler.sendMessage(msg);
	    	        
	    			File fileAudioTrack = new File(mFileProject,idxAudioTracks + "-tmp.wav");
	    			MediaDesc out = ffmpegc.convertToWaveAudio(audioTrack, fileAudioTrack.getCanonicalPath(), mAudioSampleRate, MediaAudioExporter.CHANNELS, sc);
	    			mAudioTracksPaths.add(out.path);
	    			idxAudioTracks++;
	    			

	    		}
	    		
	    		mAudioTracksPaths.add(maOut.path);
	    		
	    		String finalAudioMix = maOut.path + "-mix.wav";
	
	    		msg = mHandler.obtainMessage(0);
                msg.getData().putString("status","Mixing tracks");
    	        mHandler.sendMessage(msg);
    	        
	    		SoxController sxCon = new SoxController(mContext,sc);
	    		sxCon.combineMix(mAudioTracksPaths, finalAudioMix);
	    		
	    		if (!new File(finalAudioMix).exists())
	    		{
	    			throw new Exception("Audio rendering error");
	    		}
	    		
	    		maOut.path = finalAudioMix;
    		}
    		
    		MediaDesc mMerge = new MediaDesc();
    		mMerge.path = new File(mFileProject,"merge.mp4").getCanonicalPath();
    	   
    		
    		for (int i = 0; i < mMediaList.size(); i++)
    		{
    			
    			MediaDesc media = mMediaList.get(i);
    			
    			Log.d(AppConstants.TAG,"parsing times for clip: " + media.startTime + " to " + media.duration);
    			
    			/*
    			if (media.startTime != null)
    			{
    				double startTime = parseTimePeriod(media.startTime);
    				media.startTime = formatTimePeriod(startTime + (mFadeLen));
    			}
    			
    			if (media.duration != null)
    			{
    				double duration = parseTimePeriod(media.duration);
    				media.duration = formatTimePeriod(duration-(mFadeLen));
    			}*/
    			
    		}
    		
    		msg = mHandler.obtainMessage(0);
            msg.getData().putString("status","Trimming and merging videos");
	        mHandler.sendMessage(msg);
	        
        	ffmpegc.concatAndTrimFilesMP4Stream(mMediaList, mMerge, mPreconvertClipsToMP4, mUseCatCmd, sc);
        	
        	msg = mHandler.obtainMessage(0);
            msg.getData().putString("status","Merging video and audio...");
	        mHandler.sendMessage(msg);
	       
    		ffmpegc.combineAudioAndVideo(mMerge, maOut, mOut, sc);

    		//processing complete message
    		 msg = mHandler.obtainMessage(0);
	         mHandler.sendMessage(msg);

	         //now scan for media to add to gallery
    		File fileTest = new File(mOut.path);
	         if (fileTest.exists() && fileTest.length() > 0)
	         {
	    		MediaScannerConnection.scanFile(
	     				mContext,
	     				new String[] {mOut.path},
	     				new String[] {mOut.mimeType},
	     				new OnScanCompletedListener ()
	     				{

							@Override
							public void onScanCompleted(String path, Uri uri) {

					    		Message msg = mHandler.obtainMessage(4);
					            msg.getData().putString("path",mOut.path);
					            
					            mHandler.sendMessage(msg);
								
							}
	     					
	     				});
	    
	         }
	         else
	         {
	        		msg = mHandler.obtainMessage(0);
		            msg.getData().putString("error","Something went wrong with media export");
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
				else if (line.startsWith("Input"))
				{
				    //12-18 02:48:07.187: D/StoryMaker(10508): Input #0, mov,mp4,m4a,3gp,3g2,mj2, from '/storage/sdcard0/DCIM/Camera/VID_20121211_140815.mp4':
				   // idx1 = line.indexOf("'");
				   // int idx2 = line.indexOf('\'', idx1+1);
				   // newStatus = "Rendering clip: " + line.substring(idx1, idx2);
				}
				else if (line.startsWith("status:"))
				{
					newStatus = line.substring(7);
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
			
				
				
			}
 	};
	    

	/**
	 * Takes a seconds.frac value and formats it into:
	 * 	hh:mm:ss:ss.frac
	 * @param seconds
	 */
 	public String formatTimePeriod(double seconds) {
 		
 		/*
		DecimalFormat df = new DecimalFormat("#.##");
	//	DecimalFormat df = new DecimalFormat("#");
		
		df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		 String seconds_frac = df.format(seconds);
		return String.format(Locale.US, "%s", seconds_frac);
		*/
 		
		long milliTime = (long)(seconds * 100f);
		Date dateTime = new Date(milliTime);
		return String.format(Locale.US, "%s:%s:%s", dateTime.getHours(),dateTime.getMinutes(),dateTime.getSeconds());
	}
	
	/**
	 * Takes a seconds.frac value and formats it into:
	 * 	hh:mm:ss:ss.frac
	 * @param seconds
	 */
	public Double parseTimePeriod(String seconds) throws ParseException {
		DecimalFormat format = new DecimalFormat("#.##");
		return format.parse(seconds).doubleValue();
	}
	
}
