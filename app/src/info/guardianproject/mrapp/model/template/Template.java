package info.guardianproject.mrapp.model.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

/**
 * @author n8fr8
 *
 */
public class Template {

    public String mTitle;
	ArrayList<Scene> mArrayScenes;
	
	private Template ()
	{
		
	}
	
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
	
	/**
	 * @param context
	 * @param assetPathTemplate the path to the multi-scene template, that has no clips
	 * @param assetPathScene the path to the specific scene template with clips to use for each scene
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static Template parseAsset(Context context, String assetPathTemplate, String assetPathScene) throws IOException, JSONException
	{
		Template template = parseAsset(context, assetPathTemplate);
		Template templateScene = parseAsset(context, assetPathScene);
		
		for (Scene scene : template.getScenes())
		{
			if (templateScene.getScenes().size() > 0)
				scene.setClips(templateScene.getScene(0).getClips());
		}
		
		return template;
	}
	
	/**
	 * @param context
	 * @param assetPath a complete template with scene and clips
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static Template parseAsset(Context context, String assetPath) throws IOException, JSONException
	{
		Template result = new Template ();
		
		StringBuffer jsonsb = new StringBuffer();
		
		Iterator<String> it = IOUtils.readLines(context.getAssets().open(assetPath)).iterator();
		while (it.hasNext()) {
			jsonsb.append(it.next());
		}
		
		JSONObject jobjTemplate = new JSONObject(jsonsb.toString());
		
        if (!jobjTemplate.isNull("title")) {
            result.mTitle = jobjTemplate.getString("title");
            
            int resId = context.getResources().getIdentifier(result.mTitle, "string", context.getPackageName());
            
            if (resId != 0)
            	result.mTitle = context.getString(resId);
        }
		
		JSONArray jarrayScenes = jobjTemplate.getJSONArray("scenes");
		
		JSONArray jarrayClips;

		for (int scenesIdx = 0; scenesIdx < jarrayScenes.length() && (!jarrayScenes.isNull(scenesIdx)); scenesIdx++)
        {
		    Scene scene = new Scene();

		    JSONObject jobjScene = jarrayScenes.getJSONObject(scenesIdx);

            if (!jobjScene.isNull("title")) {
                scene.mTitle = jobjScene.getString("title");
                
                int resId = context.getResources().getIdentifier(scene.mTitle, "string", context.getPackageName());
                
                if (resId != 0)
                	scene.mTitle = context.getString(resId);
            }
            
            if (!jobjScene.isNull("description")) {
                scene.mDescription = jobjScene.getString("description");
                
                int resId = context.getResources().getIdentifier(scene.mDescription, "string", context.getPackageName());
                
                if (resId != 0)
                	scene.mDescription = context.getString(resId);
            }

            if (!jobjScene.isNull("clips"))
            {
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
            }
            
    		result.addScene(scene);
        }
		
		return result;
	}
}
