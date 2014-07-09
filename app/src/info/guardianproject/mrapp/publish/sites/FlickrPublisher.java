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

    @Override
    public void jobSucceeded(Job job) {
        Log.d(TAG, "jobSucceeded() - " + job);

        if (job.isType(JobTable.TYPE_RENDER)) {
            Log.d(TAG, "successful render");
            
            mController.publishJobSucceeded(mPublishJob);
        } else if (job.isType(JobTable.TYPE_UPLOAD)) {
            if (job.isSite(Auth.SITE_FLICKR))  {
                Log.d(TAG, "successful upload");
                
                if (mPublishJob.getPublishToStoryMaker()) {
                    publishToStoryMaker();
                }
            }
        } 
    }

    @Override
    public void jobFailed(Job job, int errorCode, String errorMessage) {
        Log.d(TAG, "jobFailed()");
        mController.publishJobFailed(mPublishJob, errorCode, errorMessage);
    }

    @Override
    public void jobProgress(Job job, float progress, String message) {
        Log.d(TAG, "jobProgress()");
        
        mController.publishJobProgress(mPublishJob, progress, message);
    }
    
    public String getEmbed(Job job) {
//        return "[flickr video=" + job.getResult() + "]";
        return "[flickr " + job.getResult() + "]";
    }
}
