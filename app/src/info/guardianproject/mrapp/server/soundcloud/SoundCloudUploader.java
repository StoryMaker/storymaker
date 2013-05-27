package info.guardianproject.mrapp.server.soundcloud;

import info.guardianproject.mrapp.R;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Endpoints;
import com.soundcloud.api.Env;
import com.soundcloud.api.Params;
import com.soundcloud.api.Request;
import com.soundcloud.api.Token;


public class SoundCloudUploader {

//	public final static String BASE_URL = "https://soundcloud.com/";
	
	/*
	public static String buildSoundCloudURL (String username, File myAudioFile, String title)
	{	
		String result = BASE_URL + username + '/';
		
		String titlePath = title.replace(' ', '-').replace(".", "").replace("!", "");
		result += titlePath;
		
		return result;
	}*/
	
	private Handler mHandler;
	private File mAudioFile;
	private Context mContext;
	

	private long mFileSize = -1;
	
	public String uploadSound (File audioFile, String title, String desc, int REQCODE, Activity activity, Handler handler) throws OperationCanceledException, AuthenticatorException, IOException
	{
		mHandler = handler;
		mAudioFile = audioFile;
		mContext = activity.getBaseContext();
		
		mFileSize = audioFile.length();
		
		AccountManager accountManager = AccountManager.get(mContext.getApplicationContext());
		Account[] acc = accountManager.getAccountsByType("com.soundcloud.android.account");
		if (acc.length > 0) {
		    // possibly ask user for permission to obtain token
			//accountManager.getAuthToken(account, authTokenType, notifyAuthFailure, callback, handler)
			
		    String access = accountManager.blockingGetAuthToken(acc[0], "access_token", true);
			
		    if (access == null)
		    {
		    	Bundle options = new Bundle();
		    
	          	accountManager.getAuthToken(acc[0], "access_token", options, activity,  new AccountManagerCallback<Bundle>() {

					@Override
					public void run(AccountManagerFuture<Bundle> arg0) {
						// TODO Auto-generated method stub
						
					}}, handler);

	          	/*
		    	AccountManagerFuture<Bundle> bundleFuture = accountManager.getAuthToken(acc[0], "access_token", options, false, new AccountManagerCallback<Bundle>() {

					@Override
					public void run(AccountManagerFuture<Bundle> arg0) {
						// TODO Auto-generated method stub
						
					}}, mHandler);
				*/
	          	
		    	throw new IOException ("Please press 'Publish' again once you have authorized SoundCloud access");
		    }
		    
		    Token token = new Token(access, null, Token.SCOPE_NON_EXPIRING);
		    ApiWrapper wrapper = new ApiWrapper(null, null, null, token, Env.LIVE);

		    HttpResponse resp = wrapper.post(Request.to(Endpoints.TRACKS)
                    .withFile(Params.Track.ASSET_DATA, mAudioFile)
                    .add(Params.Track.TITLE, title)
                    .add(Params.Track.DESCRIPTION, desc)
                    .add(Params.Track.SHARING, Params.Track.PUBLIC)
                    .setProgressListener(new Request.TransferProgressListener() {
                        @Override
                        public void transferred(long l) throws IOException {
                        	
                        	  double percent = (l / mFileSize) * 100;

                        	  String status = String.format ("%,d/%,d bytes transfered",  Math.round(l),  Math.round(mFileSize));

                       	   
                        	   Message msg = mHandler.obtainMessage(888);
                               
                               String title = mContext.getString(R.string.uploading);
                               
                               msg.getData().putString("statusTitle", title);
                               msg.getData().putString("status", status);
                               msg.getData().putInt("progress", (int)percent);
                               mHandler.sendMessage(msg);
                               
                        }
                    }));
		    
		    if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
		    	
		    	String location = resp.getFirstHeader("Location").getValue();
		    	return location;
            } else {
                System.err.println("Invalid status received: " + resp.getStatusLine());
                
            }
		    
		    
		    // do something with resp
		} 
		
		return null;
		/*
		String clientId = "storymaker";
		
		Intent intent = new Intent("com.soundcloud.android.SHARE")
		  .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(myAudiofile))
		  .putExtra("com.soundcloud.android.extra.title", title)
		  .putExtra("com.soundcloud.android.extra.description", desc)
		  .putExtra("com.soundcloud.android.extra.public", true)
		  .putExtra("com.soundcloud.android.extra.tags", new String[] {
                  "soundcloud:created-with-client-id="+clientId
                  });
		
		  // more metadata can be set, see below

		try {
		    // takes the user to the SoundCloud sharing screen
			activity.startActivityForResult(intent, REQCODE);
		} catch (ActivityNotFoundException e) {
		    // SoundCloud Android app not installed, show a dialog etc.
		}*/
	}
	/*
	 * title	String	the title of the track
where	String	the location of the recording
description	String	description of the recording
public	boolean	if the track should be public or not
location	Location	the location
tags	String[]	tags for the track
genre	String	the location of the recording
artwork	Uri	artwork to use for this track (needs to be file schema)
	 */
	
	public static boolean isCompatibleSoundCloudInstalled(Context context) {
	    try {
	        PackageInfo info = context.getPackageManager()
	                                  .getPackageInfo("com.soundcloud.android",
	                PackageManager.GET_META_DATA);

	        // intent sharing only got introduced with version 22
	        return info != null && info.versionCode >= 22;
	    } catch (PackageManager.NameNotFoundException e) {
	        // not installed at all
	        return false;
	    }
	}
	
	public static void installSoundCloud (Context context)
	{
		context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.soundcloud.android")));

	}
}
