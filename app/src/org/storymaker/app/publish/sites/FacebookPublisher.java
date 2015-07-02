package org.storymaker.app.publish.sites;

import org.storymaker.app.model.Auth;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.JobTable;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.PublishController;
import org.storymaker.app.publish.PublisherBase;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.util.Log;

import io.scal.secureshareui.controller.FacebookSiteController;

public class FacebookPublisher extends PublisherBase {
    private final String TAG = "FacebookPublisher";
	
	public FacebookPublisher(Context context, PublishController publishController, PublishJob publishJob) {
	    super(context, publishController, publishJob);
	}
	
	public void startRender() {
        Log.d(TAG, "startRender");
		
        if (mPublishJob.getProject().getStoryType() == Project.STORY_TYPE_VIDEO) {
			Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
			mController.enqueueJob(videoRenderJob);
		} else if (mPublishJob.getProject().getStoryType() == Project.STORY_TYPE_ESSAY) {
			startUpload(); // FIXME won't this cause double uploads?
		} else if (mPublishJob.getProject().getStoryType() == Project.STORY_TYPE_PHOTO) {
			startUpload(); // FIXME won't this cause double uploads?
		} else if (mPublishJob.getProject().getStoryType() == Project.STORY_TYPE_AUDIO) {
			Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
			mController.enqueueJob(videoRenderJob); // FIXME video render job to render audio? i  think the MPM handles it correctly, but wtf
		}
	}
	
	public void startUpload() {
        Log.d(TAG, "startUpload");
        Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_FACEBOOK, null);
        mController.enqueueJob(newJob);
	}
	
	public String getEmbed(Job job) {
	    return "[FB " + job.getResult() + "]"; // FIXME we need full embeds
//	    return null; // 
	    // https://developers.facebook.com/docs/wordpress/embedded-posts/
//	    return "[facebook_embedded_post href=\"https://www.facebook.com/WordPress/posts/" + "10151630721717911" + "\"]";
	}

    public String getResultUrl(Job job) {
        return "https://facebook.com/" + FacebookSiteController.getUserId() + "/posts/" + job.getResult();
    }
}
