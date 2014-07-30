package org.codeforafrica.timby.listeningpost.spy;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;

public class SoundRecorderService extends Service{
	
	private MediaRecorder recorder;
	@Override
	public IBinder onBind(Intent arg0) {
	    // TODO Auto-generated method stub
	    return null;
	}



	@Override
	public void onStart(Intent intent, int startId) {
	    startRecording();       
	    super.onStart(intent, startId);
	}



	@Override
	public void onDestroy() {
	    stopRecording();
	    stopSelf();
	    super.onDestroy();
	}

	private void startRecording(){

	    try {
	    recorder = new MediaRecorder();

	    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
	    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);


	    Date today = Calendar.getInstance().getTime();    
	    Format formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
	    String reportDate = formatter.format(today);


	    File instanceRecordDirectory = new File(Environment.getExternalStorageDirectory() + File.separator + "sound");

	    if(!instanceRecordDirectory.exists()){
	        instanceRecordDirectory.mkdirs();
	    }

	    File instanceRecord = new File(instanceRecordDirectory.getAbsolutePath() + File.separator + reportDate + "_Recondsound.mp4");
	    if(!instanceRecord.exists()){
	        instanceRecord.createNewFile();
	    }
	    recorder.setOutputFile(instanceRecord.getAbsolutePath());



	        recorder.prepare();
	        recorder.start();
	    } catch (IllegalStateException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }catch (Exception e){
	          e.printStackTrace();
	    }
	}

	private void stopRecording() {
	    if (recorder != null) {       
	        recorder.stop();
	        recorder.release();
	        recorder = null;
	    }
	}




}
