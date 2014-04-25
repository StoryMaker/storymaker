package info.guardianproject.mrapp.publish;

import android.content.Context;
import android.util.Log;
import info.guardianproject.mrapp.model.Job;

public class JobBase {
    private final String TAG = "JobBase";
    
    Context context;
    ServiceBase service;
    Job job;
    
    protected JobBase(Context context, ServiceBase service, Job job) {
        this.context = context;
        this.service = service; 
        this.job = job;
    }
    
    public void start() {
        Log.d(TAG, "start");
        jobSucceeded("foo"); // FIXME foo
    }
    
    public void jobSucceeded(String result) {
        Log.d(TAG, "onSuccess: " + result);
        job.setResult(result);
        job.setFinishedAtNow();
        job.save();
        service.jobSucceeded(job, result);
    }

    public void jobFailed(int errorCode, String errorMessage) {
        Log.d(TAG, "onFailure: errorCode: " + errorCode + ", with message: " + errorCode);
        job.setResult(null);
        job.setErrorCode(errorCode);
        job.setErrorMessage(errorMessage);
        job.save();
        service.jobFailed(job, errorCode, errorMessage);
    }
}