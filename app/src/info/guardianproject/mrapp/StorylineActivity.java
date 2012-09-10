package info.guardianproject.mrapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.MediaUtils;
import org.ffmpeg.android.ShellUtils.ShellCallback;
import org.ffmpeg.android.filters.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class StorylineActivity extends Activity implements MediaScannerConnectionClient {

	private final static int GALLERY_RESULT = 1;
	private final static int CAMERA_RESULT = 2;
	
    private final static String MIME_TYPE_MP4 = "video/mp4";

	private final static String DEFAULT_IMAGE_DURATION = "00:00:05";
	
	private final static String TAG = "MRAPP";
	
	private ArrayList<MediaDesc> mediaList = new ArrayList<MediaDesc>();
	
	private File fileExternDir;
	
	private LinearLayout layoutMain;
	
	private File outFile = null;
	
	private File cameraImage;
	private String mimeType;
	
	private MediaScannerConnection msc;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        layoutMain = (LinearLayout) findViewById(R.id.layout_media_list);
        
        initExternalStorage();
    }

    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		
    	super.onConfigurationChanged(newConfig);
		
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
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
    	progressDialog.setMessage("Processing. Please wait...");
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
	    		outFile = createOutputFile("mp4");
			 
	    		concatMediaFiles(outFile.getAbsolutePath());
	    		
	    		Message msg = mHandler.obtainMessage(0);
		         mHandler.sendMessage(msg);
	    		
	    		MediaScannerConnection.scanFile(
	     				StorylineActivity.this,
	     				new String[] {outFile.getAbsolutePath()},
	     				new String[] {MIME_TYPE_MP4},
	     				null);
	    		
	    		
	    		msg = mHandler.obtainMessage(4);
	            msg.getData().putString("path",outFile.getAbsolutePath());
	            
	            mHandler.sendMessage(msg);
	    	}
	    	catch (Exception e)
	    	{
	    		Message msg = mHandler.obtainMessage(0);
	            msg.getData().putString("status","error: " + e.getMessage());

		         mHandler.sendMessage(msg);
	    		Log.e(TAG, "error exporting",e);
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
    	
    	mdout.width = 720;
    	mdout.height = 480;
    	mdout.format = "mp4";
    	
    	mdout.videoCodec = "libx264";
    	mdout.videoBitrate = 1500;
    	
    	mdout.audioCodec = "aac";
    	mdout.audioChannels = 2;
    	mdout.audioBitrate = 96;
    	
    //	DrawBoxVideoFilter vf = new DrawBoxVideoFilter(0,400,720,80,"red");
    //	mdout.videoFilter = vf.toString();
    	
    	FfmpegController ffmpegc = new FfmpegController (this);
    	
    	ffmpegc.concatAndTrimFiles(mediaList, mdout, new ShellCallback() {

			@Override
			public void shellOut(String line) {
				
				
				if (!line.startsWith("frame"))
					Log.d(TAG, line);
				
				
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
    
    
    private void addMediaFile (String path, String mimeType)
    {
    	MediaDesc mdesc = new MediaDesc ();
    	mdesc.path = path;
    	mdesc.mimeType = mimeType;
    	
    	mediaList.add(mdesc);
    	int mediaId = mediaList.size()-1;
    	
    	addMediaView(mdesc, mediaId);
    	
    }
    
    private void addMediaView (MediaDesc mdesc, int mediaId)
    {
    	File file = new File(mdesc.path);

    	 View child = null;
    	 
    	     	 
     	if (mdesc.mimeType.startsWith("image"))
     	{
     		mdesc.duration = DEFAULT_IMAGE_DURATION;
     		child = new ImageView(this);
     		((ImageView)child).setScaleType(ImageView.ScaleType.CENTER_CROP);

     		try
     		{
     			((ImageView)child).setImageBitmap(getBitmapThumb(file));
     		}
     		catch (IOException ioe)
     		{
     			Log.e(TAG,"unable to load image thumb",ioe);
     		}
     	}
     	else if (mdesc.mimeType.startsWith("video"))
     	{
     		child = new ImageView(this);
     		((ImageView)child).setScaleType(ImageView.ScaleType.CENTER_CROP);
         	
     		((ImageView)child).setImageBitmap(MediaUtils.getVideoFrame(mdesc.path, 5));
     	}
     	else 
     	{
     		child = new TextView(this);
     		((TextView)child).setText(mdesc.mimeType + ": " + mdesc.path);
     	}

     	ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
     	
     	child.setLayoutParams(lp);

     	child.setBackgroundColor(Color.WHITE);
     	child.setPadding(5, 5, 5, 5);
     	

     	child.setId(mediaId);
     	
     	child.setClickable(true);
     	child.setOnClickListener(new OnClickListener () {

			@Override
			public void onClick(View v) {
				
				int mediaId = ((ImageView)v).getId();
				MediaDesc mdesc = StorylineActivity.this.mediaList.get(mediaId);
				Toast.makeText(StorylineActivity.this, mdesc.path, Toast.LENGTH_LONG).show();
			}
     		
     	});
     	
     	
     	child.setOnLongClickListener(new OnLongClickListener () {

			@Override
			public boolean onLongClick(View v) {
				
				int mediaId = ((ImageView)v).getId();
				MediaDesc mdesc = mediaList.get(mediaId);
				mdesc.path = null;
				layoutMain.removeView(v);
				
				return true;
			}
     		
     	});
     	
     	
    	layoutMain.addView(child);
    	

    	updateStatus( file.getName() + " added to queue");
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
    
	 private void playVideo() {
			
	    	Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
	    	intent.setDataAndType(Uri.fromFile(outFile), "video/*");   
	    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	   	 	startActivity(intent);
	   	 	
	 }
    
    private Bitmap getBitmapThumb (File file) throws IOException
    {
    	 Uri contentURI = Uri.fromFile(file);        
         ContentResolver cr = getContentResolver();
         InputStream in = cr.openInputStream(contentURI);
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inSampleSize=8;
         Bitmap thumb = BitmapFactory.decodeStream(in,null,options);
         return thumb;
    }
    
    private void updateStatus (String msg)
    {
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
    public void openGalleryChooser (String mimeType)
    {
    	Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(mimeType); //limit to specific mimetype
		startActivityForResult(intent, GALLERY_RESULT);
		
    }
    
    
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		
		if (resultCode == RESULT_OK)
		{
			
			if (requestCode == GALLERY_RESULT) 
			{
				if (intent != null)
				{
					Uri uriGalleryFile = intent.getData();
					
					try
						{
							if (uriGalleryFile != null)
							{
								Cursor cursor = managedQuery(uriGalleryFile, null, 
		                                null, null, null); 
								cursor.moveToNext(); 
								// Retrieve the path and the mime type 
								String path = cursor.getString(cursor 
								                .getColumnIndex(MediaStore.MediaColumns.DATA)); 
								String mimeType = cursor.getString(cursor 
								                .getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
								
								
								addMediaFile (path, mimeType);
								
							}
							else
							{
								Toast.makeText(this, "Unable to load media.", Toast.LENGTH_LONG).show();
			
							}
						}
					catch (Exception e)
					{
						Toast.makeText(this, "Unable to load media.", Toast.LENGTH_LONG).show();
						Log.e(TAG, "error loading media: " + e.getMessage(), e);

					}
				}
				else
				{
					Toast.makeText(this, "Unable to load media.", Toast.LENGTH_LONG).show();
	
				}
					
			}
			else if(requestCode == MediaAppConstants.CAMERA_RESULT) {
				
				Uri uriCameraImage = intent.getData();
			
				Log.d(MediaAppConstants.TAG, "RETURNED URI FROM CAMERA RESULT: " + uriCameraImage.toString());
				
				String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uriCameraImage.toString());
				mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
				
				if (mimeType == null)
				{
					if(uriCameraImage.getPathSegments().contains("video")) {
						mimeType = MediaAppConstants.MIME_TYPE_MP4;
					} else if(mimeType == null && uriCameraImage.getPathSegments().contains("images")) {
						mimeType = MediaAppConstants.MIME_TYPE_JPEG;
					}
				}
				// TODO: IMPORTANTE!  Right here, we are forcing the media object to go through
				// the media scanner.  THIS MUST BE UNDONE at the end of the editing process
				// in order to maintain security/anonymity
				
				if(mimeType.equals(MediaAppConstants.MIME_TYPE_MP4)) {
					// write input stream to file
					FileOutputStream fos;
					try {
						
						fos = new FileOutputStream(cameraImage);
						InputStream media = getContentResolver().openInputStream(uriCameraImage);
						byte buf[] = new byte[1024];
						int len;
						while((len = media.read(buf)) > 0)
							fos.write(buf, 0, len);
						fos.close();
						media.close();
					} catch (FileNotFoundException e) {
						Log.e(MediaAppConstants.TAG, e.toString());
					} catch (IOException e) {
						Log.e(MediaAppConstants.TAG, e.toString());
					}
					
				}
				
				msc = new MediaScannerConnection(this, this);
				msc.connect();
				
			}
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
			 openGalleryChooser("*/*");
			 
         }
		 else if (item.getItemId() == R.id.menu_capture_media)
         {
			 captureVideo();
			 
         }
		 else if (item.getItemId() == R.id.menu_settings)
		 {
			 showMediaPrefs();
		 }
		 else if (item.getItemId() == R.id.menu_save_media)
         {
			
			 doExportMedia ();
			 
			 
         }
		 else if (item.getItemId() == R.id.menu_share_media)
         {
			
			 shareVideo();
			 
         }

		 else if (item.getItemId() == R.id.menu_play_media)
         {
			
			 if (outFile != null)
				 playVideo();
			 
			 
         }
		
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void captureVideo ()
	{
		 ContentValues values = new ContentValues();
         values.put(MediaStore.Images.Media.TITLE, MediaAppConstants.CAMCORDER_TMP_FILE);
         values.put(MediaStore.Images.Media.DESCRIPTION,"ssctmp");
         
     	sendBroadcast(new Intent().setAction(MediaAppConstants.Keys.Service.LOCK_LOGS));
        cameraImage = new File(fileExternDir, "cam" + new Date().getTime() + ".mp4");

     	Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
         startActivityForResult(intent, MediaAppConstants.CAMERA_RESULT);
	}

	private Handler mHandler = new Handler()
	{
		 public void handleMessage(Message msg) {
			 
			 String status = msg.getData().getString("status");
			 
	            switch (msg.what) {
		            case 0: //status
	
	                    progressDialog.dismiss();
	                    
	                    if (status != null)
	                    	Toast.makeText(StorylineActivity.this, status, Toast.LENGTH_SHORT).show();
	                    
	                 break;
	                case 1: //status

	                       progressDialog.setMessage(status);
	                       progressDialog.setProgress(msg.getData().getInt("progress"));
	                    break;
	               
	                case 4: //play video
	                	
	                		playVideo();
	                	break;
	                	
	                case 5:
	                	
	                		String path = msg.getData().getString("path");
	                		String mimeType = msg.getData().getString("mime");
	                		
	                		addMediaFile(path, mimeType);
	                	break;
	                default:
	                    super.handleMessage(msg);
	            }
	        }
	};
	
	
	private void shareVideo() {
		
		Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse(outFile.getPath()));
   	 	startActivityForResult(intent,0);
   	 	
   	 	/*
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.setType(MIME_TYPE_MP4);
    	intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(outFile.getPath()));
    	startActivityForResult(Intent.createChooser(intent, "Share Video"),0); 
    	*/    
	}

	@Override
	public void onMediaScannerConnected() {
		
		msc.scanFile(cameraImage.getAbsolutePath(), null);

	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		
		msc.disconnect();
		Log.d(MediaAppConstants.TAG, "new path: " + path + "\nnew uri for path: " + uri.toString());
		
		 Message msg = mHandler.obtainMessage(5);
         msg.getData().putString("path", path);
         msg.getData().putString("mime", mimeType);         
         mHandler.sendMessage(msg);

	}
    
}