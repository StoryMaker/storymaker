package org.storymaker.app.publish.sites;

import timber.log.Timber;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.storymaker.app.R;
import org.storymaker.app.Utils;
import org.storymaker.app.model.Auth;
import org.storymaker.app.model.AuthTable;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.UploaderBase;
import org.storymaker.app.publish.WorkerBase;

import java.io.File;
import java.util.HashMap;

import io.scal.secureshareui.controller.SiteController;
import io.scal.secureshareui.controller.S3SiteController;

public class S3Uploader extends UploaderBase {
    private final String TAG = "S3Uploader";
    public static final int ERROR_NO_RENDER_FILE = 761276123;

    public S3Uploader(Context context, WorkerBase worker, Job job) {
        super(context, worker, job);
        // TODO Auto-generated constructor stub
    }

    // FIXME move the render file checks into base class
    @Override
    public void start() {
        // TODO Auto-generated constructor stub
        final SiteController controller = SiteController.getSiteController(S3SiteController.SITE_KEY, mContext, mHandler, ""+mJob.getId());
        final Project project = mJob.getProject();
        final PublishJob publishJob = mJob.getPublishJob();
        final String path = publishJob.getLastRenderFilePath();
//        final Auth auth = (new AuthTable()).getAuthDefault(mContext, S3SiteController.SITE_KEY);
        // FIXME deal with lack of auth credentials here
        if (Utils.stringNotBlank(path) && (new File(path)).exists()) {
            Handler mainHandler = new Handler(mContext.getMainLooper());
            Runnable myRunnable = new Runnable() {
                // facebook seems to freak out if our service's looper is dead when it tries to send message back 
                @Override
                public void run() {
                    jobProgress(mJob, 0, mContext.getString(R.string.uploading));
                    HashMap<String, String> valueMap = publishJob.getMetadata();
                    addValuesToHashmap(valueMap, project.getTitle(), project.getDescription(), path);
//                    controller.upload(auth.convertToAccountObject(), valueMap);
                    controller.upload(null, valueMap);
                }
                
            };
            mainHandler.post(myRunnable);
        } else {
            Timber.d("Can't upload to S3, last rendered file doesn't exist.");
            // TODO get this error back to the activity for display 
            jobFailed(null, ERROR_NO_RENDER_FILE, "Can't upload to S3, last rendered file doesn't exist."); // FIXME move to strings.xml
        }
    }
}
