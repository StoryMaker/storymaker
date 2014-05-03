package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.publish.sites.FacebookUploader;
import info.guardianproject.mrapp.publish.sites.FlickrUploader;
import info.guardianproject.mrapp.publish.sites.SoundCloudUploader;
import info.guardianproject.mrapp.publish.sites.StoryMakerUploader;
import info.guardianproject.mrapp.publish.sites.YoutubeUploader;

import org.holoeverywhere.app.Activity;
import android.content.Context;
import android.util.Log;

public  class UploadWorker extends WorkerBase {
    private final String TAG = "UploadService";
    
	private PublishController mController;
	private static UploadWorker instance = null;
	
	private UploadWorker(Context context, PublishController controller) {
	    mContext = context;
        this.mController = controller;
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
            } else if (job.isSite(Auth.SITE_FACEBOOK)) {
                uploader = new FacebookUploader(mContext, this, job);
            } else if (job.isSite(Auth.SITE_FLICKR)) {
                uploader = new FlickrUploader(mContext, this, job);
            } else if (job.isSite(Auth.SITE_SOUNDCLOUD)) {
                uploader = new SoundCloudUploader(mContext, this, job);
            } 
    		uploader.start();
		}
	}

    @Override
	public void jobSucceeded(Job job, String code) {
        Log.d(TAG, "jobSucceeded: " + job + ", with code: " + code);
		// TODO start the next job
		mController.jobSucceeded(job, code);
	}

    @Override
	public void jobFailed(Job job, int errorCode, String errorMessage) {
        Log.d(TAG, "jobFailed: " + job + ", with errorCode: " + errorCode + ", and errorMessage: " + errorMessage);
		// TODO start the next job
		mController.jobFailed(job, errorCode, errorMessage);
	}

    @Override
    public void jobProgress(Job job, int progress, String message) {
        mController.jobProgress(job, progress, message);
    }
	
//	public void jobFinished() {
//		// TODO check if jobs are all done, if so let's die
////		finish();
//	}
}
