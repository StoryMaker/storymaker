package info.guardianproject.mrapp.model;

import info.guardianproject.mrapp.db.StoryMakerDB;

import java.util.Date;

import net.sqlcipher.database.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class Tag extends Model 
{
    private static final String TAG = "Tag";
    
    protected String tag;
    protected int projectId; // foreign key to a project with this tag
    protected Date createdAt; // long stored in database as 8-bit int
    
    /**
     * Create a new, blank record via the Content Provider interface
     * 
     * @param context
     */
    public Tag(Context context) 
    {
        super(context);
    }
    
    /**
     * Create a new, blank record via direct db access.  
     * 
     * This should be used within DB Migrations and Model or Table classes
     * 
     * @param db
     * @param context
     */
    public Tag(SQLiteDatabase db, Context context) 
    {
        super(db, context);
    }
    
    /**
     * Create a Model object via direct params, using Content Provider interface.
     * 
     * @param context
     * @param id
     * @param tag
     * @param projectId
     * @param createdAt
     */
    public Tag(Context context, int id, String tag, int projectId, Date createdAt) 
    {
        super(context);
        this.context = context;
        this.id = id;
        this.tag = tag;
        this.projectId = projectId;
        this.createdAt = createdAt;
    }
    
    /**
     * Create a Model object via direct params via direct db access.
     * 
     * This should be used within DB Migrations and Model or Table classes
     * 
     * @param db
     * @param context
     * @param id
     * @param tag
     * @param projectId
     * @param createdAt
     */
    public Tag(SQLiteDatabase db, Context context, int id, String tag, int projectId, Date createdAt) 
    {
        this(context, id, tag, projectId, createdAt);
        this.mDB = db;
    }
    
    /**
     * Inflate record from a cursor
     *  
     * @param context
     * @param cursor
     */
    public Tag(Context context, Cursor cursor) 
    {
        this(context,
             cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Tags.ID)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Tags.COL_TAG)),
             cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Tags.COL_PROJECT_ID)),
             (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Tags.COL_CREATED_AT)) ?
                     new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Tags.COL_CREATED_AT))) : null));
        // created_at column is nullable, need to avoid errors creating Date objects
    }

    /**
     * Default constructor to inflate record from a cursor via direct db access.  This should be used within DB Migrations and within an Model or Tabel classes
     * @param db
     * @param context
     */
    public Tag(SQLiteDatabase db, Context context, Cursor cursor) 
    {
        this(context, cursor);
        this.mDB = db;
    }
    
    @Override
    protected Table getTable() 
    {
        if (mTable == null) 
        {
            mTable = new TagTable(mDB);
        }
        
        return mTable;
    }
    
 // build values set from current record
    @Override
    protected ContentValues getValues() 
    {
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.Tags.COL_TAG, tag);
        values.put(StoryMakerDB.Schema.Tags.COL_PROJECT_ID, projectId);
        if (createdAt != null) 
        {
            values.put(StoryMakerDB.Schema.Tags.COL_CREATED_AT, createdAt.getTime());
        }
        // store dates as longs(8-bit ints)
        // can't put null in values set, so only add entry if non-null
        return values;
    }
    
 // insert/update current record
 // need to set created at date
    @Override
    public void save() 
    {
        Cursor cursor = ((TagTable)getTable()).getSpecificTag(context, tag, projectId);
        if (cursor.getCount() > 0) {
            cursor.close();
            return; // if a record already exists for the specified tag on the specified project, do not insert a duplicate
        }
        cursor.close();
                    
        cursor = getTable().getAsCursor(context, id);
        if (cursor.getCount() == 0) {
            cursor.close();
            setCreatedAt(new Date());
            insert();
        } else {
            cursor.close();
            update();            
        }
    }

    /**
     * @return tag
     */
    public String getTag() 
    {
        return tag;
    }

    /**
     * @param tag
     */
    public void setTag(String tag) 
    {
        this.tag = tag;
    }
    
    /**
     * @return projectId
     */
    public int getProjectId() 
    {
        return projectId;
    }

    /**
     * @param projectId
     */
    public void setProjectId(int projectId) 
    {
        this.projectId = projectId;
    }
    
    /**
     * @return createdAt
     */
    public Date getCreatedAt() 
    {
        return createdAt;
    }

    /**
     * @param createdAt
     */
    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }
}