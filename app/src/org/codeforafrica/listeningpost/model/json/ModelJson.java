
package org.codeforafrica.listeningpost.model.json;

import android.content.Context;

/**
 * Base class for StoryMaker Model JSON Serializer/Deserializer
 * 
 * @author David Brodsky
 */
public class ModelJson {

    public static abstract class ModelSerializerDeserializer {

        protected Context mContext;
        protected boolean mPersistOnDeserialization;

        public ModelSerializerDeserializer(Context context) {
            this(context, true);
        }

        public ModelSerializerDeserializer(Context context, boolean doPersist) {
            mContext = context;
            mPersistOnDeserialization = doPersist;
        }

        public Context getContext() {
            return mContext;
        }

        public void doPersist(boolean doPersist) {
            mPersistOnDeserialization = doPersist;
        }

    }

}
