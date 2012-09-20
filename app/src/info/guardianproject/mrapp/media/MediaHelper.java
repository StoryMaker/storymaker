package info.guardianproject.mrapp.media;

import info.guardianproject.mrapp.AppConstants;
import info.guardianproject.mrapp.MediaAppConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

public class MediaHelper implements MediaScannerConnectionClient {

	private Activity mActivity;
	private Handler mHandler;
	private MediaScannerConnection mScanner;
	
	private File mMediaFileTmp;
	private Uri mMediaUriTmp;
	
	public MediaHelper (Activity activity, Handler handler)
	{
		mActivity = activity;
		mHandler = handler;
	}
	
	public File captureVideo (File fileExternDir)
	{
		 ContentValues values = new ContentValues();
         values.put(MediaStore.Images.Media.TITLE, MediaConstants.CAMCORDER_TMP_FILE);
         values.put(MediaStore.Images.Media.DESCRIPTION,MediaConstants.CAMCORDER_TMP_FILE);
         
         mActivity.sendBroadcast(new Intent().setAction(MediaAppConstants.Keys.Service.LOCK_LOGS));
         mMediaFileTmp = new File(fileExternDir, new Date().getTime() + '-' + MediaConstants.CAMCORDER_TMP_FILE);
         mMediaUriTmp = Uri.fromFile(mMediaFileTmp);
         
     	Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra( MediaStore.EXTRA_OUTPUT, mMediaUriTmp);

     	mActivity.startActivityForResult(intent, MediaConstants.CAMERA_RESULT);
         
         return mMediaFileTmp;
	}
	
	public File capturePhoto (File fileExternDir)
	{
     	
          
        ContentValues values = new ContentValues();
      
        values.put(MediaStore.Images.Media.TITLE, MediaConstants.CAMERA_TMP_FILE);      
        values.put(MediaStore.Images.Media.DESCRIPTION,MediaConstants.CAMERA_TMP_FILE);

        mMediaFileTmp = new File(fileExternDir, new Date().getTime() + '-' + MediaConstants.CAMERA_TMP_FILE);
    	
        mMediaUriTmp = Uri.fromFile(mMediaFileTmp);
        //uriCameraImage = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE );
        intent.putExtra( MediaStore.EXTRA_OUTPUT, mMediaUriTmp);
        
        mActivity.startActivityForResult(intent, MediaConstants.CAMERA_RESULT);
        
         return mMediaFileTmp;
	}
	
	public File captureAudio (File fileExternDir)
	{
		 Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
		 mActivity.startActivityForResult(intent, MediaConstants.AUDIO_RESULT);
         File fileAudio = new File(fileExternDir, new Date().getTime() + '-' + MediaConstants.AUDIO_TMP_FILE);

         return fileAudio;
	}
	
	public void openGalleryChooser (String mimeType)
    {
    	Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(mimeType); //limit to specific mimetype
		mActivity.startActivityForResult(intent, MediaConstants.GALLERY_RESULT);
		
    }
	
	public void openFileChooser ()
    {
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*"); 
		mActivity.startActivityForResult(intent, MediaConstants.FILE_RESULT);
		
    }
	
	 public void playMedia (File mediaFile, String mimeType) {
			
	    	Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
	    	intent.setDataAndType(Uri.fromFile(mediaFile), mimeType);   
	    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	   	 	mActivity.startActivity(intent);
	   	 	
	 }
	 
	 public void shareMedia (File mediaFile)
	 {
			Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse(mediaFile.getPath()));
	   	 	mActivity.startActivityForResult(intent,0);
	   	 	
	   	 	/*
	    	Intent intent = new Intent(Intent.ACTION_SEND);
	    	intent.setType(MIME_TYPE_MP4);
	    	intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(outFile.getPath()));
	    	startActivityForResult(Intent.createChooser(intent, "Share Video"),0); 
	    	*/    
			
	 }
 
	 public Bitmap getBitmapThumb (File file) throws IOException
	 {
	 	 Uri contentURI = Uri.fromFile(file);        
	      ContentResolver cr = mActivity.getContentResolver();
	      InputStream in = cr.openInputStream(contentURI);
	      BitmapFactory.Options options = new BitmapFactory.Options();
	      options.inSampleSize=8;
	      Bitmap thumb = BitmapFactory.decodeStream(in,null,options);
	      return thumb;
	 }
	 
	 public class MediaResult 
	 {
		 public String path;
		 public String mimeType;
	 }
	 
	 public MediaHelper.MediaResult handleResult (int requestCode, int resultCode, Intent intent, File fileTmp)
	 {
		 MediaResult result = null;
		 
		 if (requestCode == MediaConstants.GALLERY_RESULT) 
			{
				if (intent != null)
				{
					Uri uriGalleryFile = intent.getData();
					
					try
						{
							if (uriGalleryFile != null)
							{
								
								Cursor cursor = mActivity.managedQuery(uriGalleryFile, null, 
		                                null, null, null); 
								cursor.moveToNext();
								
								// Retrieve the path and the mime type
								result = new MediaResult();
								result.path = cursor.getString(cursor 
								                .getColumnIndex(MediaStore.MediaColumns.DATA)); 
								result.mimeType = cursor.getString(cursor 
								                .getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
								
								
								
							}
							
						}
					catch (Exception e)
					{
						result = null;
						Log.e(AppConstants.TAG, "error loading media: " + e.getMessage(), e);

					}
				}
				else
				{
					result = null;
	
				}
					
			}
			else if(requestCode == MediaConstants.CAMERA_RESULT) {
				
				//Uri uriCameraImage = intent.getData();			
				//Log.d(MediaAppConstants.TAG, "RETURNED URI FROM CAMERA RESULT: " + uriCameraImage.toString());
				//Uri uriCameraImage = Uri.fromFile(mFileTmp);
								
				String fileExtension = MimeTypeMap.getFileExtensionFromUrl(mMediaFileTmp.getAbsolutePath());
				
				result = new MediaResult();
				result.mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
				
				if (result.mimeType  == null)
				{
					if(mMediaUriTmp.getPathSegments().contains("video")) {
						result.mimeType  = MediaConstants.MIME_TYPE_MP4;
					} else if(mMediaUriTmp.getPathSegments().contains("images")) {
						result.mimeType  = MediaConstants.MIME_TYPE_JPEG;
					}
				}
				
				// TODO: IMPORTANTE!  Right here, we are forcing the media object to go through
				// the media scanner.  THIS MUST BE UNDONE at the end of the editing process
				// in order to maintain security/anonymity
				
				if((!mMediaFileTmp.exists()) && result.mimeType.equals(MediaConstants.MIME_TYPE_MP4)) {
					// write input stream to file
					FileOutputStream fos;
					try {
						
						fos = new FileOutputStream(mMediaFileTmp);
						InputStream media = mActivity.getContentResolver().openInputStream(mMediaUriTmp);
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
				
				mScanner = new MediaScannerConnection(mActivity, this);
				mScanner.connect();
				
			}
			else if(requestCode == MediaConstants.AUDIO_RESULT) {

				if (intent.getData() != null)
				{
					Uri uriMediaResult = intent.getData();
					
					if (uriMediaResult.getScheme().equalsIgnoreCase("content"))
					{
						Cursor cursor = mActivity.managedQuery(uriMediaResult, null, 
	                            null, null, null); 
						
						if (cursor.moveToNext())
						{
						
							// Retrieve the path and the mime type
							result = new MediaResult();
							result.path = cursor.getString(cursor 
							                .getColumnIndex(MediaStore.MediaColumns.DATA)); 
							result.mimeType = cursor.getString(cursor 
							                .getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
						}
					}
					
				}
					
			}
			else if(requestCode == MediaConstants.FILE_RESULT) {
				
				if (intent.getData() != null)
				{
					Uri uriMediaResult = intent.getData();
					
					if (uriMediaResult.getScheme().equalsIgnoreCase("content"))
					{
						
						Cursor cursor = mActivity.managedQuery(uriMediaResult, null, 
	                            null, null, null);
						
						if (cursor.moveToNext())
						{						
							
							// Retrieve the path and the mime type
							result = new MediaResult();
							result.path = new String(cursor.getBlob(cursor 
							                .getColumnIndex(MediaStore.MediaColumns.DATA))); 
							result.mimeType = cursor.getString(cursor 
							                .getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
							
							if (intent.getDataString().indexOf("#")!=-1)
								result.path = new File(uriMediaResult.getPath() + '#' + uriMediaResult.getFragment()).getAbsolutePath();
							
							if (result.mimeType == null)
								result.mimeType = MediaConstants.MIME_TYPE_ANY;
						}
							
					}
					else if (uriMediaResult.getScheme().equalsIgnoreCase("file"))
					{
						result = new MediaResult();
						result.path = new File(intent.getDataString()).getAbsolutePath();
						
						String fileExtension = MimeTypeMap.getFileExtensionFromUrl(intent.getDataString());
						result.mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
						
						if (result.mimeType == null)
							result.mimeType = MediaConstants.MIME_TYPE_ANY;
					}

					
				}
			}
		 
		 return result;
	 }
	 
	 @Override
	public void onMediaScannerConnected() {
		
		 
		 mScanner.scanFile(mMediaFileTmp.getAbsolutePath(), null);
		 
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		
		mScanner.disconnect();
		//Log.d(MediaAppConstants.TAG, "new path: " + path + "\nnew uri for path: " + uri.toString());
		
		 Message msg = mHandler.obtainMessage(5);
	     msg.getData().putString("path", path);    
	     mHandler.sendMessage(msg);

	}
}
