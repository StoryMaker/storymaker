package org.storymaker.app.model;

import android.database.sqlite.SQLiteDatabase;
import org.storymaker.app.db.ProjectsProvider;
import org.storymaker.app.db.StoryMakerDB;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AudioClipTable extends Table {
    private final static String TAG = "AudioClipTable";
    
    public AudioClipTable() {
        
    }
    
    public AudioClipTable(SQLiteDatabase db) {
        super(db);
    }
    // FIXME make a db only version of this
//    /**
//     * gets media in scene at location clipIndex
//     */
//    public Cursor getAsCursor(Context context, int id) {
//        String selection = StoryMakerDB.Schema.AudioClip.COL_SCENE_ID + "=? and " +
//                StoryMakerDB.Schema.AudioClip.ID + "=?";
//        String[] selectionArgs = new String[] { "" + id };
//        if (mDB == null) {
//            return context.getContentResolver().query(ProjectsProvider.MEDIA_CONTENT_URI, null, selection, selectionArgs, null);
//        } else {
//            return mDB.query(getTableName(), null, selection, selectionArgs, null, null, null);
//        }
//    }

//    // FIXME make a db only version of this
//    /**
//     * gets media in scene at location clipIndex
//     */
//    public AudioClip get(Context context, int sceneId, int clipIndex) {
//        Cursor cursor = getAsCursor(context, sceneId, clipIndex);
//        if (cursor.moveToFirst()) {
//            return new AudioClip(mDB, context, cursor);
//        } else {
//            return null;
//        }
//    }

    @Override
    protected String getTableName() {
        return StoryMakerDB.Schema.AudioClip.NAME;
    }
    
    @Override
    protected String getIDColumnName() {
        return StoryMakerDB.Schema.AudioClip.ID;
    }

    @Override
    protected Uri getURI() {
        return ProjectsProvider.AUDIO_CLIPS_CONTENT_URI;
    }

    @Override
    protected String getProviderBasePath() {
        return ProjectsProvider.AUTH_BASE_PATH;
    }

}
