package org.codeforafrica.timby.listeningpost.facebook;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class FacebookLogin extends FragmentActivity{
	private MainFragment mainFragment;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    getActionBar().hide();
	    
	    if(savedInstanceState == null) {
	        // Add the fragment on initial activity setup
	        mainFragment = new MainFragment();
	        getSupportFragmentManager()
	        .beginTransaction()
	        .add(android.R.id.content, mainFragment)
	        .commit();
	    }else {
	        // Or set the fragment from restored state info
	        mainFragment = (MainFragment) getSupportFragmentManager()
	        .findFragmentById(android.R.id.content);
	    }
	}
	
}
