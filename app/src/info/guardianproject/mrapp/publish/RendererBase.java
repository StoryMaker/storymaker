package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Job;
import android.content.Context;
import android.util.Log;

// TODO a lot of code shared between this and uploaderBase...
public class RendererBase extends JobBase {
    private final String TAG = "RendererBase";
    
    protected RendererBase(Context context, ServiceBase service, Job job) {
        super(context, service, job);
        // TODO Auto-generated constructor stub
    }
    
//	Context context;
//	RenderService service;
//	Job job;
//	
//	protected RendererBase(Context context, RenderService service, Job job) {
//		this.context = context;
//		this.service = service; 
//		this.job = job;
//	}
//	
//	public void start() {
//        Log.d(TAG, "start");
//		jobSucceeded("foo"); // FIXME foo
// 	}
//    @Override
//    public void jobSucceeded(String result) {
//        Log.d(TAG, "onSuccess: " + result);
//		job.setResult(result);
//		job.setFinishedAtNow();
//        job.save();
//		service.jobSucceeded(job, result);
//	}
//
//    @Override
//    public void jobFailed(int errorCode, String errorMessage) {
//        Log.d(TAG, "onFailure: errorCode: " + errorCode + ", with message: " + errorCode);
//		job.setResult(null);
//		job.setErrorCode(errorCode);
//		job.setErrorMessage(errorMessage);
//        job.save();
//		service.jobFailed(job, errorCode, errorMessage);
//	}
}
