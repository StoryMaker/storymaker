package org.storymaker.app.publish.sites;

import timber.log.Timber;

import android.content.Context;

import org.storymaker.app.model.Auth;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.JobTable;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.PublishController;
import org.storymaker.app.publish.PublisherBase;
import org.storymaker.app.publish.VideoRenderer;

public class YoutubePublisher extends PublisherBase {

	private final String TAG = "YoutubePublisher";
	
    public YoutubePublisher(Context context, PublishController publishController, PublishJob publishJob) {
        super(context, publishController, publishJob);
    }

    @Override
    public void startRender() 
    {
        Timber.d("startRender()");
        
        Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
        mController.enqueueJob(videoRenderJob);
    }

    @Override
    public void startUpload() 
    {
        Timber.d("startUpload()");
        
        Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_YOUTUBE, null);
        mController.enqueueJob(newJob);
    }

    @Override
    public String getEmbed(Job job) {
        return "[youtube " + job.getResult() + "]";
    }

    @Override
    public String getResultUrl(Job job) {
        return "http://www.youtube.com/watch?v=" + job.getResult();
    }
}
