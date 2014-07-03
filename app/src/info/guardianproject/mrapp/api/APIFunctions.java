package info.guardianproject.mrapp.api;



import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONObject;

public class APIFunctions {
	
	private JSONParser jsonParser;
	private static String loginURL = "users/login";

	private static String api_base_url = "http://23.251.135.167/api/";
	private static String api_key = "6b239b3568b209";
	
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
	
}