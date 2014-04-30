package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.publish.sites.StoryMakerUploader;
import info.guardianproject.mrapp.publish.sites.YoutubeUploader;

import org.holoeverywhere.app.Activity;
import android.content.Context;
import android.util.Log;

public  class UploadWorker extends WorkerBase {
    private final String TAG = "UploadService";
    
	private PublishController controller;
	private static UploadWorker instance = null;
	
	private UploadWorker(Context context, PublishController controller) {
	    mContext = context;
        this.controller = controller;
    }
	
	public static UploadWorker getInstance(Context context, PublishController controller) {
		if (instance == null) {
			instance = new UploadWorker(context, controller);
		}
		return instance;
	}
	
	public void start() {
		// TODO guard against multiple calls if we are running already
//		ArrayList<Job> jobs = (ArrayList<Job>) (new JobTable(db)).getUnfinishedAsList(context, JobTable.TYPE_UPLOAD);
		Job job = (new JobTable(null)).getNextUnfinished(mContext, JobTable.TYPE_UPLOAD);
		UploaderBase uploader = null;
		if (job != null) {
    		if (job.isSite(Auth.SITE_YOUTUBE)) {
    			uploader = new YoutubeUploader(mContext, this, job);
    		} else if (job.isSite(Auth.STORYMAKER)) {
    			uploader = new StoryMakerUploader(mContext, this, job);
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
