package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.publish.sites.StoryMakerUploader;
import info.guardianproject.mrapp.publish.sites.YoutubeUploader;

import org.holoeverywhere.app.Activity;
import android.content.Context;
import android.util.Log;

public  class UploadService extends ServiceBase {
    private final String TAG = "UploadService";
    
	private Activity mActivity;
	private PublishController controller;
	private static UploadService instance = null;
	
	private UploadService(Activity activity, PublishController controller) {
        mActivity = activity;
        this.controller = controller;
    }
	
	public static UploadService getInstance(Activity activity, PublishController controller) {
		if (instance == null) {
			instance = new UploadService(activity, controller);
		}
		return instance;
	}
	
	public void start() {
		// TODO guard against multiple calls if we are running already
//		ArrayList<Job> jobs = (ArrayList<Job>) (new JobTable(db)).getUnfinishedAsList(context, JobTable.TYPE_UPLOAD);
		Job job = (new JobTable(null)).getNextUnfinished(mActivity, JobTable.TYPE_UPLOAD);
		UploaderBase uploader = null;
		if (job != null) {
    		if (job.isSite(Auth.SITE_YOUTUBE)) {
    			uploader = new YoutubeUploader(mActivity, this, job);
    		} else if (job.isSite(Auth.STORYMAKER)) {
    			uploader = new StoryMakerUploader(mActivity, this, job);
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
