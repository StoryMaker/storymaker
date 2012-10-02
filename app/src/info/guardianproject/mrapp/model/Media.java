package info.guardianproject.mrapp.model;

import info.guardianproject.mrapp.db.StoryMakerDB;
import android.database.Cursor;

public class Media {

	protected int id;
	protected String path;
	protected String mimeType;
	protected int projectId; // foreign key to the project which holds this media
	
	public Media() {
		
	}
	
	public Media(Cursor cursor) {
		id = cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Medias.ID)); // FIXME use indexes directly to opt
		path = cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Medias.COL_PATH)); // FIXME use indexes directly to opt
		mimeType = cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Medias.COL_MIME_TYPE)); // FIXME use indexes directly to opt
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
