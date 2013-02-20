
package info.guardianproject.mrapp;

import info.guardianproject.mrapp.media.MediaProjectManager;
import info.guardianproject.mrapp.media.OverlayCameraActivity;
import info.guardianproject.mrapp.model.template.Clip;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Scene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.micode.soundrecorder.SoundRecorder;

import org.ffmpeg.android.MediaUtils;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.ProgressDialog;
import org.json.JSONException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class SceneEditorActivity extends EditorBaseActivity implements ActionBar.TabListener {
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	
	private final static int REQ_OVERLAY_CAM = 888; //for resp handling from overlay cam launch
	
    protected Menu mMenu = null;
    
    private String mTemplateJsonPath = null;
    private int mSceneIndex = 0;
    
    private int mStoryMode = Project.STORY_TYPE_VIDEO;;
    private final static String CAPTURE_MIMETYPE_AUDIO = "audio/3gpp";
    public Fragment mFragmentTab0, mFragmentTab1, mLastTabFrag;
    public PublishFragment mPublishFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        
        mTemplateJsonPath = getIntent().getStringExtra("template_path"); 
        mStoryMode = getIntent().getIntExtra("story_mode", Project.STORY_TYPE_VIDEO);

        int pid = intent.getIntExtra("pid", -1); //project id

        mSceneIndex = getIntent().getIntExtra("scene", -1);

        if (pid != -1)
        {
            Project project = Project.get(mContext, pid);
            Scene scene = null;
            if ((mSceneIndex != -1) && (mSceneIndex < project.getScenesAsArray().length)) {
                scene = project.getScenesAsArray()[mSceneIndex];
            }
            mMPM = new MediaProjectManager(this, mContext, getIntent(), mHandlerPub, project, scene);
            mMPM.initProject();
            mMPM.addAllProjectMediaToEditor();
        }
        else
        {
            int clipCount = 5; // FIXME get rid of hardcoded clipCount = 5

            String title = intent.getStringExtra("title");
        
            Project project = new Project(mContext, clipCount);
            project.setTitle(title);
            project.save();
            mMPM = new MediaProjectManager(this, mContext, getIntent(), mHandlerPub, project);
            mMPM.initProject();
        }
        
        setContentView(R.layout.activity_scene_editor_no_swipe);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (mMPM.mScene != null) {
            actionBar.setTitle(mMPM.mScene.getTitle());
        }

        // For each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_add_clips).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_order).setTabListener(this));
        if (mMPM.mProject.isTemplateStory()) {
            actionBar.addTab(actionBar.newTab().setText(R.string.tab_finish).setTabListener(this));
        } else {
            actionBar.addTab(actionBar.newTab().setText(R.string.tab_publish).setTabListener(this));
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
                if (mMPM.mProject.isTemplateStory()) {
                    Intent intent = new Intent(this, StoryTemplateActivity.class);
                    String lang = StoryMakerApp.getCurrentLocale().getLanguage();
                    intent.putExtra("template_path", "story/templates/" + lang + "/video_3_scene.json");
                    intent.putExtra("story_mode", mMPM.mProject.getStoryType());
                    intent.putExtra("pid", mMPM.mProject.getId());
                    intent.putExtra("title", mMPM.mProject.getTitle());
                    NavUtils.navigateUpTo(this, intent);
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
    
    // FIXME move this into AddClipsFragment?
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
    // mTemplateJsonPath);
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
                    mFragmentTab0 = new AddClipsFragment(layout, fm, mTemplateJsonPath, mSceneIndex, this);

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
            if (mMPM.mProject.isTemplateStory()) {
                Intent intent = new Intent(getBaseContext(), StoryTemplateActivity.class);
                intent.putExtra("template_path", mTemplateJsonPath);
                intent.putExtra("story_mode", mMPM.mProject.getStoryType());
                intent.putExtra("pid", mMPM.mProject.getId());
                intent.putExtra("title", mMPM.mProject.getTitle());
                startActivity(intent);
                finish();
            } else {
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
