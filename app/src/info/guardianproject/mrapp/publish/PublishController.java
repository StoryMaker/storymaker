package info.guardianproject.mrapp.publish;

import java.util.Arrays;
import java.util.List;

import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.publish.sites.StoryMakerPublisher;
import org.holoeverywhere.app.Activity;

import android.content.Context;
import android.util.Log;

// TODO we need to make sure this will be thread safe since upload and render jobs are on separate threads and could callback in a race here

/**
 * 
 * @author Josh Steiner <josh@vitriolix.com>
 *
 */
public class PublishController {
    private final String TAG = "PublishController";
    
	private static PublishController publishController = null;
	private Context mContext;
	UploadWorker uploadService;
	RenderWorker renderService;
	PublisherBase publisher;
	PublishJob publishJob;
	PublishListener mListener;
	
	public PublishController(Context context, PublishListener listener) {
	    mContext = context;
	    mListener = listener;
	}

	public static PublishController getInstance(Activity activity, PublishListener listener) {
		if (publishController == null) {
			publishController = new PublishController(activity, listener);
		}
		
		return publishController;
	}
	
	// FIXME this won't help us get more than one publisher per run
	public PublisherBase getPublisher(PublishJob publishJob) {
		String[] keys = publishJob.getSiteKeys();
		List<String> ks = Arrays.asList(keys);
		if (ks.contains("storymaker")) {
			publisher = new StoryMakerPublisher(mContext, this, publishJob);
		}
		// TODO add others
		
		return publisher;
	}
	
	public void startPublish(Project project, String[] siteKeys) {
		publishJob = new PublishJob(mContext, -1, project.getId(), siteKeys);
		publishJob.save();
		PublisherBase publisher = getPublisher(publishJob);
		// TODO this needs to loop a few times until publisher start returns false or something to tell us that the publish job is totally finished
		if (publisher != null) {
    		publisher.start();
    		startRenderService();
    		startUploadService();
		}
	}
	
	public void publishJobSucceeded(PublishJob publishJob) {
		mListener.publishSucceeded(publishJob);
	}
    
    public void publishJobFailed(PublishJob publishJob) {
        mListener.publishFailed(publishJob);
    }
	
	public void publishJobProgress(PublishJob publishJob, int progress, String message) {
	    mListener.publishProgress(publishJob, progress, message);
	}
	
	public void jobSucceeded(Job job, String code) {
        Log.d(TAG, "jobSucceeded: " + job + ", with code: " + code);
		// TODO need to raise this to the interested activities here
		getPublisher(job.getPublishJob()).jobSucceeded(job);
	}
	
	public void jobFailed(Job job, int errorCode, String errorMessage) {
        Log.d(TAG, "jobFailed: " + job + ", with errorCode: " + errorCode + ", and errorMessage: " + errorMessage);
		// TODO need to raise this to the interested activities here
		getPublisher(job.getPublishJob()).jobFailed(job);
	}
	
	public void jobProgress(Job job, int progress, String message) {
	       getPublisher(job.getPublishJob()).jobProgress(job, progress, message);
	}
	
	private void startUploadService() {
		uploadService = UploadWorker.getInstance(mContext, this);
		uploadService.start();
	}
	
	private void startRenderService() {
		renderService = RenderWorker.getInstance(mContext, this);
		renderService.start();
	}
	
	public void enqueueJob(Job job) {
		job.setQueuedAtNow();
		job.save();
		if (job.isType(JobTable.TYPE_UPLOAD)) {
			startUploadService();
		} else if (job.isType(JobTable.TYPE_RENDER)) {
			startRenderService();
		}
	}
	
	public static interface PublishListener {
	    public void publishSucceeded(PublishJob publishJob);

        public void publishFailed(PublishJob publishJob);
        
        public void publishProgress(PublishJob publishJob, int progress, String message);
	}

}
