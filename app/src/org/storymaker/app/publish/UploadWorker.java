package org.storymaker.app.publish;

import org.storymaker.app.model.Auth;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.JobTable;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.sites.ArchiveUploader;
import org.storymaker.app.publish.sites.FacebookUploader;
import org.storymaker.app.publish.sites.FlickrUploader;
import org.storymaker.app.publish.sites.SSHUploader;
import org.storymaker.app.publish.sites.SoundCloudUploader;
import org.storymaker.app.publish.sites.StoryMakerUploader;
import org.storymaker.app.publish.sites.YoutubeUploader;

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
	
	public void start(PublishJob publishJob) {
//		// TODO guard against multiple calls if we are running already
////		ArrayList<Job> jobs = (ArrayList<Job>) (new JobTable(db)).getUnfinishedAsList(context, JobTable.TYPE_UPLOAD);
//		Job job = (new JobTable(null)).getNextUnfinished(mContext, JobTable.TYPE_UPLOAD, publishJob, null); // FIXME this is grabbing incomplete jobs from previous runs, we should only run the ones from our current publishJob
//		UploaderBase uploader = null;
//		if (job != null) {
//    		if (job.isSite(Auth.SITE_YOUTUBE)) {
//    			uploader = new YoutubeUploader(mContext, this, job);
//    		} else if (job.isSite(Auth.SITE_STORYMAKER)) {
//                uploader = new StoryMakerUploader(mContext, this, job);
//            } else if (job.isSite(Auth.SITE_FACEBOOK)) {
//                uploader = new FacebookUploader(mContext, this, job);
//            } else if (job.isSite(Auth.SITE_FLICKR)) {
//                uploader = new FlickrUploader(mContext, this, job);
//            } else if (job.isSite(Auth.SITE_SOUNDCLOUD)) {
//                uploader = new SoundCloudUploader(mContext, this, job);
//            } else if (job.isSite(Auth.SITE_SSH)) {
//                uploader = new SSHUploader(mContext, this, job);
//            } else if (job.isSite(Auth.SITE_ARCHIVE)) {
//                uploader = new ArchiveUploader(mContext, this, job);
//            } 
//    		uploader.start();
//		}
		// TODO guard against multiple calls if we are running already
		Job job = publishJob.getUploadJobsAsList().get(0); // FIXME extend this to multiple render jobs, for now its hard coded to 1 at a time
		UploaderBase uploader = null;
		if (job != null) {
    		if (job.isSite(Auth.SITE_YOUTUBE)) {
    			uploader = new YoutubeUploader(mContext, this, job);
    		} else if (job.isSite(Auth.SITE_STORYMAKER)) {
                uploader = new StoryMakerUploader(mContext, this, job);
            } else if (job.isSite(Auth.SITE_FACEBOOK)) {
                uploader = new FacebookUploader(mContext, this, job);
            } else if (job.isSite(Auth.SITE_FLICKR)) {
                uploader = new FlickrUploader(mContext, this, job);
            } else if (job.isSite(Auth.SITE_SOUNDCLOUD)) {
                uploader = new SoundCloudUploader(mContext, this, job);
            } else if (job.isSite(Auth.SITE_SSH)) {
                uploader = new SSHUploader(mContext, this, job);
            } else if (job.isSite(Auth.SITE_ARCHIVE)) {
                uploader = new ArchiveUploader(mContext, this, job);
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
	public void jobFailed(Job job, Exception exception, int errorCode, String errorMessage) {
        Log.d(TAG, "jobFailed: " + job + ", with errorCode: " + errorCode + ", and errorMessage: " + errorMessage);
		// TODO start the next job
		mController.jobFailed(job, exception, errorCode, errorMessage);
	}

    @Override
    public void jobProgress(Job job, float progress, String message) {
        mController.jobProgress(job, progress, message);
    }
	
//	public void jobFinished() {
//		// TODO check if jobs are all done, if so let's die
////		finish();
//	}
}
