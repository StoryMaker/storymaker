package info.guardianproject.mrapp.model;

import java.util.ArrayList;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Media {
	protected Context context;
	protected int id;
	protected String path;
	protected String mimeType;
	protected int projectId; // foreign key to the project which holds this media
	
	public Media(Context context) {
		this.context = context;
	}
	
	public Media(Context context, Cursor cursor) {
		this.context = context;
		id = cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Media.ID)); // FIXME use indexes directly to opt
		path = cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Media.COL_PATH)); // FIXME use indexes directly to opt
		mimeType = cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Media.COL_MIME_TYPE)); // FIXME use indexes directly to opt
	}
	
	public void save() {
		ContentValues values = new ContentValues();
		values.put(StoryMakerDB.Schema.Media.COL_PATH, path);
		values.put(StoryMakerDB.Schema.Media.COL_MIME_TYPE, mimeType);
		Uri uri = context.getContentResolver().insert(ProjectsProvider.MEDIA_CONTENT_URI, values);
		// FIXME grab out the id and set it on ourself
	}
    
    public static Cursor getAsCursor(Context context, int id) {
        String selection = StoryMakerDB.Schema.Media.ID + "=?";
        String[] selectionArgs = new String[] { "" + id};
        return context.getContentResolver().query(ProjectsProvider.MEDIA_CONTENT_URI, null, selection, selectionArgs, null);
    }
    
    public static Media get(Context context, int id) {
        return new Media(context, Media.getAsCursor(context, id));
    }
	
	public static Cursor getAllAsCursor(Context context) {
		return context.getContentResolver().query(ProjectsProvider.MEDIA_CONTENT_URI, null, null, null, null);
	}
	
	public static ArrayList<Project> getAllAsList(Context context) {
		ArrayList<Project> projects = new ArrayList<Project>();
		Cursor cursor = getAllAsCursor(context);
		if (cursor.moveToFirst()) {
			do {
				projects.add(new Project(context, cursor));
			} while(cursor.moveToNext());
		}
		return projects;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
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
	 * @param path the path to set
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
	 * @param mimeType the mimeType to set
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
	 * @param projectId the projectId to set
	 */
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
}
