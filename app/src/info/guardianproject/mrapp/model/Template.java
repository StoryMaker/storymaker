package info.guardianproject.mrapp.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class Template {

	ArrayList<Clip> mArrayClips;
	
	public ArrayList<Clip> getClips()
	{
		return mArrayClips;
	}
	
	public void parseAsset (Context context, String assetPath) throws IOException, JSONException
	{
		
		StringBuffer jsonsb = new StringBuffer();
		
		Iterator<String> it = IOUtils.readLines(context.getAssets().open(assetPath)).iterator();
		while (it.hasNext())
			jsonsb.append(it.next());
		
		JSONObject jObjMain = new JSONObject(jsonsb.toString());
		JSONArray jarray = jObjMain.getJSONArray("clips");
		
		mArrayClips = new ArrayList<Clip>();
		
		for (int i = 0; i < jarray.length() && (!jarray.isNull(i)); i++)
		{
			Clip clip = new Clip();
			
			JSONObject jobj = jarray.getJSONObject(i);
			clip.mTitle = jobj.getString("Title");
			clip.mArtwork = jobj.getString("Artwork");
			clip.mShotSize = jobj.getString("Shot Size");
			
			if (clip.mShotSize != null)
			{
				if (clip.mShotSize.equals("Close"))
					clip.mShotType = 0;
				else if (clip.mShotSize.equals("Detail"))
					clip.mShotType = 1;
				else if (clip.mShotSize.equals("Long"))
					clip.mShotType = 2;
				else if (clip.mShotSize.equals("Medium"))
					clip.mShotType = 3;
				else if (clip.mShotSize.equals("Wide"))
					clip.mShotType = 4;
			}
	        
			clip.mGoal = jobj.getString("Goal");
			clip.mLength = jobj.getString("Length");
			clip.mDescription = jobj.getString("Description");
			clip.mTip = jobj.getString("Tip");
			clip.mSecurity = jobj.getString("Security Concern");
			
			mArrayClips.add(clip);
		}
	}
	
	public class Clip
	{
		public String mTitle;
		public String mArtwork;
		public String mShotSize;
		public int mShotType;
		public String mGoal;
		public String mLength;
		public String mDescription;
		public String mTip;
		public String mSecurity;
		
		/*
		"Clip": "Signature",
		"Artwork": "???.svg",
		"Shot Size": "Detail",
		"Goal": "Depict something noteworthy about the character.",
		"Length": "X seconds",
		"Description": "What does the character do for a living? Show the audience the most important element of the character. This element should fill at least 50% of the frame.",
		"Tip": "If you are too close to the action, your phone’s camera may not be able to focus. Keep the camera at arm’s length from your subject.",
		"Security Concern": "-
		*/
	}
}
