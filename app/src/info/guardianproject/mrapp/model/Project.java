package info.guardianproject.mrapp.model;

import info.guardianproject.mrapp.db.StoryMakerDB;
import android.database.Cursor;

public class Project {

	protected int id;
	protected String title;
	protected String thumbnailPath;
	
	public Project() {
		
	}
	
	public Project(Cursor cursor) {
		id = cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.ID)); // FIXME use indexes directly to opt
		thumbnailPath = cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.COL_THUMBNAIL_PATH)); // FIXME use indexes directly to opt
		title = cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.COL_TITLE)); // FIXME use indexes directly to opt
	}
	
	public void save() {
		
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
