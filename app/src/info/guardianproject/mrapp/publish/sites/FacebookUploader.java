package info.guardianproject.mrapp.publish.sites;

import org.holoeverywhere.app.Activity;

import android.content.Context;
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
        io.scal.secureshareui.controller.PublishController controller;
        controller = io.scal.secureshareui.controller.PublishController.getPublishController(FacebookPublishController.SITE_KEY);
        controller.setContext(mContext);
        Project project = mJob.getProject();
        PublishJob publishJob = mJob.getPublishJob();
        String path = publishJob.getLastRenderFilePath();
        if (path != null) {
            controller.upload(project.getTitle(), project.getDescription(), path);
        } else {
            Log.d(TAG, "Can't upload to facebook, last rendered file path is null");
        }
    }
	
	
}
