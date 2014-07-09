package info.guardianproject.mrapp.publish.sites;

import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.publish.PublishController;
import info.guardianproject.mrapp.publish.PublisherBase;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.util.Log;

public class FacebookPublisher extends PublisherBase {
    private final String TAG = "FacebookPublisher";
	
	public FacebookPublisher(Context context, PublishController publishController, PublishJob publishJob) {
	    super(context, publishController, publishJob);
	}
	
	public void startRender() {
        Log.d(TAG, "startRender");
		
        
		Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
		mController.enqueueJob(videoRenderJob);
	}
	
	public void startUpload() {
        Log.d(TAG, "startUpload");
        Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_FACEBOOK, null);
        mController.enqueueJob(newJob);
	}
	
	public void jobSucceeded(Job job) {
        Log.d(TAG, "jobSucceeded: " + job);
        if (job.isType(JobTable.TYPE_RENDER)) {
            // since the user must now initiate upload, we just stop this publishjob now and wait
//            mController.jobSucceeded(job, job.getResult());
        } else if (job.isType(JobTable.TYPE_UPLOAD)) {
            if (job.isSite(Auth.SITE_FACEBOOK)) {
                if (mPublishJob.getPublishToStoryMaker()) {
                    publishToStoryMaker();
                }
            }
        }
	}
	
	public String getEmbed(Job job) {
	    return "[FB " + job.getResult() + "]";
//	    return null; // 
	    // https://developers.facebook.com/docs/wordpress/embedded-posts/
//	    return "[facebook_embedded_post href=\"https://www.facebook.com/WordPress/posts/" + "10151630721717911" + "\"]";
	}
}
