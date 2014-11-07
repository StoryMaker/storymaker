package org.storymaker.app.publish.sites;

import java.io.File;
import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import org.storymaker.app.Utils;
import org.storymaker.app.model.Auth;
import org.storymaker.app.model.AuthTable;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.UploaderBase;
import org.storymaker.app.publish.WorkerBase;
import io.scal.secureshareui.controller.SiteController;
import io.scal.secureshareui.controller.SoundCloudSiteController;

public class SoundCloudUploader extends UploaderBase {
    private final String TAG = "SoundCloudUploader";
    public static final int ERROR_NO_RENDER_FILE = 761276123;

    public SoundCloudUploader(Context context, WorkerBase worker, Job job) {
        super(context, worker, job);
        // TODO Auto-generated constructor stub
    }

    // FIXME move the render file checks into base class
    @Override
    public void start() {
        // TODO Auto-generated constructor stub
        final SiteController controller = SiteController.getSiteController(SoundCloudSiteController.SITE_KEY, mContext, mHandler, ""+mJob.getId());
        final Project project = mJob.getProject();
        final PublishJob publishJob = mJob.getPublishJob();
        final String path = publishJob.getLastRenderFilePath();
        final Auth auth = (new AuthTable()).getAuthDefault(mContext, SoundCloudSiteController.SITE_KEY);
        // FIXME deal with lack of auth credentials here
        if (Utils.stringNotBlank(path) && (new File(path)).exists()) {
            Handler mainHandler = new Handler(mContext.getMainLooper());
            Runnable myRunnable = new Runnable() {
                // facebook seems to freak out if our service's looper is dead when it tries to send message back 
                @Override
                public void run() {
                    jobProgress(mJob, 0, "Uploading to SoundCloud..."); //  FIXME move to strings.xml
                    HashMap<String, String> valueMap = publishJob.getMetadata();
                    addValuesToHashmap(valueMap, project.getTitle(), project.getDescription(), path);
                    controller.upload(auth.convertToAccountObject(), valueMap);
                }
                
            };
            mainHandler.post(myRunnable);
        } else {
            Log.d(TAG, "Can't upload to SoundCloud, last rendered file doesn't exist.");
            // TODO get this error back to the activity for display 
            jobFailed(ERROR_NO_RENDER_FILE, "Can't upload to SoundCloud, last rendered file doesn't exist."); // FIXME move to strings.xml
        }
    }
    
    @Override
    public void jobSucceeded(String result) {
        JsonElement jelement = new JsonParser().parse(result);
        JsonObject jobject = jelement.getAsJsonObject();
        String key = jobject.get("id").toString();
        super.jobSucceeded(key);
    }
}
