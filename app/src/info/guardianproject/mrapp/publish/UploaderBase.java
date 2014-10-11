package info.guardianproject.mrapp.publish;

import java.util.HashMap;
import java.util.Map;

import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import io.scal.secureshareui.controller.SiteController;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

// TODO should these vanish and have FacebookUploader et all just derive from JobBase directly?
public abstract class UploaderBase extends JobBase {
    private final String TAG = "UploaderBase";
    
    protected UploaderBase(Context context, WorkerBase worker, Job job) {
        super(context, worker, job);
        // TODO Auto-generated constructor stub
    }

    static HandlerThread bgThread = new HandlerThread("VideoRenderHandlerThread");
    static {
        bgThread.start();
    }
    public Handler mHandler = new Handler(bgThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();

            String jobIdString = data.getString(SiteController.MESSAGE_KEY_JOB_ID);
            int jobId = (jobIdString != null) ? Integer.parseInt(jobIdString) : -1;
            Job job = (Job) (new JobTable()).get(mContext, jobId);
            
            int messageType = data.getInt(SiteController.MESSAGE_KEY_TYPE);
            switch (messageType) {
                case SiteController.MESSAGE_TYPE_SUCCESS:
                    String result = data.getString(SiteController.MESSAGE_KEY_RESULT);
                    jobSucceeded(result);
                    break;
                case SiteController.MESSAGE_TYPE_FAILURE:
                    int errorCode = data.getInt(SiteController.MESSAGE_KEY_CODE);
                    String errorMessage = data.getString(SiteController.MESSAGE_KEY_MESSAGE);
                    jobFailed(errorCode, errorMessage);
                    break;
                case SiteController.MESSAGE_TYPE_PROGRESS:
                    String message = data.getString(SiteController.MESSAGE_KEY_MESSAGE);
                    float progress = data.getFloat(SiteController.MESSAGE_KEY_PROGRESS);
                    jobProgress(job, progress, message);
                    break;
            }
        }
    };
    
    protected static HashMap<String, String> addValuesToHashmap(HashMap<String, String> valueMap, String title, String body, String mediaPath) {
//    	HashMap<String, String> valueMap = new HashMap<String, String>();	
    	valueMap.put("title", title);
    	valueMap.put("body", body);
    	valueMap.put("mediaPath", mediaPath);
		
		return valueMap;
    }
}
