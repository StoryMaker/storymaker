package org.storymaker.app.model;

import timber.log.Timber;

import net.sqlcipher.database.SQLiteDatabase;
import org.storymaker.app.db.ProjectsProvider;
import org.storymaker.app.db.StoryMakerDB;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class MediaTable extends Table {
    private final static String TAG = "MediaTable";
    
    public MediaTable() {
        
    }
    
    public MediaTable(SQLiteDatabase db) {
        super(db);
    }
    // FIXME make a db only version of this
    /**
     * gets media in scene at location clipIndex
     */
    public Cursor getAsCursor(Context context, int sceneId, int clipIndex) {
        String selection = StoryMakerDB.Schema.Media.COL_SCENE_ID + "=? and " +
                StoryMakerDB.Schema.Media.COL_CLIP_INDEX + "=?";
        String[] selectionArgs = new String[] { "" + sceneId, "" + clipIndex };
        if (mDB == null) {
            return context.getContentResolver().query(ProjectsProvider.MEDIA_CONTENT_URI, null, selection, selectionArgs, null);
        } else {
            return mDB.query(getTableName(), null, selection, selectionArgs, null, null, null);
        }
    }

    // FIXME make a db only version of this
    /**
     * gets media in scene at location clipIndex
     */
    public Media get(Context context, int sceneId, int clipIndex) {
        Cursor cursor = getAsCursor(context, sceneId, clipIndex);
        if (cursor.moveToFirst()) {
            return new Media(mDB, context, cursor);
        } else {
            return null;
        }
    }

    @Override
    protected String getTableName() {
        return StoryMakerDB.Schema.Media.NAME;
    }
    
    @Override
    protected String getIDColumnName() {
        return StoryMakerDB.Schema.Media.ID;
    }

    @Override
    protected Uri getURI() {
        return ProjectsProvider.MEDIA_CONTENT_URI;
    }

    @Override
    protected String getProviderBasePath() {
        return ProjectsProvider.MEDIA_BASE_PATH;
    }

}
