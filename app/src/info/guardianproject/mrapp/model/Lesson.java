package info.guardianproject.mrapp.model;

import java.io.File;

import org.json.JSONObject;


public class Lesson {

	public String mTitle;
	public String mDescription;
	public String mResourcePath; //file, http, asset
	public String mImage;
	public int mStatus;
	public File mLocalPath;

	public final static int STATUS_NOT_STARTED = 0;
	public final static int STATUS_IN_PROGRESS = 1;
	public final static int STATUS_COMPLETE = 2;
	

	public static Lesson parse (String jsonTxt) throws Exception
	{
		Lesson result = new Lesson();
		
		JSONObject jobj= new JSONObject(jsonTxt);
		jobj = jobj.getJSONObject("lesson");
		
		result.mTitle = jobj.getString("title");
		result.mDescription = jobj.getString("description");
		//result.mImage = jobj.getString("image");
		
		result.mResourcePath = jobj.getJSONObject("resource").getString("url");
		
	    return result;

	}
	
	public String toString()
	{
		return mTitle;
	}
}
/**
{"lesson": {
"title": "Journalism Introduction",
"description": "what you need to know in order to know what you need",
"image": "journalism.jpg",
"published": "2012/09/01",
"author": "Joe Someone",
"resource": {
    "url": "index.html"        
}       
}}    
*/