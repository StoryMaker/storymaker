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
        mController.publishJobFailed(mPublishJob, ERROR_CANT_UPLOAD_PREVIEW_JOB, "You cannot upload a preview job");
	}
	
    public String getEmbed(Job job) {
        return null;
    }
}
