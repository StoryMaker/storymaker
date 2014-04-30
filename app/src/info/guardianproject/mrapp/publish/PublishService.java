package info.guardianproject.mrapp.publish;

import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.ProjectTable;
import info.guardianproject.mrapp.model.PublishJob;
import info.guardianproject.mrapp.publish.PublishController.PublishListener;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class PublishService extends IntentService implements PublishListener {
    public static final String TAG = "PublishService";
    public static final String INTENT_PROJECT_ID = "project_id";
    public static final String INTENT_SITE_KEYS = "site_keys";
    
    public PublishService() {
        super("PublishService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.hasExtra(INTENT_PROJECT_ID) && intent.hasExtra(INTENT_SITE_KEYS)) {
            int id = intent.getIntExtra(INTENT_PROJECT_ID, -1);
            String[] siteKeys = new String[] { Auth.STORYMAKER }; // FIXME testing hard coded to storymaker only
//            String[] siteKeys = intent.getStringArrayExtra(INTENT_SITE_KEYS);
            if (id != -1) {
                Project project = (Project) (new ProjectTable()).get(getApplicationContext(), id);
                (new PublishController(getApplicationContext(), this)).startPublish(project, siteKeys);
            } else {
                Log.d(TAG, "invalid publish id passed: " + id);
            }
        }
    }

    @Override
    public void publishSucceeded(PublishJob publishJob) {
        // TODO pass this back to the PublishFragment via a BroadcastIntent?
    }
}
