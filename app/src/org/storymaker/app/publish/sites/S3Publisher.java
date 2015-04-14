package org.storymaker.app.publish.sites;

import android.content.Context;
import android.util.Log;

import org.storymaker.app.model.Auth;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.JobTable;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.PublishController;
import org.storymaker.app.publish.PublisherBase;

public class S3Publisher extends PublisherBase {
    private final String TAG = "S3Publisher";
    public final static String SITE_KEY = "s3";

    public S3Publisher(Context context, PublishController publishController, PublishJob publishJob) {
        super(context, publishController, publishJob);
    }
    
    public void startRender() {
        Log.d(TAG, "startRender");
        // TODO should detect if user is directly publishing to youtube so we don't double publish to there
        
        Job job = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
        mController.enqueueJob(job);
    }
    
    public void startUpload() {
        Log.d(TAG, "startUpload");
        Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_S3, null);
        mController.enqueueJob(newJob);
    }

    @Override
    public String getEmbed(Job job) {

        String embed = "[soundcloud url=\\\"\" /]"; // FIXME we need an embed code for s3

        return embed;
    }

    @Override
    public String getResultUrl(Job job) {

        // permalink_url ?
        return null; // FIXME implement getResultUrl
    }
}
