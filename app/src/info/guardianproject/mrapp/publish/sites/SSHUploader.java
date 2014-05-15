package info.guardianproject.mrapp.publish.sites;

import java.io.File;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import info.guardianproject.mrapp.Utils;
import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.AuthTable;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.publish.UploadWorker;
import info.guardianproject.mrapp.publish.UploaderBase;
import io.scal.secureshareui.controller.SSHSiteController;
import io.scal.secureshareui.controller.SiteController;
import io.scal.secureshareui.controller.SoundCloudSiteController;

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
            controller.upload(project.getTitle(), project.getDescription(), path, auth.getUserName(), auth.getCredentials());
        } else {
            Log.d(TAG, "Can't upload to SSH, last rendered file doesn't exist.");
            // TODO get this error back to the activity for display 
            jobFailed(ERROR_NO_RENDER_FILE, "Can't upload to SSH server, last rendered file doesn't exist."); // FIXME move to strings.xml
        }
    }
}
