package org.storymaker.app.model;

import timber.log.Timber;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import org.storymaker.app.db.ProjectsProvider;
import org.storymaker.app.db.StoryMakerDB;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

public class Scene extends Model {
	final private String TAG = "Scene";
    protected String title;
    protected String thumbnailPath;
    protected int projectIndex; // position this scene is in the project
    protected int projectId; // foreign key to the Project which holds this scene
    protected Date createdAt; // long stored in database as 8-bit int
    protected Date updatedAt; // long stored in database as 8-bit int
    
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
     * @param createdAt
     * @param updatedAt
     */
    public Scene(Context context, int id, String title, String thumbnailPath, int projectIndex, int projectId, Date createdAt, Date updatedAt) {
        super(context);
        this.id = id;
        this.title = title;
        this.thumbnailPath = thumbnailPath;
        this.projectIndex = projectIndex;
        this.projectId = projectId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Create a Model object via direct params, except for auto-incremented primary key
     * 
     * @param context
     * @param title
     * @param thumbnailPath
     * @param projectIndex
     * @param projectId
     * @param createdAt
     * @param updatedAt
     */
    public Scene(Context context, String title, String thumbnailPath, int projectIndex, int projectId, Date createdAt, Date updatedAt) {
        super(context);
        this.title = title;
        this.thumbnailPath = thumbnailPath;
        this.projectIndex = projectIndex;
        this.projectId = projectId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
     * @param createdAt
     * @param updatedAt
     */
    public Scene(SQLiteDatabase db, Context context, int id, String title, String thumbnailPath, int projectIndex, int projectId, Date createdAt, Date updatedAt) {
        this(context, id, title, thumbnailPath, projectIndex, projectId, createdAt, updatedAt);
        this.mDB = db;
    }
    
    /**
     * Create a Model object via direct params, except for auto-incremented primary key, via direct db access.
     * 
     * This should be used within DB Migrations and Model or Table classes
     *
     * @param db
     * @param context
     * @param title
     * @param thumbnailPath
     * @param projectIndex
     * @param projectId
     * @param createdAt
     * @param updatedAt
     */
    public Scene(SQLiteDatabase db, Context context, String title, String thumbnailPath, int projectIndex, int projectId, Date createdAt, Date updatedAt) {
        this(context, title, thumbnailPath, projectIndex, projectId, createdAt, updatedAt);
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
                        .getColumnIndex(StoryMakerDB.Schema.Scenes.COL_PROJECT_ID)),
                (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Scenes.COL_CREATED_AT)) ?
                        new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Scenes.COL_CREATED_AT))) : null),
                (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Scenes.COL_UPDATED_AT)) ?
                        new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Scenes.COL_UPDATED_AT))) : null));
        
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
                        .getColumnIndex(StoryMakerDB.Schema.Scenes.COL_PROJECT_ID)),
                (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Scenes.COL_CREATED_AT)) ?
                        new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Scenes.COL_CREATED_AT))) : null),
                (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Scenes.COL_UPDATED_AT)) ?
                        new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Scenes.COL_UPDATED_AT))) : null));

        this.mDB = db;
        calculateMaxClipCount(); // had to dupe the Scene(context, cursor) constructor in here because of this call being fired before we set mDB
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
        if (createdAt != null) {
            values.put(StoryMakerDB.Schema.Scenes.COL_CREATED_AT, createdAt.getTime());
        }
        if (updatedAt != null) {
            values.put(StoryMakerDB.Schema.Scenes.COL_UPDATED_AT, updatedAt.getTime());
        }
        // store dates as longs(8-bit ints)
        // can't put null in values set, so only add entry if non-null
        
        return values;
    }
    
    // insert/update current record
    // need to set created at/updated at date
    @Override
    public void save() {
        Cursor cursor = getTable().getAsCursor(context, id);
        if (cursor.getCount() == 0) {
            cursor.close();
            setCreatedAt(new Date());
            insert();
        } else {
            cursor.close();
            setUpdatedAt(new Date());
            update();            
        }
    }

    // FIXME testme
    public Cursor getMediaAsCursor() {
        String selection = "scene_id=?";
        String[] selectionArgs = new String[] {"" + getId()};
        String orderBy = "clip_index";
        if (mDB == null) {
            return context.getContentResolver().query(ProjectsProvider.MEDIA_CONTENT_URI, null, selection, selectionArgs, orderBy);
        } else {
            return mDB.query((new MediaTable(mDB)).getTableName(), null, selection, selectionArgs, null, null, orderBy);
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


    public Cursor getAudioClipsAsCursor() {
        String selection = "scene_id=?";
        String[] selectionArgs = new String[] {"" + getId()};
        String orderBy = "position_index";
        if (mDB == null) {
            return context.getContentResolver().query(ProjectsProvider.AUDIO_CLIPS_CONTENT_URI, null, selection, selectionArgs, orderBy);
        } else {
            return mDB.query((new AudioClipTable(mDB)).getTableName(), null, selection, selectionArgs, null, null, orderBy);
        }
    }

    public ArrayList<AudioClip> getAudioClipsAsList() {
        Cursor cursor = getAudioClipsAsCursor();

        // FIXME this is using mClipCount, should be based on cursor length right?
        ArrayList<AudioClip> audioClips = new ArrayList<AudioClip>();

        if (cursor.moveToFirst()) {
            do {
                AudioClip audioClip = new AudioClip(mDB, context, cursor);
                audioClips.add(audioClip);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return audioClips;
    }

    // FIXME make provider free version
    public AudioClip[] getAudioClipsAsArray() {
        ArrayList<AudioClip> audioClips = getAudioClipsAsList();
        return audioClips.toArray(new AudioClip[] {});
    }

    // FIXME make provider free version
    public ArrayList<String> getAudioClipsAsPathList() {
        Cursor cursor = getAudioClipsAsCursor();
        ArrayList<String> paths = new ArrayList<String>(mClipCount);

        for (int i = 0; i < mClipCount; i++)
            paths.add(null);

        if (cursor.moveToFirst()) {
            do {
                AudioClip audioClip = new AudioClip(mDB, context, cursor);
                paths.set(audioClip.getPositionIndex(), audioClip.getPath());
            } while (cursor.moveToNext());
        }
        cursor.close();
        return paths;
    }

    // FIXME make provider free version
    public String[] getAudioClipsAsPathArray() {
        ArrayList<String> paths = getAudioClipsAsPathList();
        return paths.toArray(new String[] {});
    }



    /**
     * @param media append this media to the back of the scene's media list
     */
    // FIXME make provider free version
    public Media setMedia(int clipIndex, String clipType, String path, String mimeType) {
        Media media = new Media(mDB, context);
        media.setPath(path);
        media.setMimeType(mimeType);
        media.setClipType(clipType);
        media.setClipIndex(clipIndex);
        media.setSceneId(getId()); // need created/updated?
        media.save();
        mClipCount = Math.max((clipIndex+1), mClipCount);
        return media;
    }

    /**
     * @param media append this media to the back of the scene's media list
     */
    // FIXME make provider free version
    public Media setMedia(@NonNull int clipIndex, @NonNull String clipType, @NonNull String path, @NonNull String mimeType, int trimStart, int trimEnd, int duration, float volume) {
        Media media = new Media(mDB, context);
        media.setPath(path);
        media.setMimeType(mimeType);
        media.setClipType(clipType);
        media.setClipIndex(clipIndex);
        media.setTrimStart(trimStart);
        media.setTrimEnd(trimEnd);
        media.setSceneId(getId()); // need created/updated?
        media.setDuration(duration);
        media.setVolume(volume);

        media.save();
        mClipCount = Math.max((clipIndex+1), mClipCount);
        return media;
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

    public Project getProject() {
        return (Project) new ProjectTable().get(context, projectId);
    }

    /**
     * @param projectId the projectId to set
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    /**
     * @return createdAt
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt 
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return updatedAt
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt 
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean migrate(Project project, Date projectDate) // called on instances of class returned by Project class method
    {
        setCreatedAt(projectDate);
        setUpdatedAt(projectDate);
        update();
        
        return true;
    }    
    
    public void migrateDeleteDupedMedia() {
        Timber.d("Migrating to delete duped Media records in scene: " + this.getId());
        Cursor cursor = getMediaAsCursor();
        ArrayList<Media> medias = new ArrayList<Media>(mClipCount);
        
        // prep the list
        for (int i = 0; i < mClipCount; i++) {
            medias.add(null);
        }

        // build a reduced array list
        if (cursor.moveToFirst()) {
            do {
                Media media = new Media(mDB, context, cursor);
                medias.set(media.clipIndex, media);
            } while (cursor.moveToNext());
        }
        
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Media.ID));
                boolean found = false;
                for (Media m: medias) {
                    if (m.getId() == id) {
                        found = true;
                    }
                }
                // if it's not found in the reduced arraylist, this means it was a dupe record that we can delete
                if (!found) {
                    Timber.d("found a deplicated media: " + id);
                    (new MediaTable(mDB)).delete(context, id);
                }
            } while (cursor.moveToNext());
        }
        
        cursor.close();
    }
}
