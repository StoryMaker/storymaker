package org.storymaker.app.publish.sites;

import timber.log.Timber;

import org.storymaker.app.model.Auth;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.JobTable;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.PublishController;
import org.storymaker.app.publish.PublisherBase;
import org.storymaker.app.publish.VideoRenderer;

import android.content.Context;

public class StoryMakerPublisher extends PublisherBase {
    private final String TAG = "StoryMakerPublisher";
	
	public StoryMakerPublisher(Context context, PublishController publishController, PublishJob publishJob) {
	    super(context, publishController, publishJob);
	}
	
	public void startRender() {
        Timber.d("startRender");
		// TODO should detect if user is directly publishing to youtube so we don't double publish to there
		
		Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
		mController.enqueueJob(videoRenderJob);
	}
	
	public void startUpload() {
        Timber.d("startUpload");
        Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_YOUTUBE, null); // FIXME hardcoded to youtube?
        mController.enqueueJob(newJob);
	}

    @Override
    public String getEmbed(Job job) {
        return null; // FIXME implement getEmbed
    }

    @Override
    public String getResultUrl(Job job) {
        return null; // FIXME implement getResultUrl
    }
}
