package info.guardianproject.mrapp.publish.sites;

import java.io.File;

import org.holoeverywhere.app.Activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import info.guardianproject.mrapp.Utils;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.model.json.ProjectJson;
import info.guardianproject.mrapp.publish.UploadWorker;
import info.guardianproject.mrapp.publish.UploaderBase;
import io.scal.secureshareui.controller.FacebookSiteController;
import io.scal.secureshareui.controller.SiteController;

public class FacebookUploader extends UploaderBase {
    private final String TAG = "FacebookUploader";
    public static final int ERROR_NO_RENDER_FILE = 761276123;

	public FacebookUploader(Context context, UploadWorker worker, Job job) {
		super(context, worker, job);
	}

	// FIXME move the render file checks into base class
    @Override
    public void start() {
        // TODO Auto-generated constructor stub
        final SiteController controller = SiteController.getSiteController(FacebookSiteController.SITE_KEY, mContext, mHandler, ""+mJob.getId());
        final Project project = mJob.getProject();
        final PublishJob publishJob = mJob.getPublishJob();
        final String path = publishJob.getLastRenderFilePath();
        if (Utils.stringNotBlank(path) && (new File(path)).exists()) {
            Handler mainHandler = new Handler(mContext.getMainLooper());
            Runnable myRunnable = new Runnable() {
                // facebook seems to freak out if our service's looper is dead when it tries to send message back 
                @Override
                public void run() {
                    jobProgress(mJob, 0, "Uploading to Facebook..."); //  FIXME move to strings.xml
                    controller.upload(project.getTitle(), project.getDescription(), path, null, null);
                }
                
            };
            mainHandler.post(myRunnable);
        } else {
            Log.d(TAG, "Can't upload to facebook, last rendered file doesn't exist.");
            // TODO get this error back to the activity for display 
            jobFailed(ERROR_NO_RENDER_FILE, "Can't upload to facebook, last rendered file doesn't exist."); // FIXME move to strings.xml
        }
    }
}
