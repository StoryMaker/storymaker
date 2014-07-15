package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.publish.sites.AudioRenderer;
import info.guardianproject.mrapp.publish.sites.VideoRenderer;

import org.holoeverywhere.app.Activity;

import android.content.Context;
import android.util.Log;

public class RenderWorker extends WorkerBase {
    private final String TAG = "RenderService";
    
	private PublishController mController;
	private static RenderWorker instance = null;
	
	private RenderWorker(Context context, PublishController controller) {
	    mContext = context;
        mController = controller; // FIXME move to base class
    }
	
	public static RenderWorker getInstance(Context context, PublishController controller) {
		if (instance == null) {
			instance = new RenderWorker(context, controller);
		}
		return instance;
	}
	
	public void start(PublishJob publishJob) {
		// TODO guard against multiple calls if we are running already
//		ArrayList<Job> jobs = (ArrayList<Job>) (new JobTable(db)).getUnfinishedAsList(context, JobTable.TYPE_UPLOAD);
//	    SQLiteDatabase db = (new StoryMakerDB(context)).getWritableDatabase("foo");
		Job job = (new JobTable(null)).getNextUnfinished(mContext, JobTable.TYPE_RENDER, publishJob, null);
		RendererBase renderer = null;
		if (job != null) {
            if (job.isSpec(VideoRenderer.SPEC_KEY)) {
                renderer = new VideoRenderer(mContext, this, job);
            } else if (job.isSpec(AudioRenderer.SPEC_KEY)) {
                renderer = new AudioRenderer(mContext, this, job);
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

    @Override
    public void jobProgress(Job job, float progress, String message) {
        mController.jobProgress(job, progress, message);
        
    }
	
//	public void jobFinished() {
//		// TODO check if jobs are all done, if so let's die
////		finish();
//		controller.
//	}
}
