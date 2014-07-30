package org.codeforafrica.timby.listeningpost.spy;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;

import net.micode.soundrecorder.RecorderService;
import net.micode.soundrecorder.SoundRecorder;

import org.codeforafrica.timby.listeningpost.AppConstants;
import org.codeforafrica.timby.listeningpost.EditorBaseActivity;
import org.codeforafrica.timby.listeningpost.R;
import org.codeforafrica.timby.listeningpost.encryption.Encryption;
import org.codeforafrica.timby.listeningpost.media.MediaProjectManager;
import org.codeforafrica.timby.listeningpost.media.OverlayCameraActivity;
import org.codeforafrica.timby.listeningpost.model.Media;
import org.codeforafrica.timby.listeningpost.model.Project;
import org.codeforafrica.timby.listeningpost.model.ProjectTable;
import org.codeforafrica.timby.listeningpost.model.Scene;
import org.codeforafrica.timby.listeningpost.model.template.Clip;
import org.codeforafrica.timby.listeningpost.model.template.Template;
import org.ffmpeg.android.MediaDesc;
import org.ffmpeg.android.MediaUtils;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.widget.Toast;
import org.json.JSONArray;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class SceneEditorActivity_spy extends EditorBaseActivity{

    private int taken = 0;
    private final static String CAPTURE_MIMETYPE_AUDIO = "audio/3gpp";
    public Fragment mFragmentTab0, mFragmentTab1, mLastTabFrag;

    public int quickstory;
    
    public boolean importing;
    public MediaProjectManager mMPM;
    public MediaDesc mdExported = null;
    private ProgressDialog mProgressDialog = null;

    // sublaunch codes
    public final static int REQ_YOUTUBE_AUTH = 999;
    public final static int REQ_OVERLAY_CAM = 888; // for resp handling from
                                                   // overlay cam launch

    public Project mProject = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        
//        mTemplateJsonPath = getIntent().getStringExtra("template_path"); 
 //       mStoryMode = getIntent().getIntExtra("story_mode", Project.STORY_TYPE_VIDEO);

        int pid = intent.getIntExtra("pid", -1); //project id
        quickstory = intent.getIntExtra("quickstory", 0);
        importing = intent.getBooleanExtra("importing", false);
        	
        int mSceneIndex = getIntent().getIntExtra("scene", 0);

        if (pid != -1)
        {
        	mProject = (Project)(new ProjectTable()).get(getApplicationContext(), pid); // FIXME ugly
            Scene scene = null;
            if ((mSceneIndex != -1) && (mSceneIndex < mProject.getScenesAsArray().length)) {
                scene = mProject.getScenesAsArray()[mSceneIndex];
            }
            mMPM = new MediaProjectManager(this, getApplicationContext(), getIntent(), mHandlerPub, mProject, scene);
            mMPM.initProject();
            mMPM.addAllProjectMediaToEditor();

        }
        else
        {
            int clipCount = 5; // FIXME get rid of hardcoded clipCount = 5

            String title = intent.getStringExtra("title");
        
            mProject = new Project(getApplicationContext(), clipCount);
            mProject.setTitle(title);
            mProject.save();
            mMPM = new MediaProjectManager(this, getApplicationContext(), getIntent(), mHandlerPub, mProject);
            mMPM.initProject();
        }
        try
        {
	        if (mProject.getScenesAsList().size() > 1)
	        {
	        	mTemplate = Template.parseAsset(this, mProject.getTemplatePath(),Project.getSimpleTemplateForMode(getApplicationContext(), mProject.getStoryType()));
	        	
	        }
	        else
	        {
	        	mTemplate = Template.parseAsset(this, Project.getSimpleTemplateForMode(getApplicationContext(), mProject.getStoryType()));
	        	
	        }
        }catch (Exception e)
        {
        	Log.e(AppConstants.TAG,"could not parse templates",e);
        }
        
        
        	openCaptureMode(0, 0);
        
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

	public void openCaptureMode (int shotType, int clipIndex)
	{
		
		if (mProject.getStoryType() == Project.STORY_TYPE_AUDIO)
		{
			Intent i = new Intent(getApplicationContext(), SoundRecorderService.class);
			i.putExtra("dir", mMPM.getExternalProjectFolder(mProject, getBaseContext()));
			i.setType(CAPTURE_MIMETYPE_AUDIO);
			i.putExtra("mode", mProject.getStoryType());
			mMPM.mClipIndex = clipIndex;
			startActivityForResult(i,mProject.getStoryType());
        }
        else
        {
            Intent i = new Intent(getApplicationContext(), OverlayCameraActivity.class);
            i.putExtra("group", shotType);
            i.putExtra("mode", mProject.getStoryType());
            
            if(mProject.getMediaAsList().size()<2){
            	if(taken<1){
            		i.putExtra("take1", "1");
            		taken++;
            	}
            }
            
            mMPM.mClipIndex = clipIndex;
            startActivityForResult(i, REQ_OVERLAY_CAM);
        }
    }

    private File mCapturePath;

    protected void onActivityResult(int reqCode, int resCode, Intent intent) {

    	
        if (resCode == RESULT_OK)
        {
            if (reqCode == REQ_OVERLAY_CAM)
            {
                File fileMediaFolder = mMPM.getExternalProjectFolder(mProject, getBaseContext());

                if (mProject.getStoryType() == Project.STORY_TYPE_VIDEO)
                {
                    mCapturePath = mMPM.mMediaHelper.captureVideo(fileMediaFolder);

                }
                else if (mProject.getStoryType() == Project.STORY_TYPE_PHOTO)
                {
                    mCapturePath = mMPM.mMediaHelper.capturePhoto(fileMediaFolder);
                }
                else if (mProject.getStoryType() == Project.STORY_TYPE_ESSAY)
                {
                    mCapturePath = mMPM.mMediaHelper.capturePhoto(fileMediaFolder);
                }

            }
            else if (reqCode == REQ_YOUTUBE_AUTH)
            {/*
            	if (resCode == RESULT_OK)
            	{
          @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        
//        mTemplateJsonPath = getIntent().getStringExtra("template_path"); 
 //       mStoryMode = getIntent().getIntExtra("story_mode", Project.STORY_TYPE_VIDEO);

        int pid = intent.getIntExtra("pid", -1); //project id
        quickstory = intent.getIntExtra("quickstory", 0);
        importing = intent.getBooleanExtra("importing", false);
        	
        mSceneIndex = getIntent().getIntExtra("scene", 0);

        if (pid != -1)
        {
        	mProject = (Project)(new ProjectTable()).get(getApplicationContext(), pid); // FIXME ugly
            Scene scene = null;
            if ((mSceneIndex != -1) && (mSceneIndex < mProject.getScenesAsArray().length)) {
                scene = mProject.getScenesAsArray()[mSceneIndex];
            }
            mMPM = new MediaProjectManager(this, getApplicationContext(), getIntent(), mHandlerPub, mProject, scene);
            mMPM.initProject();
            mMPM.addAllProjectMediaToEditor();

        }
        else
        {
            int clipCount = 5; // FIXME get rid of hardcoded clipCount = 5

            String title = intent.getStringExtra("title");
        
            mProject = new Project(getApplicationContext(), clipCount);
            mProject.setTitle(title);
            mProject.save();
            mMPM = new MediaProjectManager(this, getApplicationContext(), getIntent(), mHandlerPub, mProject);
            mMPM.initProject();
        }
        try
        {
	        if (mProject.getScenesAsList().size() > 1)
	        {
	        	mTemplate = Template.parseAsset(this, mProject.getTemplatePath(),Project.getSimpleTemplateForMode(getApplicationContext(), mProject.getStoryType()));
	        	
	        }
	        else
	        {
	        	mTemplate = Template.parseAsset(this, Project.getSimpleTemplateForMode(getApplicationContext(), mProject.getStoryType()));
	        	
	        }
        }catch (Exception e)
        {
        	Log.e(AppConstants.TAG,"could not parse templates",e);
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
        if (mMPM.mProject.isTemplateStory()) {
            actionBar.addTab(actionBar.newTab().setText(R.string.tab_finish).setTabListener(this));
	    } else {
	         actionBar.addTab(actionBar.newTab().setText("Caption").setTabListener(this));
	    }
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_add_clips).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Scene").setTabListener(this));     
        
        if(importing == true){
        	addMediaFromGallery();
        }else if (intent.hasExtra("auto_capture")	&& intent.getBooleanExtra("auto_capture", false))
        {
        	openCaptureMode(0, 0);
        }
    }  		
            		String oauthToken = intent.getStringExtra("token");
            		Log.d("OAuth","got token: " + oauthToken);
            		mPublishFragment.setYouTubeAuth(oauthToken);
            	}*/
            }
            else
            {
            	try
            	{
            		mMPM.handleResponse(intent, mCapturePath);
            		
            		
            	}
            	catch (IOException e)
            	{
            		Log.e(AppConstants.TAG,"error handling capture response: " + mCapturePath,e);
            	}
            }

        }
    }

}
