/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package info.guardianproject.mrapp;

import org.holoeverywhere.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;


public class SimplePreferences extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {

	public static final String KEY_VIDEO_RESOLUTION = "p_video_resolution";
	public static final String KEY_VIDEO_WIDTH = "p_video_width";
	public static final String KEY_VIDEO_HEIGHT = "p_video_height";

    public static final String KEY_LANGUAGE = "pintlanguage";
    public static final String KEY_LESSON = "pleslanguage";
	
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
        }
        else if (key.equals(KEY_LESSON))
        {
            ((StoryMakerApp)getApplication()).updateLessonLocation();
        }
	}
}
