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

public class S3Publisher extends PublisherBase {
    private final String TAG = "S3Publisher";
    public final static String SITE_KEY = "s3";

    public S3Publisher(Context context, PublishController publishController, PublishJob publishJob) {
        super(context, publishController, publishJob);
    }
    
    public void startRender() {
        Timber.d("startRender");
        // TODO should detect if user is directly publishing to youtube so we don't double publish to there
        
        Job job = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
        mController.enqueueJob(job);
    }
    
    public void startUpload() {
        Timber.d("startUpload");
        Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_S3, null);
        mController.enqueueJob(newJob);
    }

    @Override
    public String getEmbed(Job job) {

        String embed = "[s3 " + job.getResult() + "]"; // FIXME we need an embed code for s3

        return embed;
    }

    @Override
    public String getResultUrl(Job job) {

        // permalink_url ?
        return null; // FIXME implement getResultUrl
    }
}
