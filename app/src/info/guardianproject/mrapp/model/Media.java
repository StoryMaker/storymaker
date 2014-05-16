package info.guardianproject.mrapp.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;

import org.ffmpeg.android.MediaUtils;

import info.guardianproject.mrapp.AppConstants;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import info.guardianproject.mrapp.media.MediaProjectManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class Media extends Model {
	private static final String TAG = "Media";
	
    protected String path;
    protected String mimeType;
    protected String clipType; // R.arrays.cliptypes
    protected int clipIndex; // which clip is this in the scene
    protected int sceneId; // foreign key to the Scene which holds this media
    protected float trimStart;
    protected float trimEnd;
    protected float duration;
    protected Date createdAt; // long stored in database as 8-bit int
    protected Date updatedAt; // long stored in database as 8-bit int

    public final static int IMAGE_SAMPLE_SIZE = 4;

    /**
     * Create a new, blank record via the Content Provider interface
     * 
     * @param context
     */
    public Media(Context context) {
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
    public Media(SQLiteDatabase db, Context context) {
        super(context);
        this.mDB = db;
    }

    /**
     * Create a Model object via direct params
     * 
     * @param context
     * @param id
     * @param path
     * @param mimeType
     * @param clipType
     * @param clipIndex
     * @param sceneId
     * @param trimStart
     * @param trimEnd
     * @param duration
     * @param createdAt
     * @param updatedAt
     */
    public Media(Context context, int id, String path, String mimeType, String clipType, int clipIndex,
            int sceneId, float trimStart, float trimEnd, float duration, Date createdAt, Date updatedAt) {
        super(context);
        this.context = context;
        this.id = id;
        this.path = path;
        this.mimeType = mimeType;
        this.clipType = clipType;
        this.clipIndex = clipIndex;
        this.sceneId = sceneId;
        this.trimStart = trimStart;
        this.trimEnd = trimEnd;
        this.duration = duration;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Create a Model object via direct params, except for auto-incremented primary key
     * 
     * @param context
     * @param path
     * @param mimeType
     * @param clipType
     * @param clipIndex
     * @param sceneId
     * @param trimStart
     * @param trimEnd
     * @param duration
     * @param createdAt
     * @param updatedAt
     */
    public Media(Context context, String path, String mimeType, String clipType, int clipIndex,
            int sceneId, float trimStart, float trimEnd, float duration, Date createdAt, Date updatedAt) {
        super(context);
        this.context = context;
        this.path = path;
        this.mimeType = mimeType;
        this.clipType = clipType;
        this.clipIndex = clipIndex;
        this.sceneId = sceneId;
        this.trimStart = trimStart;
        this.trimEnd = trimEnd;
        this.duration = duration;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Create a Model object via direct params via direct db access.
     * 
     * This should be used within DB Migrations and Model or Table classes
     *
     * @param db
     * @param context
     * @param id
     * @param path
     * @param mimeType
     * @param clipType
     * @param clipIndex
     * @param sceneId
     * @param trimStart
     * @param trimEnd
     * @param duration
     * @param createdAt
     * @param updatedAt
     */
    public Media(SQLiteDatabase db, Context context, int id, String path, String mimeType, String clipType, int clipIndex,
            int sceneId, float trimStart, float trimEnd, float duration, Date createdAt, Date updatedAt) {
        this(context, id, path, mimeType, clipType, clipIndex, sceneId, trimStart, trimEnd, duration, createdAt, updatedAt);
        this.mDB = db;
    }
    
    /**
     * Create a Model object via direct params, except for auto-incremented primary key, via direct db access.
     * 
     * This should be used within DB Migrations and Model or Table classes
     *
     * @param db
     * @param context
     * @param path
     * @param mimeType
     * @param clipType
     * @param clipIndex
     * @param sceneId
     * @param trimStart
     * @param trimEnd
     * @param duration
     * @param createdAt
     * @param updatedAt
     */
    public Media(SQLiteDatabase db, Context context, String path, String mimeType, String clipType, int clipIndex,
            int sceneId, float trimStart, float trimEnd, float duration, Date createdAt, Date updatedAt) {
        this(context, path, mimeType, clipType, clipIndex, sceneId, trimStart, trimEnd, duration, createdAt, updatedAt);
        this.mDB = db;
    }

    /**
     * Inflate record from a cursor via the Content Provider
     * 
     * @param context
     * @param cursor
     */
    public Media(Context context, Cursor cursor) {
        // FIXME use column id's directly to optimize this one schema stabilizes
        this(
                context,
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Media.ID)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Media.COL_PATH)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Media.COL_MIME_TYPE)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Media.COL_CLIP_TYPE)),
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Media.COL_CLIP_INDEX)),
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Media.COL_SCENE_ID)),
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Media.COL_TRIM_START)),
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Media.COL_TRIM_END)),
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Media.COL_DURATION)),
                (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Media.COL_CREATED_AT)) ?
                        new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Media.COL_CREATED_AT))) : null),
                (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Media.COL_UPDATED_AT)) ?
                        new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Media.COL_UPDATED_AT))) : null));
    }

    /**
     * Inflate record from a cursor via direct db access.
     * 
     * This should be used within DB Migrations and Model or Table classes
     *
     * @param db
     * @param context
     * @param cursor
     */
    public Media(SQLiteDatabase db, Context context, Cursor cursor) {
        this(context, cursor);
        this.mDB = db;
    }

    @Override
    protected Table getTable() {
        if (mTable == null) {
            mTable = new MediaTable(mDB);
        }
        return mTable;
    }
    
    /***** Calculated object level methods *****/

    /** 
     * @return 0.0-1.0 percent into the clip to start play
     */
    public float getTrimmedStartPercent() {
        return (trimStart + 1) / 100F;
    }

    /** 
     * @return 0.0-1.0 percent into the clip to end play
     */
    public float getTrimmedEndPercent() {
        return (trimEnd + 1) / 100F;
    }

    /** 
     * @return milliseconds into clip trimmed clip to start playback
     */
    public int getTrimmedStartTime() {
        return Math.round(getTrimmedStartPercent() * duration);
    }

    public float getTrimmedStartTimeFloat() {
        return (getTrimmedStartPercent() * duration);
    }
    /** 
     * @return milliseconds to end of trimmed clip
     */
    public int getTrimmedEndTime() {
        return Math.round(getTrimmedEndPercent() * duration);
    }
    
    public float getTrimmedEndTimeFloat() {
        return (getTrimmedEndPercent() * duration);
    }

    /** 
     * @return milliseconds trimmed clip will last
     */
    public int getTrimmedDuration() {
        return getTrimmedEndTime() - getTrimmedStartTime();
    }
    
    protected ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.Media.COL_PATH, path);
        values.put(StoryMakerDB.Schema.Media.COL_MIME_TYPE, mimeType);
        values.put(StoryMakerDB.Schema.Media.COL_CLIP_TYPE, clipType);
        values.put(StoryMakerDB.Schema.Media.COL_CLIP_INDEX, clipIndex);
        values.put(StoryMakerDB.Schema.Media.COL_SCENE_ID, sceneId);
        values.put(StoryMakerDB.Schema.Media.COL_TRIM_START, trimStart);
        values.put(StoryMakerDB.Schema.Media.COL_TRIM_END, trimEnd);
        values.put(StoryMakerDB.Schema.Media.COL_DURATION, duration);
        if (createdAt != null) {
            values.put(StoryMakerDB.Schema.Media.COL_CREATED_AT, createdAt.getTime());
        }
        if (updatedAt != null) {
            values.put(StoryMakerDB.Schema.Media.COL_UPDATED_AT, updatedAt.getTime());
        }
        // store dates as longs(8-bit ints)
        // can't put null in values set, so only add entry if non-null
        
        return values;
    }
    
    // insert/update current record
    // need to set created at/updated at date
    @Override
    public void save() {
        Cursor cursor = getTable().getAsCursor(context, id);
        if (cursor.getCount() == 0) {
            cursor.close();
            setCreatedAt(new Date());
            setUpdatedAt(new Date());
            insert();
        } else {
            cursor.close();
            setUpdatedAt(new Date());
            update();            
        }
    }
    
    // FIXME make a db only version of this
    // FIXME testme
    @Override
    public void insert() {
    	// There can be only one!  check if a media item exists at this location already, if so purge it first.
    	Cursor cursorDupes = (new MediaTable(mDB)).getAsCursor(context, sceneId, clipIndex);
    	if ((cursorDupes.getCount() > 0) && cursorDupes.moveToFirst()) {
        	// FIXME we should allow audio clips to remain so they can be mixed down with their buddies
    		do {
    			(new Media(mDB, context, cursorDupes)).delete(); // always pass mDB when newing models within models, this way if we are in provider mode that is null anyhow
    		} while (cursorDupes.moveToNext());
    	}
    	
        ContentValues values = getValues();
        
        if (mDB == null) {
        	Uri uri = context.getContentResolver().insert(ProjectsProvider.MEDIA_CONTENT_URI, values);
        	String lastSegment = uri.getLastPathSegment();
            int newId = Integer.parseInt(lastSegment);
            this.setId(newId);
        } else {
        	int newId = (int)mDB.insert((new MediaTable(mDB)).getTableName(), null, values);
        	this.setId(newId);
        }
        
        cursorDupes.close();
        super.insert();
    }

    
    /***** getters and setters *****/

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType
     *            the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the sceneId
     */
    public int getSceneId() {
        return sceneId;
    }

    /**
     * @param sceneId
     *            the sceneId to set
     */
    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

	/**
	 * @return the clipType
	 */
	public String getClipType() {
		return clipType;
	}

	/**
	 * @param clipType the clipType to set
	 */
	public void setClipType(String clipType) {
		this.clipType = clipType;
	}

    /**
     * @return the clipIndex
     */
    public int getClipIndex() {
        return clipIndex;
    }

    /**
     * @param clipIndex the clipIndex to set
     */
    public void setClipIndex(int clipIndex) {
        this.clipIndex = clipIndex;
    }

    /**
     * @return the trimStart
     */
    public float getTrimStart() {
        return trimStart;
    }

    /**
     * @param trimStart the trimStart to set
     */
    public void setTrimStart(float trimStart) {
        this.trimStart = trimStart;
    }

    /**
     * @return the trimEnd
     */
    public float getTrimEnd() {
        return trimEnd;
    }

    /**
     * @param trimEnd the trimEnd to set
     */
    public void setTrimEnd(float trimEnd) {
        this.trimEnd = trimEnd;
    }


    /**
     * @return the duration
     */
    public float getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * @return createdAt
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt 
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return updatedAt
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt 
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // FIXME this should probably be refactored and split half into the media layer
    public static Bitmap getThumbnail(Context context, Media media, Project project) 
    {
    	if (media == null)
    		return null;
    	
    	
        if (media.getMimeType() == null)
        {
            return null;
        }
        else if (media.getMimeType().startsWith("video"))
        {
            File fileThumb = new File(MediaProjectManager.getExternalProjectFolder(project, context), media.getId() + "_thumb.jpg");
            
            if (fileThumb.exists())
            {

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = IMAGE_SAMPLE_SIZE;
                return BitmapFactory.decodeFile(fileThumb.getAbsolutePath(), options);
            }
            else
            {
            	try
            	{
	                Bitmap bmp = MediaUtils.getVideoFrame(new File(media.getPath()).getCanonicalPath(), -1);
	                
	                if (bmp != null)
	                {
		                try {
		                    bmp.compress(Bitmap.CompressFormat.PNG, 70, new FileOutputStream(fileThumb));
		                } catch (FileNotFoundException e) {
		                    Log.e(AppConstants.TAG, "could not cache video thumb", e);
		                }
	                }
	                
	                return bmp;
            	}
            	catch (Exception e)
            	{
            		Log.w(AppConstants.TAG,"Could not generate thumbnail: " + media.getPath(),e);
            		return null;
            	}
            	catch (OutOfMemoryError oe)
            	{
            		Log.e(AppConstants.TAG,"Could not generate thumbnail - OutofMemory!: " + media.getPath());
            		return null;
            	}
            }
        }
        else if (media.getMimeType().startsWith("image"))
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = IMAGE_SAMPLE_SIZE * 2; //images will be bigger than video or audio
        
            return BitmapFactory.decodeFile(media.getPath(), options);
        }
        else if (media.getMimeType().startsWith("audio"))
        {
        	 final BitmapFactory.Options options = new BitmapFactory.Options();
             options.inSampleSize = IMAGE_SAMPLE_SIZE;

             int audioId;
             
             //mod by 5 to repeat the colors in order
             switch(media.clipIndex % 5)
             {    
	             case 0:
	            	 audioId = R.drawable.cliptype_audio_signature;
	            	 break;
	             case 1:
	            	 audioId = R.drawable.cliptype_audio_ambient;
	            	 break;
	             case 2:
	            	 audioId = R.drawable.cliptype_audio_narrative;
	            	 break;
	             case 3:
	            	 audioId = R.drawable.cliptype_audio_interview;
	            	 break;
	             case 4:
	            	 audioId = R.drawable.cliptype_audio_environmental;
	            	 break;
	             default:
	            	audioId = R.drawable.thumb_audio; 
             }
             
            return BitmapFactory.decodeResource(context.getResources(), audioId,  options);
        }
        else 
        {
        	 final BitmapFactory.Options options = new BitmapFactory.Options();
             options.inSampleSize = IMAGE_SAMPLE_SIZE;
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.thumb_complete,options);
        }
    }
    
    public boolean migrate(Project project, Date projectDate) // called on instances of class returned by Project class method
    {
    	try 
        {
    		Date mediaDate = null;
    		
    		// first check thumbnail date
    		if (getMimeType().startsWith("video"))
    		{
    		    File fileThumb = new File(MediaProjectManager.getExternalProjectFolderOld(project, context), getId() + "_thumb.jpg");
                if (fileThumb.exists())
                {
                    mediaDate = new Date(fileThumb.lastModified()); // creation time not stored with file
                }
    		}
    		
    		// next try file date
    		if (mediaDate == null)
    		{
    		    File mediaFile = new File(path);
    		    if (mediaFile.exists())
    		    {
    			    mediaDate = new Date(mediaFile.lastModified()); // creation time not stored with file
    		    }
    		}
    		
    		// if all else fails, use project date
    		if (mediaDate == null)
    		{
    		    mediaDate = projectDate;
    		}
    		
    		setCreatedAt(mediaDate);
            setUpdatedAt(mediaDate);
    		
    	    String fileName = path.substring(path.lastIndexOf(File.separator) + 1);    	    
			String newPath = MediaProjectManager.getExternalProjectFolder(project, context).getCanonicalPath() + File.separator + fileName;
			setPath(newPath);
		} 
    	catch (IOException e) 
    	{
    		Log.e("MEDIA MIGRATION", "unexpected exception: " + e.getMessage());
			return false;
		}
    	
    	update();
    	return true;
    }
}
