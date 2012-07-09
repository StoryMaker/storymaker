package info.guardianproject.mrapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final static int GALLERY_RESULT = 1;
	private final static int CAMERA_RESULT = 2;
	
    private final static String MIME_TYPE_MP4 = "video/mp4";

	private final static int DEFAULT_IMAGE_DURATION = 5;
	
	private final static String TAG = "MRAPP";
	
	private ArrayList<MediaDesc> mediaList = new ArrayList<MediaDesc>();
	
	private File fileExternDir;
	
	private LinearLayout layoutMain;
	
	private File outFile = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        layoutMain = (LinearLayout) findViewById(R.id.layout_media_list);
        
        initExternalStorage();
    }

    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		
    	//super.onConfigurationChanged(newConfig);
		
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
	     				MainActivity.this,
	     				new String[] {outFile.getAbsolutePath()},
	     				new String[] {MIME_TYPE_MP4},
	     				null);
	    		
	    		playVideo();
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
    	
    	mdout.kbitrate = 500;
    	mdout.width = 480;
    	mdout.height = 240;
    	mdout.vcodec = "libx264";
    	mdout.acodec = "copy";
    	
    	FfmpegController ffmpegc = new FfmpegController (this);
    	
    	ffmpegc.concatAndTrimFiles(mediaList, mdout, new ShellCallback() {

			@Override
			public void shellOut(String line) {
				
				
				Log.d(TAG, line);
				
				//progressDialog.setMessage(new String(msg));
				//Duration: 00:00:00.99,
				//time=00:00:00.00
				
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
    	mediaList.add(mdesc);
    	int mediaId = mediaList.size()-1;
    	
    	File file = new File(path);
    	
    	 final ImageView child = new ImageView(this);
    	 child.setLayoutParams(new ViewGroup.LayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT));

    	 child.setPadding(30,30,30,30);
    	 
     	if (mimeType.startsWith("image"))
     	{
     		mdesc.duration = DEFAULT_IMAGE_DURATION;
     		
     		try
     		{
     			child.setImageBitmap(getBitmapThumb(file));
     		}
     		catch (IOException ioe)
     		{
     			Log.e(TAG,"unable to load image thumb",ioe);
     		}
     	}
     	else if (mimeType.startsWith("video"))
     	{
     		child.setImageBitmap(getVideoFrame(path, 5));
     	}

     	child.setId(mediaId);
     	
     	child.setClickable(true);
     	child.setOnClickListener(new OnClickListener () {

			@Override
			public void onClick(View v) {
				
				int mediaId = ((ImageView)v).getId();
				MediaDesc mdesc = MainActivity.this.mediaList.get(mediaId);
				Toast.makeText(MainActivity.this, mdesc.path, Toast.LENGTH_LONG).show();
			}
     		
     	});
     	
     	
     	child.setOnLongClickListener(new OnLongClickListener () {

			@Override
			public boolean onLongClick(View v) {
				
				int mediaId = ((ImageView)v).getId();
				MediaDesc mdesc = mediaList.get(mediaId);
				mediaList.remove(mediaId);
				layoutMain.removeView(v);
				
				return true;
			}
     		
     	});
     	
     	child.setScaleType(ImageView.ScaleType.CENTER_CROP);
     	
    	layoutMain.addView(child);
    	
    	updateStatus( file.getName() + " added to queue");
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
								/*
								Intent passingIntent = new Intent(this,VideoEditor.class);
								passingIntent.setData(uriGalleryFile);
								startActivityForResult(passingIntent,VIDEO_EDITOR);
								*/
							
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
			/*
			else if (requestCode == CAMERA_RESULT)
			{
				//Uri uriCameraImage = intent.getData();
				
				if (uriCameraImage != null)
				{
					Intent passingIntent = new Intent(this,ImageEditor.class);
					passingIntent.setData(uriCameraImage);
					startActivityForResult(passingIntent,IMAGE_EDITOR);
				}
			}*/
		}
		
		
	}	
	
	public void showMediaPrefs ()
	{
		Intent intent = new Intent(this, MediaOutputPreferences.class);
		startActivityForResult(intent,0);
		
	}
	
	private void addVideoToGallery (File videoToAdd, String mimeType)
	{
		/*
		   // Save the name and description of a video in a ContentValues map.  
        ContentValues values = new ContentValues(2);
        values.put(MediaStore.Video.Media.MIME_TYPE, MIME_TYPE_MP4);
        // values.put(MediaStore.Video.Media.DATA, f.getAbsolutePath()); 

        // Add a new record (identified by uri) without the video, but with the values just set.
        Uri uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

        // Now get a handle to the file for that record, and save the data into it.
        try {
            InputStream is = new FileInputStream(videoToAdd);
            OutputStream os = getContentResolver().openOutputStream(uri);
            byte[] buffer = new byte[4096]; // tweaking this number may increase performance
            int len;
            while ((len = is.read(buffer)) != -1){
                os.write(buffer, 0, len);
            }
            os.flush();
            is.close();
            os.close();
        } catch (Exception e) {
            Log.e(LOGTAG, "exception while writing video: ", e);
        } 
        */
		
	
     // force mediascanner to update file
     		MediaScannerConnection.scanFile(
     				this,
     				new String[] {videoToAdd.getAbsolutePath()},
     				new String[] {mimeType},
     				null);

//        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
	}

	public static Bitmap getVideoFrame(String videoPath,long frameTime) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);                   
            return retriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "error getting video frame", ex);
            
        } catch (RuntimeException ex) {
        	Log.e(TAG, "error getting video frame", ex);
                                } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return null;
    }
	

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		 if (item.getItemId() == R.id.menu_add_media)
         {
			 openGalleryChooser("*/*");
			 
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

	private Handler mHandler = new Handler()
	{
		 public void handleMessage(Message msg) {
			 
			 String status = msg.getData().getString("status");
			 
	            switch (msg.what) {
		            case 0: //status
	
	                    progressDialog.dismiss();
	                    
	                    if (status != null)
	                    	Toast.makeText(MainActivity.this, status, Toast.LENGTH_SHORT).show();
	                    
	                 break;
	                case 1: //status

	                       progressDialog.setMessage(status);
	                       progressDialog.setProgress(msg.getData().getInt("progress"));
	                    break;
	               
	                default:
	                    super.handleMessage(msg);
	            }
	        }
	};
	
	private void playVideo() {
		
		
    	Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
    	intent.setDataAndType(Uri.parse(outFile.getPath()), MIME_TYPE_MP4);    	
   	 	startActivityForResult(intent,0);
   	 	
	}
	
	private void shareVideo() {
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.setType(MIME_TYPE_MP4);
    	intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(outFile.getPath()));
    	startActivityForResult(Intent.createChooser(intent, "Share Video"),0);     
	}
    
}