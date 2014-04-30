package info.guardianproject.mrapp.publish.sites;

import org.holoeverywhere.app.Activity;

import android.content.Context;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.publish.UploadWorker;
import info.guardianproject.mrapp.publish.UploaderBase;

public class StoryMakerUploader extends UploaderBase {
    private final String TAG = "StoryMakerUploader";

	public StoryMakerUploader(Context context, UploadWorker service, Job job) {
		super(context, service, job);
		// TODO Auto-generated constructor stub
		
		// grab the youtube id from the job.result, create the embed wrapper and post it to wordpress
	}

}
