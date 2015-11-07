package org.storymaker.app.publish.sites;

import timber.log.Timber;

import java.util.HashMap;

import org.storymaker.app.model.Auth;
import org.storymaker.app.model.AuthTable;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.UploadWorker;
import org.storymaker.app.publish.UploaderBase;
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
        Timber.d("start()");
        
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
                    Timber.d("run()");
                    // FIXME, this might not be wise to run on the main thread youtube, does the youtube SDK automatically run itself on a backgroundthread?
                    HashMap<String, String> valueMap = publishJob.getMetadata();
                    addValuesToHashmap(valueMap, project.getTitle(), project.getDescription(), path);
                    controller.upload(auth.convertToAccountObject(), valueMap); 
                }
            };
            mainHandler.post(myRunnable);
        } else {
            Timber.e("youtube upload failed, file path is null");
        }
    }
}
