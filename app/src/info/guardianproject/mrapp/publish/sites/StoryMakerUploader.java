package info.guardianproject.mrapp.publish.sites;

import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.publish.UploadService;
import info.guardianproject.mrapp.publish.UploaderBase;
import android.content.Context;

public class StoryMakerUploader extends UploaderBase {
    private final String TAG = "StoryMakerUploader";

	public StoryMakerUploader(Context context, UploadService service, Job job) {
		super(context, service, job);
		// TODO Auto-generated constructor stub
		
		// grab the youtube id from the job.result, create the embed wrapper and post it to wordpress
	}

}
