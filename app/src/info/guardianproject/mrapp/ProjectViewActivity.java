package info.guardianproject.mrapp;

import info.guardianproject.mrapp.media.MediaClip;
import info.guardianproject.mrapp.media.MediaConstants;
import info.guardianproject.mrapp.media.MediaExporter;
import info.guardianproject.mrapp.media.MediaHelper;
import info.guardianproject.mrapp.media.MediaManager;
import info.guardianproject.mrapp.media.MediaMerger;
import info.guardianproject.mrapp.media.MediaRenderer;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.ui.MediaView;
import info.guardianproject.mrapp.ui.OverlayCamera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.MediaUtils;
import org.ffmpeg.android.ShellUtils.ShellCallback;
import org.ffmpeg.android.filters.DrawBoxVideoFilter;
import org.ffmpeg.android.filters.DrawTextVideoFilter;
import org.ffmpeg.android.filters.FadeVideoFilter;
import org.ffmpeg.android.filters.VideoFilter;

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
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ProjectViewActivity extends SherlockActivity implements MediaManager {

	private ViewPager pager;
	private AwesomePagerAdapter adapter;
	
	private ArrayList<MediaClip> mediaList = new ArrayList<MediaClip>();
	private ArrayList<MediaView> mediaViewList = new ArrayList<MediaView>();
	
	private File fileExternDir;
	
	private File mMediaTmp;
	private MediaDesc mOut;
	
	private MediaHelper mMediaHelper;
	private MediaHelper.MediaResult mMediaResult;
	
	private Project mProject = null;
	
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
        	
        	int pid = intent.getIntExtra("pid", -1);
            if (pid != -1) {
                mProject = Project.get(getApplicationContext(), pid);
                Media[] _medias = mProject.getMediaAsArray();
                for (Media media: _medias) {
                    try
                    {
                        addMediaFile(media.getPath(), media.getMimeType());
                    }
                    catch (IOException ioe)
                    {
                        Log.e(MediaAppConstants.TAG,"error adding media from saved project", ioe);
                    }
                }
            } // FIXME else what?
        	
        	if (result != null && result.path != null && result.mimeType != null)
        	{
        		try
        		{
        			addMediaFile(result.path, result.mimeType);
        			mProject.appendMedia(result.path, result.mimeType);
        		}
    			catch (IOException ioe)
    			{
    				Log.e(MediaAppConstants.TAG,"error adding media result",ioe);
    			}
            }
        }
    }

    private void addDefaultView ()
	{
		TextView view = new TextView(this);
		view.setTextSize(30);
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

    	ArrayList<MediaDesc> listMediaDesc = new ArrayList<MediaDesc>();
    	for (MediaClip mClip : mediaList)
    		if (mClip.mMediaDescRendered != null)
    			listMediaDesc.add(mClip.mMediaDescRendered);
    		else
    			listMediaDesc.add(mClip.mMediaDescOriginal);

	    try
	    {
		    mOut = new MediaDesc ();
		    applyExportSettings(mOut);
		    mOut.path = createOutputFile("mp4").getAbsolutePath();
		    
		   MediaExporter mEx = new MediaExporter(this, mHandler, listMediaDesc, mOut);
		   
			// Convert to video
			Thread thread = new Thread (mEx);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
	    }
	    catch (IOException ioe)
	    {
	    	updateStatus("error creating file: " + ioe.getMessage());
	    	Log.e(MediaAppConstants.TAG,"error creating file",ioe);
	        progressDialog.cancel();
	    }
    
    }
    

    private File createOutputFile (String fileext) throws IOException
    {
		File saveFile = File.createTempFile("output", '.' + fileext, fileExternDir);	
		return saveFile;
    }
    
    public void applyExportSettings (MediaDesc mdout)
    {
    	//look this up from prefs?
    	mdout.videoCodec = "libx264";
    	mdout.videoBitrate = 1500;
    	mdout.audioBitrate = 128;
    	mdout.videoFps = "29.97";
    	mdout.width = 720;
    	mdout.height = 480;
    	
    }
    
    private void addMediaFile (String path, String mimeType) throws IOException
    {
    	MediaDesc mdesc = new MediaDesc ();
    	mdesc.path = path;
    	mdesc.mimeType = mimeType;
    	
		if (mimeType.startsWith("audio") && mediaList.size() > 0 && (!mediaList.get(mediaList.size()-1).mMediaDescOriginal.mimeType.equals(mimeType)))
		{
			MediaClip mClipVideo =  mediaList.get(mediaList.size()-1);
			MediaView mView = (MediaView)pager.getChildAt(mediaList.size());
			
			MediaClip mClipAudio = new MediaClip();
			mClipAudio.mMediaDescOriginal = mdesc;
		
			try {
	    		MediaMerger mm = new MediaMerger(this, (MediaManager)this, mHandler, mClipVideo, mClipAudio, fileExternDir, mView);
	    		// Convert to video
	    		Thread thread = new Thread (mm);
	    		thread.setPriority(Thread.NORM_PRIORITY);
	    		thread.start();
			} catch (Exception e) {
				updateStatus("error merging video and audio");
				Log.e(AppConstants.TAG,"error merging video and audio",e);
			}
		
			
		}
		else
		{
			//its the first clip and/or the previous item is the same type as this
    		
			MediaClip mClip = new MediaClip();
			mClip.mMediaDescOriginal = mdesc;
			mediaList.add(mClip);
			
			int mediaId = mediaList.size()-1;
			
			MediaView mView = addMediaView(mClip, mediaId);
			
			prerenderMedia (mClip, mView);
		}
		
		mOut = null;
    	
    }
    
    public void prerenderMedia (MediaClip mClip, ShellCallback shellCallback)
    {
    	
    	try {
    		MediaRenderer mRenderer = new MediaRenderer(this, (MediaManager)this, mHandler, mClip, fileExternDir, shellCallback);
    		// Convert to video
    		Thread thread = new Thread (mRenderer);
    		thread.setPriority(Thread.NORM_PRIORITY);
    		thread.start();
		} catch (Exception e) {
			Toast.makeText(this,"error converting video to mpeg",Toast.LENGTH_SHORT).show();
			Log.e(AppConstants.TAG,"error converting video to mpeg",e);
		}
	
    	
    }
    
    private MediaView addMediaView (MediaClip mediaClip, int mediaId) throws IOException
    {
    	File fileMediaClip = new File(mediaClip.mMediaDescOriginal.path);

    	 MediaView mediaView = null;
    	 MediaDesc mdesc = mediaClip.mMediaDescOriginal;
    	     	 
     	if (mdesc.mimeType.startsWith("image"))
     	{
     		
     		mediaView = new MediaView(this,(MediaManager)this, mediaClip,fileMediaClip.getName(), mMediaHelper.getBitmapThumb(fileMediaClip), Color.WHITE, Color.BLACK);

     	}
     	else if (mdesc.mimeType.startsWith("video"))
     	{
     		mediaView = new MediaView(this,(MediaManager)this,  mediaClip,fileMediaClip.getName(), MediaUtils.getVideoFrame(mdesc.path, 5), Color.WHITE, Color.BLACK);
     		
     	}
     	else if (mdesc.mimeType.startsWith("audio")) 
     	{
     		mediaView = new MediaView(this,(MediaManager)this,  mediaClip, fileMediaClip.getName(), null, Color.WHITE, Color.BLACK);

     	}
     	else 
     	{
     		mediaView = new MediaView(this,(MediaManager)this,  mediaClip, fileMediaClip.getName(),null, Color.WHITE, Color.BLACK);

     	}
    
     	mediaView.setId(mediaId);
     	
     	//mediaViewList.add(mediaView);
     	adapter.addProjectView(mediaView);
		adapter.notifyDataSetChanged();
		pager.setCurrentItem(adapter.getCount()-1, true);
		
		return mediaView;
    }
    
    
    private void updateStatus (String msg)
    {
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		
		if (resultCode == RESULT_OK)
		{
			if (requestCode == 777) //overlay camerae
			{
				//now launch real camera
				mMediaTmp = mMediaHelper.captureVideo(fileExternDir);
				

			}
			else
			{
				mMediaResult = mMediaHelper.handleResult(requestCode, resultCode, intent, mMediaTmp);
				
				try
				{
					if (mMediaResult != null)
						if (mMediaResult.path != null)
							addMediaFile(mMediaResult.path, mMediaResult.mimeType);
				}
				catch (IOException ioe)
				{
					Log.e(MediaAppConstants.TAG,"error adding media result",ioe);
				}
				
			
				//if path is null, wait for the scanner callback in the mHandler
			}
		}
		
		
	}	
	
	
	public void showMediaPrefs ()
	{
		Intent intent = new Intent(this, MediaOutputPreferences.class);
		startActivityForResult(intent,0);
		
	}

	private void playMedia ()
	{
		 mMediaHelper.playMedia(new File(mOut.path), MediaConstants.MIME_TYPE_VIDEO);

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
			
			 if (mOut != null && mOut.path != null)
				 playMedia();
			 else
			 {
				 doExportMedia ();
			 }				 
			 
         }
		 else if (item.getItemId() == R.id.menu_share_media)
         {
			 if (mOut != null && mOut.path != null)
				 mMediaHelper.shareMedia(new File(mOut.path), MediaConstants.MIME_TYPE_VIDEO);
			 else
				 updateStatus("You must render your story first!");
         }
		 else if (item.getItemId() == R.id.menu_play_media)
         {
			
			
			 
         }
		 else if (item.getItemId() == R.id.menu_save_project)
		 {
			 // TODO prompt user for storage location?
			 if (mOut != null && mOut.path != null) {
				 File inFile = new File(mOut.path);
				 FileChannel in;
				 try {
					 in = new FileInputStream(inFile).getChannel();
					 FileChannel out = new FileOutputStream(new File("/sdcard/"
							 + inFile.getName())).getChannel();
					 in.transferTo(0, in.size(), out);
				 } catch (FileNotFoundException e) {
					 // TODO Auto-generated catch block
					 e.printStackTrace();
				 } catch (IOException e) {
					 // TODO Auto-generated catch block
					 e.printStackTrace();
				 }
			 } else
				 updateStatus("You must render your story first!");

		 }

		return super.onMenuItemSelected(featureId, item);
	}
	
	private void showAddMediaDialog ()
	{
		
		final CharSequence[] items = {"Open Gallery","Open File","Choose Shot","Record Video", "Record Audio", "Take Photo"};

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
		    			showOverlayCamera();
		    			break;
		    		case 3:
		    			mMediaTmp = mMediaHelper.captureVideo(fileExternDir);

		    			break;
		    		case 4:
		    			mMediaTmp = mMediaHelper.captureAudio(fileExternDir);

		    			break;
		    		case 5:
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

	private void showOverlayCamera ()
	{
		Intent intent = new Intent(this, OverlayCamera.class);
		startActivityForResult(intent,777);
		
	}
	
	private Handler mHandler = new Handler()
	{
		 public void handleMessage(Message msg) {
			 
			 String status = msg.getData().getString("status");
			 
	            switch (msg.what) {
		            case 0: //status
	
		            	if (progressDialog != null)
	                    progressDialog.dismiss();
	                    
	                    if (status != null)
	                    	Toast.makeText(ProjectViewActivity.this, status, Toast.LENGTH_LONG).show();
	                    
	                 break;
	                case 1: //status
	                	if (progressDialog != null)
	                	{
	                       progressDialog.setMessage(status);
	                       progressDialog.setProgress(msg.getData().getInt("progress"));
	                	}
	                    break;
	               
	                case 4: //play video
	                	
	                	playMedia();
	                	break;
	                	
	                case 5:
	                		
	                		if (mMediaResult != null)
	                		{
	                			String path = msg.getData().getString("path");
	                			try
	                			{
	                				addMediaFile(path, mMediaResult.mimeType);
	                                mProject.appendMedia(path, mMediaResult.mimeType);
	                			}
	                			catch (IOException ioe)
	                			{
	                				Log.e(MediaAppConstants.TAG,"error adding media result",ioe);
	                			}
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
