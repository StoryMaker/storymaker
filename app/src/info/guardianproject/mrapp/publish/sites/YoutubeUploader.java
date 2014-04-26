package info.guardianproject.mrapp.publish.sites;

import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.publish.UploadService;
import info.guardianproject.mrapp.publish.UploaderBase;
import org.holoeverywhere.app.Activity;

public class YoutubeUploader extends UploaderBase {
    private final String TAG = "YoutubeUploader";

	public YoutubeUploader(Activity activity, UploadService service, Job job) {
		super(activity, service, job);
	}
	
}
