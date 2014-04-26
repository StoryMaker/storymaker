package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.publish.sites.VideoRenderer;

import org.holoeverywhere.app.Activity;
import android.util.Log;

public class RenderService extends ServiceBase {
    private final String TAG = "RenderService";
    
	private Activity mActivity;
	private PublishController mController;
	private static RenderService instance = null;
	
	private RenderService(Activity activity, PublishController controller) {
	    this.mActivity = activity;
        this.mController = controller; // FIXME move to base class
    }
	
	public static RenderService getInstance(Activity activity, PublishController controller) {
		if (instance == null) {
			instance = new RenderService(activity, controller);
		}
		return instance;
	}
	
	public void start() {
		// TODO guard against multiple calls if we are running already
//		ArrayList<Job> jobs = (ArrayList<Job>) (new JobTable(db)).getUnfinishedAsList(context, JobTable.TYPE_UPLOAD);
//	    SQLiteDatabase db = (new StoryMakerDB(context)).getWritableDatabase("foo");
		Job job = (new JobTable(null)).getNextUnfinished(mActivity, JobTable.TYPE_RENDER);
		RendererBase renderer = null;
		if (job != null) {
    		if (job.isSpec(VideoRenderer.SPEC_KEY)) {
    			renderer = new VideoRenderer(mActivity, this, job);
    		} //else if (job.isSpec(Auth.SITE_STORYMAKER)) {
    //			renderer = new StoryMakerUploader(context, this, job);
    //		}
            renderer.start();
		}
	}
	
	public void jobSucceeded(Job job, String code) {
		// TODO start the next job
	    Log.d(TAG, "jobSucceeded: " + job + ", with code: " + code);
		mController.jobSucceeded(job, code);
	}
	
	public void jobFailed(Job job, int errorCode, String errorMessage) {
        Log.d(TAG, "jobFailed: " + job + ", with errorCode: " + errorCode + ", and errorMessage: " + errorMessage);
		// TODO start the next job
		mController.jobFailed(job, errorCode, errorMessage);
	}
	
//	public void jobFinished() {
//		// TODO check if jobs are all done, if so let's die
////		finish();
//		controller.
//	}
}
