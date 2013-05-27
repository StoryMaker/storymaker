package info.guardianproject.mrapp.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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

public class Media {
	private static final String TAG = "Media";
	
    protected Context context;
    protected int id;
    protected String path;
    protected String mimeType;
    protected String clipType; // R.arrays.cliptypes
    protected int clipIndex; // which clip is this in the scene
    protected int sceneId; // foreign key to the Scene which holds this media
    protected int trimStart;
    protected int trimEnd;
    protected int duration;

    public final static int IMAGE_SAMPLE_SIZE = 4;
    
    public Media(Context context) {
        this.context = context;
    }

    public Media(Context context, int id, String path, String mimeType, String clipType, int clipIndex,
            int sceneId, int trimStart, int trimEnd, int duration) {
        super();
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
    }

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
                        .getColumnIndex(StoryMakerDB.Schema.Media.COL_DURATION)));
    }

    /***** Table level static methods *****/

    public static Cursor getAsCursor(Context context, int id) {
        String selection = StoryMakerDB.Schema.Media.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
        return context.getContentResolver().query(
                ProjectsProvider.MEDIA_CONTENT_URI, null, selection,
                selectionArgs, null);
    }

    public static Media get(Context context, int id) {
        Cursor cursor = Media.getAsCursor(context, id);
        if (cursor.moveToFirst()) {
            return new Media(context, cursor);
        } else {
            return null;
        }
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

    /** 
     * @return milliseconds to end of trimmed clip
     */
    public int getTrimmedEndTime() {
        return Math.round(getTrimmedEndPercent() * duration);
    }

    /** 
     * @return milliseconds trimmed clip will last
     */
    public int getTrimmedDuration() {
        return getTrimmedEndTime() - getTrimmedStartTime();
    }
    
    /***** Object level methods *****/


    /*
     * gets media in scene at location clipIndex
     */
    public static Cursor getAsCursor(Context context, int sceneId, int clipIndex) {
        String selection = StoryMakerDB.Schema.Media.COL_SCENE_ID + "=? and " +
        		StoryMakerDB.Schema.Media.COL_CLIP_INDEX + "=?";
        String[] selectionArgs = new String[] { "" + sceneId, "" + clipIndex };
        return context.getContentResolver().query(
                ProjectsProvider.MEDIA_CONTENT_URI, null, selection,
                selectionArgs, null);
    }

    /*
     * gets media in scene at location clipIndex
     */
    public static Media get(Context context, int sceneId, int clipIndex) {
        Cursor cursor = Media.getAsCursor(context, sceneId, clipIndex);
        if (cursor.moveToFirst()) {
            return new Media(context, cursor);
        } else {
            return null;
        }
    }

    public static Cursor getAllAsCursor(Context context) {
        return context.getContentResolver().query(
                ProjectsProvider.MEDIA_CONTENT_URI, null, null, null, null);
    }

    public static ArrayList<Media> getAllAsList(Context context) {
        ArrayList<Media> medias = new ArrayList<Media>();
        Cursor cursor = getAllAsCursor(context);
        if (cursor.moveToFirst()) {
            do {
                medias.add(new Media(context, cursor));
            } while (cursor.moveToNext());
        }
        return medias;
    }

    /***** Object level methods *****/
    
    public void save() {
    	Cursor cursor = getAsCursor(context, id);
    	if (cursor.getCount() == 0) {
    		cursor.close();
    		insert();
    	} else {
    		cursor.close();
    		update();    		
    	}
    	
    	
    }
    
    private ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.Media.COL_PATH, path);
        values.put(StoryMakerDB.Schema.Media.COL_MIME_TYPE, mimeType);
        values.put(StoryMakerDB.Schema.Media.COL_CLIP_TYPE, clipType);
        values.put(StoryMakerDB.Schema.Media.COL_CLIP_INDEX, clipIndex);
        values.put(StoryMakerDB.Schema.Media.COL_SCENE_ID, sceneId);
        values.put(StoryMakerDB.Schema.Media.COL_TRIM_START, trimStart);
        values.put(StoryMakerDB.Schema.Media.COL_TRIM_END, trimEnd);
        values.put(StoryMakerDB.Schema.Media.COL_DURATION, duration);
        
        return values;
    }
    
    private void insert() {
    	// There can be only one!  check if a media item exists at this location already, if so purge it first.
    	Cursor cursorDupes = getAsCursor(context, sceneId, clipIndex);
    	if ((cursorDupes.getCount() > 0) && cursorDupes.moveToFirst()) {
        	// FIXME we should allow audio clips to remain so they can be mixed down with their buddies
    		do {
    			(new Media(context, cursorDupes)).delete();
    		} while (cursorDupes.moveToNext());
    	}
    	
        ContentValues values = getValues();
        Uri uri = context.getContentResolver().insert(
                ProjectsProvider.MEDIA_CONTENT_URI, values);
        String lastSegment = uri.getLastPathSegment();
        int newId = Integer.parseInt(lastSegment);
        this.setId(newId);
        
        cursorDupes.close();
    }
    
    private void update() {
    	Uri uri = ProjectsProvider.MEDIA_CONTENT_URI.buildUpon().appendPath("" + id).build();
        String selection = StoryMakerDB.Schema.Media.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
    	ContentValues values = getValues();
        int count = context.getContentResolver().update(
                uri, values, selection, selectionArgs);
        // FIXME make sure 1 row updated
    }
    
    public void delete() {
    	Uri uri = ProjectsProvider.MEDIA_CONTENT_URI.buildUpon().appendPath("" + id).build();
        String selection = StoryMakerDB.Schema.Media.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
        int count = context.getContentResolver().delete(
                uri, selection, selectionArgs);
        Log.d(TAG, "deleted media: " + id + ", rows deleted: " + count);
        // FIXME make sure 1 row updated
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
    public int getTrimStart() {
        return trimStart;
    }

    /**
     * @param trimStart the trimStart to set
     */
    public void setTrimStart(int trimStart) {
        this.trimStart = trimStart;
    }

    /**
     * @return the trimEnd
     */
    public int getTrimEnd() {
        return trimEnd;
    }

    /**
     * @param trimEnd the trimEnd to set
     */
    public void setTrimEnd(int trimEnd) {
        this.trimEnd = trimEnd;
    }


    /**
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
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

            return BitmapFactory.decodeResource(context.getResources(), R.drawable.thumb_audio,  options);
        }
        else 
        {
        	 final BitmapFactory.Options options = new BitmapFactory.Options();
             options.inSampleSize = IMAGE_SAMPLE_SIZE;
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.thumb_complete,options);
        }
    }
}
