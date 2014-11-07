package org.storymaker.app.publish.sites;

import android.content.Context;
import org.storymaker.app.model.Job;
import org.storymaker.app.publish.UploadWorker;
import org.storymaker.app.publish.UploaderBase;

public class StoryMakerUploader extends UploaderBase {
    private final String TAG = "StoryMakerUploader";

	public StoryMakerUploader(Context context, UploadWorker worker, Job job) {
		super(context, worker, job);
		// TODO Auto-generated constructor stub
		
		// grab the youtube id from the job.result, create the embed wrapper and post it to wordpress
	}

}
