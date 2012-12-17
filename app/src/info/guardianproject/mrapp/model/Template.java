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
	
	public void addClip (Clip clip)
	{
	    mArrayClips.add(clip);
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
			
			if (!jobj.isNull("Artwork"))
				clip.mArtwork = jobj.getString("Artwork");
			
			if (!jobj.isNull("Shot Size"))
				clip.mShotSize = jobj.getString("Shot Size");
			
			if (clip.mArtwork != null)
			{

				if (clip.mArtwork.equals("cliptype_close"))
					clip.mShotType = 0;
				else if (clip.mArtwork.equals("cliptype_detail"))
					clip.mShotType = 1;
				else if (clip.mArtwork.equals("cliptype_long"))
					clip.mShotType = 2;
				else if (clip.mArtwork.equals("cliptype_medium"))
					clip.mShotType = 3;
				if (clip.mArtwork.equals("cliptype_wide"))
					clip.mShotType = 4;
				
			}
			else if (clip.mShotSize != null)
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
	
	
}
