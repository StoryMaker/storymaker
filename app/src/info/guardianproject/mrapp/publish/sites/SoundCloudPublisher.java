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
    
    public String getEmbed(Job job) {
        return "[soundcloud url=\"https://api.soundcloud.com/tracks/" 
                + job.getResult() + "\" params=\"color=00cc11&auto_play=false&hide_related=false&show_artwork=true\" width=\"100%\" height=\"166\" iframe=\"true\" /]";
        
        // [soundcloud url="https://api.soundcloud.com/tracks/156197566" params="auto_play=false&hide_related=false&show_comments=true&show_user=true&show_reposts=false&visual=true" width="100%" height="450" iframe="true" /]
    }

}
