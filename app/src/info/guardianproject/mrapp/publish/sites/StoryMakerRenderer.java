package info.guardianproject.mrapp.publish.sites;

import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.publish.RenderWorker;
import info.guardianproject.mrapp.publish.RendererBase;
import org.holoeverywhere.app.Activity;

public class StoryMakerRenderer extends RendererBase {
    private final String TAG = "StoryMakerRenderer";
	public static String SPEC_KEY = "storymaker";
	
	StoryMakerRenderer(Activity activity, RenderWorker service, Job job) {
		super(activity, service, job);
	}
	
}
