package info.guardianproject.mrapp.model;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Project {

	protected int id;
	protected String title;
	protected String thumbnailPath;
	Context context;
	
	public Project(Context context) {
		this.context = context; 
	}
	
	public Project(Context context, Cursor cursor) {
		this.context = context;
		id = cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.ID)); // FIXME use indexes directly to optimize this later
		thumbnailPath = cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.COL_THUMBNAIL_PATH)); // FIXME use indexes directly to optimize this later
		title = cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.COL_TITLE)); // FIXME use indexes directly to optimize this later
	}
	
	public void save() {
		ContentValues values = new ContentValues();
		values.put(StoryMakerDB.Schema.Projects.COL_TITLE, title);
		values.put(StoryMakerDB.Schema.Projects.COL_THUMBNAIL_PATH, thumbnailPath);
		context.getContentResolver().insert(ProjectsProvider.CONTENT_URI, values);
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
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the thumbnailPath
	 */
	public String getThumbnailPath() {
		return thumbnailPath;
	}
	/**
	 * @param thumbnailPath the thumbnailPath to set
	 */
	public void setThumbnailPath(String thumbnailPath) {
		this.thumbnailPath = thumbnailPath;
	}
	
}
