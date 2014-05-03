package info.guardianproject.mrapp.publish.sites;

import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.publish.UploadWorker;
import info.guardianproject.mrapp.publish.UploaderBase;
import org.holoeverywhere.app.Activity;

import android.content.Context;

public class YoutubeUploader extends UploaderBase {
    private final String TAG = "YoutubeUploader";

	public YoutubeUploader(Context context, UploadWorker worker, Job job) {
		super(context, worker, job);
	}
	
}
