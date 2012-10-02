package info.guardianproject.mrapp.ui;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;


public class OverlayCamera extends Activity implements Callback {
    private Camera camera;
    private SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    private View mOverlayView;
    
    String[] overlays = null;
    int overlayIdx = 0;
    
    
    ShutterCallback shutter = new ShutterCallback(){

        @Override
        public void onShutter() {
            // TODO Auto-generated method stub
            // No action to be perfomed on the Shutter callback.

        }

       };
       
    PictureCallback raw = new PictureCallback(){
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            // No action taken on the raw data. Only action taken on jpeg data.
      }

       };

    PictureCallback jpeg = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
           

        }

       };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mOverlayView = new ImageView(this);
        mOverlayView.setOnClickListener(new OnClickListener (){

			@Override
			public void onClick(View v) {
				overlayIdx++;
				if (overlayIdx == overlays.length)
					overlayIdx = 0;
				
				setOverlayImage(overlayIdx);
				
			}
        	
        	
        });
        setOverlayImage (overlayIdx);
    

        mSurfaceView = new SurfaceView(this);
         addContentView(mSurfaceView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
       mSurfaceHolder = mSurfaceView.getHolder();
       mSurfaceHolder.addCallback(this);
       mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
       mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT|LayoutParams.FLAG_BLUR_BEHIND);
       addContentView(mOverlayView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));

       }

    
    private void setOverlayImage (int idx)
    {
        try 
        {
        	if (overlays == null)
        		overlays = getAssets().list("images/overlays");
        	
            // get input stream
            InputStream ims = getAssets().open("images/overlays/" + overlays[idx]);
            
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            ((ImageView)mOverlayView).setImageDrawable(d);
        }
        catch(IOException ex) 
        {
            return;
        }
        
    }
    
    private void takePicture() {
        // TODO Auto-generated method stub
      //  camera.takePicture(shutter, raw, jpeg);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub
        Camera.Parameters p = camera.getParameters();
        p.setPreviewSize(arg2, arg3);
        try {
        camera.setPreviewDisplay(arg0);
        } catch (IOException e) {
        e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
    camera = Camera.open();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera.stopPreview();
        camera.release();
    }
  }