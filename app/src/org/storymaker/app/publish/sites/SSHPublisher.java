package org.storymaker.app.publish.sites;

import timber.log.Timber;

import org.storymaker.app.model.Auth;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.JobTable;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.ProjectTable;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.PublishController;
import org.storymaker.app.publish.PublisherBase;
import org.storymaker.app.publish.VideoRenderer;

import android.content.Context;

public class SSHPublisher extends PublisherBase {
    private final String TAG = "SSHPublisher";
	
	public SSHPublisher(Context context, PublishController publishController, PublishJob publishJob) {
	    super(context, publishController, publishJob);
	}
	
	public void startRender() {
        Timber.d("startRender");
        Project project = (Project) (new ProjectTable()).get(mContext, mPublishJob.getProjectId());
        if (project.getStoryType() == Project.STORY_TYPE_VIDEO) {
            Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
            mController.enqueueJob(videoRenderJob);
        } else if (project.getStoryType() == Project.STORY_TYPE_AUDIO) {
            Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, AudioRenderer.SPEC_KEY);
            mController.enqueueJob(videoRenderJob);
        } else { 
            mController.publishJobSucceeded(mPublishJob, null); // skip render, no point
        }
	}
	
	public void startUpload() {
        Timber.d("startUpload");
        Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_SSH, null);
        mController.enqueueJob(newJob);
	}

    @Override
    public String getEmbed(Job job) {
        return null;
    }

    @Override
    public String getResultUrl(Job job) {
        return null;
    }
}
