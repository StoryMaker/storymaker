package org.storymaker.app.publish.sites;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import org.storymaker.app.model.Auth;
import org.storymaker.app.model.AuthTable;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.Media;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.UploaderBase;
import org.storymaker.app.publish.WorkerBase;
import io.scal.secureshareui.controller.FlickrSiteController;
import io.scal.secureshareui.controller.SiteController;

public class FlickrUploader extends UploaderBase {
    private final String TAG = "FlickrUploader";
    
    public FlickrUploader(Context context, WorkerBase worker, Job job) {
        super(context, worker, job);
    }

    @Override
    public void start() {
        Log.d(TAG, "start()");
        
        final SiteController controller = SiteController.getSiteController(FlickrSiteController.SITE_KEY, mContext, mHandler, ""+mJob.getId());
        final Project project = mJob.getProject();
        final PublishJob publishJob = mJob.getPublishJob();
        final String path = publishJob.getLastRenderFilePath();
        final Auth auth = (new AuthTable()).getAuthDefault(mContext, Auth.SITE_FLICKR);
        if (path != null) {
            Handler mainHandler = new Handler(mContext.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run()");
                 // FIXME, this might not be wise to run on the main thread flickr, does the flickr SDK automatically run itself on a backgroundthread?
                    HashMap<String, String> valueMap = publishJob.getMetadata();

                    // need to extract raw photos
                    // what happened to STORY_TYPE_PHOTO?

                    // TODO: currently checking preferences, will revisit when ui is updated
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    boolean uploadPhotos = sharedPref.getBoolean("pphotoformat", false);

                    if ((publishJob.getProject().getStoryType() == Project.STORY_TYPE_ESSAY) && uploadPhotos) {
                        ArrayList<Media> photos = publishJob.getProject().getMediaAsList();
                        String photoString = "";
                        for (Media photo : photos) {
                            photoString = photoString + photo.getPath() + ";";
                        }

                        // drop trailing ;
                        photoString = photoString.substring(0, photoString.length() - 1);

                        valueMap.put(FlickrSiteController.PHOTO_SET_KEY, photoString);

                    }

                    addValuesToHashmap(valueMap, project.getTitle(), project.getDescription(), path);
                    controller.upload(auth.convertToAccountObject(), valueMap);
                }
            };
            mainHandler.post(myRunnable);
        } else {
            Log.e(TAG, "flickr upload failed, file path is null");
        }
    }
}
