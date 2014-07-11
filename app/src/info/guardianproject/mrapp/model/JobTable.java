package info.guardianproject.mrapp.model;

import java.util.ArrayList;
import java.util.Date;

import net.sqlcipher.database.SQLiteDatabase;
import android.content.Context;
import android.net.Uri;
import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;


/**
 * 
 * @author Josh Steiner <josh@vitriolix.com>
 *
 * This is fake table, the real tables are based on the subclasses
 */
public class JobTable extends Table {
    private final String TAG = "JobTable";
    
	public final static String TYPE_UPLOAD = "upload";
	public final static String TYPE_RENDER = "render";
//	public final static String TYPE_PUBLISH = "publish";

	public JobTable() {
	    
	}
	
    public JobTable(SQLiteDatabase db) {
        super(db);
    }
	
    @Override
    protected String getTableName() {
        return StoryMakerDB.Schema.Jobs.NAME;
    }
    
    @Override
    protected String getIDColumnName() {
        return StoryMakerDB.Schema.Jobs.ID;
    }

    @Override
    protected Uri getURI() {
        return ProjectsProvider.JOBS_CONTENT_URI;
    }

    @Override
    protected String getProviderBasePath() {
        return ProjectsProvider.JOBS_BASE_PATH;
    }

    public ArrayList<Job> getUnfinishedAsList(Context context, String type, PublishJob publishJob, String site) {
        // FIXME this isn't very optimized
    	// type can be TYPE_UPLOAD or TYPE_RENDER
        ArrayList<Job> jobs = (ArrayList<Job>) getAllAsList(context); // TODO need to make this method for reals
        ArrayList<Job> purgedList = null;
        if (jobs != null) {
            for (Job job: jobs) {
                if ((job.getFinishedAt() == null) 
                        && job.getType().equals(type) 
                        && job.getPublishJobId() == publishJob.getId()
                        && (site == null ? true : job.getSite().equals(site))) { // FIXME do we need to check something other than null?
//                    jobs.remove(job); // it's finished.  purge it
                    if (purgedList == null) {
                        purgedList = new ArrayList<Job>();
                    }
                    purgedList.add(job);
                }
            }
        }
        return purgedList;
    }
	
    // job's are only ready to be run if they have a queuedAt time.  You can enter jobs in the table before they are ready to run
    public Job getNextUnfinished(Context context, String type, PublishJob publishJob, String site) {
        ArrayList<Job> jobs = getUnfinishedAsList(context, type, publishJob, site);
        if (jobs != null) {
            return jobs.get(0); // FIXME is the 0th really the next in line ?  is the list sorted by id?
        } else {
            return null;
        }
        // FIXME this isn't very optimized
//    	Job newJob = null;
//    	if (type == JobTable.TYPE_RENDER) {
//			newJob = new Job(context, 1, 1, JobTable.TYPE_UPLOAD, Auth.SITE_YOUTUBE, null);
//		} else if ((type == JobTable.TYPE_UPLOAD)) { 		
//			newJob  = new Job(context, 1, 2, JobTable.TYPE_UPLOAD, Auth.STORYMAKER, null);
//		}
//    	return newJob;
    }
    
    /**
     * This will match any jobs that are finished that match the correct type and were finished after the passed date
     * @param context
     * @param type
     * @param date
     * @return
     */
    public Job getMatchingFinishedJob(Context context, int projectId, String type, String spec, Date date) { // FIXME this isn't very optimized
        ArrayList<Job> jobs = (ArrayList<Job>) getAllAsList(context); // TODO need to make this method for reals
        if (jobs != null) {
            for (Job job: jobs) {
                if ((job.getFinishedAt() != null) 
                        && job.getProjectId() == projectId
                        && job.getType().equals(type) 
                        && job.getSpec().equals(spec) 
                        && job.getFinishedAt().after(date)) {
                    return job;
                }
            }
        }
        return null;
    }
    
    public static Job cloneJob(Context context, Job job) {
        Job newJob = new Job(context);
        newJob.projectId = job.projectId;
        newJob.publishJobId = job.publishJobId;
        newJob.setType(job.getType());
        newJob.setSite(job.getSite());
        newJob.setSpec(job.getSpec());
        newJob.setResult(job.getResult());
        newJob.errorCode = job.errorCode;
        newJob.errorMessage = job.errorMessage;
        newJob.queuedAt = job.queuedAt;
        newJob.finishedAt = job.finishedAt;
        newJob.save();
        return newJob;
    }
}
