package info.guardianproject.mrapp.ui;

import info.guardianproject.mrapp.MediaAppConstants;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
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

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;


public class OverlayCamera extends Activity implements Callback, SwipeInterface {
    private Camera camera;
    private SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    private ImageView mOverlayView;
    private Canvas canvas;
    private Bitmap bitmap;
    
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
        

        
        ActivitySwipeDetector swipe = new ActivitySwipeDetector(this);
        mOverlayView.setOnTouchListener(swipe);
      
        mOverlayView.setOnClickListener(new OnClickListener ()
        {

			@Override
			public void onClick(View v) {
				closeOverlay();
			}
        	
        });
        
        mSurfaceView = new SurfaceView(this);
         addContentView(mSurfaceView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
       mSurfaceHolder = mSurfaceView.getHolder();
       mSurfaceHolder.addCallback(this);
       mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
     //  mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT|LayoutParams.FLAG_BLUR_BEHIND);
       addContentView(mOverlayView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));

    //   bitmap = Bitmap.createBitmap(mOverlayView.getWidth(),mOverlayView.getHeight(), Bitmap.Config.ARGB_8888);
     //  canvas = new Canvas(bitmap);
       

      // setOverlayImage (overlayIdx);

       
       }

    private void closeOverlay ()
    {
    	
    	camera.stopPreview();
        camera.release();
    	setResult(RESULT_OK);
		finish();
    }
    
    private void setOverlayImage (int idx)
    {
        try 
        {
        	
        	if (overlays == null)
        		overlays = getAssets().list("images/overlays/svg");
        	
        	bitmap = Bitmap.createBitmap(mOverlayView.getWidth(),mOverlayView.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
          //  SVG svg = SVGParser.getSVGFromAsset(getAssets(), "images/overlays/svg/" + overlays[idx],0xFF000000,0xFFFFFFFF);
            SVG svg = SVGParser.getSVGFromAsset(getAssets(), "images/overlays/svg/" + overlays[idx]);
            
            float sx = svg.getLimits().width() / ((float)mOverlayView.getWidth());
            float sy = svg.getLimits().height() / ((float)mOverlayView.getHeight());
            
            canvas.scale(1/sx, 1/sy);
            
            PictureDrawable d = svg.createPictureDrawable();
            d.setBounds(new Rect(0,0,mOverlayView.getWidth(),mOverlayView.getHeight()));
            d.draw(canvas);
            
            mOverlayView.setImageBitmap(bitmap);
        }
        catch(IOException ex) 
        {
        	Log.e(MediaAppConstants.TAG,"error rendering overlay",ex);
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
        bitmap = Bitmap.createBitmap(arg2, arg3, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        setOverlayImage (overlayIdx);
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


	@Override
	public void bottom2top(View v) {
		// TODO Auto-generated method stub
		
		
	}


	@Override
	public void left2right(View v) {
		// TODO Auto-generated method stub
		overlayIdx--;
		if (overlayIdx < 0)
			overlayIdx = overlays.length-1;
		
		setOverlayImage(overlayIdx);
	}


	@Override
	public void right2left(View v) {
		
		overlayIdx++;
		if (overlayIdx == overlays.length)
			overlayIdx = 0;
		
		setOverlayImage(overlayIdx);
		
	}


	@Override
	public void top2bottom(View v) {
		// TODO Auto-generated method stub
		
	}
  }