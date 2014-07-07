package info.guardianproject.mrapp.api;



import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;

public class APIFunctions {
	
	private JSONParser jsonParser;
	private static String loginURL = "login.php";
	private static String reportURL = "article.php";
	
	private static String api_base_url = "http://listeningpost.codeforafrica.net/sm-api/";
	private static String api_key = "2a80f2f6094f4e3d2e7b01ba21951f3720060454";
	
	// constructor
	public APIFunctions(){
		jsonParser = new JSONParser();
	}
	
	public JSONObject loginUser(String firstname, String lastname, String username, String email, String location){		
		// Building Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("firstname", firstname));
		params.add(new BasicNameValuePair("lastname", lastname));
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("location", location));
		params.add(new BasicNameValuePair("api_key", api_key));

		JSONObject json = jsonParser.getJSONFromUrl(api_base_url + loginURL, params);
		
		// return json
		return json;
	}

	public JSONObject newReport(String token, String user_id, String title,
			String lat, String lon, String date, String description) {
		// Building Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("token", token));
		params.add(new BasicNameValuePair("user_id", user_id));
		params.add(new BasicNameValuePair("title", title));
		params.add(new BasicNameValuePair("location", lon +", "+ lat));
		params.add(new BasicNameValuePair("date", date));
		params.add(new BasicNameValuePair("description", description));
		params.add(new BasicNameValuePair("api_key", api_key));

		JSONObject json = jsonParser.getJSONFromUrl(api_base_url + reportURL, params);
		
		// return json
		return json;
	}
	
}