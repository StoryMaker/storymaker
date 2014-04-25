package info.guardianproject.mrapp.model;

import net.sqlcipher.database.SQLiteDatabase;
import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.net.Uri;


/**
 * 
 * @author Josh Steiner <josh@vitriolix.com>
 *
 */
public class PublishJobTable extends JobTable {
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
}
