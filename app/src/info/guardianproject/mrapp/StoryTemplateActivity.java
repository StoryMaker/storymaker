package info.guardianproject.mrapp;

import info.guardianproject.mrapp.media.MediaProjectManager;
import info.guardianproject.mrapp.model.Project;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;

public class StoryTemplateActivity extends EditorBaseActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    private String mTemplateJsonPath = null;
    private Project mProject; 
    private PublishFragment mPublishFragment;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_template);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        mTemplateJsonPath = getIntent().getStringExtra("template_path"); 
        int pid = intent.getIntExtra("pid", -1); //project id
        mMPM = new MediaProjectManager(this, this, mHandlerPub, pid);
        mMPM.initProject();
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (mMPM.mScene != null) {
            actionBar.setTitle(mMPM.mScene.getTitle());
        }

        
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding tab.
        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
        // Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            if (i == 0) {
                fragment = new TemplateStoryMakeFragment();
                
                Bundle args = new Bundle();
                args.putInt(TemplateStoryMakeFragment.ARG_SECTION_NUMBER, i + 1);
                args.putString("title", mMPM.mProject.getTitle());
                fragment.setArguments(args);
            } else if (i == 1) {
                fragment = new ReviewFragment();
                Bundle args = new Bundle();
                args.putInt(TemplateStoryMakeFragment.ARG_SECTION_NUMBER, i + 1);
                fragment.setArguments(args);
            } else if (i == 2) {
               
            	mPublishFragment = new PublishFragment();
                fragment = mPublishFragment;
                
                Bundle args = new Bundle();
                args.putInt(TemplateStoryMakeFragment.ARG_SECTION_NUMBER, i + 1);
            	args.putInt("layout", R.layout.fragment_complete_story);
                fragment.setArguments(args);
            } 
            
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.tab_make).toUpperCase();
                case 1: return getString(R.string.tab_review).toUpperCase();
                case 2: return getString(R.string.tab_publish).toUpperCase();
            }
            return null;
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent intent) {

    	
        if (resCode == RESULT_OK)
        {
            if (reqCode == REQ_YOUTUBE_AUTH)
            {
            	if (resCode == RESULT_OK)
            	{
            		String oauthToken = intent.getStringExtra("token");
            		Log.d("OAuth","got token: " + oauthToken);
            		mPublishFragment.setYouTubeAuth(oauthToken);
            	}
            }

        }
    }

    
}
