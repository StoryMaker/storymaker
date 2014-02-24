package info.guardianproject.mrapp.model.json;

import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Scene;

import java.lang.reflect.Type;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SceneJson {
	
public static class SceneSerializerDeserializer implements JsonSerializer<Scene>, JsonDeserializer<Scene> {
		
		private Context mContext;
		
		public SceneSerializerDeserializer(Context context){
			mContext = context;
		}
		
		private Context getContext(){
			return mContext;
		}
		 
	    public JsonElement serialize(final Scene scene, final Type type, final JsonSerializationContext context) {
	        JsonObject result = new JsonObject();
	        result.add("id", new JsonPrimitive(scene.getId()));
	        result.add("projectIndex", new JsonPrimitive(scene.getProjectIndex()));
	        result.add("projectId", new JsonPrimitive(scene.getProjectId()));
	        result.add("clipCount", new JsonPrimitive(scene.getClipCount()));
	        
	        if(scene.getTitle() != null)
	        	result.add("title", new JsonPrimitive(scene.getTitle()));
	        if(scene.getThumbnailPath() != null)
	        	result.add("thumbnailPath", new JsonPrimitive(scene.getThumbnailPath()));
	        
	        
	        JsonArray mediaArray = new JsonArray();
	        for(Media media : scene.getMediaAsList()){
	        	mediaArray.add(context.serialize(media));
	        }
	        result.add("media", mediaArray);
	        
	        return result;
	    }

		public Scene deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		      throws JsonParseException {
			  JsonObject source = json.getAsJsonObject();
			  JsonArray media = source.getAsJsonArray("media");
			  
			  Scene scene = new Scene(mContext, (media == null) ? 0 : media.size());
			  scene.setId(source.getAsJsonPrimitive("id").getAsInt());
			  scene.setProjectIndex(source.getAsJsonPrimitive("projectIndex").getAsInt());
			  scene.setProjectId(source.getAsJsonPrimitive("projectId").getAsInt());
			  scene.setClipCount(source.getAsJsonPrimitive("clipCount").getAsInt());
			  
			  if(source.has("title"))
				  scene.setTitle(source.getAsJsonPrimitive("title").getAsString());
			  if(source.has("thumbnailPath"))
				  scene.setThumbnailPath(source.getAsJsonPrimitive("thumbnailPath").getAsString());
			  

			  if(media != null){
				  for(int x = 0; x < media.size(); x++){
					  scene.setMedia(0, (Media) context.deserialize(media.get(x), Media.class));
				  }
			  }

			  return scene;
		}
	}

}
