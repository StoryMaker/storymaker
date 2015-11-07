package org.storymaker.app.publish.sites;

import timber.log.Timber;

import android.app.Activity;

import org.storymaker.app.model.Job;
import org.storymaker.app.publish.RenderWorker;
import org.storymaker.app.publish.RendererBase;

public class StoryMakerRenderer extends RendererBase {
    private final String TAG = "StoryMakerRenderer";
	public static String SPEC_KEY = "storymaker";
	
	StoryMakerRenderer(Activity activity, RenderWorker service, Job job) {
		super(activity, service, job);
	}
	
}
