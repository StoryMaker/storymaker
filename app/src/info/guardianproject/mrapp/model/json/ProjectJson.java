package info.guardianproject.mrapp.model.json;

import java.lang.reflect.Type;

import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Scene;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ProjectJson {
	
	private static Gson mGson;

	/***** Public API *****/
	
	public static JsonElement serializeAsObject(Context context, Project project){
		return getGson(context).toJsonTree(project);
	}
	
	public static String serializeAsString(Context context, Project project){
		return getGson(context).toJson(project);
	}
	
	public static Project deserialize(Context context, JsonElement projectJson){
		return getGson(context).fromJson(projectJson, Project.class);
	}
	
	/***** Private components *****/

	private static Gson getGson(Context context){
		if(mGson == null){
			mGson = new GsonBuilder()
						.registerTypeAdapter(Project.class, new ProjectSerializerDeserializer(context))
						.registerTypeAdapter(Scene.class, new SceneJson.SceneSerializerDeserializer(context))
						.registerTypeAdapter(Media.class, new MediaJson.MediaSerializerDeserializer(context))
						.create();
		}
		return mGson;
	}

	/**
	 * Serialize a Project into JSON.
	 * Requires the Gson context to have registered type adapters
	 * for Media and Scene serialization
	 */
	private static class ProjectSerializerDeserializer implements JsonSerializer<Project>, JsonDeserializer<Project> {
		
		private Context mContext;
		
		public ProjectSerializerDeserializer(Context context){
			mContext = context;
		}
		
		private Context getContext(){
			return mContext;
		}
		 
	    public JsonElement serialize(final Project project, final Type type, final JsonSerializationContext context) {
	        JsonObject result = new JsonObject();
	        result.add("id", new JsonPrimitive(project.getId()));
	        result.add("storyType", new JsonPrimitive(project.getStoryType()));
	        
	        if(project.getTitle() != null)
	        	result.add("title", new JsonPrimitive(project.getTitle()));
	        if(project.getThumbnailPath() != null)
	        	result.add("thumbnailPath", new JsonPrimitive(project.getThumbnailPath()));
	        if(project.getTemplatePath() != null)
	        	result.add("templatePath", new JsonPrimitive(project.getTemplatePath()));
	        
	        // Each Scene will serialize its media
	        JsonArray sceneArray = new JsonArray();
	        for(Scene scene : project.getScenesAsList()){
	        	sceneArray.add(context.serialize(scene));
	        }
	        result.add("scenes", sceneArray);
	        
	        return result;
	    }

		public Project deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		      throws JsonParseException {
			  JsonObject source = json.getAsJsonObject();
			  JsonArray scenes = source.getAsJsonArray("scenes");
			  
			  Project project = new Project(mContext, (scenes == null) ? 0 : scenes.size());
			  project.setId(source.getAsJsonPrimitive("id").getAsInt());
			  project.setStoryType(source.getAsJsonPrimitive("storyType").getAsInt());
			  
			  if(source.has("title"))
				  project.setTitle(source.getAsJsonPrimitive("title").getAsString());
			  if(source.has("thumbnailPath"))
				  project.setThumbnailPath(source.getAsJsonPrimitive("thumbnailPath").getAsString());
			  if(source.has("templatePath"))
				  project.setTemplatePath(source.getAsJsonPrimitive("templatePath").getAsString());
			  
			  if(scenes != null){
				  for(int x = 0; x < scenes.size(); x++){
					  project.setScene(0, (Scene) context.deserialize(scenes.get(x), Scene.class));
				  }
			  }

			  return project;
		}
	}

}
