package info.guardianproject.mrapp.publish;

import org.holoeverywhere.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import info.guardianproject.mrapp.model.Job;

public class JobBase {
    private final String TAG = "JobBase";
    
    protected Context mContext;
    protected WorkerBase mService;
    protected Job mJob;
    protected SharedPreferences mSettings; // FIXME rename to mPrefs
    
    protected JobBase(Context context, WorkerBase service, Job job) {
        mContext = context;
        mService = service; 
        mJob = job;
        mSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
    }
    
    public void start() {
        Log.d(TAG, "start");
        jobSucceeded("foo"); // FIXME foo
    }
    
    public void jobSucceeded(String result) {
        Log.d(TAG, "onSuccess: " + result);
        mJob.setResult(result);
        mJob.setFinishedAtNow();
        mJob.save();
        mService.jobSucceeded(mJob, result);
    }

    public void jobFailed(int errorCode, String errorMessage) {
        Log.d(TAG, "onFailure: errorCode: " + errorCode + ", with message: " + errorCode);
        mJob.setResult(null);
        mJob.setErrorCode(errorCode);
        mJob.setErrorMessage(errorMessage);
        mJob.save();
        mService.jobFailed(mJob, errorCode, errorMessage);
    }
}