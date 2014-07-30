package org.codeforafrica.timby.listeningpost.facebook;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.codeforafrica.timby.listeningpost.ConnectionDetector;
import org.codeforafrica.timby.listeningpost.HomeActivity;
import org.codeforafrica.timby.listeningpost.R;
import org.codeforafrica.timby.listeningpost.api.APIFunctions;
public class RegisterActivity extends Activity implements Runnable{
	Button btnRegister;
	Button btnLinkToLogin;
	EditText username;
	EditText rpassword;
	EditText cPassword;
	EditText email;
	EditText first_name;
	EditText last_name;
	EditText phone_number;
	EditText location;
	TextView txtStatus;
	TextView registerErrorMsg;
	
	// JSON Response node names
	private static String KEY_SUCCESS = "result";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "message";
	private static String KEY_UID = "uid";
	private static String KEY_USERNAME = "username";
	private static String KEY_CREATED_AT = "created_at";
	
	// Connection detector class
    ConnectionDetector cd;
    //flag for Internet connection status
    Boolean isInternetPresent = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to register.xml
        setContentView(R.layout.activity_registration);
        txtStatus = (TextView)findViewById(R.id.reg_error);

    	// Importing all assets like buttons, text fields
		username = (EditText) findViewById(R.id.registerUsername);
		rpassword = (EditText) findViewById(R.id.registerPassword);
		cPassword = (EditText) findViewById(R.id.confirmPassword);
		email = (EditText) findViewById(R.id.email);
		first_name = (EditText) findViewById(R.id.first_name);
		last_name = (EditText) findViewById(R.id.last_name);
		location = (EditText) findViewById(R.id.location);
		
		phone_number = (EditText) findViewById(R.id.phone_number);
		
		//Get phone number
		TelephonyManager tMgr = (TelephonyManager)getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
		String mPhoneNumber = tMgr.getLine1Number();
		phone_number.setText(mPhoneNumber);
		
		btnRegister = (Button) findViewById(R.id.btnRegister);
		btnLinkToLogin = (Button) findViewById(R.id.btnLogin);
		
		// Register Button Click event
		btnRegister.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View view) {
	     	    	String p = rpassword.getText().toString();
					String p2 = cPassword.getText().toString();
	         	if(!p.equals(p2)){
					Toast.makeText(getBaseContext(), "Passwords not matching!", Toast.LENGTH_LONG).show();
					}else{   
	                 handleRegistration ();
					}
			}
		});

		// Link to Login Screen
		btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(),
						FacebookLogin.class);
				startActivity(i);
				// Close Registration View
				finish();
			}
		});
	}
    public void run ()
    {
      	// creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());
        
		//get Internet status
        isInternetPresent = cd.isConnectingToInternet();
        
        if(!isInternetPresent){
            	Toast.makeText(getApplicationContext(), "Check your internet connection!", Toast.LENGTH_LONG).show();          
        }else{
        	String Vusername = username.getText().toString();
        	String Vpassword = rpassword.getText().toString();
    		String Vemail = email.getText().toString();
    		String Vfirst_name = first_name.getText().toString();
    		String Vlast_name = last_name.getText().toString();
    		String Vlocation = location.getText().toString();
    		String Vphone_number = phone_number.getText().toString();
    		
            APIFunctions userFunction = new APIFunctions();
            JSONObject json = userFunction.registerUser(Vusername, Vpassword, Vemail, Vfirst_name, Vlast_name, Vlocation, Vphone_number);
				try {
						String res = json.getString("status"); 
						if(res.equals("OK")){
							mHandler.sendEmptyMessage(0);
						}else{
							Message msgErr= mHandler.obtainMessage(1);
	                        msgErr.getData().putString("err",json.getString("error"));
	                        mHandler.sendMessage(msgErr);
						}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
        	
        }
    }

    private Handler mHandler = new Handler ()
    {
    	@Override
                public void handleMessage(Message msg) {
                        
                        switch (msg.what)
                        {
                                case 0:
                                        registrationSuccess();
                                        break;
                                case 1:
                                        registrationFailed(msg.getData().getString("err"));
                                default:
                        }
                }
            
    };
    private void handleRegistration ()
    {
            txtStatus.setText("Attempting registration...");
            
            new Thread(this).start();
    }
    private void registrationFailed (String err)
    {
            txtStatus.setText(err);
            
    }
    
    private void registrationSuccess ()
    {
    	Toast.makeText(getApplicationContext(), "Registration successfull!", Toast.LENGTH_LONG).show();
    	
    	Intent intent = new Intent(RegisterActivity.this, FacebookLogin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        
        finish();
    }
}