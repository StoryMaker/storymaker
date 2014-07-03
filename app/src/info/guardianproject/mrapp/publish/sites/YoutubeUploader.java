package info.guardianproject.mrapp.publish.sites;

import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.AuthTable;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.publish.UploadWorker;
import info.guardianproject.mrapp.publish.UploaderBase;
import io.scal.secureshareui.controller.SiteController;
import io.scal.secureshareui.controller.YoutubeSiteController;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class YoutubeUploader extends UploaderBase {
    private final String TAG = "YoutubeUploader";

	public YoutubeUploader(Context context, UploadWorker worker, Job job) {
		super(context, worker, job);
	}

    @Override
    public void start() {
        Log.d(TAG, "start()");
        
        final SiteController controller = SiteController.getSiteController(YoutubeSiteController.SITE_KEY, mContext, mHandler, "" + mJob.getId());
        final Project project = mJob.getProject();
        final PublishJob publishJob = mJob.getPublishJob();
        final String path = publishJob.getLastRenderFilePath();
        final Auth auth = (new AuthTable()).getAuthDefault(mContext, Auth.SITE_YOUTUBE);
        if (path != null) {
            Handler mainHandler = new Handler(mContext.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run()");
                    // FIXME, this might not be wise to run on the main thread youtube, does the youtube SDK automatically run itself on a backgroundthread?
                    controller.upload(project.getTitle(), project.getDescription(), path, auth.convertToAccountObject(), publishJob.getUseTor()); 
                }
            };
            mainHandler.post(myRunnable);
        } else {
            Log.e(TAG, "youtube upload failed, file path is null");
        }
    }
}
