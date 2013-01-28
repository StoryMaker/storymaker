package info.guardianproject.mrapp.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Project {
	final private String TAG = "Project";
    protected Context context;
    protected int id;
    protected String title;
    protected String thumbnailPath;
    protected int storyType;
    
    public final static int STORY_TYPE_VIDEO = 0;
    public final static int STORY_TYPE_AUDIO = 1;
    public final static int STORY_TYPE_PHOTO = 2;
    public final static int STORY_TYPE_ESSAY = 3;
    
    public int mClipCount = -1;
    
    public Project(Context context, int clipCount) {
        this.context = context;
        mClipCount = clipCount;
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
        
        getMaxClipCount();
        
    }
    
    private void getMaxClipCount ()
    {
        Cursor cursor = getMediaAsCursor();
        
        int clipIndex = 0;
        
        if (cursor.moveToFirst()) {
            do {
                Media media = new Media(context, cursor);
                clipIndex = Math.max(clipIndex, media.clipIndex);
            } while (cursor.moveToNext());
        }
        
        mClipCount = clipIndex + 1; //size is one higher than max index
        
        cursor.close();
        
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
        Project project = null;
        
        if (cursor.moveToFirst()) {
            project = new Project(context, cursor);
           
        } 
        
        cursor.close();
        return project;
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
    	Cursor cursor = getAsCursor(context, id);
    	if (cursor.getCount() == 0) {
    		insert();
    	} else {
    		update();
    	}
    	
    	cursor.close();
    }
    
    private ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.Projects.COL_TITLE, title);
        values.put(StoryMakerDB.Schema.Projects.COL_THUMBNAIL_PATH,
                thumbnailPath);
        values.put(StoryMakerDB.Schema.Projects.COL_STORY_TYPE,
                storyType);
        
        
        return values;
    }
    private void insert() {
        ContentValues values = getValues();
        Uri uri = context.getContentResolver().insert(
                ProjectsProvider.PROJECTS_CONTENT_URI, values);
        String lastSegment = uri.getLastPathSegment();
        int newId = Integer.parseInt(lastSegment);
        this.setId(newId);
    }
    
    private void update() {
    	Uri uri = ProjectsProvider.PROJECTS_CONTENT_URI.buildUpon().appendPath("" + id).build();
        String selection = StoryMakerDB.Schema.Projects.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
    	ContentValues values = getValues();
        int count = context.getContentResolver().update(
                uri, values, selection, selectionArgs);
        // FIXME make sure 1 row updated
    }
    
    public void delete() {
    	Uri uri = ProjectsProvider.PROJECTS_CONTENT_URI.buildUpon().appendPath("" + id).build();
        String selection = StoryMakerDB.Schema.Projects.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
        int count = context.getContentResolver().delete(
                uri, selection, selectionArgs);
        Log.d(TAG, "deleted project: " + id + ", rows deleted: " + count);
        // FIXME make sure 1 row updated
    }

    public ArrayList<Media> getMediaAsList() {
        Cursor cursor = getMediaAsCursor();
        
        ArrayList<Media> medias = new ArrayList<Media>(mClipCount);
        
        for (int i = 0; i < mClipCount; i++)
            medias.add(null);
        
        if (cursor.moveToFirst()) {
            do {
            	Media media = new Media(context, cursor);
                medias.set(media.clipIndex, media);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return medias;
    }

    public Media[] getMediaAsArray() {
        ArrayList<Media> medias = getMediaAsList();
        return medias.toArray(new Media[] {});
    }
    
    public int getClipCount ()
    {
        return mClipCount;
    }

    public ArrayList<String> getMediaAsPathList() {
        Cursor cursor = getMediaAsCursor();
        ArrayList<String> paths = new ArrayList<String>(mClipCount);
        
        for (int i = 0; i < mClipCount; i++)
            paths.add(null);
       
        if (cursor.moveToFirst()) {
            do {
            	Media media = new Media(context, cursor);
                paths.set(media.clipIndex, media.getPath());
            } while (cursor.moveToNext());
        }
        cursor.close();
        return paths;
    }

    public String[] getMediaAsPathArray() {
        ArrayList<String> paths = getMediaAsPathList();
        return paths.toArray(new String[] {});
    }

    public Cursor getMediaAsCursor() {
        String selection = "project_id=?";
        String[] selectionArgs = new String[] { "" + getId() };
        String orderBy = "clip_index";
        return context.getContentResolver().query(
                ProjectsProvider.MEDIA_CONTENT_URI, null, selection,
                selectionArgs, orderBy);
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
        
        mClipCount = Math.max((clipIndex+1), mClipCount);
                
    }

    public void swapMediaIndex(int oldIndex, int newIndex) {
    	Media media[] = getMediaAsArray();
		Media oldMedia = media[oldIndex];
		Media newMedia = media[newIndex];
		
		// FIXME we need objects to represent the empty template dummy's, otherwise the template won't be rearranged on next load
    	if (oldMedia != null) {
    		oldMedia.setClipIndex(newIndex);
    		oldMedia.save();
    	}
    	if (newMedia != null) {
    		newMedia.setClipIndex(oldIndex);
    		newMedia.save();
    	}
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
