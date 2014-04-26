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
    
	private PublishController controller;
	private PublishJob publishJob;
	private Context context;
	
	public StoryMakerPublisher(Context context, PublishController controller, PublishJob publishJob) {
		this.context = context;
		this.controller = controller;
		this.publishJob = publishJob;
	}
	
	public void start() {
        Log.d(TAG, "start");
		// TODO this creates all the necessary jobs to complete this publish
		// should detect if user is directly publishing to youtube so we don't double publish to there
		
		Job videoRenderJob = new Job(context, -1, publishJob.getProjectId(), publishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
		videoRenderJob.setQueuedAtNow();
		videoRenderJob.save();
		// TODO needs to tell the PublishContoller to start the renderservice

//		publishJob.makeJobLive();
		
	}
	
	public void jobSucceeded(Job job) {
        Log.d(TAG, "jobSucceeded: " + job);
		Job newJob = null;
		if (job.isType(JobTable.TYPE_RENDER)) {
			newJob = new Job(context, -1, job.getProjectId(), publishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_YOUTUBE, null);
		} else if ((job.isType(JobTable.TYPE_UPLOAD)) && (job.isSite(Auth.SITE_YOUTUBE))) { 		
			newJob  = new Job(context, -1, job.getProjectId(), publishJob.getId(), JobTable.TYPE_UPLOAD, Auth.STORYMAKER, null);
		}
		// FIXME we need to mark the job as successful at some point
		if (newJob != null) {
			controller.enqueueJob(newJob);
		} else {
		    publishJob.setFinishedAtNow();
		    controller.publishJobSucceeded(publishJob);
		}
	}
	
	public void jobFailed(Job job) {
        Log.d(TAG, "jobFailed: " + job);
		// nop
	}
}
