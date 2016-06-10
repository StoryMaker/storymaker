package org.storymaker.app.publish.sites;

import timber.log.Timber;

import java.io.File;
import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import org.storymaker.app.Utils;
import org.storymaker.app.model.Auth;
import org.storymaker.app.model.AuthTable;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.UploadWorker;
import org.storymaker.app.publish.UploaderBase;
import io.scal.secureshare.controller.SSHSiteController;
import io.scal.secureshare.controller.SiteController;

public class SSHUploader extends UploaderBase {
    private final String TAG = "SSHUploader";
    public static final int ERROR_NO_RENDER_FILE = 761276123;

	public SSHUploader(Context context, UploadWorker worker, Job job) {
		super(context, worker, job);
	}

	// FIXME move the render file checks into base class
    @Override
    public void start() {
        // TODO Auto-generated constructor stub
        final SiteController controller = SiteController.getSiteController(SSHSiteController.SITE_KEY, mContext, mHandler, ""+mJob.getId());
        final Project project = mJob.getProject();
        final PublishJob publishJob = mJob.getPublishJob();
        final String path = publishJob.getLastRenderFilePath();
        final Auth auth = (new AuthTable()).getAuthDefault(mContext, SSHSiteController.SITE_KEY);
        if (Utils.stringNotBlank(path) && (new File(path)).exists()) {
            jobProgress(mJob, 0, "Uploading to SSH server..."); //  FIXME move to strings.xml
            HashMap<String, String> valueMap = publishJob.getMetadata();
            addValuesToHashmap(valueMap, project.getTitle(), project.getDescription(), path);
            controller.upload(auth.convertToAccountObject(), valueMap);
        } else {
            Timber.d("Can't upload to SSH, last rendered file doesn't exist.");
            // TODO get this error back to the activity for display 
            jobFailed(null, ERROR_NO_RENDER_FILE, "Can't upload to SSH server, last rendered file doesn't exist."); // FIXME move to strings.xml
        }
    }
}
