package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.AuthTable;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.publish.sites.StoryMakerUploader;
import info.guardianproject.mrapp.publish.sites.YoutubeUploader;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import net.sqlcipher.database.SQLiteDatabase;

public  class UploadService extends ServiceBase {
    private final String TAG = "UploadService";
    
	private Context context;
	private PublishController controller;
	private static UploadService instance = null;
	
	private UploadService(Context context, PublishController controller) {
        this.context = context;
        this.controller = controller;
    }
	
	public static UploadService getInstance(Context context, PublishController controller) {
		if (instance == null) {
			instance = new UploadService(context, controller);
		}
		return instance;
	}
	
	public void start() {
		// TODO guard against multiple calls if we are running already
//		ArrayList<Job> jobs = (ArrayList<Job>) (new JobTable(db)).getUnfinishedAsList(context, JobTable.TYPE_UPLOAD);
		Job job = (new JobTable(null)).getNextUnfinished(context, JobTable.TYPE_UPLOAD);
		UploaderBase uploader = null;
		if (job != null) {
    		if (job.isSite(Auth.SITE_YOUTUBE)) {
    			uploader = new YoutubeUploader(context, this, job);
    		} else if (job.isSite(Auth.STORYMAKER)) {
    			uploader = new StoryMakerUploader(context, this, job);
    		} 
    		uploader.start();
		}
	}
	
	public void jobSucceeded(Job job, String code) {
        Log.d(TAG, "jobSucceeded: " + job + ", with code: " + code);
		// TODO start the next job
		controller.jobSucceeded(job, code);
	}
	
	public void jobFailed(Job job, int errorCode, String errorMessage) {
        Log.d(TAG, "jobFailed: " + job + ", with errorCode: " + errorCode + ", and errorMessage: " + errorMessage);
		// TODO start the next job
		controller.jobFailed(job, errorCode, errorMessage);
	}
	
//	public void jobFinished() {
//		// TODO check if jobs are all done, if so let's die
////		finish();
//	}
}
