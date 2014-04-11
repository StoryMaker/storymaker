package info.guardianproject.mrapp.model;

import java.util.ArrayList;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
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

    public Cursor getUniqueTagsAsCursor(Context context)
    {
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