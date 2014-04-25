package info.guardianproject.mrapp.publish.sites;

import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.publish.RenderService;
import info.guardianproject.mrapp.publish.RendererBase;
import android.content.Context;

public class StoryMakerRenderer extends RendererBase {
    private final String TAG = "StoryMakerRenderer";
	public static String SPEC_KEY = "storymaker";
	
	StoryMakerRenderer(Context context, RenderService service, Job job) {
		super(context, service, job);
	}
	
}
