package info.guardianproject.mrapp.publish;

import android.content.Context;
import info.guardianproject.mrapp.model.Job;

public abstract class ServiceBase {
    private final String TAG = "ServiceBase";

    protected Context mContext;
    
	public abstract void jobSucceeded(Job job, String code);
	
	public abstract void jobFailed(Job job, int errorCode, String errorMessage);
	
}
