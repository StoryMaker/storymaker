package info.guardianproject.mrapp.publish.sites;

import android.content.Context;
import android.util.Log;
import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.publish.PublishController;
import info.guardianproject.mrapp.publish.PublisherBase;

public class FlickrPublisher extends PublisherBase {
    private final String TAG = "FlickrPublisher";
    
    public FlickrPublisher(Context context, PublishController publishController, PublishJob publishJob) {
        super(context, publishController, publishJob);
    }

    @Override
    public void startRender() {
        Log.d(TAG, "startRender()");
        
        Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
        mController.enqueueJob(videoRenderJob);
    }

    @Override
    public void startUpload() {
        Log.d(TAG, "startUpload()");
        
        Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_FLICKR, null);
        mController.enqueueJob(newJob);
    }

    public String getEmbed(Job job) {
        return "[flickr " + job.getResult() + "]";
    }
}
