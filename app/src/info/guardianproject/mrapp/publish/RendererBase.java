package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Job;
import org.holoeverywhere.app.Activity;

import android.content.Context;

// TODO a lot of code shared between this and uploaderBase...
public class RendererBase extends JobBase {
    private final String TAG = "RendererBase";
    
    protected RendererBase(Context context, ServiceBase service, Job job) {
        super(context, service, job);
        // TODO Auto-generated constructor stub
    }
}
