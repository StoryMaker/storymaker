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

public class PreviewPublisher extends PublisherBase {
    private final String TAG = "PreviewPublisher";
    public final static int ERROR_CANT_UPLOAD_PREVIEW_JOB = 862861932;
	public final static String SITE_KEY = "preview";
	
	public PreviewPublisher(Context context, PublishController publishController, PublishJob publishJob) {
	    super(context, publishController, publishJob);
	}
	
	public void startRender() {
        Log.d(TAG, "startRender");
        
		Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, SITE_KEY, VideoRenderer.SPEC_KEY);
		mController.enqueueJob(videoRenderJob);
	}
	
	public void startUpload() {
        Log.d(TAG, "startUpload");
        mController.publishJobFailed(mPublishJob, null, ERROR_CANT_UPLOAD_PREVIEW_JOB, "You cannot upload a preview job");
	}
	
	public void jobSucceeded(Job job) {
        Log.d(TAG, "jobSucceeded: " + job);
        if (job.isType(JobTable.TYPE_RENDER)) {
            mController.publishJobSucceeded(mPublishJob, job);
        } 
	}
	
	public void jobFailed(Job job, int errorCode, String errorMessage) {
        Log.d(TAG, "jobFailed: " + job);
        mController.publishJobFailed(mPublishJob, job, errorCode, errorMessage);
	}
	
	public void jobProgress(Job job, float progress, String message) {
	    mController.publishJobProgress(mPublishJob, job, progress, message);
	}
}
