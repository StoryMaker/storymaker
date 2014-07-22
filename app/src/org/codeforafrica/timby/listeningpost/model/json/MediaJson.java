
package org.codeforafrica.timby.listeningpost.model.json;

import java.lang.reflect.Type;

import org.codeforafrica.timby.listeningpost.model.Media;
import org.codeforafrica.timby.listeningpost.model.json.ModelJson.ModelSerializerDeserializer;

import android.content.Context;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Media Json Serialization and Deserialization
 * 
 * @author David Brodsky
 */
public class MediaJson {

    public static class MediaSerializerDeserializer extends ModelSerializerDeserializer implements
            JsonSerializer<Media>, JsonDeserializer<Media> {

        public MediaSerializerDeserializer(Context context, boolean doPersist) {
            super(context, doPersist);
        }

        public JsonElement serialize(final Media media, final Type type,
                final JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.add("id", new JsonPrimitive(media.getId()));
            result.add("clipIndex", new JsonPrimitive(media.getClipIndex()));
            result.add("sceneId", new JsonPrimitive(media.getSceneId()));
            result.add("trimStart", new JsonPrimitive(media.getTrimStart()));
            result.add("trimEnd", new JsonPrimitive(media.getTrimEnd()));
            result.add("duration", new JsonPrimitive(media.getDuration()));

            if (media.getPath() != null) {
                result.add("path", new JsonPrimitive(media.getPath()));
            }
            if (media.getMimeType() != null) {
                result.add("mimeType", new JsonPrimitive(media.getMimeType()));
            }
            if (media.getClipType() != null) {
                result.add("clipType", new JsonPrimitive(media.getClipType()));
            }

            return result;
        }

        public Media deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject source = json.getAsJsonObject();

            Media media = new Media(mContext);
            media.setId(source.getAsJsonPrimitive("id").getAsInt());
            media.setClipIndex(source.getAsJsonPrimitive("clipIndex").getAsInt());
            media.setSceneId(source.getAsJsonPrimitive("sceneId").getAsInt());
            media.setTrimStart(source.getAsJsonPrimitive("trimStart").getAsFloat());
            media.setTrimEnd(source.getAsJsonPrimitive("trimEnd").getAsFloat());
            // Is setDuration's int arg intentionally not a float?
            media.setDuration(source.getAsJsonPrimitive("duration").getAsInt());

            if (source.has("path")) {
                media.setPath(source.getAsJsonPrimitive("path").getAsString());
            }
            if (source.has("mimeType")) {
                media.setMimeType(source.getAsJsonPrimitive("mimeType").getAsString());
            }
            if (source.has("clipType")) {
                media.setClipType(source.getAsJsonPrimitive("clipType").getAsString());
            }

            if (mPersistOnDeserialization) {
                media.save();
            }

            return media;
        }

    }

}
