package org.storymaker.app.publish.sites;

import timber.log.Timber;

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
        Timber.d("startRender");
		
        
		Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
		mController.enqueueJob(videoRenderJob);
	}
	
	public void startUpload() {
        Timber.d("startUpload");
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
