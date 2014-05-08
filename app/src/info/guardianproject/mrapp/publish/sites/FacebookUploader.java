package info.guardianproject.mrapp.publish.sites;

import org.holoeverywhere.app.Activity;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.model.json.ProjectJson;
import info.guardianproject.mrapp.publish.UploadWorker;
import info.guardianproject.mrapp.publish.UploaderBase;
import io.scal.secureshareui.controller.FacebookPublishController;

public class FacebookUploader extends UploaderBase {
    private final String TAG = "FacebookUploader";

	public FacebookUploader(Context context, UploadWorker worker, Job job) {
		super(context, worker, job);
	}

    @Override
    public void start() {
        // TODO Auto-generated constructor stub
        final io.scal.secureshareui.controller.PublishController controller = io.scal.secureshareui.controller.PublishController.getPublishController(FacebookPublishController.SITE_KEY);
        controller.setContext(mContext);
        final Project project = mJob.getProject();
        final PublishJob publishJob = mJob.getPublishJob();
        final String path = publishJob.getLastRenderFilePath();
        if (path != null) {
            Handler mainHandler = new Handler(mContext.getMainLooper());
            Runnable myRunnable = new Runnable() {
                // facebook seems to freak out if our service's looper is dead when it tries to send message back 
                @Override
                public void run() {
                    controller.upload(project.getTitle(), project.getDescription(), path, null);
                }
                
            };
            mainHandler.post(myRunnable);
        } else {
            Log.d(TAG, "Can't upload to facebook, last rendered file path is null");
        }
    }
	
	
}
