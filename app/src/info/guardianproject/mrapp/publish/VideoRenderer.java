package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Job;
import android.content.Context;

public class VideoRenderer extends RendererBase {
    private final String TAG = "VideoRenderer";
	public static String SPEC_KEY = "video";
	
	VideoRenderer(Context context, RenderService service, Job job) {
		super(context, service, job);
	}
}
