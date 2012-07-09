/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package info.guardianproject.mrapp;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class MediaOutputPreferences 
		extends PreferenceActivity  {

	
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.mediaoutprefs);
		
	}
	
	
	@Override
	protected void onResume() {
	
		super.onResume();
	
		
		
	};
	
	
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		
		//Log.d(getClass().getName(),"Exiting Preferences");
	}

	
}
