package org.storymaker.app.model;

import timber.log.Timber;

import java.util.ArrayList;

import org.storymaker.app.db.ProjectsProvider;
import org.storymaker.app.db.StoryMakerDB;
import net.sqlcipher.database.SQLiteDatabase;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class TagTable extends Table 
{
    private static final String TAG = "TagTable";
    
    public TagTable() 
    {
        
    }

    /**
     * Default Table constructor that uses the direct db.
     * 
     * This should be used within DB Migrations and Model or Table classes
     * 
     * @param db
     */
    public TagTable(SQLiteDatabase db) 
    {
        super(db);
    }

    @Override
    protected String getTableName() 
    {
        return StoryMakerDB.Schema.Tags.NAME;
    }
    
    @Override
    protected String getIDColumnName() 
    {
        return StoryMakerDB.Schema.Tags.ID;
    }

    @Override
    protected Uri getURI() 
    {
        return ProjectsProvider.TAGS_CONTENT_URI;
    }

    @Override
    protected String getProviderBasePath() 
    {
        return ProjectsProvider.TAGS_BASE_PATH;
    }
    
    public Cursor getSpecificTag(Context context, String tag, int projectId)
    {
        String selection = StoryMakerDB.Schema.Tags.COL_TAG + " = ? AND " +
                           StoryMakerDB.Schema.Tags.COL_PROJECT_ID + " = ? ";
        String[] selectionArgs = new String[] { "" + tag, "" + projectId };
        
        if (mDB == null) 
            return context.getContentResolver().query(ProjectsProvider.TAGS_CONTENT_URI, null, selection, selectionArgs, null);
        else 
            return mDB.query(getTableName(), null, selection, selectionArgs, null, null, null, null);
    }
    
    public Cursor getUniqueTagsAsCursor(Context context)
    {
        // we need to only return the tag column in order for DISTINCT to work
        String[] projection = new String[] { StoryMakerDB.Schema.Tags.COL_TAG };
        
        if (mDB == null) 
            return context.getContentResolver().query(ProjectsProvider.DISTINCT_TAGS_CONTENT_URI, projection, null, null, StoryMakerDB.Schema.Tags.COL_TAG);
        else
            return mDB.query(true, getTableName(), projection, null, null, null, null, null, null); // 1st "true" specifies distinct results
    }
    
    public Cursor getUniqueTagsMatchingCursor(Context context, String match)
    {
        String selection = StoryMakerDB.Schema.Tags.COL_TAG + " LIKE ? ";
        String[] selectionArgs = new String[] { match + "%" };
        String[] projection = new String[] { StoryMakerDB.Schema.Tags.COL_TAG };
        
        if (mDB == null) 
            return context.getContentResolver().query(ProjectsProvider.DISTINCT_TAGS_CONTENT_URI, projection, selection, selectionArgs, null);
        else
            return mDB.query(true, getTableName(), projection, selection, selectionArgs, null, null, null, null); // 1st "true" specifies distinct results
    }
    
    public ArrayList<String> getUniqueTagsAsList(Context context)
    {
        Cursor cursor = getUniqueTagsAsCursor(context);
        ArrayList<String> tags = new ArrayList<String>();
        if (cursor.moveToFirst()) 
        {
            do 
            {
                tags.add(cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Tags.COL_TAG)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tags;
    }
    
    public ArrayList<String> getUniqueTagsMatching(Context context, String match)
    {
        Cursor cursor = getUniqueTagsMatchingCursor(context, match);
        ArrayList<String> tags = new ArrayList<String>();
        if (cursor.moveToFirst()) 
        {
            do 
            {
                tags.add(cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Tags.COL_TAG)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tags;
    }
}
