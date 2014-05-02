package info.guardianproject.mrapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SqliteWrapper;

import info.guardianproject.mrapp.db.StoryMakerDB;

import java.util.Date;

/**
 * 
 * @author Josh Steiner <josh@vitriolix.com>
 *
 */
public class Job extends Model {
    private final String TAG = "Job";
    
	protected int projectId; 			// fk to the Project this job is connected to
    protected int publishJobId; 		// fk to PublishJob, if it's -1, this is itself a PublishJob
    private String type;				// type upload, render and maybe publish
    private String site;				// site key
    private String spec;				// key to a render type spec (video, slideshow, photo, etc) 
    private String result;				// depending on job and site type, might be a url, a id on the publishing site, a file path, etc
	protected int errorCode = -1; 		// -1 for never set, 0 for a-ok, positive for error state
	protected String errorMessage = ""; // error message to show user
	protected Date queuedAt = null; 	// long stored in database as 8-bit int.  
    protected Date finishedAt = null; 	// long stored in database as 8-bit int

    // CONSTRUCTORS //////
    
    Job(Context context) {
        super(context);
    }
    
    Job (SQLiteDatabase db, Context context) {
        super(db, context);
    }
    
    public Job(Context context, int id, int projectId, int publishJobId, String type,
			String site, String spec, String result, int errorCode,
			String errorMessage, Date queuedAt, Date finishedAt) {
		super(context);
		this.id = id;
		this.projectId = projectId;
		this.publishJobId = publishJobId;
		this.type = type;
		this.site = site;
		this.spec = spec;
		this.result = result;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.queuedAt = queuedAt;
		this.finishedAt = finishedAt;
	}
    
    public Job(SQLiteDatabase db, Context context, int id, int projectId, int publishJobId, String type,
			String site, String spec, String result, int errorCode,
			String errorMessage, Date queuedAt, Date finishedAt) {
		super(db, context);
		this.id = id;
		this.projectId = projectId;
		this.publishJobId = publishJobId;
		this.type = type;
		this.site = site;
		this.spec = spec;
		this.result = result;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.queuedAt = queuedAt;
		this.finishedAt = finishedAt;
	}
    
    public Job(Context context, int id, int projectId, int publishJobId, String type, String site, String spec) {
		super(context);
		this.id = id;
		this.projectId = projectId;
		this.publishJobId = publishJobId;
		this.type = type;
		this.site = site;
		this.spec = spec;
		this.result = null;
		this.errorCode = -1;
		this.errorMessage = null;
		this.queuedAt = null;
		this.finishedAt = null;
	}
    
    public Job(SQLiteDatabase db, Context context, int id, int projectId, int publishJobId, String type, String site, String spec) {
		super(db, context);
		this.id = id;
		this.projectId = projectId;
		this.publishJobId = publishJobId;
		this.type = type;
		this.site = site;
		this.spec = spec;
		this.result = null;
		this.errorCode = -1;
		this.errorMessage = null;
		this.queuedAt = null;
		this.finishedAt = null;
	}
    
    /**
     * Inflate record from a cursor
     *  
     * @param context
     * @param cursor
     */
    public Job(Context context, Cursor cursor) {
        this(context,
             cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.ID)),
             cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.COL_PROJECT_ID)),
             cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.COL_PUBLISH_JOB_ID)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.COL_TYPE)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.COL_SITE)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.COL_SPEC)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.COL_RESULT)),
             cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.COL_ERROR_CODE)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.COL_ERROR_MESSAGE)),
             (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.COL_QUEUED_AT)) ?
                 new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.COL_QUEUED_AT))) : null),
             (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.COL_FINISHED_AT)) ?
                 new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Jobs.COL_FINISHED_AT))) : null));
        // queued_at & finished_at column are nullable, need to avoid errors creating Date objects
    }

    /**
     * Default constructor to inflate record from a cursor via direct db access.  This should be used within DB Migrations and within an Model or Tabel classes
     * @param db
     * @param context
     * @param cursor
     */
    public Job(SQLiteDatabase db, Context context, Cursor cursor) {
        this(context, cursor);
        this.mDB = db;
    }

	// CONSTANTS ///////
	public final static int ERROR_OUT_OF_SPACE = 1;
	public final static int ERROR_READ_PERMISSION = 2;
	public final static int ERROR_WRITE_PERMISSION = 3;
	public final static int ERROR_INPUT_FORMAT = 4;
	public final static int ERROR_404 = 404;

	// CALLBACK INTERFACE //////
	
	// TODO can't use a callback interface, we need to use Handler's since we cross Activity bounds
	public static class Callback {
		private Context context;
		
		Callback(Context context) {
			this.context = context;
		}
		
		public Job getJob(int jobId) {
			return (Job) (new JobTable(null)).get(context, jobId);
		}
		
		public void onSucess(int jobId, String code) { throw new UnsupportedOperationException("Not implemented yet"); }
		
		/**
		 * 
		 * @param errorCode code referencing the type of error
		 * @param message Human readable error message returned from the rendering engine
		 */
		public void onFailure(int jobId, int errorCode, String errorMessage) { throw new UnsupportedOperationException("Not implemented yet"); }
	}
	
	// HELPER METHODS //////

    public PublishJob getPublishJob() {
        return (PublishJob) (new PublishJobTable(mDB)).get(context, publishJobId);
    }
    
    public Project getProject() {
        return (Project) (new ProjectTable(mDB)).get(context, projectId);
    }
	
	// GETTERS AND SETTERS //////////////
	
	public int getPublishJobId() {
		return publishJobId;
	}

	public void setPublishJobId(int publishJobId) {
		this.publishJobId = publishJobId;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public Date getQueuedAt() {
		return queuedAt;
	}
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
	
	public boolean isFinished() {
	    return finishedAt != null;
	}

	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}
	
	public void setFinishedAtNow() {
	    setFinishedAt(new Date());
	}

    protected ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.Jobs.COL_PROJECT_ID, projectId);
        values.put(StoryMakerDB.Schema.Jobs.COL_PUBLISH_JOB_ID, publishJobId);
        values.put(StoryMakerDB.Schema.Jobs.COL_TYPE, type);
        values.put(StoryMakerDB.Schema.Jobs.COL_SITE, site);
        values.put(StoryMakerDB.Schema.Jobs.COL_SPEC, spec);
        values.put(StoryMakerDB.Schema.Jobs.COL_RESULT, result);
        values.put(StoryMakerDB.Schema.Jobs.COL_ERROR_CODE, errorCode);
        values.put(StoryMakerDB.Schema.Jobs.COL_ERROR_MESSAGE, errorMessage);
        if (queuedAt != null) {
            values.put(StoryMakerDB.Schema.Jobs.COL_QUEUED_AT, queuedAt.getTime());
        }
        if (finishedAt != null) {
            values.put(StoryMakerDB.Schema.Jobs.COL_FINISHED_AT, finishedAt.getTime());
        }
        // store dates as longs(8-bit ints)
        // can't put null in values set, so only add entry if non-null
        
        return values;
    }

	@Override
	protected Table getTable() {
        if (mTable == null) {
            mTable = new JobTable(mDB);
        }
        return mTable;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
    
    public boolean isSite(String string) {
        return site.equals(string);
    }

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}
    
    public boolean isSpec(String string) {
        return spec.equals(string);
    }

	public String getSpec() {
		return spec;
	}

	public void setSpec(String spec) {
		this.spec = spec;
	}
	
	public boolean isType(String string) {
	    return type.equals(string);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
