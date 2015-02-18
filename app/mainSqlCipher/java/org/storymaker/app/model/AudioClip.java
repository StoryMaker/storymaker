package org.storymaker.app.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;

import org.ffmpeg.android.MediaUtils;

import org.storymaker.app.AppConstants;
import org.storymaker.app.R;
import org.storymaker.app.db.ProjectsProvider;
import org.storymaker.app.db.StoryMakerDB;
import org.storymaker.app.media.MediaProjectManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class AudioClip extends Model {
	private static final String TAG = "AudioClip";

    protected int sceneId; // foreign key to the Scene which holds this media
    protected String path;
    protected String positionClipId; // can be null.  card id we are linked to either this or the next must have a value, but only one
    protected int positionIndex; // can null
    protected float volume; // 1.0 is full volume
    protected int clipSpan;  // how many clips it should try to span
    protected boolean truncate; // should this play out past the clips its spans, or trim its end to match
    protected boolean overlap; // if overlap the next clip or push it out, can we
    protected boolean fillRepeat;  // repeat to fill if this audioclip is shorter than the clips it spans
    protected Date createdAt; // long stored in database as 8-bit int
    protected Date updatedAt; // long stored in database as 8-bit int

    public final static int IMAGE_SAMPLE_SIZE = 4;

    /**
     * Create a new, blank record via the Content Provider interface
     * 
     * @param context
     */
    public AudioClip(Context context) {
        super(context);
    }

    /**
     * Create a new, blank record via direct db access.  
     * 
     * This should be used within DB Migrations and Model or Table classes
     *  
     * @param db
     * @param context
     */
    public AudioClip(SQLiteDatabase db, Context context) {
        super(context);
        this.mDB = db;
    }

    /**
     * Create a Model object via direct params
     * 
     * @param context
     * @param id
     * @param sceneId
     * @param path
     * @param positionClipId
     * @param positionIndex
     * @param volume
     * @param clipSpan
     * @param truncate
     * @param overlap
     * @param fillRepeat
     * @param createdAt
     * @param updatedAt
     */
    public AudioClip(Context context, int id, int sceneId, String path, String positionClipId, int positionIndex, float volume,
                     int clipSpan, boolean truncate, boolean overlap, boolean fillRepeat, Date createdAt, Date updatedAt) {
        super(context);
        this.context = context;
        this.sceneId = sceneId;
        this.id = id;
        this.path = path;
        this.positionClipId = positionClipId;
        this.positionIndex = positionIndex;
        this.volume = volume;
        this.clipSpan = clipSpan;
        this.truncate = truncate;
        this.overlap = overlap;
        this.fillRepeat = fillRepeat;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Create a Model object via direct params
     *
     * @param context
     * @param sceneId
     * @param path
     * @param positionClipId
     * @param positionIndex
     * @param volume
     * @param clipSpan
     * @param truncate
     * @param overlap
     * @param fillRepeat
     * @param createdAt
     * @param updatedAt
     */
    public AudioClip(Context context, int sceneId, String path, String positionClipId, int positionIndex, float volume,
                     int clipSpan, boolean truncate, boolean overlap, boolean fillRepeat, Date createdAt, Date updatedAt) {
        super(context);
        this.context = context;
        this.sceneId = sceneId;
        this.path = path;
        this.positionClipId = positionClipId;
        this.positionIndex = positionIndex;
        this.volume = volume;
        this.clipSpan = clipSpan;
        this.truncate = truncate;
        this.overlap = overlap;
        this.fillRepeat = fillRepeat;
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
     * @param sceneId
     * @param path
     * @param positionClipId
     * @param positionIndex
     * @param volume
     * @param clipSpan
     * @param truncate
     * @param overlap
     * @param fillRepeat
     * @param createdAt
     * @param updatedAt
     */
    public AudioClip(SQLiteDatabase db, Context context, int id, int sceneId, String path, String positionClipId, int positionIndex, float volume,
                     int clipSpan, boolean truncate, boolean overlap, boolean fillRepeat, Date createdAt, Date updatedAt) {
        this(context, id, sceneId, path, positionClipId, positionIndex, volume, clipSpan, truncate, overlap, fillRepeat, createdAt, updatedAt);
        this.mDB = db;
    }
    
    /**
     * Create a Model object via direct params, except for auto-incremented primary key, via direct db access.
     * 
     * This should be used within DB Migrations and Model or Table classes
     *
     * @param db
     * @param context
     * @param sceneId
     * @param path
     * @param positionClipId
     * @param positionIndex
     * @param volume
     * @param clipSpan
     * @param truncate
     * @param overlap
     * @param fillRepeat
     * @param createdAt
     * @param updatedAt
     */
    public AudioClip(SQLiteDatabase db, Context context, int sceneId, String path, String positionClipId, int positionIndex, float volume,
                     int clipSpan, boolean truncate, boolean overlap, boolean fillRepeat, Date createdAt, Date updatedAt) {
        this(context, sceneId, path, positionClipId, positionIndex, volume, clipSpan, truncate, overlap, fillRepeat, createdAt, updatedAt);
        this.mDB = db;
    }

    /**
     * Inflate record from a cursor via the Content Provider
     * 
     * @param context
     * @param cursor
     */
    public AudioClip(Context context, Cursor cursor) {
        // FIXME use column id's directly to optimize this one schema stabilizes
        this(
                context,
                cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.AudioClip.ID)),
                cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.AudioClip.COL_SCENE_ID)),
                cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.AudioClip.COL_PATH)),
                cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.AudioClip.COL_POSITION_CLIP_ID)),
                cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.AudioClip.COL_POSITION_INDEX)),
                cursor.getFloat(cursor.getColumnIndex(StoryMakerDB.Schema.AudioClip.COL_VOLUME)),
                cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.AudioClip.COL_CLIP_SPAN)),
                cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.AudioClip.COL_TRUNCATE)) == 1,
                cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.AudioClip.COL_OVERLAP)) == 1,
                cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.AudioClip.COL_FILL_REPEAT)) == 1,
                (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Media.COL_CREATED_AT)) ?
                        new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Media.COL_CREATED_AT))) : null),
                (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Media.COL_UPDATED_AT)) ?
                        new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Media.COL_UPDATED_AT))) : null));
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
    public AudioClip(SQLiteDatabase db, Context context, Cursor cursor) {
        this(context, cursor);
        this.mDB = db;
    }

    public static AudioClip getInstanceFromLigerAudioClip(Context context, scal.io.liger.model.AudioClipFull ac, int sceneId, String path) {
        AudioClip audioClip = new AudioClip(context, sceneId, ac.getPath(), ac.getPositionClipId(), ac.getPositionIndex(), ac.getVolume(), ac.getClipSpan(), ac.doTruncate(), ac.doOverlap(), ac.doFillRepeat(), new Date(), new Date());

        return audioClip;
    }

    @Override
    protected Table getTable() {
        if (mTable == null) {
            mTable = new AudioClipTable(mDB);
        }
        return mTable;
    }
    
    /***** Calculated object level methods *****/

    protected ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.AudioClip.COL_SCENE_ID, sceneId);
        values.put(StoryMakerDB.Schema.AudioClip.COL_PATH, path);
        values.put(StoryMakerDB.Schema.AudioClip.COL_POSITION_CLIP_ID, positionClipId);
        values.put(StoryMakerDB.Schema.AudioClip.COL_POSITION_INDEX, positionIndex);
        values.put(StoryMakerDB.Schema.AudioClip.COL_VOLUME, volume);
        values.put(StoryMakerDB.Schema.AudioClip.COL_CLIP_SPAN, clipSpan);
        values.put(StoryMakerDB.Schema.AudioClip.COL_TRUNCATE, truncate);
        values.put(StoryMakerDB.Schema.AudioClip.COL_OVERLAP, overlap);
        values.put(StoryMakerDB.Schema.AudioClip.COL_FILL_REPEAT, fillRepeat);
        if (createdAt != null) {
            values.put(StoryMakerDB.Schema.Media.COL_CREATED_AT, createdAt.getTime());
        }
        if (updatedAt != null) {
            values.put(StoryMakerDB.Schema.Media.COL_UPDATED_AT, updatedAt.getTime());
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
            setUpdatedAt(new Date());
            insert();
        } else {
            cursor.close();
            setUpdatedAt(new Date());
            update();            
        }
    }
    
//    // FIXME make a db only version of this
//    // FIXME testme
//    @Override
//    public void insert() {
//    	// There can be only one!  check if a media item exists at this location already, if so purge it first.
//    	Cursor cursorDupes = (new MediaTable(mDB)).getAsCursor(context, sceneId, clipIndex);
//    	if ((cursorDupes.getCount() > 0) && cursorDupes.moveToFirst()) {
//        	// FIXME we should allow audio clips to remain so they can be mixed down with their buddies
//    		do {
//    			(new Media(mDB, context, cursorDupes)).delete(); // always pass mDB when newing models within models, this way if we are in provider mode that is null anyhow
//    		} while (cursorDupes.moveToNext());
//    	}
//
//        ContentValues values = getValues();
//
//        if (mDB == null) {
//        	Uri uri = context.getContentResolver().insert(ProjectsProvider.MEDIA_CONTENT_URI, values);
//        	String lastSegment = uri.getLastPathSegment();
//            int newId = Integer.parseInt(lastSegment);
//            this.setId(newId);
//        } else {
//        	int newId = (int)mDB.insert((new MediaTable(mDB)).getTableName(), null, values);
//        	this.setId(newId);
//        }
//
//        cursorDupes.close();
//    }

    
    /***** getters and setters *****/

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPositionClipId() {
        return positionClipId;
    }

    public void setPositionClipId(String positionClipId) {
        this.positionClipId = positionClipId;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public int getClipSpan() {
        return clipSpan;
    }

    public void setClipSpan(int clipSpan) {
        this.clipSpan = clipSpan;
    }

    public boolean doTruncate() {
        return truncate;
    }

    public void setTruncate(boolean truncate) {
        this.truncate = truncate;
    }

    public boolean doOverlap() {
        return overlap;
    }

    public void setOverlap(boolean overlap) {
        this.overlap = overlap;
    }

    public boolean doFillRepeat() {
        return fillRepeat;
    }

    public void setFillRepeat(boolean fillRepeat) {
        this.fillRepeat = fillRepeat;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Scene getScene() {
        // TODO opt caching
        return (Scene) (new SceneTable()).get(context, sceneId);
    }

    /**
     * this calculates a float time in seconds that this is supposed to start within the sample. if the start is trimmed it will be positive, if we need to pad the start it will be negative
     * @return
     */
    public String getStartTime() {
        Scene scene = getScene();
        ArrayList<Media> media = scene.getMediaAsList();
        int index = -1;
        if (positionIndex > -1) {
            index = positionIndex;
        } else {
            // TODO we need to handle positionClipId ... by the time we get here it should probably have been flatted down to just the index
        }


        if (index == 0) {
            return "0"; // FIXME do we need to support trimming narration files?
        } else if (index > 0) {
            int length = 0;
            for (int i = 0 ; i < index ; i++) {
                length += media.get(i).getTrimmedDuration(); // FIXME does this factor in crossfades?
                // FIXME handle case where index > media.size()
                return "-" + length / 1000f; // negative time will be padded in export
            }
        } else {
            // TODO handle error
        }
        return "0";
    }

    public String getDuration() {
        Scene scene = getScene();
        ArrayList<Media> media = scene.getMediaAsList();
        int index = -1;
        if (positionIndex > -1) {
            index = positionIndex;
        } else {
            // TODO we need to handle positionClipId ... by the time we get here it should probably have been flatted down to just the index
        }

        // index, clipSpan and truncate
        int length = 0;
        // clipspan is 1 if we should only span the clip we are indexed to, so subtract 1 to get the extra number of clips we should span
        int indexEnd = Math.min((index + clipSpan - 1), (media.size() - 1));
        for (int i = index ; i < indexEnd; i++) {
            // FIXME handle non-truncated, this is basically assumes truncate
            length += media.get(i).getTrimmedDuration(); // FIXME does this factor in crossfades?
        }
        return "" + length;
    }
}
