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
    public static final String INTENT_EXTRA_PROJECT_ID = "project_id";
    public static final String INTENT_EXTRA_PUBLISH_JOB_ID = "publish_job_id";
    public static final String INTENT_EXTRA_SITE_KEYS = "site_keys";
    public static final String INTENT_EXTRA_PROGRESS = "progress_percent";
    public static final String INTENT_EXTRA_PROGRESS_MESSAGE = "progress_message";
    public static final String ACTION_RENDER = "info.guardianproject.mrapp.publish.action.RENDER";
    public static final String ACTION_UPLOAD = "info.guardianproject.mrapp.publish.action.UPLOAD";
    public static final String ACTION_SUCCESS = "info.guardianproject.mrapp.publish.action.SUCCESS";
    public static final String ACTION_FAILURE = "info.guardianproject.mrapp.publish.action.FAILURE";
    public static final String ACTION_PROGRESS = "info.guardianproject.mrapp.publish.action.PROGRESS";
    
    public PublishService() {
        super("PublishService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.hasExtra(INTENT_EXTRA_PROJECT_ID) && intent.hasExtra(INTENT_EXTRA_SITE_KEYS)) {
            int id = intent.getIntExtra(INTENT_EXTRA_PROJECT_ID, -1);
            if (id != -1) {
//                String[] siteKeys = new String[] { Auth.STORYMAKER }; // FIXME testing hard coded to storymaker only
                String[] siteKeys = intent.getStringArrayExtra(INTENT_EXTRA_SITE_KEYS);
                PublishController controller = (new PublishController(getApplicationContext(), this));
                Project project = (Project) (new ProjectTable()).get(getApplicationContext(), id);
                if (intent.getAction().equals(ACTION_RENDER)) {
                    controller.startRender(project, siteKeys);
                } else if (intent.getAction().equals(ACTION_UPLOAD)) {
                    controller.startUpload(project, siteKeys);
                }
            } else {
                Log.d(TAG, "invalid publish id passed: " + id);
            }
        }
    }

    @Override
    public void publishSucceeded(PublishJob publishJob) {
        Intent intent = new Intent(ACTION_SUCCESS);
        intent.putExtra(INTENT_EXTRA_PROJECT_ID, publishJob.getProjectId());
        intent.putExtra(INTENT_EXTRA_PUBLISH_JOB_ID, publishJob.getId());
        sendBroadcast(intent);
    }

    @Override
    public void publishFailed(PublishJob publishJob) {
        Intent intent = new Intent(ACTION_FAILURE);
        intent.putExtra(INTENT_EXTRA_PROJECT_ID, publishJob.getProjectId());
        intent.putExtra(INTENT_EXTRA_PUBLISH_JOB_ID, publishJob.getId());
        sendBroadcast(intent);
    }

    @Override
    public void publishProgress(PublishJob publishJob, int progress, String message) {
        Intent intent = new Intent(ACTION_PROGRESS);
        intent.putExtra(INTENT_EXTRA_PROJECT_ID, publishJob.getProjectId());
        intent.putExtra(INTENT_EXTRA_PUBLISH_JOB_ID, publishJob.getId());
        intent.putExtra(INTENT_EXTRA_PROGRESS, progress);
        intent.putExtra(INTENT_EXTRA_PROGRESS_MESSAGE, message);
        sendBroadcast(intent);
    }
}
