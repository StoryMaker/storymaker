package info.guardianproject.mrapp.model;

import org.json.JSONObject;


public class Lesson {

	public String mTitle;
	public String mDescription;
	public String mResourcePath; //file, http, asset
	public String mImage;

	public static Lesson parse (String jsonTxt) throws Exception
	{
		Lesson result = new Lesson();
		
		JSONObject jobj= new JSONObject(jsonTxt);
		jobj = jobj.getJSONObject("lesson");
		
		result.mTitle = jobj.getString("title");
		result.mDescription = jobj.getString("description");
		result.mImage = jobj.getString("image");
		
		result.mResourcePath = jobj.getJSONObject("resource").getString("url");
		
	    return result;

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