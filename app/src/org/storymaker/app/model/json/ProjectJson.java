
package org.storymaker.app.model.json;

import java.lang.reflect.Type;

import org.storymaker.app.model.Media;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.Scene;
import org.storymaker.app.model.json.ModelJson.ModelSerializerDeserializer;
import org.storymaker.app.model.json.SceneJson.SceneSerializerDeserializer;
import org.storymaker.app.model.json.MediaJson.MediaSerializerDeserializer;

import android.content.Context;
import android.util.Log;

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

/**
 * Project Json Serialization and Deserialization
 * 
 * @author David Brodsky
 */
public class ProjectJson {
    private static final String TAG = "ProjectJson";

    private static Gson mGson;

    /***** Public API *****/

    /**
     * Construct a JsonElement serialization of the given Project
     * 
     * @param context an Android Application Context
     * @param project the Project to serialize
     * @return a JsonElement serialization of project
     */
    public static JsonElement serializeAsJsonElement(Context context, Project project) {
        return getGson(context).toJsonTree(project);
    }

    /**
     * Construct a Json String serialization of the given Project
     * 
     * @param context an Android Application Context
     * @param project the Project to convert
     * @return a Json String serialization of project
     */
    public static String serializeAsString(Context context, Project project) {
        return getGson(context).toJson(project);
    }

    /**
     * Construct a Project from the given JsonElement
     * 
     * @param context an Android Application Context
     * @param projectJson the JsonElement serialization of the desired Project
     * @return the Project deserialization of projectJson
     */
    public static Project deserializeFromJsonElement(Context context, JsonElement projectJson) {
        return getGson(context).fromJson(projectJson, Project.class);
    }

    /**
     * Construct a Project from the given Json String
     * 
     * @param context an Android Application Context
     * @param projectJson the Json String serizliation of the desired Project
     * @return the Project deserialization of projectJson
     */
    public static Project deserializeFromString(Context context, String projectJson) {
        return getGson(context).fromJson(projectJson, Project.class);
    }

    /***** Private components *****/

    private static Gson getGson(Context context) {
        return getGson(context, true);
    }

    private static Gson getGson(Context context, boolean doPersist) {
        if (mGson == null) {
            mGson = new GsonBuilder()
                    .registerTypeAdapter(Project.class,
                            new ProjectSerializerDeserializer(context, doPersist))
                    .registerTypeAdapter(Scene.class,
                            new SceneSerializerDeserializer(context, doPersist))
                    .registerTypeAdapter(Media.class,
                            new MediaSerializerDeserializer(context, doPersist))
                    .create();
        }
        return mGson;
    }

    /**
     * Serialize a Project into JSON. Requires the Gson context to have
     * registered type adapters for Media and Scene serialization
     */
    private static class ProjectSerializerDeserializer extends ModelSerializerDeserializer
            implements
            JsonSerializer<Project>, JsonDeserializer<Project> {

        public ProjectSerializerDeserializer(Context context, boolean doPersist) {
            super(context, doPersist);
        }

        public JsonElement serialize(final Project project, final Type type,
                final JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.add("id", new JsonPrimitive(project.getId()));
            result.add("storyType", new JsonPrimitive(project.getStoryType()));

            if (project.getTitle() != null) {
                result.add("title", new JsonPrimitive(project.getTitle()));
            }
            if (project.getThumbnailPath() != null) {
                result.add("thumbnailPath",
                        new JsonPrimitive(project.getThumbnailPath()));
            }
            if (project.getTemplatePath() != null) {
                result.add("templatePath",
                        new JsonPrimitive(project.getTemplatePath()));
            }

            // Each Scene will serialize its media
            JsonArray sceneArray = new JsonArray();
            for (Scene scene : project.getScenesAsList()) {
                if (scene != null) {
                    sceneArray.add(context.serialize(scene));
                } else {
                    Log.e(TAG, "Ignoring null Project Scene. Project.mSceneCount (" + project.mSceneCount + ") may be incorrect.");
                }
            }
            result.add("scenes", sceneArray);

            return result;
        }

        public Project deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            JsonObject source = json.getAsJsonObject();
            JsonArray scenes = source.getAsJsonArray("scenes");

            Project project = new Project(mContext, (scenes == null) ? 0 : scenes.size());
            project.setId(source.getAsJsonPrimitive("id").getAsInt());
            project.setStoryType(source.getAsJsonPrimitive("storyType").getAsInt());

            if (source.has("title")) {
                project.setTitle(source.getAsJsonPrimitive("title").getAsString());
            }
            if (source.has("thumbnailPath")) {
                project.setThumbnailPath(source.getAsJsonPrimitive("thumbnailPath").getAsString());
            }
            if (source.has("templatePath")) {
                project.setTemplatePath(source.getAsJsonPrimitive("templatePath").getAsString());
            }

            if (scenes != null) {
                for (int x = 0; x < scenes.size(); x++) {
                    project.setScene(0, (Scene) context.deserialize(scenes.get(x), Scene.class));
                }
            }

            if (mPersistOnDeserialization) {
                project.save();
            }

            return project;
        }
    }

}
