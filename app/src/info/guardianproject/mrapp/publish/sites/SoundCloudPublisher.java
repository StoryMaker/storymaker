package info.guardianproject.mrapp.publish.sites;

import android.content.Context;
import android.util.Log;
import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.publish.PublishController;
import info.guardianproject.mrapp.publish.PublisherBase;

public class SoundCloudPublisher extends PublisherBase {
    private final String TAG = "SoundCloudPublisher";

    public SoundCloudPublisher(Context context, PublishController publishController, PublishJob publishJob) {
        super(context, publishController, publishJob);
    }
    
    public void startRender() {
        Log.d(TAG, "startRender");
        // TODO should detect if user is directly publishing to youtube so we don't double publish to there
        
        Job job = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, AudioRenderer.SPEC_KEY);
        mController.enqueueJob(job);
    }
    
    public void startUpload() {
        Log.d(TAG, "startUpload");
        Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_SOUNDCLOUD, null);
        mController.enqueueJob(newJob);
    }
    
    public void jobSucceeded(Job job) {
        Log.d(TAG, "jobSucceeded: " + job);
        if (job.isType(JobTable.TYPE_RENDER)) {
            // since the user must now initiate upload, we just stop this publishjob now and wait
            mController.publishJobSucceeded(mPublishJob);
        } else if (job.isType(JobTable.TYPE_UPLOAD)) {
            if (job.isSite(Auth.SITE_SOUNDCLOUD)) {
//                Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.STORYMAKER, null);
//                mController.enqueueJob(newJob);
//            } else if (job.isSite(Auth.STORYMAKER)) {

//                mPublishJob.setFinishedAtNow();
//                mPublishJob.save();
//                mController.publishJobSucceeded(mPublishJob);
                if (mPublishJob.getPublishToStoryMaker()) {
                    publishToStoryMaker();
                }
            }
        }
    }
    
    public void jobFailed(Job job, int errorCode, String errorMessage) {
        Log.d(TAG, "jobFailed: " + job);
        mController.publishJobFailed(mPublishJob, errorCode, errorMessage);
    }
    
    public void jobProgress(Job job, float progress, String message) {
        mController.publishJobProgress(mPublishJob, progress, message);
    }
    
    public String getEmbed(Job job) {
        return "[soundcloud url=\"https://api.soundcloud.com/tracks/" 
                + job.getResult() + "\" params=\"color=00cc11&auto_play=false&hide_related=false&show_artwork=true\" width=\"100%\" height=\"166\" iframe=\"true\" /]";
        
        // [soundcloud url="https://api.soundcloud.com/tracks/156197566" params="auto_play=false&hide_related=false&show_comments=true&show_user=true&show_reposts=false&visual=true" width="100%" height="450" iframe="true" /]
    }

}
