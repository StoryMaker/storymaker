package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.db.StoryMakerDB;
import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.AuthTable;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.model.JobTable;
import info.guardianproject.mrapp.model.Project;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import net.sqlcipher.database.SQLiteDatabase;

public class RenderService extends ServiceBase {
    private final String TAG = "RenderService";
    
	private Context context;
	private PublishController controller;
	private static RenderService instance = null;
	
	private RenderService(Context context, PublishController controller) {
        this.context = context;
        this.controller = controller;
    }
	
	public static RenderService getInstance(Context context, PublishController controller) {
		if (instance == null) {
			instance = new RenderService(context, controller);
		}
		return instance;
	}
	
	public void start() {
		// TODO guard against multiple calls if we are running already
//		ArrayList<Job> jobs = (ArrayList<Job>) (new JobTable(db)).getUnfinishedAsList(context, JobTable.TYPE_UPLOAD);
//	    SQLiteDatabase db = (new StoryMakerDB(context)).getWritableDatabase("foo");
		Job job = (new JobTable(null)).getNextUnfinished(context, JobTable.TYPE_RENDER);
		RendererBase renderer = null;
		if (job != null) {
    		if (job.isSpec(VideoRenderer.SPEC_KEY)) {
    			renderer = new VideoRenderer(context, this, job);
    		} //else if (job.isSpec(Auth.SITE_STORYMAKER)) {
    //			renderer = new StoryMakerUploader(context, this, job);
    //		}
            renderer.start();
		}
	}
	
	public void jobSucceeded(Job job, String code) {
		// TODO start the next job
	    Log.d(TAG, "jobSucceeded: " + job + ", with code: " + code);
		controller.jobSucceeded(job, code);
	}
	
	public void jobFailed(Job job, int errorCode, String errorMessage) {
        Log.d(TAG, "jobFailed: " + job + ", with errorCode: " + errorCode + ", and errorMessage: " + errorMessage);
		// TODO start the next job
		controller.jobFailed(job, errorCode, errorMessage);
	}
	
//	public void jobFinished() {
//		// TODO check if jobs are all done, if so let's die
////		finish();
//		controller.
//	}
}
