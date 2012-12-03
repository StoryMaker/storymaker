package info.guardianproject.mrapp.model;

import java.util.ArrayList;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Project {
    protected Context context;
    protected int id;
    protected String title;
    protected String thumbnailPath;
    protected int storyType;
    
    public final static int STORY_TYPE_VIDEO = 0;
    public final static int STORY_TYPE_AUDIO = 1;
    public final static int STORY_TYPE_PHOTO = 2;
    public final static int STORY_TYPE_ESSAY = 3;
    
    public Project(Context context) {
        this.context = context;
    }

    public Project(Context context, int id, String title, String thumbnailPath, int storyType) {
        super();
        this.context = context;
        this.id = id;
        this.title = title;
        this.thumbnailPath = thumbnailPath;
        this.storyType = storyType;
    }

    public Project(Context context, Cursor cursor) {
        // FIXME use column id's directly to optimize this one schema stabilizes
        this(
                context,
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.ID)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_TITLE)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_THUMBNAIL_PATH)),
                  cursor.getInt(cursor
                                .getColumnIndex(StoryMakerDB.Schema.Projects.COL_STORY_TYPE))      
        		);
    }

    /***** Table level static methods *****/

    public static Cursor getAsCursor(Context context, int id) {
        String selection = StoryMakerDB.Schema.Projects.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
        return context.getContentResolver().query(
                ProjectsProvider.PROJECTS_CONTENT_URI, null, selection,
                selectionArgs, null);
    }

    public static Project get(Context context, int id) {
        Cursor cursor = Project.getAsCursor(context, id);
        if (cursor.moveToFirst()) {
            return new Project(context, cursor);
        } else {
            return null;
        }
    }

    public static Cursor getAllAsCursor(Context context) {
        return context.getContentResolver().query(
                ProjectsProvider.PROJECTS_CONTENT_URI, null, null, null, null);
    }

    public static ArrayList<Project> getAllAsList(Context context) {
        ArrayList<Project> projects = new ArrayList<Project>();
        Cursor cursor = getAllAsCursor(context);
        if (cursor.moveToFirst()) {
            do {
                projects.add(new Project(context, cursor));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return projects;
    }

    /***** Object level methods *****/

    public void save() {
        // FIXME be smart about insert vs update
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.Projects.COL_TITLE, title);
        values.put(StoryMakerDB.Schema.Projects.COL_THUMBNAIL_PATH,
                thumbnailPath);
        values.put(StoryMakerDB.Schema.Projects.COL_STORY_TYPE,
                storyType);
        Uri uri = context.getContentResolver().insert(
                ProjectsProvider.PROJECTS_CONTENT_URI, values);
        String lastSegment = uri.getLastPathSegment();
        int newId = Integer.parseInt(lastSegment);
        this.setId(newId);
    }

    public ArrayList<Media> getMediaAsList() {
        Cursor cursor = getMediaAsCursor();
        ArrayList<Media> medias = new ArrayList<Media>(5); // FIXME convert 5 to a constant... is it always 5 long?
        medias.add(null); medias.add(null); medias.add(null); medias.add(null); medias.add(null); // FIXME oh java, you ugly dog
        if (cursor.moveToFirst()) {
            do {
            	Media media = new Media(context, cursor);
                medias.set(media.clipIndex, media);
            } while (cursor.moveToNext());
        }
        return medias;
    }

    public Media[] getMediaAsArray() {
        ArrayList<Media> medias = getMediaAsList();
        return medias.toArray(new Media[] {});
    }

    public Cursor getMediaAsCursor() {
        String selection = "project_id=?";
        String[] selectionArgs = new String[] { "" + getId() };
        return context.getContentResolver().query(
                ProjectsProvider.MEDIA_CONTENT_URI, null, selection,
                selectionArgs, null);
    }

    /**
     * @param media append this media to the back of the projects media list
     */
    public void setMedia(int clipIndex, String clipType, String path, String mimeType) {
        Media media = new Media(context);
        media.setPath(path);
        media.setMimeType(mimeType);
        media.setClipType(clipType);
        media.setClipIndex(clipIndex);
        media.setProjectId(getId());
        media.save();
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
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
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
     * @param thumbnailPath
     *            the thumbnailPath to set
     */
    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

	public int getStoryType() {
		return storyType;
	}

	public void setStoryType(int storyType) {
		this.storyType = storyType;
	}
    
    
}
