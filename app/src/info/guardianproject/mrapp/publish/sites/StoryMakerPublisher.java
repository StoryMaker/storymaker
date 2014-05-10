package info.guardianproject.mrapp.publish.sites;

import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.publish.PublishController;
import info.guardianproject.mrapp.publish.PublisherBase;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.util.Log;

public class StoryMakerPublisher extends PublisherBase {
    private final String TAG = "StoryMakerPublisher";
	
	public StoryMakerPublisher(Context context, PublishController publishController, PublishJob publishJob) {
	    super(context, publishController, publishJob);
	}
	
	public void startRender() {
        Log.d(TAG, "startRender");
		// TODO should detect if user is directly publishing to youtube so we don't double publish to there
		
		Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
		mController.enqueueJob(videoRenderJob);
	}
	
	public void startUpload() {
        Log.d(TAG, "startUpload");
        Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_YOUTUBE, null);
        mController.enqueueJob(newJob);
	}
	
	public void jobSucceeded(Job job) {
        Log.d(TAG, "jobSucceeded: " + job);
        if (job.isType(JobTable.TYPE_RENDER)) {
            // since the user must now initiate upload, we just stop this publishjob now and wait
            mController.publishJobSucceeded(mPublishJob);
        } else if (job.isType(JobTable.TYPE_UPLOAD)) {
            if (job.isSite(Auth.SITE_YOUTUBE)) {
                Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.STORYMAKER, null);
                mController.enqueueJob(newJob);
            } else if (job.isSite(Auth.STORYMAKER)) {
                mPublishJob.setFinishedAtNow();
                mPublishJob.save();
                mController.publishJobSucceeded(mPublishJob);
            }
        }
	}
	
	public void jobFailed(Job job) {
        Log.d(TAG, "jobFailed: " + job);
		// nop
	}
	
	public void jobProgress(Job job, float progress, String message) {
	    mController.publishJobProgress(mPublishJob, progress, message);
	}
}
