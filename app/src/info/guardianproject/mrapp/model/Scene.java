package info.guardianproject.mrapp.model;

import java.util.ArrayList;
import java.util.List;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Scene {
	final private String TAG = "Scene";
    protected Context context;
    protected int id;
    protected String title;
    protected String thumbnailPath;
    protected int projectIndex; // position this scene is in the project
    protected int projectId; // foreign key to the Scene which holds this media
    
    protected int mClipCount = -1;
    
    public Scene(Context context, int clipCount) {
        this.context = context;
        mClipCount = clipCount;
    }

    public Scene(Context context, int id, String title, String thumbnailPath, int projectIndex, int projectId) {
        super();
        this.context = context;
        this.id = id;
        this.title = title;
        this.thumbnailPath = thumbnailPath;
        this.projectIndex = projectIndex;
        this.projectId = projectId;
    }

    public Scene(Context context, Cursor cursor) {
        // FIXME use column id's directly to optimize this one schema stabilizes
        this(
                context,
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Scenes.ID)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Scenes.COL_TITLE)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Scenes.COL_THUMBNAIL_PATH)),
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Scenes.COL_PROJECT_INDEX)),
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Scenes.COL_PROJECT_ID))
        		);
        
        calculateMaxClipCount();
        
    }
    
    private void calculateMaxClipCount ()
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
        String selection = StoryMakerDB.Schema.Scenes.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
        return context.getContentResolver().query(
                ProjectsProvider.SCENES_CONTENT_URI, null, selection,
                selectionArgs, null);
    }

    public static Scene get(Context context, int id) {
        Cursor cursor = Scene.getAsCursor(context, id);
        Scene scene = null;
        
        if (cursor.moveToFirst()) {
            scene = new Scene(context, cursor);
           
        } 
        
        cursor.close();
        return scene;
    }

    public static Cursor getAllAsCursor(Context context) {
        return context.getContentResolver().query(
                ProjectsProvider.SCENES_CONTENT_URI, null, null, null, null);
    }

    public static ArrayList<Scene> getAllAsList(Context context) {
        ArrayList<Scene> scenes = new ArrayList<Scene>();
        Cursor cursor = getAllAsCursor(context);
        if (cursor.moveToFirst()) {
            do {
                scenes.add(new Scene(context, cursor));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return scenes;
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
        values.put(StoryMakerDB.Schema.Scenes.COL_TITLE, title);
        values.put(StoryMakerDB.Schema.Scenes.COL_THUMBNAIL_PATH, thumbnailPath);
        values.put(StoryMakerDB.Schema.Scenes.COL_PROJECT_INDEX, projectIndex);
        values.put(StoryMakerDB.Schema.Scenes.COL_PROJECT_ID, projectId);
        
        return values;
    }
    private void insert() {
        ContentValues values = getValues();
        Uri uri = context.getContentResolver().insert(
                ProjectsProvider.SCENES_CONTENT_URI, values);
        String lastSegment = uri.getLastPathSegment();
        int newId = Integer.parseInt(lastSegment);
        this.setId(newId);
    }
    
    private void update() {
    	Uri uri = ProjectsProvider.SCENES_CONTENT_URI.buildUpon().appendPath("" + id).build();
        String selection = StoryMakerDB.Schema.Scenes.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
    	ContentValues values = getValues();
        int count = context.getContentResolver().update(
                uri, values, selection, selectionArgs);
        // FIXME make sure 1 row updated
    }
    
    public void delete() {
    	Uri uri = ProjectsProvider.SCENES_CONTENT_URI.buildUpon().appendPath("" + id).build();
        String selection = StoryMakerDB.Schema.Scenes.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
        int count = context.getContentResolver().delete(
                uri, selection, selectionArgs);
        Log.d(TAG, "deleted scene: " + id + ", rows deleted: " + count);
        // FIXME make sure 1 row updated
        
        //TODO should we also delete all media files associated with this scene?
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
    
    public void setClipCount (int clipCount){
    	mClipCount = clipCount;
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
        String selection = "scene_id=?";
        String[] selectionArgs = new String[] { "" + getId() };
        String orderBy = "clip_index";
        return context.getContentResolver().query(
                ProjectsProvider.MEDIA_CONTENT_URI, null, selection,
                selectionArgs, orderBy);
    }

    /**
     * @param media append this media to the back of the scene's media list
     */
    public void setMedia(int clipIndex, String clipType, String path, String mimeType) {
        Media media = new Media(context);
        media.setPath(path);
        media.setMimeType(mimeType);
        media.setClipType(clipType);
        media.setClipIndex(clipIndex);
        media.setSceneId(getId());
        media.save();
        
        mClipCount = Math.max((clipIndex+1), mClipCount);
                
    }
    
    public void setMedia(int clipIndex, Media media){
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
    
    
    public void moveMedia(int oldIndex, int newIndex) {
    	
    	//first get the list, and the item that is to be moved
    	List<Media> alMedia = getMediaAsList();
        Media oldMedia = alMedia.get(oldIndex);
        
        //remove the media clip from the old location
        alMedia.remove(oldIndex);
        
        //insert it into the new location
        alMedia.add(newIndex, oldMedia);
        
        //now reset all the indexes based on position in the list
        for (int i = 0 ; i < alMedia.size() ; i++) {
            Media m = alMedia.get(i);
            if (m != null)
            {
            	m.setClipIndex(i);
            	m.save();
            }
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

    /**
     * @return the projectIndex
     */
    public int getProjectIndex() {
        return projectIndex;
    }

    /**
     * @param projectIndex the projectIndex to set
     */
    public void setProjectIndex(int projectIndex) {
        this.projectIndex = projectIndex;
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
