package info.guardianproject.mrapp.publish.sites;

import org.holoeverywhere.app.Activity;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.publish.UploadService;
import info.guardianproject.mrapp.publish.UploaderBase;

public class StoryMakerUploader extends UploaderBase {
    private final String TAG = "StoryMakerUploader";

	public StoryMakerUploader(Activity activity, UploadService service, Job job) {
		super(activity, service, job);
		// TODO Auto-generated constructor stub
		
		// grab the youtube id from the job.result, create the embed wrapper and post it to wordpress
	}

}
