package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Job;
import org.holoeverywhere.app.Activity;

import android.content.Context;

public abstract class UploaderBase extends JobBase {
    private final String TAG = "UploaderBase";
    
    protected UploaderBase(Context context, WorkerBase worker, Job job) {
        super(context, worker, job);
        // TODO Auto-generated constructor stub
    }
}
