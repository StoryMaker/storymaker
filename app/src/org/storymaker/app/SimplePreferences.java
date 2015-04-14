/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package org.storymaker.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


public class SimplePreferences extends LockablePreferenceActivity implements OnSharedPreferenceChangeListener {

	public static final String KEY_VIDEO_RESOLUTION = "p_video_resolution";
	public static final String KEY_VIDEO_WIDTH = "p_video_width";
	public static final String KEY_VIDEO_HEIGHT = "p_video_height";

    public static final String KEY_USE_TOR = "pusetor";
    public static final String KEY_USE_MANAGER = "pusedownloadmanager";

    public static final String KEY_LANGUAGE = "pintlanguage";

	public static final int MAX_VIDEO_WIDTH = 1920;
	public static final int MAX_VIDEO_HEIGHT = 1080;
	
	
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.simpleprefs);
		
		setResult(RESULT_OK);
		
		Preference prefVideoWidth = (Preference) getPreferenceScreen().findPreference(KEY_VIDEO_WIDTH);	
		prefVideoWidth.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() 
		{
		    public boolean onPreferenceChange(Preference preference, Object newValue) 
		    {
		    	int vWidth = Integer.parseInt(newValue.toString());
		    	
		    	if(vWidth > MAX_VIDEO_WIDTH)
				{
					Toast.makeText(getApplicationContext(), "Width must be less than 1920.", Toast.LENGTH_SHORT).show();
					return false;
				}
		    	else
		    	{
		    		return true;
		    	}
		    }
		});
		
		Preference prefVideoHeight = (Preference) getPreferenceScreen().findPreference(KEY_VIDEO_HEIGHT);	
		prefVideoHeight.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() 
		{
		    public boolean onPreferenceChange(Preference preference, Object newValue) 
		    {
		    	int vHeight = Integer.parseInt(newValue.toString());

		    	if(vHeight > MAX_VIDEO_HEIGHT)
				{
					Toast.makeText(getApplicationContext(), "Height must be less than 1080.", Toast.LENGTH_SHORT).show();
					return false;
				}
		    	else
		    	{
		    		return true;
		    	}
		    }
		});

        Preference prefUseTor = (Preference)getPreferenceScreen().findPreference(KEY_USE_TOR);
        prefUseTor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                boolean useTor = Boolean.parseBoolean(newValue.toString());

                if(useTor) {
                    //SharedPreferences settings = ((Preference)getPreferenceScreen().findPreference(KEY_USE_MANAGER)).getSharedPreferences();
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    boolean useManager = settings.getBoolean(KEY_USE_MANAGER, false);

                    if (useManager) {
                        Toast.makeText(getApplicationContext(), "Can't select both \"Use Orbot\" and \"Use Download Manager\"", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                return true;
            }
        });

        Preference prefUseManager = (Preference)getPreferenceScreen().findPreference(KEY_USE_MANAGER);
        prefUseManager.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                boolean useManager = Boolean.parseBoolean(newValue.toString());

                if(useManager) {
                    //SharedPreferences settings = ((Preference)getPreferenceScreen().findPreference(KEY_USE_MANAGER)).getSharedPreferences();
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    boolean useTor = settings.getBoolean(KEY_USE_TOR, false);

                    if (useTor) {
                        Toast.makeText(getApplicationContext(), "Can't select both \"Use Orbot\" and \"Use Download Manager\"", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                return true;
            }
        });
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener((OnSharedPreferenceChangeListener) this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				
	    
		if (key.equals(KEY_VIDEO_RESOLUTION)) 
		{
	    	
			int videoRes = Integer.parseInt(sharedPreferences.getString(KEY_VIDEO_RESOLUTION, "0"));
			int vWidth = Integer.parseInt(sharedPreferences.getString(KEY_VIDEO_WIDTH, AppConstants.DEFAULT_WIDTH+""));
			int vHeight = Integer.parseInt(sharedPreferences.getString(KEY_VIDEO_HEIGHT, AppConstants.DEFAULT_HEIGHT+""));
			
	    	switch (videoRes)
	    	{
		        case 1080:
		        	vWidth = 1920;
	        		vHeight = 1080;
	            	break;
		        case 720:
		        	vWidth = 1280;
		        	vHeight = 720;
	        		break;
		        case 480:
		        	vWidth = 720;
		        	vHeight = 480;
	             	break;
		        case 360:
		        	vWidth = 640;
		        	vHeight = 360;
	        		break;
	    	}
	    	
	    	sharedPreferences.edit().putString(KEY_VIDEO_WIDTH, Integer.toString(vWidth)).commit();	    	
	    	sharedPreferences.edit().putString(KEY_VIDEO_HEIGHT, Integer.toString(vHeight)).commit();
        }
	    // force update so language settings are applied
        else if (key.equals(KEY_LANGUAGE))
        {
            ((StoryMakerApp)getApplication()).checkLocale();
//            restartActivity();
            restartApp(getApplicationContext());
        }
	}

	public static void restartApp(final Context c) {
        Toast.makeText(c, R.string.restarting_storymaker, Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable(){
            public void run() {
                doRestart(c);
          }}, 500);
	}

	// from: http://stackoverflow.com/a/22345538/41694
	public static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                // fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        Log.e("SimplePreferences", "Was not able to restart application, mStartActivity null");
                    }
                } else {
                    Log.e("SimplePreferences", "Was not able to restart application, PM null");
                }
            } else {
                Log.e("SimplePreferences", "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e("SimplePreferences", "Was not able to restart application");
        }
    }
}
