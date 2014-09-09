package org.codeforafrica.timby.listeningpost.facebook;

import org.codeforafrica.timby.listeningpost.BaseActivity;
import org.codeforafrica.timby.listeningpost.HomePanelsActivity;
import org.codeforafrica.timby.listeningpost.R;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
 
public class UpdateActivity extends BaseActivity {
	EditText registerUsername;
	EditText first_name;
	EditText last_name;
	EditText email;
	EditText phone_number;
	EditText location;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to register.xml
        setContentView(R.layout.activity_update_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        registerUsername = (EditText)findViewById(R.id.registerUsername);
        first_name = (EditText)findViewById(R.id.first_name);
        last_name = (EditText)findViewById(R.id.last_name);
        email = (EditText)findViewById(R.id.email);
        phone_number = (EditText)findViewById(R.id.phone_number);
        location = (EditText)findViewById(R.id.location);
        
        setValues();
        
    }
    public void setValues(){
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	registerUsername.setText(prefs.getString("username", ""));
    	first_name.setText(prefs.getString("firstname", ""));
    	last_name.setText(prefs.getString("lastname", ""));
    	email.setText(prefs.getString("email", ""));
    	phone_number.setText(prefs.getString("phone_number", ""));
    	location.setText(prefs.getString("location", ""));

    	
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
        {
        	Intent fl = new Intent(getApplicationContext(), HomePanelsActivity.class);
            fl.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	startActivity(fl);
            return true;
        }
        
        return super.onOptionsItemSelected(item);

  }
}