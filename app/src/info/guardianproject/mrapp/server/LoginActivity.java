package info.guardianproject.mrapp.server;


import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.guardianproject.mrapp.AppConstants;
import info.guardianproject.mrapp.BaseActivity;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.StoryMakerApp;
import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.AuthTable;
import net.bican.wordpress.Category;

import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import redstone.xmlrpc.XmlRpcFault;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;
 
public class LoginActivity extends BaseActivity implements Runnable 
{
	
	private ImageView viewLogo;
	private TextView txtStatus;
	private EditText txtUser;
	private EditText txtPass;
	List<Category> categories;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.activity_login);
        
        viewLogo = (ImageView)findViewById(R.id.logo);
        txtStatus = (TextView)findViewById(R.id.status);
        txtUser = (EditText)findViewById(R.id.login_username);
        txtPass = (EditText)findViewById(R.id.login_password);
        
        getCreds();
        
        getSupportActionBar().hide();
        
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new OnClickListener ()
        {

			@Override
			public void onClick(View v) {
				
				handleLogin ();
				
			}
        	
        });
        
        Button registerScreen = (Button) findViewById(R.id.link_to_register);
        // Listening to register new account link
        registerScreen.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View v) {
                // Switching to Register screen
               // Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                //startActivity(i);
            	StoryMakerApp.getServerManager().createAccount(LoginActivity.this);
            }
        });
        
        final CheckBox showPassword = (CheckBox)findViewById(R.id.showPassword);

		showPassword.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if(showPassword.isChecked()){
							txtPass.setInputType(InputType.TYPE_CLASS_TEXT);
				        }else{
				        	txtPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				        }
					}
				});
        Button skipScreen = (Button) findViewById(R.id.link_to_skip);
        
        // Listening to skip link
        skipScreen.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View v) {
              
            	 saveCreds("","");//skip login
            	 loginSuccess ();
            }
        });
    }
    
    private void handleLogin ()
    {
    	txtStatus.setText("Connecting to server...");
    	
    	new Thread(this).start();
    }
    
    private void saveCreds (String user, String pass)
    {   
        ArrayList<Auth> results = (new AuthTable()).getAuthsAsList(getApplicationContext(), Auth.STORYMAKER);
        for (Auth deleteAuth : results) {
        	// only a single username/password is stored at a time
        	deleteAuth.delete();
        }
		
        Auth storymakerAuth = new Auth(getApplicationContext(),
        		                       -1, // should be set to a real value by insert method
        		                       "StoryMaker.cc",
        		                       Auth.STORYMAKER,
        		                       user,
        		                       pass,
        		                       null,
        		                       new Date());
        storymakerAuth.save();
    } 
    
    private void getCreds()
    { 
        Auth storymakerAuth = (new AuthTable()).getAuthDefault(getApplicationContext(), Auth.STORYMAKER);
        if (storymakerAuth != null) {
        	txtUser.setText(storymakerAuth.getUserName());
        	txtPass.setText(storymakerAuth.getCredentials());
        } 
    }
    public void run ()
    {
    	String username = txtUser.getText().toString();
    	String password = txtPass.getText().toString();

    	try {
			StoryMakerApp.getServerManager().connect(username, password);

			// only store username/password for a successful login
	    	saveCreds(username, password);
	    	
	    	categories = StoryMakerApp.getServerManager().getCategories();
	    	Log.d("cats", "cats:"+categories.toString());
			mHandler.sendEmptyMessage(0);
	         
		} catch (Exception e) {
			
			Message msgErr= mHandler.obtainMessage(1);
			msgErr.getData().putString("err",e.getLocalizedMessage());
			mHandler.sendMessage(msgErr);
			Log.e(AppConstants.TAG,"login err",e);
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
    	txtStatus.setText(err);
    	//Toast.makeText(this, "Login failed: " + err, Toast.LENGTH_LONG).show();
    }
    
    private void loginSuccess ()
    {
    	updateCategories();
    	finish();
    }
    private void updateCategories(){
	
    	SharedPreferences prefs = PreferenceManager
		        .getDefaultSharedPreferences(getApplicationContext());
		JSONArray catsList = new JSONArray();

		for(int i=0;i<categories.size();i++){
			Category cat = categories.get(i);

			catsList.put(cat.getCategoryName());
		}
		
		Editor editor = prefs.edit();
		editor.putString("categories", catsList.toString());
		
		editor.commit();
    }
}
