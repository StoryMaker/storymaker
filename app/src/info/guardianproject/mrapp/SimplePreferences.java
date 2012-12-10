/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package info.guardianproject.mrapp;

import org.holoeverywhere.preference.PreferenceActivity;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import android.os.Bundle;


public class SimplePreferences 
		extends SherlockPreferenceActivity  {

	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.simpleprefs);
		
		setResult(RESULT_OK);
	}
	
	
	
}
