package info.guardianproject.mrapp.model;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;


public class Lesson {

	public String mTitle;
	public String mDescription;
	public String mResourceUrl;
	public String mImage;

	public static Lesson parse (String jsonTxt) throws Exception
	{
		Lesson result = new Lesson();
		
		JSONObject json = (JSONObject) JSONSerializer.toJSON( jsonTxt );
		
		result.mTitle = json.getString("title");
		result.mDescription = json.getString("description");
		result.mImage = json.getString("image");
	    
	    JSONObject resource = json.getJSONObject("resource");
	    result.mResourceUrl = resource.getString("url");
	    
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