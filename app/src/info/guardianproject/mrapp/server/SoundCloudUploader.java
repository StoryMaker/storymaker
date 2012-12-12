package info.guardianproject.mrapp.server;

import java.io.File;

import org.holoeverywhere.app.Activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

public class SoundCloudUploader {

	public final static String BASE_URL = "https://soundcloud.com/";
	
	public static String buildSoundCloudURL (String username, File myAudioFile, String title)
	{	
		String result = BASE_URL + username + '/';
		
		/*
		String fileName = myAudioFile.getName();
		fileName = fileName.substring(0,fileName.indexOf("."));
		fileName = fileName.replace(' ', '_');
		
		result += fileName;
		*/
		String titlePath = title.replace(' ', '_').replace(".", "").replace("!", "");
		result += titlePath;
		
		return result;
	}
	
	public static void uploadSound (File myAudiofile, String title, String desc, int REQCODE, Activity activity)
	{
		Intent intent = new Intent("com.soundcloud.android.SHARE")
		  .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(myAudiofile))
		  .putExtra("com.soundcloud.android.extra.title", title)
		  .putExtra("com.soundcloud.android.extra.description", desc);
		  // more metadata can be set, see below

		try {
		    // takes the user to the SoundCloud sharing screen
			activity.startActivityForResult(intent, REQCODE);
		} catch (ActivityNotFoundException e) {
		    // SoundCloud Android app not installed, show a dialog etc.
		}
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
