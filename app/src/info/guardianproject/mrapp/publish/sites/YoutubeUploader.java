package info.guardianproject.mrapp.publish.sites;

import java.io.File;

import info.guardianproject.mrapp.Utils;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.publish.UploadWorker;
import info.guardianproject.mrapp.publish.UploaderBase;
import io.scal.secureshareui.controller.FacebookSiteController;
import io.scal.secureshareui.controller.SiteController;

import org.holoeverywhere.app.Activity;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class YoutubeUploader extends UploaderBase {
    private final String TAG = "YoutubeUploader";

	public YoutubeUploader(Context context, UploadWorker worker, Job job) {
		super(context, worker, job);
	}

}
