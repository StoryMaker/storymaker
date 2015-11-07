package org.storymaker.app.publish;

import timber.log.Timber;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import org.storymaker.app.model.Job;

public class JobBase {
    private final String TAG = "JobBase";
    
    protected Context mContext;
    protected WorkerBase mWorker;
    protected Job mJob;
    protected SharedPreferences mSettings; // FIXME rename to mPrefs
    
    protected JobBase(Context context, WorkerBase worker, Job job) {
        mContext = context;
        mWorker = worker; 
        mJob = job;
        mSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
    }
    
    /**
     * 
     * @param type upload or render
     */
    public void start() {
        Timber.d("start");
    }
    
    public void jobSucceeded(String result) {
        Timber.d("onSuccess: " + result);
        mJob.setResult(result);
        mJob.setFinishedAtNow();
        mJob.save();
        mWorker.jobSucceeded(mJob, result);
    }

    public void jobFailed(Exception exception, int errorCode, String errorMessage) {
        Timber.d("onFailure: errorCode: " + errorCode + ", with message: " + errorCode);
        mJob.setResult(null);
        mJob.setErrorCode(errorCode);
        mJob.setErrorMessage(errorMessage);
        mJob.save();
        mWorker.jobFailed(mJob, exception, errorCode, errorMessage);
    }
    
    // FIXME why do we pass in job instead of using mJob?
    /**
     * 
     * @param job
     * @param progress 0 to 1
     * @param message message displayed to the user
     */
    public void jobProgress(Job job, float progress, String message) {
        mWorker.jobProgress(job, progress, message);
    }
}
