package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Job;
import android.content.Context;
import android.util.Log;


public abstract class UploaderBase extends JobBase {
    private final String TAG = "UploaderBase";
    
//	Context context;
//	UploadService service;
//	Job job;

    protected UploaderBase(Context context, ServiceBase service, Job job) {
        super(context, service, job);
        // TODO Auto-generated constructor stub
    }
    
//	protected UploaderBase(Context context, UploadService service, Job job) {
//		this.context = context;
//		this.service = service; 
//		this.job = job;
//	}
//	
//	public void start() {
//        Log.d(TAG, "start");
//		jobSucceeded("foo"); // FIXME foo
// 	}
//	
//    @Override
//    public void jobSucceeded(String result) {
//        Log.d(TAG, "onSuccess: " + result);
//		job.setResult(result);
//		job.setFinishedAtNow();
//		job.save();
//		service.jobSucceeded(job, result);
//	}
//    @Override
//    public void jobFailed(int errorCode, String errorMessage) {
//        Log.d(TAG, "onFailure: errorCode: " + errorCode + ", with message: " + errorCode);
//		job.setResult(null);
//		job.setErrorCode(errorCode);
//		job.setErrorMessage(errorMessage);
//		job.save();
//		service.jobFailed(job, errorCode, errorMessage);
//	}
}
