package info.guardianproject.mrapp;

import info.guardianproject.mrapp.lessons.LessonManager;
import info.guardianproject.mrapp.model.Lesson;
import info.guardianproject.mrapp.model.LessonGroup;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.server.LoginActivity;
import info.guardianproject.onionkit.ui.OrbotHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.ffmpeg.android.MediaUtils;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.ProgressDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;

public class HomeActivity extends BaseActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    
    Button mButtonNewStory;
    Button mButtonStartALesson;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
        checkForTor ();
        
        try {
            String pkg = getPackageName();
            String vers= getPackageManager().getPackageInfo(pkg, 0).versionName;
            setTitle(getTitle() + " v" + vers);
                    
        } catch (NameNotFoundException e) {
           
        }
        
        
        
        setContentView(R.layout.activity_home);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        
        // action bar stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
//        // setup drawer
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
//        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setBehindWidthRes(R.dimen.slidingmenu_offset);

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
        
        Eula.show(this);
        
        
        if (getIntent().hasExtra("showtab"))
        {
        	int tab = getIntent().getExtras().getInt("showtab");
        	mViewPager.setCurrentItem(tab);
        }
        
    }
    
    private void checkForTor ()
    {
    	 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

	     boolean useTor = settings.getBoolean("pusetor", false);
	     
	     if (useTor)
	     {
	    	 OrbotHelper oh = new OrbotHelper(this);
	    	 
	    	 if (!oh.isOrbotInstalled())
	    	 {
	    		 oh.promptToInstall(this);
	    	 }
	    	 else if (!oh.isOrbotRunning())
	    	 {
	    		 oh.requestOrbotStart(this);
	    	 }
	    	 
	     }
    }

    //if the user hasn't registered with the user, show the login screen
    private void checkCreds ()
    {
    	
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
       
        String user = settings.getString("user",null);
        
        if (user == null)
        {
        	Intent intent = new Intent(this,LoginActivity.class);
        	startActivity(intent);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
        {
            toggle();
        }
        else if (item.getItemId() == R.id.menu_settings)
        {
			showPreferences();
		}
		else if (item.getItemId() == R.id.menu_logs)
		{
			collectAndSendLog();
		}
		
		return true;
	}
    
	void collectAndSendLog(){
        final PackageManager packageManager = getPackageManager();
        final Intent intent = new Intent(ACTION_SEND_LOG);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        final boolean isInstalled = list.size() > 0;
        
        if (!isInstalled){
            new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setMessage("Install the free and open source Log Collector application to collect the device log and send it to the developer.")
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:" + LOG_COLLECTOR_PACKAGE_NAME));
                    marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(marketIntent); 
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
        }
        else{
            new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setMessage("Run Log Collector application.\nIt will collect the device log and send it to <support email>.\nYou will have an opportunity to review and modify the data being sent.")
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(EXTRA_SEND_INTENT_ACTION, Intent.ACTION_SEND);
                    final String email = "";
                    intent.putExtra(EXTRA_DATA, Uri.parse("mailto:" + email));
                    intent.putExtra(EXTRA_ADDITIONAL_INFO, "Additonal info: <additional info from the device (firmware revision, etc.)>\n");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Application failure report");
                    
                    intent.putExtra(EXTRA_FORMAT, "time");
                    
                    //The log can be filtered to contain data relevant only to your app
                    /*String[] filterSpecs = new String[3];
                    filterSpecs[0] = "AndroidRuntime:E";
                    filterSpecs[1] = TAG + ":V";
                    filterSpecs[2] = "*:S";
                    intent.putExtra(EXTRA_FILTER_SPECS, filterSpecs);*/
                    
                    startActivity(intent);
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
        }
    }
	private void showPreferences ()
	{
		Intent intent = new Intent(this,SimplePreferences.class);
		this.startActivityForResult(intent, 9999);
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

            Fragment fragment = new HomeSectionFragment();
            
            if (i == 1) {
            	 fragment = new ProjectsSectionFragment();
            }
            
            
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.title_home_activity).toUpperCase();
                case 1: return getString(R.string.title_home_projects).toUpperCase();
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    
    public static class HomeSectionFragment extends Fragment {
       
        public static final String ARG_SECTION_NUMBER = "section_number";

        private View mView;
        private LinearLayout mLayoutView;
        private ProgressDialog mLoading;
        private Context mContext;
        private ArrayList<Lesson> mLessonsCompleted;
        private ArrayList<Project> mListProjects;
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

    		mView = inflater.inflate(R.layout.fragment_home_activity, null);
    		new getAsynctask().execute("");
    		mLayoutView = (LinearLayout)mView.findViewById(R.id.activityll);
        	
    		mContext = mView.getContext();

            return mView;
        }
        
        class getAsynctask extends AsyncTask<String, Long, Integer> {

            protected void onPreExecute() {
                super.onPreExecute();
                mLoading = ProgressDialog.show(mView.getContext(), null, "Please wait...");
            }
            protected Integer doInBackground(String... params) {
                try {
                	
                	mLessonsCompleted = getLessonsCompleted(mContext);
                	mListProjects = Project.getAllAsList(mContext);

                    return null;
                } catch (Exception e) {
                	Log.e(AppConstants.TAG,"error loading home view",e);
                	return null;
                }

            }

            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                try {
                    if (mLoading != null && mLoading.isShowing())
                    	mLoading.dismiss();
                    

                	initActivityList(mView);
                } catch (Throwable t) {
                    Log.v("this is praki", "loading.dismiss() problem", t);
                }
            }
        }
        
        private void initActivityList (View view)
        {
        	LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); // Verbose!
    		//lp.weight = 1.0f; // This is critical. Doesn't work without it.
            lp1.setMargins(0, 20, 0, 5);
            
            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); // Verbose!
    		//lp.weight = 1.0f; // This is critical. Doesn't work without it.
            lp2.setMargins(40, 0, 0, 0);
            
        	Context context = view.getContext();
           
        	Button button;
        	Drawable img;
        	
        	
        	//setup new lessons
        	button = new Button(context);
    		button.setText(R.string.home_start_a_lesson);
    		button.setBackgroundColor(Color.WHITE);
    		img = context.getResources().getDrawable( R.drawable.ic_list_lessons );
    		img.setBounds( 0, 0, 60, 60 );
    		button.setCompoundDrawables( img, null, null, null );
    		button.setGravity(Gravity.LEFT|Gravity.CENTER);
    		button.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {


                    startActivity(new Intent(getActivity(), LessonsActivity.class));


                }
            });
    		mLayoutView.addView(button, lp1);        		
        	
        	
        	
        	for (int i = mLessonsCompleted.size()-1; i > mLessonsCompleted.size()-4 && i > -1; i--)
            {
        		Lesson lesson = mLessonsCompleted.get(i);
        		button = new Button(context);
        		button.setText(lesson.mTitle + " completed!");
        		button.setBackgroundColor(Color.WHITE);
        		button.setGravity(Gravity.LEFT|Gravity.CENTER);
        		button.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {


                        startActivity(new Intent(getActivity(), LessonsActivity.class));


                    }
                });
        		mLayoutView.addView(button, lp2);        		
        		
        	}
        	

        	//setup projects section
        	button = new Button(context);
    		button.setText(R.string.home_new_story);
    		button.setBackgroundColor(Color.WHITE);
    		
    		img = context.getResources().getDrawable( R.drawable.ic_list_projects );
    		img.setBounds( 0, 0, 60, 60 );
    		button.setCompoundDrawables( img, null, null, null );
    		button.setGravity(Gravity.LEFT|Gravity.CENTER);
    		button.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {


                    startActivity(new Intent(getActivity(), StoryNewActivity.class));


                }
            });
    		mLayoutView.addView(button, lp1); 
    		
        	
        	for (int i = mListProjects.size()-1; i > mListProjects.size()-4 && i > -1; i--)
        	{
        		Project project = mListProjects.get(i);
        		button = new Button(context);
        		button.setText("  " + project.getTitle());
        		button.setBackgroundColor(Color.WHITE);
        		button.setGravity(Gravity.LEFT|Gravity.CENTER);
        		button.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                    	
                    	showProject(((Button)v).getText().toString());
                    }
                });
        		
        		// FIXME default to use first scene
        	    Media[] mediaList = project.getScenesAsArray()[0].getMediaAsArray();
                
                if (mediaList != null && mediaList.length > 0)    
                {
                	for (Media media: mediaList)
                		if (media != null)
                		{
                			Bitmap bmp = getThumbnail(media);
                			 
                			if (bmp != null)
                			{
                				img = new BitmapDrawable(getResources(),bmp);
                				img.setBounds( 0, 0, 100,80 );
                				button.setCompoundDrawables( img, null, null, null );
                			
                				break;
                			}
                		}
                }
                
                mLayoutView.addView(button, lp2);        		
        		
        	}
        	
        	
        }
        
        private void showProject (String title)
        {

        	ArrayList<Project> listProjects = Project.getAllAsList(getActivity());
        	Project project = null;
        	
        	for (Project projectMatch: listProjects)
        	{
        		if (projectMatch.getTitle().equals(title))
        		{
        			project = projectMatch;
        			break;
        		}
        	}
        	
        	if (project == null)
        		return;

        	Intent intent = new Intent(getActivity(), SceneEditorActivity.class);
	    	intent.putExtra("story_mode", project.getStoryType());
	    	intent.putExtra("pid", project.getId());
	    	intent.putExtra("title", project.getTitle());
	    	
	    	String templateJsonPath = null;
	    	String lang = StoryMakerApp.getCurrentLocale().getLanguage();
	    	
	    	if (project.getStoryType() == Project.STORY_TYPE_VIDEO)
	    	{
	    		//video
	    		templateJsonPath = "story/templates/" + lang + "/video_simple.json";
	    	}
	    	else if (project.getStoryType() == Project.STORY_TYPE_PHOTO)
	    	{
	
	    		//photo
	    	
	    		templateJsonPath = "story/templates/" + lang + "/photo_simple.json";
	    	}
	    	else if (project.getStoryType() == Project.STORY_TYPE_AUDIO)
	    	{
	
	    		//audio
	    	
	    		templateJsonPath = "story/templates/" + lang + "/audio_simple.json";
	    	}
	    	else if (project.getStoryType() == Project.STORY_TYPE_ESSAY)
	    	{
	    		//essay
	    		templateJsonPath = "story/templates/" + lang + "/essay_simple.json";
	    		
	    	}
	    	
	    	intent.putExtra("template_path", templateJsonPath);
	    	
	        startActivity(intent);
        }
        
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
                    options.inSampleSize = 8;
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
        
        
        private ArrayList<Lesson> getLessonsCompleted (Context context)
        {
        	ArrayList<Lesson> result = new ArrayList<Lesson>();
        	
        	Locale locale = ((StoryMakerApp)getActivity().getApplication()).getCurrentLocale();
        	
            LessonManager lessonManager = StoryMakerApp.getLessonManager();
            
        	//show lesson categories
        	String[] lessonSections = getResources().getStringArray(R.array.lesson_sections);
        	String[] lessonSectionsFolder = getResources().getStringArray(R.array.lesson_sections_folder);

        	int idx = 0;
        	
        	for (String folder : lessonSections)
        	{
        		LessonGroup lg = new LessonGroup();
        		lg.mTitle = folder;
        		
        		String subFolder = lessonSectionsFolder[idx++];
        	
        		ArrayList<Lesson> lessons = LessonManager.loadLessonList(context, lessonManager.getLessonRoot(), subFolder, locale.getLanguage(), Lesson.STATUS_COMPLETE);
        		
        		for (Lesson lesson : lessons)
        			if (lesson.mStatus == Lesson.STATUS_COMPLETE)
        				result.add(lesson);        		
        	}
        	
        	return result;
        	
        }
        
        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            setUserVisibleHint(true);
        }
    }
    
    public static class ProjectsSectionFragment extends Fragment {
       
    	ProjectsListView listView;
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	listView = new ProjectsListView(getActivity());
         
            return listView;
        }

        
        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            setUserVisibleHint(true);
        }



		@Override
		public void onResume() {
			
			super.onResume();
			
		}
        
    }

    //for log sending
    public static final String LOG_COLLECTOR_PACKAGE_NAME = "com.xtralogic.android.logcollector";//$NON-NLS-1$
    public static final String ACTION_SEND_LOG = "com.xtralogic.logcollector.intent.action.SEND_LOG";//$NON-NLS-1$
    public static final String EXTRA_SEND_INTENT_ACTION = "com.xtralogic.logcollector.intent.extra.SEND_INTENT_ACTION";//$NON-NLS-1$
    public static final String EXTRA_DATA = "com.xtralogic.logcollector.intent.extra.DATA";//$NON-NLS-1$
    public static final String EXTRA_ADDITIONAL_INFO = "com.xtralogic.logcollector.intent.extra.ADDITIONAL_INFO";//$NON-NLS-1$
    public static final String EXTRA_SHOW_UI = "com.xtralogic.logcollector.intent.extra.SHOW_UI";//$NON-NLS-1$
    public static final String EXTRA_FILTER_SPECS = "com.xtralogic.logcollector.intent.extra.FILTER_SPECS";//$NON-NLS-1$
    public static final String EXTRA_FORMAT = "com.xtralogic.logcollector.intent.extra.FORMAT";//$NON-NLS-1$
    public static final String EXTRA_BUFFER = "com.xtralogic.logcollector.intent.extra.BUFFER";//$NON-NLS-1$


	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		
		super.onActivityResult(arg0, arg1, arg2);
		

		boolean changed = ((StoryMakerApp)getApplication()).checkLocale();
		if (changed)
		{
			startActivity(new Intent(this,HomeActivity.class));
			
			finish();
			
		}
	}

    
    
}
