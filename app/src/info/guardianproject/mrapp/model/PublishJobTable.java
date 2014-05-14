package info.guardianproject.mrapp.model;

import java.util.ArrayList;
import java.util.Arrays;

import net.sqlcipher.database.SQLiteDatabase;
import info.guardianproject.mrapp.Utils;
import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.content.Context;
import android.net.Uri;


/**
 * 
 * @author Josh Steiner <josh@vitriolix.com>
 *
 */
public class PublishJobTable extends Table {
    private final static String TAG = "PublishJobTable";
    

    public PublishJobTable() {
        
    }
    
    public PublishJobTable(SQLiteDatabase db) {
        super(db);
    }
    
    @Override            
    protected String getTableName() {
        return StoryMakerDB.Schema.PublishJobs.NAME;
    }
    
    @Override
    protected String getIDColumnName() {
        return StoryMakerDB.Schema.PublishJobs.ID;
    }

    @Override
    protected Uri getURI() {
        return ProjectsProvider.PUBLISH_JOBS_CONTENT_URI;
    }

    @Override
    protected String getProviderBasePath() {
        return ProjectsProvider.PUBLISH_JOBS_BASE_PATH;
    }
    
    // HELPERS //////
    
    public ArrayList<PublishJob> getUnfinishedAsList(Context context, int projectId, String[] siteKeys) {
        // FIXME this isn't very optimized
        // type can be TYPE_UPLOAD or TYPE_RENDER
        ArrayList<PublishJob> jobs = (ArrayList<PublishJob>) getAllAsList(context); // TODO need to make this method for reals
        ArrayList<PublishJob> purgedList = null;
        if (jobs != null) {
            for (PublishJob job: jobs) {
                if ((job.finishedAt == null) // FIXME this should be handled in SQL not here
                        && job.projectId == projectId
                        && keysMatch(job.siteKeys, siteKeys)) { // FIXME this siteKeys check is fragile, we should sort the list first
                    if (purgedList == null) {
                        purgedList = new ArrayList<PublishJob>();
                    }
                    purgedList.add(job);
                }
            }
        }
        return purgedList;
    }
    
    // FIXME kaybe we need to trim the arrays to weed out dupes?
    private boolean keysMatch(String[] keys1, String[] keys2) {
        Arrays.sort(keys1);
        Arrays.sort(keys2);
        String keyString1 = Utils.stringArrayToCommaString(keys1);
        String keyString2 = Utils.stringArrayToCommaString(keys2);
        return keyString1.equals(keyString2);
    }
    
    // job's are only ready to be run if they have a queuedAt time.  You can enter jobs in the table before they are ready to run
    public PublishJob getNextUnfinished(Context context, int projectId, String[] siteKeys) {
        ArrayList<PublishJob> jobs = getUnfinishedAsList(context, projectId, siteKeys);
        if (jobs != null) {
            return jobs.get(0); // FIXME is the 0th really the next in line ?  is the list sorted by id?
        } else {
            return null;
        }
    }
}
