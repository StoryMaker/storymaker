package org.storymaker.app.publish.sites;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import org.storymaker.app.R;
import org.storymaker.app.Utils;
import org.storymaker.app.model.Auth;
import org.storymaker.app.model.AuthTable;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.JobTable;
import org.storymaker.app.model.Media;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.model.json.ProjectJson;
import org.storymaker.app.publish.UploadWorker;
import org.storymaker.app.publish.UploaderBase;
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
        final Project project = mJob.getProject();
        final PublishJob publishJob = mJob.getPublishJob();
        if ((publishJob.getProject().getStoryType() == Project.STORY_TYPE_PHOTO) || (publishJob.getProject().getStoryType() == Project.STORY_TYPE_AUDIO)) {
            uploadAudioAndVideo(project, publishJob);
        } else {
            uploadPhoto(project, publishJob);
        }
    }

    public void uploadAudioAndVideo(final Project project, final PublishJob publishJob) {
        final String path = publishJob.getLastRenderFilePath();
        final Auth auth = (new AuthTable()).getAuthDefault(mContext, FacebookSiteController.SITE_KEY);
        if (Utils.stringNotBlank(path) && (new File(path)).exists()) {
            Handler mainHandler = new Handler(mContext.getMainLooper());
            Runnable myRunnable = new Runnable() {
                // facebook seems to freak out if our service's looper is dead when it tries to send message back
                @Override
                public void run() {
                    jobProgress(mJob, 0, mContext.getString(R.string.uploading_to_facebook));
                    HashMap<String, String> valueMap = publishJob.getMetadata();

                    // need to extract raw photos
                    // what happened to STORY_TYPE_PHOTO?

                    // TODO: currently checking preferences, will revisit when ui is updated
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    boolean uploadPhotos = sharedPref.getBoolean("pphotoformat", false);

                    addValuesToHashmap(valueMap, project.getTitle(), project.getDescription(), path);
                    final SiteController controller = SiteController.getSiteController(FacebookSiteController.SITE_KEY, mContext, mHandler, "" + mJob.getId());
                    controller.upload(auth.convertToAccountObject(), valueMap);
                }
            };
            mainHandler.post(myRunnable);
        } else {
            Log.d(TAG, "Can't upload to facebook, last rendered file doesn't exist.");
            // TODO get this error back to the activity for display
            jobFailed(null, ERROR_NO_RENDER_FILE, "Can't upload to facebook, last rendered file doesn't exist."); // FIXME move to strings.xml
        }

    }

    // FIXME DON'T MERGE THIS, THIS IS A HACKED BIT OF CODE TO FORCE FACEBOOK SUBMISSION TO GO THROUGH
    public void uploadPhoto(final Project project, final PublishJob publishJob) {
        final String path = publishJob.getLastRenderFilePath();
        final Auth auth = (new AuthTable()).getAuthDefault(mContext, FacebookSiteController.SITE_KEY);
        Handler mainHandler = new Handler(mContext.getMainLooper());
        Runnable myRunnable = new Runnable() {
            // facebook seems to freak out if our service's looper is dead when it tries to send message back
            @Override
            public void run() {
                jobProgress(mJob, 0, mContext.getString(R.string.uploading_to_facebook));
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

                    valueMap.put(FacebookSiteController.PHOTO_SET_KEY, photoString);

                }

                addValuesToHashmap(valueMap, project.getTitle(), project.getDescription(), path);
                final SiteController controller = SiteController.getSiteController(FacebookSiteController.SITE_KEY, mContext, mHandler, "" + mJob.getId());
                controller.upload(auth.convertToAccountObject(), valueMap);
            }
        };
        mainHandler.post(myRunnable);
    }
}
