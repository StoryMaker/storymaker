package info.guardianproject.mrapp.model;

import java.util.ArrayList;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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

    public Media(Context context) {
        this.context = context;
    }

    public Media(Context context, int id, String path, String mimeType, String clipType, int clipIndex,
            int sceneId) {
        super();
        this.context = context;
        this.id = id;
        this.path = path;
        this.mimeType = mimeType;
        this.clipType = clipType;
        this.clipIndex = clipIndex;
        this.sceneId = sceneId;
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
                        .getColumnIndex(StoryMakerDB.Schema.Media.COL_SCENE_ID)));
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

//    public void save() {
//        // FIXME be smart about insert vs update
//        ContentValues values = new ContentValues();
//        values.put(StoryMakerDB.Schema.Media.COL_PATH, path);
//        values.put(StoryMakerDB.Schema.Media.COL_MIME_TYPE, mimeType);
//        values.put(StoryMakerDB.Schema.Media.COL_CLIP_TYPE, clipType);
//        values.put(StoryMakerDB.Schema.Media.COL_CLIP_INDEX, clipIndex);
//        values.put(StoryMakerDB.Schema.Media.COL_SCENE_ID, sceneId);
//        ContentResolver cr = context.getContentResolver();
//        Uri uri = cr.insert(
//                ProjectsProvider.MEDIA_CONTENT_URI, values);
//        String lastSegment = uri.getLastPathSegment();
//        int newId = Integer.parseInt(lastSegment);
//        this.setId(newId);
//    }
    
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
    
    private void delete() {
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
}
