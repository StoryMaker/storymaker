package info.guardianproject.mrapp.publish.sites;

import java.io.File;

import info.guardianproject.mrapp.media.MediaProjectManager;
import info.guardianproject.mrapp.model.Job;
import info.guardianproject.mrapp.publish.RenderWorker;
import info.guardianproject.mrapp.publish.RendererBase;

import org.ffmpeg.android.MediaDesc;
import org.holoeverywhere.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class VideoRenderer extends RendererBase {
    private final String TAG = "VideoRenderer";
	public static String SPEC_KEY = "video";
	private MediaProjectManager mMPM;
	
	public VideoRenderer(Context context, RenderWorker service, Job job) {
		super(context, service, job);
        Handler handler = new Handler() {

            @Override
            public void dispatchMessage(Message msg) {
                // TODO Auto-generated method stub
                super.dispatchMessage(msg);
            }

            @Override
            public String getMessageName(Message message) {
                // TODO Auto-generated method stub
                return super.getMessageName(message);
            }

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
            }

            @Override
            public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
                // TODO Auto-generated method stub
                return super.sendMessageAtTime(msg, uptimeMillis);
            }

            
        }; // FIXME we need to use this to communicate back with the activity
        mMPM = new MediaProjectManager(null, mContext, handler, mJob.getProjectId());
	}

    @Override
    public void start() {
//        super.start();
        renderVideo();
    }
	
    private void renderVideo() {
        File mFileLastExport = mMPM.getExportMediaFile();
        boolean compress = mSettings.getBoolean("pcompress", false);// compress
        boolean doOverwrite = true;
        try {
            MediaDesc mdExported = mMPM.doExportMedia(mFileLastExport, compress, doOverwrite);
            jobSucceeded(mdExported.path);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            jobFailed(0, e.getMessage());
        }
    }
}
