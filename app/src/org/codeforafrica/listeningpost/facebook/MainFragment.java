package org.codeforafrica.listeningpost.facebook;

import java.util.Arrays;

import org.codeforafrica.listeningpost.HomePanelsActivity;
import org.codeforafrica.listeningpost.R;
import org.codeforafrica.listeningpost.api.APIFunctions;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.LinearLayout;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphLocation;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MainFragment extends Fragment {
	private static final String TAG = "MainFragment";
	private UiLifecycleHelper uiHelper;
	String location = "";
	String username = "";
	String id = ""; 
    String firstname = ""; 
    String lastname = "";
    String email = "";
    View view;
    LoginButton authButton;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	}
	@Override
    public View onCreateView(LayoutInflater inflater, 
        ViewGroup container, 
        Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_login_facebook, container, false);
        authButton = (LoginButton) view.findViewById(R.id.authButton);
        authButton.setFragment(this);

        authButton.setReadPermissions(Arrays.asList("email", "user_location")); 
               
        return view;
    }
	class userRegister extends AsyncTask<String, String, String>{
		@Override
        protected void onPreExecute() {
            super.onPreExecute(); 
        }
        protected String doInBackground(String... args) {
        	APIFunctions apiFunctions = new APIFunctions();
			
			
    		JSONObject json = apiFunctions.loginUser(firstname, lastname, username, email, location);
    		try {
				String res = json.getString("status"); 
				if(res.equals("OK")){
					JSONObject json_user = json.getJSONObject("message");
					
					String user_id = json_user.getString("user_id");
					String token = json_user.getString("token");
					String api_key = json_user.getString("api_key");
					
					//login successful
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
			        Editor editor = settings.edit();
					editor.putString("logged_in", "1");
					editor.putString("api_key", api_key);
					editor.putString("token", token);
					editor.putString("user_id", user_id);
					
					//save profule information 
					editor.putString("location", location);
					editor.putString("username", username);
					editor.putString("firstname", firstname);
					editor.putString("lastname", lastname);
					editor.putString("email", email);
					
					editor.commit();
			        
					if(!getActivity().getIntent().hasExtra("logout")){
						Intent i = new Intent(getActivity(), HomePanelsActivity.class);
						startActivity(i);
					}
				}else{
					//login not successful: what to do?
					
				}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}				
	        
        	return null;
        }
        protected void onPostExecute(String file_url) {
            
        }
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	        
	        // make request to the /me API
	          Request.newMeRequest(session, new Request.GraphUserCallback() {

	            // callback after Graph API response with user object
	           
				@Override
				public void onCompleted(GraphUser user, Response response) {
					// TODO Auto-generated method stub
					if(user.getProperty("email").toString()!=null){
						location = "";//null, Wait for FB approval: user.getLocation().getCountry();
						firstname = user.getFirstName();
						lastname = user.getLastName();		
						username = user.getName().replace(" ", "").toLowerCase();
						email = user.getProperty("email").toString();
						Log.d("Email", "Email:" + email);
						new userRegister().execute();
					}
				}
	          }).executeAsync();
	          
	       //if registered
	        	//redirect to home
	        
	       //else
	        	//capture additional details
			      //set values
	          /*
	          		EditText username_e = (EditText) view.findViewById(R.id.registerUsername);
	          		username_e.setText(username);
			        EditText email_e = (EditText) view.findViewById(R.id.email);
			        email_e.setText(email);
			        EditText first_name = (EditText) view.findViewById(R.id.first_name);
			        first_name.setText(firstname);
			        EditText last_name = (EditText) view.findViewById(R.id.last_name);
			        last_name.setText(lastname);
			        EditText e_location = (EditText) view.findViewById(R.id.location);
				    e_location.setText(location);		
			        EditText phone_number = (EditText) view.findViewById(R.id.phone_number);
				    
			        //Get phone number
		    		TelephonyManager tMgr = (TelephonyManager)getActivity().getSystemService(getActivity().TELEPHONY_SERVICE);
		    		String mPhoneNumber = tMgr.getLine1Number();
		    		phone_number.setText(mPhoneNumber);
		    		
			        //authButton.setVisibility(View.GONE);
			        LinearLayout regLayout = (LinearLayout)view.findViewById(R.id.regLayout);
			        regLayout.setVisibility(View.VISIBLE);*/
	        
	    } else if (state.isClosed()) {
	        Log.i(TAG, "Logged out...");
	    }
	}
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	@Override
	public void onResume() {
	    super.onResume();
	 // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }

	    uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
}