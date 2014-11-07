package org.storymaker.app.media;

import java.io.IOException;
import android.app.Activity;
import android.content.pm.ActivityInfo;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class VideoCamera extends Activity implements OnClickListener, SurfaceHolder.Callback{

    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording=false;
    public static final String TAG = "VIDEOCAPTURE";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
         WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String videoPath = "";
        
        recorder = new MediaRecorder();// Instantiate our media recording object
        initRecorder(videoPath);
        //setContentView(R.layout.view);

        SurfaceView cameraView = null;// (SurfaceView) findViewById(R.id.surface_view);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraView.setClickable(true);// make the surface view clickable
        cameraView.setOnClickListener((OnClickListener) this);// onClicklistener to be called when the surface view is clicked
    }


    private void initRecorder (String path)
    {
    	initRecorder(-1, path, CamcorderProfile.QUALITY_HIGH);
    }
    
    private void initRecorder(int maxDuration, String outputFile, int camProfile) {
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(camProfile);
        recorder.setProfile(cpHigh);
        recorder.setOutputFile(outputFile);
        
        if (maxDuration != -1)
        recorder.setMaxDuration(maxDuration);
    }

    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }

    public void onClick(View v) {
        if (recording) {
            recorder.stop();
            recording = false;

        } else {

            recorder.start();
            Log.v(TAG,"Recording Started"); 
            recording = true;

        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
     
        Log.v(TAG,"surfaceCreated");
        prepareRecorder();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        finish();

    }
}
