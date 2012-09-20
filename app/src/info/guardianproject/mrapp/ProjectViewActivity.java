package info.guardianproject.mrapp;

import info.guardianproject.mrapp.media.MediaConstants;
import info.guardianproject.mrapp.media.MediaHelper;
import info.guardianproject.mrapp.model.Project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.MediaUtils;
import org.ffmpeg.android.ShellUtils.ShellCallback;
import org.ffmpeg.android.filters.DrawBoxVideoFilter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ProjectViewActivity extends SherlockActivity {

	private ViewPager pager;
	private AwesomePagerAdapter adapter;
	
	private ArrayList<MediaDesc> mediaList = new ArrayList<MediaDesc>();
	
	private File fileExternDir;
	
	private File mRenderPath;	
	private File mMediaTmp;
	private MediaDesc mMediaDescTmp;
	
	private MediaHelper mMediaHelper;
	private MediaHelper.MediaResult mMediaResult;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_view);        
        
        adapter = new AwesomePagerAdapter();
        
        pager = (ViewPager) findViewById(R.id.awesomepager);
        pager.setAdapter(adapter);
        
        if (mediaList.size() == 0)
        {
        	addDefaultView ();
        }
        
        Intent intent = getIntent();
        
        initExternalStorage();
        
        mMediaHelper = new MediaHelper (this, mHandler);
        
        if (intent != null)
        {
        	String title = intent.getStringExtra("title");
        	if (title != null)
        		setTitle(title);
        	
        	MediaDesc result = mMediaHelper.handleIntentLaunch(intent);
        	
        	if (result != null && result.path != null && result.mimeType != null)
        	{
        		addMediaFile(result.path, result.mimeType);
        	}
        }
        
        
    }

    private void addDefaultView ()
	{
		TextView view = new TextView(this);
		view.setTextSize(32);
		view.setText(R.string.default_project_view_message);
		view.setTextColor(Color.DKGRAY);
		view.setBackgroundColor(Color.LTGRAY);
		view.setPadding(6,6,6,6);
		view.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		
		view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				showAddMediaDialog();
			}
			
		});
		
		adapter.addProjectView(view);
		adapter.notifyDataSetChanged();		
		pager.setCurrentItem(0, true);
	}
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		
    	super.onConfigurationChanged(newConfig);
		
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.project_view_main, menu);
        return true;
    }
    
    private void initExternalStorage ()
    {
    	String extState = Environment.getExternalStorageState();
    	
    	if (extState.equals(Environment.MEDIA_MOUNTED) || extState.equals(Environment.MEDIA_SHARED))
    	{
			//fileExternDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    		fileExternDir = getExternalFilesDir(null);
    		
    	}
    	else
    	{
    		fileExternDir = Environment.getDataDirectory();
    	}
    }
    
    ProgressDialog progressDialog;
    
    private void doExportMedia ()
    {
    	progressDialog = new ProgressDialog(this);
    	progressDialog.setTitle("Rendering. Please wait...");
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	progressDialog.setMax(100);
        progressDialog.setCancelable(true);
       
    	 Message msg = mHandler.obtainMessage(0);
         msg.getData().putString("status","cancelled");
         progressDialog.setCancelMessage(msg);
    	
         progressDialog.show();
     	
		// Convert to video
		Thread thread = new Thread (runExportVideo);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
    
    }
    
	Runnable runExportVideo = new Runnable () {
		
		public void run ()
		{
	    	try
	    	{
	    		String outputExt = "mp4";//or mpg
	    		String outputType = MediaConstants.MIME_TYPE_MP4;
	    		
	    		mRenderPath = createOutputFile(outputExt); 
			 
	    		concatMediaFiles(mRenderPath.getAbsolutePath());
	    		
	    		Message msg = mHandler.obtainMessage(0);
		         mHandler.sendMessage(msg);
	    		
		         if (mRenderPath.exists() && mRenderPath.length() > 0)
		         {
		    		MediaScannerConnection.scanFile(
		     				ProjectViewActivity.this,
		     				new String[] {mRenderPath.getAbsolutePath()},
		     				new String[] {outputType},
		     				null);
		    
		    		msg = mHandler.obtainMessage(4);
		            msg.getData().putString("path",mRenderPath.getAbsolutePath());
		            
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
	};
	
	 private void doPreconvertMedia (MediaDesc mediaIn)
    {
		mMediaDescTmp = mediaIn;
		
    	progressDialog = new ProgressDialog(this);    	
    	progressDialog.setTitle("Importing Media. Please wait...");
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	progressDialog.setMax(100);
        progressDialog.setCancelable(true);
       
    	 Message msg = mHandler.obtainMessage(0);
         msg.getData().putString("status","cancelled");
         progressDialog.setCancelMessage(msg);
    	
         progressDialog.show();
     	
		// Convert to video
		Thread thread = new Thread (runConvertVideo);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
    
    }
	
	Runnable runConvertVideo = new Runnable () {
		
		public void run ()
		{
	    	try
	    	{
	    		//mMediaDescTmp.startTime = "00:00:01";
	    		//mMediaDescTmp.duration = "00:00:01";
	    		
	    		MediaDesc mediaOut = prerenderMedia(mMediaDescTmp);
	    		File fileMediaOut = new File(mediaOut.path);
	    		
	    		Message msg = mHandler.obtainMessage(0);
		         mHandler.sendMessage(msg);
	    		
		         if (fileMediaOut.exists() && fileMediaOut.length() > 0)
		         {
		        	 /*
		    		MediaScannerConnection.scanFile(
		     				ProjectViewActivity.this,
		     				new String[] {fileMediaOut.getAbsolutePath()},
		     				new String[] {MediaConstants.MIME_TYPE_MPEG},
		     				null);
		    		*/
		    		mMediaDescTmp.path = fileMediaOut.getAbsolutePath();
		    		mMediaDescTmp.mimeType = MediaConstants.MIME_TYPE_MP4;
		    
		         }
		         else
		         {
		        	 msg = mHandler.obtainMessage(0);
			            msg.getData().putString("status","Something went wrong with media import");

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
	};

    private File createOutputFile (String fileext) throws IOException
    {
		File saveFile = File.createTempFile("output", '.' + fileext, fileExternDir);	
		return saveFile;
    }
    
    private int current, total;
    
    
    private void concatMediaFiles (String outpath) throws Exception
    {
    	
    	MediaDesc mdout = new MediaDesc ();
    	mdout.path = outpath;
    	
    	
    	/*
    	mdout.width = 720;
    	mdout.height = 480;
    	mdout.format = "mp4";
    	
    	mdout.videoCodec = "libx264";
    	mdout.videoBitrate = 1500;
    	
    	mdout.audioCodec = "aac";
    	mdout.audioChannels = 2;
    	mdout.audioBitrate = 96;
    	*/
    	
    	//DrawBoxVideoFilter vf = new DrawBoxVideoFilter(0,400,720,80,"red");
    	//mdout.videoFilter = vf.toString();
    	
    	boolean mediaNeedConvert = false;
    	
    	FfmpegController ffmpegc = new FfmpegController (this);
    	
    	ffmpegc.concatAndTrimFilesMP4Stream(mediaList, mdout, mediaNeedConvert, new ShellCallback() {

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
					
					newStatus = line;
					progress = 0;
				}
				else if ((idx1 = line.indexOf("time="))!=-1)
				{
					int idx2 = line.indexOf(" ", idx1);
					String time = line.substring(idx1+5,idx2);
					newStatus = line;
					
					int hour = Integer.parseInt(time.substring(0,2));
					int min = Integer.parseInt(time.substring(3,5));
					int sec = Integer.parseInt(time.substring(6,8));
					
					current = (hour * 60 * 60) + (min * 60) + sec;
					
					progress = (int)( ((float)current) / ((float)total) *100f );
				}
				
				if (newStatus != null)
				{
				 Message msg = mHandler.obtainMessage(1);
		         msg.getData().putInt("progress", progress);
		         msg.getData().putString("status", newStatus);		         
		         mHandler.sendMessage(msg);
				}
			}
    	});
    
    
   }
    
    private MediaDesc prerenderMedia (MediaDesc mediaIn) throws Exception
    {
    	
    //	DrawBoxVideoFilter vf = new DrawBoxVideoFilter(0,400,720,80,"red");
    //	mdout.videoFilter = vf.toString();
    	
    	FfmpegController ffmpegc = new FfmpegController (this);
    	
    	MediaDesc mediaOut = ffmpegc.convertToMP4Stream(mediaIn, new ShellCallback() {

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
					
					newStatus = line;
					progress = 0;
				}
				else if ((idx1 = line.indexOf("time="))!=-1)
				{
					int idx2 = line.indexOf(" ", idx1);
					String time = line.substring(idx1+5,idx2);
					newStatus = line;
					
					int hour = Integer.parseInt(time.substring(0,2));
					int min = Integer.parseInt(time.substring(3,5));
					int sec = Integer.parseInt(time.substring(6,8));
					
					current = (hour * 60 * 60) + (min * 60) + sec;
					
					progress = (int)( ((float)current) / ((float)total) *100f );
				}
				
				if (newStatus != null)
				{
				 Message msg = mHandler.obtainMessage(1);
		         msg.getData().putInt("progress", progress);
		         msg.getData().putString("status", newStatus);		         
		         mHandler.sendMessage(msg);
				}
			}
    	});
    
    	return mediaOut;
    
   }
    
    
    
    private void addMediaFile (String path, String mimeType)
    {
    	MediaDesc mdesc = new MediaDesc ();
    	mdesc.path = path;
    	mdesc.mimeType = mimeType;
    	
    	mediaList.add(mdesc);
    	int mediaId = mediaList.size()-1;
    	
    	addMediaView(mdesc, mediaId);
    	
    	if (mRenderPath != null && mRenderPath.exists())
    	{
    		mRenderPath.delete();    	
    		mRenderPath = null;
    	}
    	
    	try {
    		doPreconvertMedia(mdesc);
		} catch (Exception e) {
			Toast.makeText(this,"error converting video to mpeg",Toast.LENGTH_SHORT).show();
			Log.e(AppConstants.TAG,"error converting video to mpeg",e);
		}
	
    	
    }
    
    private void addMediaView (MediaDesc mdesc, int mediaId)
    {
    	File file = new File(mdesc.path);

    	 View child = null;
    	 
    	     	 
     	if (mdesc.mimeType.startsWith("image"))
     	{
     		//mdesc.duration = DEFAULT_IMAGE_DURATION;
     		
     		child = new ImageView(this);
     		child.setBackgroundColor(Color.BLACK);
     		
     		((ImageView)child).setScaleType(ImageView.ScaleType.CENTER_CROP);

     		try
     		{
     			((ImageView)child).setImageBitmap(mMediaHelper.getBitmapThumb(file));
     		}
     		catch (IOException ioe)
     		{
     			Log.e(AppConstants.TAG,"unable to load image thumb",ioe);
     		}
     	}
     	else if (mdesc.mimeType.startsWith("video"))
     	{
     		child = new ImageView(this);
     		child.setBackgroundColor(Color.BLACK);
     		((ImageView)child).setScaleType(ImageView.ScaleType.CENTER_CROP);
         	
     		((ImageView)child).setImageBitmap(MediaUtils.getVideoFrame(mdesc.path, 5));
     	}
     	else if (mdesc.mimeType.startsWith("audio")) 
     	{
     		child = new TextView(this);
     		((TextView)child).setText(new File(mdesc.path).getName());
     	}
     	else 
     	{
     		child = new TextView(this);
     		((TextView)child).setText(mdesc.mimeType + ": " + mdesc.path);
     	}
	
     	child.setBackgroundColor(Color.WHITE);
     	child.setPadding(10, 10, 10, 10);

     	child.setId(mediaId);
     	
     	child.setClickable(true);
     	child.setOnClickListener(new OnClickListener () {

			@Override
			public void onClick(View v) {
				
				
			}
     		
     	});
     	
     	adapter.addProjectView(child);
		adapter.notifyDataSetChanged();
		pager.setCurrentItem(adapter.getCount()-1, true);
    }
    
    /*
    public void playVideo(String path, boolean autoplay){
        //get current window information, and set format, set it up differently, if you need some special effects
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        //the VideoView will hold the video
        VideoView videoHolder = new VideoView(this);
        //MediaController is the ui control howering above the video (just like in the default youtube player).
        videoHolder.setMediaController(new MediaController(this));
        //assing a video file to the video holder
        videoHolder.setVideoURI(Uri.parse(path));
        //get focus, before playing the video.
        videoHolder.requestFocus();
        if(autoplay){
            videoHolder.start();
        }
     
     }*/
    
	
    
    private void updateStatus (String msg)
    {
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
    
    
    
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		
		if (resultCode == RESULT_OK)
		{
			
			mMediaResult = mMediaHelper.handleResult(requestCode, resultCode, intent, mMediaTmp);
			
			if (mMediaResult != null)
				if (mMediaResult.path != null)
					addMediaFile(mMediaResult.path, mMediaResult.mimeType);
			
			//if path is null, wait for the scanner callback in the mHandler
		}
		
		
	}	
	
	
	public void showMediaPrefs ()
	{
		Intent intent = new Intent(this, MediaOutputPreferences.class);
		startActivityForResult(intent,0);
		
	}

	

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		 if (item.getItemId() == R.id.menu_add_media)
         {
			 
			 showAddMediaDialog();
         }		 
		 else if (item.getItemId() == R.id.menu_settings)
		 {
			 showMediaPrefs();
		 }
		 else if (item.getItemId() == R.id.menu_play_media)
         {
			
			 if (mRenderPath != null)
				 mMediaHelper.playMedia(mRenderPath, MediaConstants.MIME_TYPE_VIDEO);
			 else
				 doExportMedia ();
			 
			 
         }
		 else if (item.getItemId() == R.id.menu_share_media)
         {
			
			 mMediaHelper.shareMedia(mRenderPath);
			 
         }

		 else if (item.getItemId() == R.id.menu_play_media)
         {
			
			
			 
         }
		
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void showAddMediaDialog ()
	{
		
		final CharSequence[] items = {"Open Gallery","Open File","Record Video", "Record Audio", "Take Photo"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose medium");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        
		    	switch (item) {
		    		case 0:
		    			mMediaHelper.openGalleryChooser("*/*");
		    			break;
		    		case 1:
		    			mMediaHelper.openFileChooser();
		    			break;
		    		case 2:
		    			mMediaTmp = mMediaHelper.captureVideo(fileExternDir);

		    			break;
		    		case 3:
		    			mMediaTmp = mMediaHelper.captureAudio(fileExternDir);

		    			break;
		    		case 4:
		    			mMediaTmp = mMediaHelper.capturePhoto(fileExternDir);
		    			break;
		    		default:
		    			//do nothing!
		    	}
		    	
		    	
		    }
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}

	private Handler mHandler = new Handler()
	{
		 public void handleMessage(Message msg) {
			 
			 String status = msg.getData().getString("status");
			 
	            switch (msg.what) {
		            case 0: //status
	
	                    progressDialog.dismiss();
	                    
	                    if (status != null)
	                    	Toast.makeText(ProjectViewActivity.this, status, Toast.LENGTH_SHORT).show();
	                    
	                 break;
	                case 1: //status

	                       progressDialog.setMessage(status);
	                       progressDialog.setProgress(msg.getData().getInt("progress"));
	                    break;
	               
	                case 4: //play video
	                	
	                	mMediaHelper.playMedia(mRenderPath, MediaConstants.MIME_TYPE_VIDEO);
	                	break;
	                	
	                case 5:
	                		
	                		if (mMediaResult != null)
	                		{
	                			String path = msg.getData().getString("path");	                		
	                			addMediaFile(path, mMediaResult.mimeType);
	                		}
	                	break;
	                default:
	                    super.handleMessage(msg);
	            }
	        }
	};
	
	private class AwesomePagerAdapter extends PagerAdapter{

    	private ArrayList<View> listProjectViews;
	 	
    	public AwesomePagerAdapter ()
		{
			listProjectViews = new ArrayList<View>();
		}
		
    	public void addProjectView (View view)
    	{
    		listProjectViews.add(view);
    	}
    	
    	public void removeProjectView (View view)
    	{
    		listProjectViews.remove(view);
    	}
    	
    	public void removeProjectView (int viewIdx)
    	{
    		listProjectViews.remove(viewIdx);
    	}
	 	
		@Override
		public int getCount() {
			
			return listProjectViews.size();
			
		}

	    /**
	     * Create the page for the given position.  The adapter is responsible
	     * for adding the view to the container given here, although it only
	     * must ensure this is done by the time it returns from
	     * {@link #finishUpdate()}.
	     *
	     * @param container The containing View in which the page will be shown.
	     * @param position The page position to be instantiated.
	     * @return Returns an Object representing the new page.  This does not
	     * need to be a View, but can be some other container of the page.
	     */
		@Override
		public Object instantiateItem(View collection, int position) {
			
			((ViewPager) collection).addView(listProjectViews.get(position));
			
			return listProjectViews.get(position);
		}

	    /**
	     * Remove a page for the given position.  The adapter is responsible
	     * for removing the view from its container, although it only must ensure
	     * this is done by the time it returns from {@link #finishUpdate()}.
	     *
	     * @param container The containing View from which the page will be removed.
	     * @param position The page position to be removed.
	     * @param object The same object that was returned by
	     * {@link #instantiateItem(View, int)}.
	     */
		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager) collection).removeView((View) view);
		}

		
		
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view==((View)object);
		}

		
	    /**
	     * Called when the a change in the shown pages has been completed.  At this
	     * point you must ensure that all of the pages have actually been added or
	     * removed from the container as appropriate.
	     * @param container The containing View which is displaying this adapter's
	     * page views.
	     */
		@Override
		public void finishUpdate(View arg0) {}
		

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {}
    	
    }
    
}