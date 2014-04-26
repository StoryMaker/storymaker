package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Job;
import org.holoeverywhere.app.Activity;

// TODO a lot of code shared between this and uploaderBase...
public class RendererBase extends JobBase {
    private final String TAG = "RendererBase";
    
    protected RendererBase(Activity activity, ServiceBase service, Job job) {
        super(activity, service, job);
        // TODO Auto-generated constructor stub
    }
}
