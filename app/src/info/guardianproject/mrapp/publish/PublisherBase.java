package info.guardianproject.mrapp.publish;

import java.net.MalformedURLException;

import redstone.xmlrpc.XmlRpcFault;
import android.content.Context;
import android.os.Message;
import android.util.Log;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.StoryMakerApp;
import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.AuthTable;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.publish.sites.SoundCloudPublisher;
import info.guardianproject.mrapp.server.ServerManager;
import io.scal.secureshareui.controller.SiteController;
import io.scal.secureshareui.lib.ChooseAccountFragment;

public abstract class PublisherBase {
    private final static String TAG = "PublisherBase";
    protected PublishController mController;
    protected PublishJob mPublishJob;
    protected Context mContext;
    
    public PublisherBase(Context context, PublishController publishController, PublishJob publishJob) {
        mContext = context;
        mController = publishController;
        mPublishJob = publishJob;
    }
    
    public abstract void startRender();
    
    public abstract void startUpload();
	
//	public abstract void jobSucceeded(Job job);
    
	public abstract String getEmbed(Job job);

	public String publishToStoryMaker() {
	    Job job = getPreferredUploadJob();
	    
	    Project project = mPublishJob.getProject();
	    String title = project.getTitle();
	    String desc = project.getDescription(); 
	    String mediaEmbed = getEmbed(job);
	    String[] categories = project.getCategories();
	    String medium =  getMedium();
	    String mediaService = job.getSite();
	    String mediaGuid = job.getResult(); // TODO get the id from the preferred job to publish to facebook

	    try {
            String ret = publishToStoryMaker(title, desc, mediaEmbed, categories, medium, mediaService, mediaGuid);
            return ret;
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlRpcFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    return null;
	}
	
	private Job getPreferredUploadJob() {
	    // TODO this should chose from all the selected sites for this publish the preferred site from this list: youtube, facebook, ...
	    for (Job job: mPublishJob.getJobsAsList()) {
	        if (job.isType(JobTable.TYPE_UPLOAD)) {
	            return job;
	        }
	    }
	    return null;
	}
	
	public String getMedium() {
	    if (mPublishJob.getProject().getStoryType() == Project.STORY_TYPE_AUDIO) {
	        return ServerManager.CUSTOM_FIELD_MEDIUM_AUDIO;
	    } else if (mPublishJob.getProject().getStoryType() == Project.STORY_TYPE_VIDEO) {
            return ServerManager.CUSTOM_FIELD_MEDIUM_VIDEO;
        } else if (mPublishJob.getProject().getStoryType() == Project.STORY_TYPE_PHOTO) {
            return ServerManager.CUSTOM_FIELD_MEDIUM_PHOTO;
        } else {
            return "";
        }
	}
    
    public String publishToStoryMaker(String title, String desc, String mediaEmbed, String[] categories, String medium, String mediaService, String mediaGuid) throws MalformedURLException, XmlRpcFault
    {
        ServerManager sm = StoryMakerApp.getServerManager();
        sm.setContext(mContext);

//        Message msgStatus = mHandlerPub.obtainMessage(888);
//        msgStatus.getData().putString("status",
//                getActivity().getString(R.string.uploading_to_storymaker));
//        mHandlerPub.sendMessage(msgStatus);
        
        mController.publishJobProgress(mPublishJob, 0, "Publishing to StoryMaker.cc...");
        String descWithMedia = desc + "\n\n" + mediaEmbed;
        String postId = sm.post(title, descWithMedia, categories, medium, mediaService, mediaGuid);
        mController.publishJobProgress(mPublishJob, 0.5f, "Publishing to StoryMaker.cc...");
        String urlPost = sm.getPostUrl(postId);

        // FIXME make this async and put this in the callback
        // FIXME store the final published url in the project table?
        publishToStoryMakerSecceeded(urlPost);
        
        return urlPost;
    }
    
    public void publishToStoryMakerSecceeded(String url) {
        mController.publishJobProgress(mPublishJob, 1, "Published to StoryMaker.cc!");
        publishSucceeded(url);
    }
    
    public void publishToStoryMakerFailed(int errorCode, String errorMessage) {
        publishFailed(errorCode, errorMessage);
    }
	
    public void publishSucceeded(String url) {
//        mPublishJob.setResult(url); // FIXME implement this
        mPublishJob.setFinishedAtNow();
        mPublishJob.save();
        mController.publishJobSucceeded(mPublishJob, url);
    }

    public void publishFailed(int errorCode, String errorMessage) {
        mController.publishJobFailed(mPublishJob, errorCode, errorMessage);
    }
    
    public void jobSucceeded(Job job) {
        Log.d(TAG, "jobSucceeded: " + job);
        if (job.isType(JobTable.TYPE_RENDER)) {
            // since the user must now initiate upload, we just stop this publishjob now and wait
//            mController.publishJobSucceeded(mPublishJob);
		} else if (job.isType(JobTable.TYPE_UPLOAD)) {
			String publishToStoryMaker = mPublishJob.getMetadata().get(SiteController.VALUE_KEY_PUBLISH_TO_STORYMAKER);
			if (publishToStoryMaker != null && publishToStoryMaker.equals("true")) {
				Auth auth = (new AuthTable()).getAuthDefault(mContext, Auth.SITE_STORYMAKER);
				if (auth != null) {
					publishToStoryMaker();
				} else {
					mController.publishJobFailed(mPublishJob, 78268832, "You are not signed into StoryMaker.cc!"); // FIXME do this nicer!
				}
			} else {
				mController.publishJobSucceeded(mPublishJob, job.getResult());
			}
		}
	}
    
    public void jobFailed(Job job, int errorCode, String errorMessage) {
        Log.d(TAG, "jobFailed: " + job);
        mController.publishJobFailed(mPublishJob, errorCode, errorMessage);
    }

    /**
     * 
     * @param job
     * @param progress 0 to 1
     * @param message Message displayed to user
     */
    public void jobProgress(Job job, float progress, String message) {
        mController.publishJobProgress(mPublishJob, progress, message);
    }
}
