package info.guardianproject.mrapp.publish;

import java.util.Arrays;
import java.util.List;

import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.model.PublishJobTable;
import info.guardianproject.mrapp.publish.sites.FacebookPublisher;
import info.guardianproject.mrapp.publish.sites.FlickrPublisher;
import info.guardianproject.mrapp.publish.sites.PreviewPublisher;
import info.guardianproject.mrapp.publish.sites.SSHPublisher;
import info.guardianproject.mrapp.publish.sites.SoundCloudPublisher;
import info.guardianproject.mrapp.publish.sites.StoryMakerPublisher;
import info.guardianproject.mrapp.publish.sites.VideoRenderer;
import info.guardianproject.mrapp.publish.sites.YoutubePublisher;
import io.scal.secureshareui.controller.FacebookSiteController;
import io.scal.secureshareui.controller.FlickrSiteController;
import io.scal.secureshareui.controller.SSHSiteController;
import io.scal.secureshareui.controller.SoundCloudSiteController;
import io.scal.secureshareui.controller.YoutubeSiteController;

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
	PublisherBase publisher = null;
	PublishJob mPublishJob = null;
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
        if (keys == null) {
            return null;
        } else {
            List<String> ks = Arrays.asList(keys);
            if (ks.contains(Auth.SITE_STORYMAKER)) {
                publisher = new StoryMakerPublisher(mContext, this, publishJob);
            } else if (ks.contains(FacebookSiteController.SITE_KEY)) {
                publisher = new FacebookPublisher(mContext, this, publishJob);
            } else if (ks.contains(YoutubeSiteController.SITE_KEY)) {
                publisher = new YoutubePublisher(mContext, this, publishJob);
            } else if (ks.contains(FlickrSiteController.SITE_KEY)) {
                publisher = new FlickrPublisher(mContext, this, publishJob);
            } else if (ks.contains(SoundCloudSiteController.SITE_KEY)) {
                publisher = new SoundCloudPublisher(mContext, this, publishJob);
            } else if (ks.contains(SSHSiteController.SITE_KEY)) {
                publisher = new SSHPublisher(mContext, this, publishJob);
            } else if (ks.contains(PreviewPublisher.SITE_KEY)) {
                publisher = new PreviewPublisher(mContext, this, publishJob);
            }
        }

        return publisher;
    }
    
    // FIXME this won't help us get more than one publisher per run
    public static Class getPublisherClass(String site) {
        if (site.equals(Auth.SITE_STORYMAKER)) {
            return StoryMakerPublisher.class;
        } else if (site.equals(FacebookSiteController.SITE_KEY)) {
            return FacebookPublisher.class;
        } else if (site.equals(YoutubeSiteController.SITE_KEY)) {
            return YoutubePublisher.class;
        } else if (site.equals(FlickrSiteController.SITE_KEY)) {
            return FlickrPublisher.class;
        } else if (site.equals(SoundCloudSiteController.SITE_KEY)) {
            return SoundCloudPublisher.class;
        } else if (site.equals(SSHSiteController.SITE_KEY)) {
            return SSHPublisher.class;
        } else if (site.equals(PreviewPublisher.SITE_KEY)) {
            return PreviewPublisher.class;
        }

        return null;
    }
    
    public void startRender(Project project, String[] siteKeys, boolean useTor, boolean publishToStoryMaker) {
        PublishJob publishJob = getPublishJob(project, siteKeys, useTor, publishToStoryMaker);
        PublisherBase publisher = getPublisher(publishJob);
        // TODO this needs to loop a few times until publisher start returns false or something to tell us that the publish job is totally finished
        if (publisher != null) {
            publisher.startRender();
        } 
    }
    
    public void startUpload(Project project, String[] siteKeys, boolean useTor, boolean publishToStoryMaker) {
        PublishJob publishJob = getPublishJob(project, siteKeys, useTor, publishToStoryMaker);
        // check if there is a rendered, unfinished job already matching these params
//        publishJob(new PublishJobTable()).getNextUnfinished(mContext, project.getId(), siteKeys);
//        publishJob = new PublishJob(mContext, -1, project.getId(), siteKeys);
//        publishJob.save();
        PublisherBase publisher = getPublisher(publishJob);
        // TODO this needs to loop a few times until publisher start returns false or something to tell us that the publish job is totally finished
        if (publisher != null) {
            publisher.startUpload();
        }
    }
    
    private PublishJob getPublishJob(Project project, String[] siteKeys, boolean useTor, boolean publishToStoryMaker) {
        if (mPublishJob == null) {
            mPublishJob = (new PublishJobTable()).getNextUnfinished(mContext, project.getId(), siteKeys);
            if (mPublishJob == null) {
                mPublishJob = new PublishJob(mContext, project.getId(), siteKeys, useTor, publishToStoryMaker);
                mPublishJob.save();
            }
        } else {
            mPublishJob.setUseTor(useTor);
            mPublishJob.setPublishToStoryMaker(publishToStoryMaker);
        }
        return mPublishJob;
    }
	
	public void publishJobSucceeded(PublishJob publishJob) {
	    // get a embedable publish
	    
		mListener.publishSucceeded(publishJob);
	}
    
    public void publishJobFailed(PublishJob publishJob, int errorCode, String errorMessage) {
        mListener.publishFailed(publishJob, errorCode, errorMessage);
    }
	
    /**
     * Aggregates and filters progress from each job associated with a publish job
     * @param publishJob
     * @param progress
     * @param message
     */
	public void publishJobProgress(PublishJob publishJob, float progress, String message) {
	    mListener.publishProgress(publishJob, progress, message);
	}
	
	public void jobSucceeded(Job job, String code) {
        Log.d(TAG, "jobSucceeded: " + job + ", with code: " + code);
		// TODO need to raise this to the interested activities here
        PublishJob publishJob = job.getPublishJob();
        PublisherBase publisher = getPublisher(publishJob);
        if (publisher != null) {
            publisher.jobSucceeded(job);
        } else {
            // TODO how to handle null publisher?
        }
        mListener.jobSucceeded(job);
	}
	
	public void jobFailed(Job job, int errorCode, String errorMessage) {
        Log.d(TAG, "jobFailed: " + job + ", with errorCode: " + errorCode + ", and errorMessage: " + errorMessage);
		// TODO need to raise this to the interested activities here
        PublishJob publishJob = job.getPublishJob();
        PublisherBase publisher = getPublisher(publishJob);
        if (publisher != null) {
            publisher.jobFailed(job, errorCode, errorMessage);
        } else {
            // TODO how to handle null publisher?
        }
        mListener.jobFailed(job, errorCode, errorMessage);
	}
	
    public void jobProgress(Job job, float progress, String message) {
        PublishJob publishJob = job.getPublishJob();
        PublisherBase publisher = getPublisher(publishJob);
        if (publisher != null) {
            publisher.jobProgress(job, progress, message);
        } else {
            // TODO how to handle null publisher?
        }
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

        public void publishFailed(PublishJob publishJob, int errorCode, String errorMessage);
        
        public void jobSucceeded(Job job);

        public void jobFailed(Job job, int errorCode, String errorMessage);
        
        public void publishProgress(PublishJob publishJob, float progress, String message);
	}

}
