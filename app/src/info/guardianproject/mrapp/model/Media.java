package info.guardianproject.mrapp.model;

import java.util.ArrayList;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Media {
    protected Context context;
    protected int id;
    protected String path;
    protected String mimeType;
    protected String clipType; // R.arrays.cliptypes
    protected int clipIndex; // which clip is this in the scene
    protected int projectId; // foreign key to the project which holds this
                             // media

    public Media(Context context) {
        this.context = context;
    }

    public Media(Context context, int id, String path, String mimeType, String clipType, int clipIndex,
            int projectId) {
        super();
        this.context = context;
        this.id = id;
        this.path = path;
        this.mimeType = mimeType;
        this.clipType = clipType;
        this.clipIndex = clipIndex;
        this.projectId = projectId;
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
                        .getColumnIndex(StoryMakerDB.Schema.Media.COL_PROJECT_ID)));
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

    public static Cursor getAllAsCursor(Context context) {
        return context.getContentResolver().query(
                ProjectsProvider.MEDIA_CONTENT_URI, null, null, null, null);
    }

    public static ArrayList<Project> getAllAsList(Context context) {
        ArrayList<Project> projects = new ArrayList<Project>();
        Cursor cursor = getAllAsCursor(context);
        if (cursor.moveToFirst()) {
            do {
                projects.add(new Project(context, cursor));
            } while (cursor.moveToNext());
        }
        return projects;
    }

    /***** Object level methods *****/

    public void save() {
        // FIXME be smart about insert vs update
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.Media.COL_PATH, path);
        values.put(StoryMakerDB.Schema.Media.COL_MIME_TYPE, mimeType);
        values.put(StoryMakerDB.Schema.Media.COL_CLIP_TYPE, clipType);
        values.put(StoryMakerDB.Schema.Media.COL_CLIP_INDEX, clipIndex);
        values.put(StoryMakerDB.Schema.Media.COL_PROJECT_ID, projectId);
        ContentResolver cr = context.getContentResolver();
        Uri uri = cr.insert(
                ProjectsProvider.MEDIA_CONTENT_URI, values);
        String lastSegment = uri.getLastPathSegment();
        int newId = Integer.parseInt(lastSegment);
        this.setId(newId);
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
     * @return the projectId
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * @param projectId
     *            the projectId to set
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
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
