package info.guardianproject.mrapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import info.guardianproject.mrapp.media.MediaProjectManager;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;

import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.MediaUtils;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.ProgressDialog;

import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.widget.Toast;

/*
 * EditorBaseActivity acts as the base class for StoryTemplateActivity and SceneEditorActivity
 */
public class EditorBaseActivity extends BaseActivity {
	
    public MediaProjectManager mMPM;
    public MediaDesc mdExported = null;
    private ProgressDialog mProgressDialog = null;
    
    public final static int REQ_YOUTUBE_AUTH = 999;
	
    public Handler mHandlerPub = new Handler()
    {

        @Override
        public void handleMessage(Message msg) {
            
            String statusTitle = msg.getData().getString("statusTitle");
            String status = msg.getData().getString("status");

            String error = msg.getData().getString("error");
            if (error == null)
                error = msg.getData().getString("err");
            
            int progress = msg.getData().getInt("progress");
            
            if (mProgressDialog != null)
            {
                if (progress >= 0)
                mProgressDialog.setProgress(progress);
            
                if (statusTitle != null)
                    mProgressDialog.setTitle(statusTitle);
                
            }

            
            switch (msg.what)
            {
                case 0:
                case 1:
                    
                    if (status != null)
                    {
                        if (mProgressDialog != null)
                        {
                            mProgressDialog.setMessage(status);
                        }
                        else
                        {
                            Toast.makeText(EditorBaseActivity.this, status, Toast.LENGTH_SHORT).show();
                            
                        }
                    }
                break;
                
                case 999:
                    
                        mProgressDialog = new ProgressDialog(EditorBaseActivity.this);
                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgressDialog.setTitle(getString(R.string.rendering));
                        mProgressDialog.setMessage(getString(R.string.rendering_project_));
                        mProgressDialog.setCancelable(true);
                        mProgressDialog.show();
                    
                break;
                
                case 888:
                	if (mProgressDialog != null)
                      mProgressDialog.setMessage(status);
                break;
                case 777:
                    
                    String videoId = msg.getData().getString("youtubeid");
                    String url = msg.getData().getString("urlPost");
                    String localPath = msg.getData().getString("fileMedia");
                    String mimeType = msg.getData().getString("mime");
                    
                    if (mProgressDialog != null)
                    {
                    	mProgressDialog.dismiss();
                    	mProgressDialog = null;
                    }
                    
                    showPublished(url,new File(localPath),videoId,mimeType);
                    
                    
                break;
                case -1:
                    Toast.makeText(EditorBaseActivity.this, error, Toast.LENGTH_SHORT).show();
                    if (mProgressDialog != null)
                    {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                break;
                default:
                
                    
            }
            
            
        }
        
    };
    
    public Bitmap getThumbnail(Media media)
    {
        if (media == null)
            return null;
        
        String path = media.getPath();

        if (media.getMimeType() == null)
        {
            return null;
        }
        else if (media.getMimeType().startsWith("video"))
        {
            File fileThumb = new File(path + ".jpg");
            if (fileThumb.exists())
            {

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                return BitmapFactory.decodeFile(fileThumb.getAbsolutePath(), options);
            }
            else
            {
                Bitmap bmp = MediaUtils.getVideoFrame(path, -1);
                try {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 70, new FileOutputStream(fileThumb));
                } catch (FileNotFoundException e) {
                    Log.e(AppConstants.TAG, "could not cache video thumb", e);
                }

                return bmp;
            }
        }
        else if (media.getMimeType().startsWith("image"))
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;

            return BitmapFactory.decodeFile(path, options);
        }
        else if (media.getMimeType().startsWith("audio"))
        {
            return BitmapFactory.decodeResource(getResources(), R.drawable.thumb_audio);
        }
        else 
        {
            return BitmapFactory.decodeResource(getResources(), R.drawable.thumb_complete);
        }
    }
    
    public void showPublished(final String postUrl, final File localMedia, final String youTubeId,
            final String mimeType)
    {
        if (youTubeId != null || postUrl != null)
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:


                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(postUrl));
                            startActivity(i);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:

                           
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.view_published_media_online_or_local_copy_)
                    .setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener).show();
        }
        else
        {
        	
        	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:

                        	mMPM.mMediaHelper.playMedia(localMedia, mimeType);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            mMPM.mMediaHelper.shareMedia(localMedia, mimeType);

                           
                            break;
                    }
                }
            };

            if (this.getWindow().isActive())
            {
	            AlertDialog.Builder builder = new AlertDialog.Builder(this);
	            builder.setMessage(R.string.play_or_share_exported_media_)
	                    .setPositiveButton(R.string.menu_play_media, dialogClickListener)
	                    .setNegativeButton(R.string.menu_share_media, dialogClickListener).show();
            }
        }

    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mMPM.mProject.isTemplateStory()) {
                    Intent intent = new Intent(this, StoryTemplateActivity.class);
                    String lang = StoryMakerApp.getCurrentLocale().getLanguage();
                    intent.putExtra("template_path", "story/templates/" + lang + "/event/event_basic.json");
                    intent.putExtra("story_mode", mMPM.mProject.getStoryType());
                    intent.putExtra("pid", mMPM.mProject.getId());
                    intent.putExtra("title", mMPM.mProject.getTitle());
                    NavUtils.navigateUpTo(this, intent);
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    
    
}
