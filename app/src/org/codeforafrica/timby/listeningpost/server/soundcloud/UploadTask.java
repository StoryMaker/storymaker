package org.codeforafrica.timby.listeningpost.server.soundcloud;

import com.soundcloud.api.Endpoints;
import com.soundcloud.api.Params;
import com.soundcloud.api.Request;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.codeforafrica.timby.listeningpost.AppConstants;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;


/**
 * An AsyncTask which shows how to upload a track to SoundCloud using the
 * <a href="https://github.com/soundcloud/java-api-wrapper">Java API wrapper</a>.
 */
public class UploadTask extends AsyncTask<File, Long, HttpResponse> {
    private Exception mException;
    private WeakReference<Activity> mContext;

    public UploadTask(Activity context) {
       mContext = new WeakReference<Activity>(context);
    }

    @Override
    protected HttpResponse doInBackground(File... params) {
        final File file = params[0];

        try {
            return Api.wrapper.post(Request.to(Endpoints.TRACKS)
                    .withFile(Params.Track.ASSET_DATA, file)
                    .add(Params.Track.TITLE, file.getName())
                    .add(Params.Track.SHARING, Params.Track.PRIVATE)
                    .setProgressListener(new Request.TransferProgressListener() {
                        @Override
                        public void transferred(long l) throws IOException {
                            if (isCancelled()) throw new IOException("canceled");
                            publishProgress(l, file.length());
                        }
                    }));

        } catch (IOException e) {
            Log.w(AppConstants.TAG, "error", e);
            mException = e;
            return null;
        }
    }

   public void attachContext(Activity context) {
        if (getStatus() != Status.FINISHED) {
            mContext = new WeakReference<Activity>(context);
            onPreExecute();
        }
    }

    @Override
    protected void onPreExecute() {
       
    }

    @Override
    protected void onProgressUpdate(Long... values) {
      //  if (!mProgress.isShowing()) mProgress.show();

       // mProgress.setProgress(values[0].intValue());
       // mProgress.setMax(values[1].intValue());
    }

    @Override
    protected void onPostExecute(HttpResponse response) {
        Activity activity = mContext.get();
        if (activity != null) {
            if (response == null) {
                Toast.makeText(activity, "Error during upload: " + mException.getMessage(),
                        Toast.LENGTH_LONG).show();

            } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                Toast.makeText(activity, "File has been uploaded",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity, "Error uploading file: " + response.getStatusLine(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
