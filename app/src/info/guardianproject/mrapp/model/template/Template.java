package info.guardianproject.mrapp.model.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;

public class Template {

    public String mTitle;
	ArrayList<Scene> mArrayScenes;
	
	public ArrayList<Scene> getScenes()
	{
		return mArrayScenes;
	}
	
	public void addScene(Scene scene)
	{
	    if (mArrayScenes == null) {
	        mArrayScenes = new ArrayList<Scene>();
	    }
	    
	    mArrayScenes.add(scene);
	}
	
	public Scene getScene(int idx) {
	    return mArrayScenes.get(idx);
	}
	
	public void parseAsset(Context context, String assetPath) throws IOException, JSONException
	{
		StringBuffer jsonsb = new StringBuffer();
		
		Iterator<String> it = IOUtils.readLines(context.getAssets().open(assetPath)).iterator();
		while (it.hasNext()) {
			jsonsb.append(it.next());
		}
		
		JSONObject jobjTemplate = new JSONObject(jsonsb.toString());
		
        if (!jobjTemplate.isNull("title")) {
            mTitle = jobjTemplate.getString("title");
        }
		
		JSONArray jarrayScenes = jobjTemplate.getJSONArray("scenes");
		
		JSONArray jarrayClips;

		for (int scenesIdx = 0; scenesIdx < jarrayScenes.length() && (!jarrayScenes.isNull(scenesIdx)); scenesIdx++)
        {
		    Scene scene = new Scene();

		    JSONObject jobjScene = jarrayScenes.getJSONObject(scenesIdx);

            if (!jobjScene.isNull("title")) {
                scene.mTitle = jobjScene.getString("title");
                
                int resId = Resources.getSystem().getIdentifier(scene.mTitle, "string", null);
                
                if (resId != 0)
                	scene.mTitle = context.getString(resId);
            }
            
            if (!jobjScene.isNull("description")) {
                scene.mDescription = jobjScene.getString("description");
                
                int resId = Resources.getSystem().getIdentifier(scene.mDescription, "string", null);
                
                if (resId != 0)
                	scene.mDescription = context.getString(resId);
            }

	        jarrayClips = jobjScene.getJSONArray("clips");
		    
    		for (int clipsIdx = 0; clipsIdx < jarrayClips.length() && (!jarrayClips.isNull(clipsIdx)); clipsIdx++)
    		{
    			Clip clip = new Clip();
    			
    			JSONObject jobjClip = jarrayClips.getJSONObject(clipsIdx);
    			clip.mTitle = jobjClip.getString("Title");
    			
    			if (!jobjClip.isNull("Artwork")) {
    				clip.mArtwork = jobjClip.getString("Artwork");
    			}
    			
    			if (!jobjClip.isNull("Shot Size")) {
    				clip.mShotSize = jobjClip.getString("Shot Size");
    			}
    			
    			if (clip.mArtwork != null) {
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
    				
    			} else if (clip.mShotSize != null) {
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
    	        
    			clip.mGoal = jobjClip.getString("Goal");
    			clip.mLength = jobjClip.getString("Length");
    			clip.mDescription = jobjClip.getString("Description");
    			clip.mTip = jobjClip.getString("Tip");
    			clip.mSecurity = jobjClip.getString("Security Concern");
    			
    			scene.addClip(clip);
    		}
    		addScene(scene);
        }
	}
}
