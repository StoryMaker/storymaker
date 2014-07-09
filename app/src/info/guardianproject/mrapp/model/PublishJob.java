package info.guardianproject.mrapp.model;

import info.guardianproject.mrapp.Utils;
import info.guardianproject.mrapp.db.StoryMakerDB;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

/**
 *  job which wraps renderjob's and upload jobs.  takes callbacks from render and publish classes to assemble the entire rendering

    fields:

    job_queued, datetime
    job_finished, datetime
    project_id, fk
    jobs_to_sites, fk to join of RenderJobs and which sites they should be published to when complete
    
 * 
 * @author Josh Steiner <josh@vitriolix.com>
 *
 */
public class PublishJob extends Model {
    private final String TAG = "PublishJob";
    
	protected int projectId; 			// fk to the Project this job is connected to
	protected String[] siteKeys = null; // TODO not sure how to store this.  comma separated string field probably
	protected Date queuedAt = null; 	// long stored in database as 8-bit int.  
    protected Date finishedAt = null; 	// long stored in database as 8-bit int.
    protected boolean useTor = false;
    protected boolean publishToStoryMaker = false;

    /**
     * Default constructor to inflate record from a cursor via direct db access.  This should be used within DB Migrations and within an Model or Tabel classes
     * @param db
     * @param context
     * @param cursor
     */
    public PublishJob(SQLiteDatabase db, Context context, Cursor cursor) {
        this(context, cursor);
        this.mDB = db;
    }
    
    public PublishJob(Context context, int id, int projectId, String[] siteKeys, boolean useTor, boolean publishToStoryMaker) {
        this(context, id, projectId, siteKeys, useTor, publishToStoryMaker, null, null);
    }
    
    public PublishJob(SQLiteDatabase db, Context context, int id, int projectId, String[] siteKeys, boolean useTor, boolean publishToStoryMaker) {
        this(db, context, id, projectId, siteKeys, useTor, publishToStoryMaker, null, null);
    }

    PublishJob(Context context) {
        super(context);
    }

    PublishJob (SQLiteDatabase db, Context context) {
        super(db, context);
    }
    
	public PublishJob(Context context, int id, int projectId, String[] siteKeys, boolean useTor, boolean publishToStoryMaker, Date queuedAt, Date finishedAt) {
		super(context);
		this.id = id;
		this.projectId = projectId;
		this.siteKeys = siteKeys;
		this.queuedAt = queuedAt;
		this.finishedAt = finishedAt;
		this.useTor = useTor;
		this.publishToStoryMaker = publishToStoryMaker;
	}
	
	public PublishJob(SQLiteDatabase db, Context context, int id, int projectId, String[] siteKeys, boolean useTor, boolean publishToStoryMaker, Date queuedAt, Date finishedAt) {
		super(db, context);
		this.id = id;
		this.projectId = projectId;
		this.siteKeys = siteKeys;
		this.queuedAt = queuedAt;
		this.finishedAt = finishedAt;
        this.useTor = useTor;
        this.publishToStoryMaker = publishToStoryMaker;
	}
	
	// additional constructors that do not require an id value:
	
    public PublishJob(Context context, int projectId, String[] siteKeys, boolean useTor, boolean publishToStoryMaker) {
        this(context, projectId, siteKeys, useTor, publishToStoryMaker, null, null);
    }
    
    public PublishJob(SQLiteDatabase db, Context context, int projectId, String[] siteKeys, boolean useTor, boolean publishToStoryMaker) {
        this(db, context, projectId, siteKeys, useTor, publishToStoryMaker, null, null);
    }
    
    public PublishJob(Context context, int projectId, String[] siteKeys, boolean useTor, boolean publishToStoryMaker, Date queuedAt, Date finishedAt) {
        super(context);
        this.projectId = projectId;
        this.siteKeys = siteKeys;
        this.queuedAt = queuedAt;
        this.finishedAt = finishedAt;
        this.useTor = useTor;
        this.publishToStoryMaker = publishToStoryMaker;
    }
    
    public PublishJob(SQLiteDatabase db, Context context, int projectId, String[] siteKeys, boolean useTor, boolean publishToStoryMaker, Date queuedAt, Date finishedAt) {
        super(db, context);
        this.projectId = projectId;
        this.siteKeys = siteKeys;
        this.queuedAt = queuedAt;
        this.finishedAt = finishedAt;
        this.useTor = useTor;
        this.publishToStoryMaker = publishToStoryMaker;
    }
    
    /**
     * Inflate record from a cursor
     *  
     * @param context
     * @param cursor
     */
    public PublishJob(Context context, Cursor cursor) {
        this(context,
            cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.PublishJobs.ID)),
            cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.PublishJobs.COL_PROJECT_ID)),
            Utils.commaStringToStringArray(cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.PublishJobs.COL_SITE_KEYS))),
            cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.PublishJobs.COL_USE_TOR)) > 0,
            cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.PublishJobs.COL_PUBLISH_TO_STORYMAKER)) > 0,
            (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.PublishJobs.COL_QUEUED_AT)) ?
                new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.PublishJobs.COL_QUEUED_AT))) : null),
            (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.PublishJobs.COL_FINISHED_AT)) ?
                new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.PublishJobs.COL_FINISHED_AT))) : null));
        // queued_at & finished_at column are nullable, need to avoid errors creating Date objects
    }

    protected ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.PublishJobs.COL_PROJECT_ID, projectId);
        values.put(StoryMakerDB.Schema.PublishJobs.COL_SITE_KEYS, (siteKeys != null) ? Utils.stringArrayToCommaString(siteKeys) : null);
        values.put(StoryMakerDB.Schema.PublishJobs.COL_USE_TOR, useTor);
        values.put(StoryMakerDB.Schema.PublishJobs.COL_PUBLISH_TO_STORYMAKER, publishToStoryMaker);        
        if (queuedAt != null) {
            values.put(StoryMakerDB.Schema.PublishJobs.COL_QUEUED_AT, queuedAt.getTime());
        }
        if (finishedAt != null) {
            values.put(StoryMakerDB.Schema.PublishJobs.COL_FINISHED_AT, finishedAt.getTime());
        }
        // store dates as longs(8-bit ints)
        // can't put null in values set, so only add entry if non-null
        
        return values;
    }
    
	// FIXME this isn't thread safe
	@Override
	protected Table getTable() {
        if (mTable == null) {
            mTable = new PublishJobTable(mDB);
        }
        return mTable;
	}
    
    public Cursor getJobsAsCursor() {
        String selection = StoryMakerDB.Schema.Jobs.COL_PUBLISH_JOB_ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
        if (mDB == null) {
            return context.getContentResolver().query((new JobTable()).getURI(), null, selection, selectionArgs, null);
        } else {
            return mDB.query(mTable.getTableName(), null, selection, selectionArgs, null, null, null);
        }
    }
    
    public Cursor getJobsAsCursor(String type, String site, String spec) {
        String selection = StoryMakerDB.Schema.Jobs.COL_PUBLISH_JOB_ID + "=?";
//        String[] selectionArgs = new String[] { "" + id, "" + type };
        ArrayList<String> selArgs = new ArrayList<String>();
        selArgs.add("" + id);
        
        if (Utils.stringNotBlank(type)) {
            selection += " and " + StoryMakerDB.Schema.Jobs.COL_TYPE + "=?";
            selArgs.add("" + type);
        } else if (Utils.stringNotBlank(site)) {
            selection += " and " + StoryMakerDB.Schema.Jobs.COL_SITE + "=?";
            selArgs.add("" + site);
        } else if (Utils.stringNotBlank(spec)) {
            selection += " and " + StoryMakerDB.Schema.Jobs.COL_SPEC + "=?";
            selArgs.add("" + spec);
        }
        String[] selectionArgs = selArgs.toArray(new String[] {});
        if (mDB == null) {
            return context.getContentResolver().query((new JobTable()).getURI(), null, selection, selectionArgs, null);
        } else {
            return mDB.query((new JobTable()).getTableName(), null, selection, selectionArgs, null, null, null);
        }
    }
    
    public ArrayList<Job> getJobsAsList() {
        Cursor cursor = getJobsAsCursor();
        return _cursorToList(cursor);
    }
    
    public ArrayList<Job> getJobsAsList(String type, String site, String spec) {
        Cursor cursor = getJobsAsCursor(type, site, spec);
        return _cursorToList(cursor);
    }
    
    private ArrayList<Job> _cursorToList(Cursor cursor) {
        ArrayList<Job> models = null;
        Model model = null;
        models = new ArrayList<Job>();
        if (cursor.moveToFirst()) {
            do {
                model = new Job(mDB, context, cursor);
                ((ArrayList<Job>)models).add((Job)model);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return models;
    }
    
    public String[] getRenderedFilePaths() {
//        if (isFinished()) {
            // FIXME probably should only return finished jobs
            ArrayList<Job> jobs = getJobsAsList(JobTable.TYPE_RENDER, null, null);
            String[] paths = new String[jobs.size()];
            for (int i = 0; i < jobs.size() ; i++) {
                paths[i] = jobs.get(i).getResult();
            }
            return paths;
//        } else {
//            return null;
//        }
    }
    
    // FIXME getLastRenderFilePath needs to not just assume the 0th is the last?
    public String getLastRenderFilePath() {
        String[] paths = getRenderedFilePaths();
        if (paths.length > 0) {
            return paths[0];
        }
        return null;
    }
	
	// GETTERS AND SETTERS //////
	
	public String[] getSiteKeys() {
		return siteKeys;
	}
	
	public void setSiteKeys(String[] keys) {
		siteKeys = keys;
	}

    public int getProjectId() {
        return projectId;
    }

    public Project getProject() {
        return (Project) (new ProjectTable().get(context, projectId));
    }

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public Date getQueuedAt() {
		return queuedAt;
	}

    // TODO This needs to be manually set when a whole batch is ready and be set for Job's attached to this in a transaction
	public void setQueuedAt(Date queuedAt) {
		this.queuedAt = queuedAt;
	}
    
    public void setQueuedAtNow() {
        setQueuedAt(new Date());
    }
    
    public boolean isQueued() {
        return queuedAt != null;
    }

	public Date getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(Date finished) {
		this.finishedAt = finished;
	}
    
    public void setFinishedAtNow() {
        setFinishedAt(new Date());
    }
    
    public boolean isFinished() {
        return finishedAt != null;
    }
    
    public boolean getUseTor() {
        return useTor;
    }
    
    public void setUseTor(boolean useTor) {
        this.useTor = useTor;
    }
    
    public boolean getPublishToStoryMaker() {
        return publishToStoryMaker;
    }
    
    public void setPublishToStoryMaker(boolean val) {
        publishToStoryMaker = val;
    }
}
