package info.guardianproject.mrapp.media;

import info.guardianproject.mrapp.AppConstants;
import info.guardianproject.mrapp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

//http://stackoverflow.com/questions/5734332/vu-audio-meter-when-recording-audio-in-android

public class AudioRecorderView {
	
	private static final int RECORDER_BPP = 16;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	
	private AudioRecord recorder = null;
	private AudioTrack livePlayer = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	
	private File mFilePath;
	
	private MediaPlayer mPlayer = null;
	
	private boolean isRecording = false;
	private Context mContext;
	
    public AudioRecorderView (File path, Context context)
    {
     
    	mContext = context;
    	
    	mFilePath = path;
    	
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING);
        
    }
    
	public File getTempFile(){
		
		File tempFile = new File(mContext.getExternalFilesDir(null),AUDIO_RECORDER_TEMP_FILE);
		
		if(tempFile.exists())
			tempFile.delete();
		
		return tempFile;
	}
	
	public void startRecording(){
		
		
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
						RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
		
		livePlayer =  new AudioTrack( AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE, 
				RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, 
				bufferSize, AudioTrack.MODE_STREAM);
		
		
		recorder.startRecording();		
		
		isRecording = true;
		
		
		recordingThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				writeAudioDataToFile();
			}
		},"AudioRecorder Thread");
		
		recordingThread.start();
	}
	
	private void writeAudioDataToFile(){
		byte data[] = new byte[bufferSize];
		FileOutputStream os = null;
		
		try {
			os = new FileOutputStream(getTempFile());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int read = 0;
		
		if(null != os){
			while(isRecording){
				read = recorder.read(data, 0, bufferSize);
					
				if(AudioRecord.ERROR_INVALID_OPERATION != read){
					
					try {
						livePlayer.write(data, 0, bufferSize);
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}

					try {
						os.write(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}
			
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopRecording(){
		if(null != recorder){
			isRecording = false;
			
			recorder.stop();
			recorder.release();
			
			livePlayer.stop();
			livePlayer.release();
			
			recorder = null;
			recordingThread = null;
		}
		
		copyWaveFile(getTempFile(),mFilePath);
		getTempFile().delete();
	}
	
	public void startPlaying() {
        mPlayer = new MediaPlayer();
        
        if (mFilePath != null && mFilePath.exists())
        {
	        try {
	        	
	            mPlayer.setDataSource(mFilePath.getAbsolutePath());
	            mPlayer.prepare();
	            mPlayer.start();
	        } catch (IOException e) {
	            Log.e(AppConstants.TAG, "prepare() failed",e);
	        }
        }
    }
	
	

	public void stopPlaying() {
		if (mPlayer != null)
		{
			mPlayer.release();
			mPlayer = null;
		}
	}

	

	
	private void copyWaveFile(File inFilename,File outFilename){
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = RECORDER_SAMPLERATE;
		int channels = 2;
		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;
		
		byte[] data = new byte[bufferSize];
                
		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			
			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			
			while(in.read(data) != -1){
				out.write(data);
			}
			
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void WriteWaveFileHeader(
			FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate) throws IOException {
		
		byte[] header = new byte[44];
		
		header[0] = 'R';  // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f';  // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;  // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8);  // block align
		header[33] = 0;
		header[34] = RECORDER_BPP;  // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}
	
	public boolean isPlaying ()
	{
		if (mPlayer != null)
			return mPlayer.isPlaying();
		else
			return false;
	}
	
	public boolean isRecording ()
	{
		return isRecording;
	}
	
}