package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Job;

public abstract class PublisherBase {
    private final String TAG = "PublisherBase";
    
	public abstract void start();
	
	public abstract void jobSucceeded(Job job);

	public abstract void jobFailed(Job job);
	
	public abstract void jobProgress(Job job, int progress, String message);
}
