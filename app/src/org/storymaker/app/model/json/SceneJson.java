
package org.storymaker.app.model.json;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.storymaker.app.model.Media;
import org.storymaker.app.model.Scene;
import org.storymaker.app.model.json.ModelJson.ModelSerializerDeserializer;

import java.lang.reflect.Type;

/**
 * Scene Json Serialization and Deserialization
 * 
 * @author David Brodsky
 */
public class SceneJson {
    private static final String TAG = "SceneJson";

    public static class SceneSerializerDeserializer extends ModelSerializerDeserializer implements
            JsonSerializer<Scene>,

            JsonDeserializer<Scene> {

        public SceneSerializerDeserializer(Context context, boolean doPersist) {
            super(context, doPersist);
        }

        public JsonElement serialize(final Scene scene, final Type type,
                final JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.add("id", new JsonPrimitive(scene.getId()));
            result.add("projectIndex", new JsonPrimitive(scene.getProjectIndex()));
            result.add("projectId", new JsonPrimitive(scene.getProjectId()));
            result.add("clipCount", new JsonPrimitive(scene.getClipCount()));

            if (scene.getTitle() != null) {
                result.add("title", new JsonPrimitive(scene.getTitle()));
            }
            if (scene.getThumbnailPath() != null) {
                result.add("thumbnailPath", new JsonPrimitive(scene.getThumbnailPath()));
            }

            JsonArray mediaArray = new JsonArray();
            for (Media media : scene.getMediaAsList()) {
                if (media != null) {
                    mediaArray.add(context.serialize(media));
                } else {
                    Log.w(TAG, "Ignoring null Scene Media. Scene.mClipCount (" + scene.getClipCount() + ") may be incorrect");
                }
            }
            if (mediaArray.size() > 0) {
                result.add("media", mediaArray);
            }

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

            if (source.has("title")) {
                scene.setTitle(source.getAsJsonPrimitive("title").getAsString());
            }
            if (source.has("thumbnailPath")) {
                scene.setThumbnailPath(source.getAsJsonPrimitive("thumbnailPath").getAsString());
            }

            if (media != null && mPersistOnDeserialization) {
                for (int x = 0; x < media.size(); x++) {
                    ((Media) context.deserialize(media.get(x), Media.class)).save();
                }
            }

            if (mPersistOnDeserialization) {
                scene.save();
            }

            return scene;
        }

    }

}
