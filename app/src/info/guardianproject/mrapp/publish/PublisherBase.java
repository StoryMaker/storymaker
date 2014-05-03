package info.guardianproject.mrapp.publish;

import android.content.Context;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.PublishJob;

public abstract class PublisherBase {
    protected PublishController mController;
    protected PublishJob mPublishJob;
    protected Context mContext;
    
    public PublisherBase(Context context, PublishController publishController, PublishJob publishJob) {
        mContext = context;
        mController = publishController;
        mPublishJob = publishJob;
    }

    public abstract void startRender();
    
    public abstract void startUpload();
	
	public abstract void jobSucceeded(Job job);

	public abstract void jobFailed(Job job);
	
	public abstract void jobProgress(Job job, int progress, String message);
}
