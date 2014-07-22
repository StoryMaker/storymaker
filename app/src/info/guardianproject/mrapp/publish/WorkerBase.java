package info.guardianproject.mrapp.publish;

import android.content.Context;
import info.guardianproject.mrapp.model.Job;

public abstract class WorkerBase {
    private final String TAG = "ServiceBase";

    protected Context mContext;
    
	public abstract void jobSucceeded(Job job, String code);
	
	public abstract void jobFailed(Job job, int errorCode, String errorMessage);
	
    /**
     * 
     * @param job
     * @param progress 0 to 1
     * @param message message displayed to the user
     */
	public abstract void jobProgress(Job job, float progress, String message);
	
}
