package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Job;
import org.holoeverywhere.app.Activity;

public abstract class UploaderBase extends JobBase {
    private final String TAG = "UploaderBase";
    
    protected UploaderBase(Activity activity, ServiceBase service, Job job) {
        super(activity, service, job);
        // TODO Auto-generated constructor stub
    }
}
