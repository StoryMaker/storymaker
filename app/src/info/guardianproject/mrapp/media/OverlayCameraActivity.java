package info.guardianproject.mrapp.media;

import info.guardianproject.mrapp.AppConstants;
import info.guardianproject.mrapp.BaseActivity;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.ui.ActivitySwipeDetector;
import info.guardianproject.mrapp.ui.SwipeInterface;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Picture;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.PictureDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.actionbarsherlock.app.SherlockActivity;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;


public class OverlayCameraActivity extends SherlockActivity implements Callback, SwipeInterface 
{
   

	private Camera camera;
    private SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    private ImageView mOverlayView;
    private Canvas canvas;
    private Bitmap bitmap;
    
    String[] overlays = null;
    int overlayIdx = 0;
    int overlayGroup = 0;
    
    boolean cameraOn = false;
    
    private int mColorRed = 0;
    private int mColorGreen = 0;
    private int mColorBlue = 0;
    
    private int mStoryMode = -1;
    
    private Handler mMediaHandler = new Handler ()
    {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			// FIXME handle response from media capture... send result back to story editor
		}
    	
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	

    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        
    
        overlayGroup = getIntent().getIntExtra("group", 0);
        overlayIdx = getIntent().getIntExtra("overlay", 0);
        mStoryMode = getIntent().getIntExtra("mode",-1);

        
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
       addContentView(mOverlayView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));

       }
    

    private void closeOverlay ()
    {
    	
    	if (cameraOn)
    	{
    		cameraOn = false;    
    		
    		if (camera != null)
    		{
    			camera.stopPreview();
    			camera.release();
    		}
    	}
	    
    	setResult(RESULT_OK);
	    	
    	
    	
		finish();
    }
    
    @Override
	protected void onDestroy() {
    	
		super.onDestroy();
		
		closeOverlay();
	}


	private void setOverlayImage (int idx)
    {
        try 
        {
        	String groupPath = "images/overlays/svg/" + overlayGroup;
        	
        	if (overlays == null)
        		overlays = getAssets().list(groupPath);
        	
        	bitmap = Bitmap.createBitmap(mOverlayView.getWidth(),mOverlayView.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            
            String imgPath = groupPath + '/' + overlays[idx];
        //    SVG svg = SVGParser.getSVGFromAsset(getAssets(), "images/overlays/svg/" + overlays[idx],0xFFFFFF,0xCC0000);
            
            SVG svg = SVGParser.getSVGFromAsset(getAssets(), imgPath);

            Rect rBounds = new Rect(0,0,mOverlayView.getWidth(),mOverlayView.getHeight());
            Picture p = svg.getPicture();                       
            canvas.drawPicture(p, rBounds);            
            
            mOverlayView.setImageBitmap( bitmap);
        }
        catch(IOException ex) 
        {
        	Log.e(AppConstants.TAG,"error rendering overlay",ex);
            return;
        }
        
    }
    
    /*
    private Bitmap changeColor(Bitmap src,int pixelRed, int pixelGreen, int pixelBlue){
    	
    	int width = src.getWidth();
    	int height = src.getHeight();
    	
        Bitmap dest = Bitmap.createBitmap(
          width, height, src.getConfig());
             
        for(int x = 0; x < width; x++){
         for(int y = 0; y < height; y++){
          int pixelColor = src.getPixel(x, y);
          int pixelAlpha = Color.alpha(pixelColor);
           
	          int newPixel = Color.argb(
	            pixelAlpha, pixelRed, pixelGreen, pixelBlue);
	           
	          dest.setPixel(x, y, newPixel);          
         } 
        }
        return dest; 
       }
    */
    
    private void takePicture() {
        // TODO Auto-generated method stub
      //  camera.takePicture(shutter, raw, jpeg);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    
    	if (camera != null)
    	{
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
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        
    	camera = Camera.open();
    	cameraOn = true;
    	
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        
    	closeOverlay();
    }


	@Override
	public void bottom2top(View v) {
		mColorRed = 255;
		mColorGreen = 255;
		mColorBlue = 255;
		setOverlayImage(overlayIdx);
		
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
		mColorRed = 0;
		mColorGreen = 0;
		mColorBlue = 0;
		
		setOverlayImage(overlayIdx);
	}
	

    
    ShutterCallback shutter = new ShutterCallback(){

        @Override
        public void onShutter() {
            //no action for the shutter

        }

       };
       
    PictureCallback raw = new PictureCallback(){
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
           //we aren't taking a picture here
      }

       };

    PictureCallback jpeg = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	   //we aren't taking a picture here

        }

       };
  }


/*
float sx = svg.getLimits().width() / ((float)mOverlayView.getWidth());
float sy = svg.getLimits().height() / ((float)mOverlayView.getHeight());
canvas.scale(1/sx, 1/sy);                      
PictureDrawable d = svg.createPictureDrawable();              
d.setBounds(new Rect(0,0,mOverlayView.getWidth(),mOverlayView.getHeight()));

//d.setColorFilter(0xffff0000, Mode.MULTIPLY);
int iColor = Color.parseColor("#FFFFFF");

int red = (iColor & 0xFF0000) / 0xFFFF;
int green = (iColor & 0xFF00) / 0xFF;
int blue = iColor & 0xFF;

float[] matrix = { 0, 0, 0, 0, red
                 , 0, 0, 0, 0, green
                 , 0, 0, 0, 0, blue
                 , 0, 0, 0, 1, 0 };

ColorFilter colorFilter = new ColorMatrixColorFilter(matrix);            
d.setColorFilter(colorFilter);            
d.draw(canvas);
*/