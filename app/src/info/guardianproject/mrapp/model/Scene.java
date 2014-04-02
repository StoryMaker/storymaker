package info.guardianproject.mrapp.model;

import java.util.ArrayList;
import java.util.List;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Scene extends Model {
	final private String TAG = "Scene";
    protected String title;
    protected String thumbnailPath;
    protected int projectIndex; // position this scene is in the project
    protected int projectId; // foreign key to the Scene which holds this media
    
    protected int mClipCount = -1;
    
    /**
     * Create new blank Scene with a predetermined clip count via the Content Provider
     * 
     * @param context
     * @param clipCount
     */
    public Scene(Context context, int clipCount) {
        super(context);
        mClipCount = clipCount;
    }

    /**
     * Create new blank Scene with a predetermined clip count via direct db access.
     * 
     * This should be used within DB Migrations and Model or Table classes
     *
     * @param context
     * @param clipCount
     */
    public Scene(SQLiteDatabase db, Context context, int clipCount) {
        this(context, clipCount);
        this.mDB = db;
    }

    /**
     * Create a Model object via direct params
     * 
     * @param context
     * @param id
     * @param title
     * @param thumbnailPath
     * @param projectIndex
     * @param projectId
     */
    public Scene(Context context, int id, String title, String thumbnailPath, int projectIndex, int projectId) {
        super(context);
        this.id = id;
        this.title = title;
        this.thumbnailPath = thumbnailPath;
        this.projectIndex = projectIndex;
        this.projectId = projectId;
    }
    
    /**
     * Create a Model object via direct params via direct db access.
     * 
     * This should be used within DB Migrations and Model or Table classes
     *
     * @param db
     * @param context
     * @param id
     * @param title
     * @param thumbnailPath
     * @param projectIndex
     * @param projectId
     */
    public Scene(SQLiteDatabase db, Context context, int id, String title, String thumbnailPath, int projectIndex, int projectId) {
        this(context, id, title, thumbnailPath, projectIndex, projectId);
        this.mDB = db;
    }

    /**
     * Inflate record from a cursor via the Content Provider
     *
     * @param context
     * @param cursor
     */
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

    /**
     * Inflate record from a cursor via direct db access.  
     * 
     * This should be used within DB Migrations and Model or Table classes
     * 
     * @param db
     * @param context
     * @param cursor
     */
    public Scene(SQLiteDatabase db, Context context, Cursor cursor) {
        this(context, cursor);
        this.mDB = db;
    }

    @Override
    protected Table getTable() {
        if (mTable == null) {
            mTable = new SceneTable(mDB);
        }
        return mTable;
    }
    
    private void calculateMaxClipCount ()
    {
        Cursor cursor = getMediaAsCursor();
        
        int clipIndex = 0;
        
        if (cursor.moveToFirst()) {
            do {
                Media media = new Media(mDB, context, cursor);
                clipIndex = Math.max(clipIndex, media.clipIndex);
            } while (cursor.moveToNext());
        }
        
        mClipCount = clipIndex + 1; //size is one higher than max index
        
        cursor.close();
        
    }
    
    protected ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.Scenes.COL_TITLE, title);
        values.put(StoryMakerDB.Schema.Scenes.COL_THUMBNAIL_PATH, thumbnailPath);
        values.put(StoryMakerDB.Schema.Scenes.COL_PROJECT_INDEX, projectIndex);
        values.put(StoryMakerDB.Schema.Scenes.COL_PROJECT_ID, projectId);
        
        return values;
    }

    // FIXME testme
    public Cursor getMediaAsCursor() {
        String selection = "scene_id=?";
        String[] selectionArgs = new String[] {"" + getId()};
        String orderBy = "clip_index";
        if (mDB == null) {
            return context.getContentResolver().query(ProjectsProvider.MEDIA_CONTENT_URI, null, selection, selectionArgs, orderBy);
        } else {
            return mDB.query(getTable().getTableName(), null, selection, selectionArgs, null, null, orderBy);
        }
    }
       
    public ArrayList<Media> getMediaAsList() {
        Cursor cursor = getMediaAsCursor();
        
        ArrayList<Media> medias = new ArrayList<Media>(mClipCount);
        
        for (int i = 0; i < mClipCount; i++) {
            medias.add(null);
        }
        
        if (cursor.moveToFirst()) {
            do {
            	Media media = new Media(mDB, context, cursor);
                medias.set(media.clipIndex, media);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return medias;
    }

 // FIXME make provider free version
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

 // FIXME make provider free version
    public ArrayList<String> getMediaAsPathList() {
        Cursor cursor = getMediaAsCursor();
        ArrayList<String> paths = new ArrayList<String>(mClipCount);
        
        for (int i = 0; i < mClipCount; i++)
            paths.add(null);
       
        if (cursor.moveToFirst()) {
            do {
            	Media media = new Media(mDB, context, cursor);
                paths.set(media.clipIndex, media.getPath());
            } while (cursor.moveToNext());
        }
        cursor.close();
        return paths;
    }

 // FIXME make provider free version
    public String[] getMediaAsPathArray() {
        ArrayList<String> paths = getMediaAsPathList();
        return paths.toArray(new String[] {});
    }

    /**
     * @param media append this media to the back of the scene's media list
     */
 // FIXME make provider free version
    public void setMedia(int clipIndex, String clipType, String path, String mimeType) {
        Media media = new Media(mDB, context);
        media.setPath(path);
        media.setMimeType(mimeType);
        media.setClipType(clipType);
        media.setClipIndex(clipIndex);
        media.setSceneId(getId());
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
