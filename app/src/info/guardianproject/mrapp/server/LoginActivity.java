package info.guardianproject.mrapp.server;


import info.guardianproject.mrapp.Home;
import info.guardianproject.mrapp.MediaAppConstants;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.StoryMakerApp;

import java.net.MalformedURLException;

import com.WazaBe.HoloEverywhere.widget.Toast;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
 
public class LoginActivity extends com.WazaBe.HoloEverywhere.sherlock.SActivity implements Runnable 
{
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.activity_login);
 
        getSupportActionBar().hide();
        
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new OnClickListener ()
        {

			@Override
			public void onClick(View v) {
				
				handleLogin ();
				
			}
        	
        });
        
        TextView registerScreen = (TextView) findViewById(R.id.link_to_register);
 
        // Listening to register new account link
        registerScreen.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View v) {
                // Switching to Register screen
               // Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                //startActivity(i);
            	StoryMakerApp.getServerManager().createAccount(LoginActivity.this);
            }
        });
        
        TextView skipScreen = (TextView) findViewById(R.id.link_to_skip);
        
        // Listening to skip link
        skipScreen.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View v) {
              
            	 loginSuccess ();
            }
        });
    }
    
    private void handleLogin ()
    {
    	new Thread(this).start();
    }
    
    public void run ()
    {
    	String username = ((EditText)findViewById(R.id.login_username)).getText().toString();
    	String password = ((EditText)findViewById(R.id.login_password)).getText().toString();
    	
    	try {
			StoryMakerApp.getServerManager().connect(username, password);

			mHandler.sendEmptyMessage(0);
	         
		} catch (Exception e) {
			
			Message msgErr= mHandler.obtainMessage(1);
			msgErr.getData().putString("err",e.getLocalizedMessage());
			mHandler.sendMessage(msgErr);
			Log.e(MediaAppConstants.TAG,"login err",e);
		}
    }
    
    private Handler mHandler = new Handler ()
    {

		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what)
			{
				case 0:
					loginSuccess();
					break;
				case 1:
					loginFailed(msg.getData().getString("err"));
				default:
			}
		}
    	
    };
    
    private void loginFailed (String err)
    {
    	Toast.makeText(this, "Login failed: " + err, Toast.LENGTH_LONG).show();
    }
    
    private void loginSuccess ()
    {
   	 Intent i = new Intent(getApplicationContext(), Home.class);
     startActivity(i);
    }
}