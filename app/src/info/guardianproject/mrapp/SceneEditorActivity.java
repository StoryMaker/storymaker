
package info.guardianproject.mrapp;

import info.guardianproject.mrapp.media.AudioRecorderView;
import info.guardianproject.mrapp.media.MediaProjectManager;
import info.guardianproject.mrapp.media.OverlayCameraActivity;
import info.guardianproject.mrapp.model.Clip;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Template;
import info.guardianproject.mrapp.server.LoginActivity;
import info.guardianproject.mrapp.server.ServerManager;
import info.guardianproject.mrapp.server.SoundCloudUploader;
import info.guardianproject.mrapp.server.YouTubeSubmit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import net.micode.soundrecorder.SoundRecorder;

import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.MediaUtils;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.widget.SeekBar;
import org.holoeverywhere.widget.SeekBar.OnSeekBarChangeListener;
import org.holoeverywhere.widget.ToggleButton;
import org.json.JSONException;

import redstone.xmlrpc.XmlRpcFault;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.animoto.android.views.DraggableGridView;
import com.animoto.android.views.OnRearrangeListener;

public class SceneEditorActivity extends EditorBaseActivity implements ActionBar.TabListener {
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	
	private final static int REQ_OVERLAY_CAM = 888; //for resp handling from overlay cam launch
	
    protected boolean templateStory = false; 
    protected Menu mMenu = null;
    private Context mContext = null;
    private String templateJsonPath = null;
    private int mStoryMode = Project.STORY_TYPE_VIDEO;;
    private final static String CAPTURE_MIMETYPE_AUDIO = "audio/3gpp";
    public Fragment mFragmentTab0, mFragmentTab1, mLastTabFrag;
    public PublishFragment mPublishFragment;
	
    private ProgressDialog mProgressDialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        
        templateStory = intent.hasExtra("template_story");
        templateJsonPath = getIntent().getStringExtra("template_path");
        mStoryMode = getIntent().getIntExtra("story_mode", Project.STORY_TYPE_VIDEO);

        int pid = intent.getIntExtra("pid", -1); //project id

        mContext = getBaseContext();

        if (pid != -1)
        {
            mMPM = new MediaProjectManager(this, mContext, getIntent(), mHandlerPub, pid);
        }
        else
        {
            int clipCount = 5;

            String title = intent.getStringExtra("title");
        
            Project project = new Project(mContext, clipCount);
            project.setTitle(title);
            project.save();
            
            mMPM = new MediaProjectManager(this, mContext, getIntent(), mHandlerPub, project);
        }
        
        setContentView(R.layout.activity_scene_editor_no_swipe);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // For each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_add_clips).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_order).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_publish).setTabListener(this));

    }

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
							Toast.makeText(mContext, status, Toast.LENGTH_SHORT).show();
							
						}
					}
				break;
				
				case 999:
					
						mProgressDialog = new ProgressDialog(SceneEditorActivity.this);
	          		    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	          		    mProgressDialog.setTitle(getString(R.string.rendering));
	          		    mProgressDialog.setMessage(getString(R.string.rendering_project_));
	          		    mProgressDialog.setCancelable(true);
	          		    mProgressDialog.show();
					
				break;
				
				case 888:
	          		  mProgressDialog.setMessage(status);
	            break;
				case 777:
					
		  	        String videoId = msg.getData().getString("youtubeid");
		  	        String url = msg.getData().getString("urlPost");
		  	        String localPath = msg.getData().getString("fileMedia");
		  	        String mimeType = msg.getData().getString("mime");
		  	        
					mProgressDialog.dismiss();
					mProgressDialog = null;
					
					showPublished(url,new File(localPath),videoId,mimeType);
					
					
				break;
				case -1:
					Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
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

                            String urlOnline = postUrl;

                            if (youTubeId != null)
                                urlOnline = "https://www.youtube.com/watch?v=" + youTubeId;

                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(urlOnline));
                            startActivity(i);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:

                            mMPM.mMediaHelper.playMedia(localMedia, mimeType);

                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.view_published_media_online_or_local_copy_)
                    .setPositiveButton(R.string.youtube, dialogClickListener)
                    .setNegativeButton(R.string.local, dialogClickListener).show();
        }
        else
        {

            mMPM.mMediaHelper.playMedia(localMedia, mimeType);

        }

    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getSupportActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getSupportMenuInflater().inflate(R.menu.activity_scene_editor, menu);
        mMenu = menu;
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (templateStory) {
                    NavUtils.navigateUpTo(this, new Intent(this, StoryTemplateActivity.class));
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            case R.id.itemForward:
                int idx = getSupportActionBar().getSelectedNavigationIndex();
                if (idx < 2) {
                    getSupportActionBar().setSelectedNavigationItem(Math.min(2, idx + 1));
                } else {
                    mPublishFragment.doPublish();
                }
                return true;
            case R.id.addFromGallery:
                addMediaFromGallery();
            
                return true;
            case R.id.addNewShot:
             
                addShotToScene();
                
                return true;
                
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void addShotToScene ()
    {
        try
        {
            Clip tClip = new Clip();
            tClip.setDefaults();
            AddClipsFragment acf = ((AddClipsFragment)mFragmentTab0);
            acf.addTemplateClip(tClip);
        }
        catch (Exception e)
        {
            Log.e(AppConstants.TAG,"error adding new clip",e);
        }
    }

    private void addMediaFromGallery()
    {
        mMPM.mMediaHelper.openGalleryChooser("*/*");
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().hide(mLastTabFrag).commit();
    }

    // protected void setupAddClipsFragment() {
    // FragmentManager fm = getSupportFragmentManager();
    //
    // try {
    // mFragmentTab0 = new SceneChooserFragment(R.layout.fragment_add_clips, fm,
    // templateJsonPath);
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (JSONException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, show the tab contents in the
        // container
        int layout = R.layout.fragment_add_clips;
        FragmentManager fm = getSupportFragmentManager();

        if (mMenu != null) {
            mMenu.findItem(R.id.itemInfo).setVisible(false);
            mMenu.findItem(R.id.itemTrim).setVisible(false);
        }

        if (tab.getPosition() == 0) {
            if (mMenu != null) {
                mMenu.findItem(R.id.itemForward).setEnabled(true);
            }
            layout = R.layout.fragment_add_clips;

            if (mFragmentTab0 == null)
            {
                try {
                    mFragmentTab0 = new AddClipsFragment(layout, fm, templateJsonPath, this);

                    Bundle args = new Bundle();
                    args.putInt(AddClipsFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
                    mFragmentTab0.setArguments(args);

                } catch (IOException e) {
                    Log.e("SceneEditr", "IO erorr", e);
                } catch (JSONException e) {
                    Log.e("SceneEditr", "json error", e);
                }

                fm.beginTransaction()
                        .add(R.id.container, mFragmentTab0, layout + "")
                        .commit();

            } else {
                fm.beginTransaction()
                        .show(mFragmentTab0)
                        .commit();
            }
            mLastTabFrag = mFragmentTab0;

        } else if (tab.getPosition() == 1) {
            layout = R.layout.fragment_order_clips;

            if (mMenu != null) {
                mMenu.findItem(R.id.itemInfo).setVisible(true);
                mMenu.findItem(R.id.itemTrim).setVisible(true);
                mMenu.findItem(R.id.itemForward).setEnabled(true);
            }

            if (mFragmentTab1 == null)
            {
                try {
                    mFragmentTab1 = new OrderClipsFragment(layout, this);

                    Bundle args = new Bundle();
                    args.putInt(OrderClipsFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
                    mFragmentTab1.setArguments(args);

                } catch (IOException e) {
                    Log.e("SceneEditr", "IO erorr", e);

                } catch (JSONException e) {
                    Log.e("SceneEditr", "json error", e);

                }

                fm.beginTransaction()
                        .add(R.id.container, mFragmentTab1, layout + "")
                        .commit();

            } else {

                ((OrderClipsFragment)mFragmentTab1).loadMedia();
                
                fm.beginTransaction()
                        .show(mFragmentTab1)
                        .commit();
            }

            mLastTabFrag = mFragmentTab1;

        } else if (tab.getPosition() == 2) {
            layout = R.layout.fragment_story_publish;
            
            if (mPublishFragment == null)
            {
                try {
                    mPublishFragment = new PublishFragment(layout, this);

                    Bundle args = new Bundle();
                    args.putInt(PublishFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
                    mPublishFragment.setArguments(args);

                } catch (IOException e) {
                    Log.e("SceneEditr", "IO erorr", e);
                } catch (JSONException e) {
                    Log.e("SceneEditr", "json error", e);
                }
                fm.beginTransaction()
                        .add(R.id.container, mPublishFragment, layout + "")
                        .commit();

            } else {

                fm.beginTransaction()
                        .show(mPublishFragment)
                        .commit();
            }

            mLastTabFrag = mPublishFragment;
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void refreshClipPager() {
        if (mFragmentTab0 != null) {
            try {
                ((AddClipsFragment) mFragmentTab0).reloadClips();
            } 
            catch (Exception e) {
                Log.e(AppConstants.TAG, "error reloading clips", e);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

	public void openCaptureMode (Clip clip, int clipIndex)
	{
		if (mStoryMode == Project.STORY_TYPE_AUDIO)
		{
			Intent i = new Intent(mContext, SoundRecorder.class);
			i.setType(CAPTURE_MIMETYPE_AUDIO);
			i.putExtra("mode", mStoryMode);
			mMPM.mClipIndex = clipIndex;
			startActivityForResult(i,mStoryMode);

        }
        else
        {

            // mMPM.mMediaHelper.openGalleryChooser("*/*");
            // mMPM.mMediaHelper.captureVideo(mContext.getExternalFilesDir(null));

            Intent i = new Intent(mContext, OverlayCameraActivity.class);
            i.putExtra("group", clip.mShotType);
            i.putExtra("mode", mStoryMode);
            mMPM.mClipIndex = clipIndex;
            startActivityForResult(i, REQ_OVERLAY_CAM);
        }
    }

    private File mCapturePath;

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent intent) {

        if (resCode == RESULT_OK)
        {
            if (reqCode == REQ_OVERLAY_CAM)
            {
                File fileMediaFolder = getExternalFilesDir(null);

                if (mStoryMode == Project.STORY_TYPE_VIDEO)
                {
                    mCapturePath = mMPM.mMediaHelper.captureVideo(fileMediaFolder);

                }
                else if (mStoryMode == Project.STORY_TYPE_PHOTO)
                {
                    mCapturePath = mMPM.mMediaHelper.capturePhoto(fileMediaFolder);
                }
                else if (mStoryMode == Project.STORY_TYPE_ESSAY)
                {
                    mCapturePath = mMPM.mMediaHelper.capturePhoto(fileMediaFolder);
                }

            }
            else
            {
                mMPM.handleResponse(intent, mCapturePath);

                refreshClipPager();
                
            }

        }
    }

    public Bitmap getThumbnail(Media media)
    {
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
        else 
        {
            return BitmapFactory.decodeResource(getResources(), R.drawable.thumb_complete);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        
    }
}
