package info.guardianproject.mrapp.publish.sites;

import android.content.Context;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.publish.PublishController;
import info.guardianproject.mrapp.publish.PublisherBase;

public class YoutubePublisher extends PublisherBase {

    public YoutubePublisher(Context context, PublishController publishController, PublishJob publishJob) {
        super(context, publishController, publishJob);
    }

    @Override
    public void startRender() {
        // TODO Auto-generated method stub

    }

    @Override
    public void startUpload() {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobSucceeded(Job job) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobFailed(Job job, int errorCode, String errorMessage) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jobProgress(Job job, float progress, String message) {
        // TODO Auto-generated method stub

    }

}
