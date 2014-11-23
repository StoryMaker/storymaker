package org.storymaker.app;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.storymaker.app.lessons.LessonManager;
import org.storymaker.app.model.Lesson;
import org.storymaker.app.model.LessonGroup;
import org.storymaker.app.model.Media;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.ProjectTable;
import org.storymaker.app.server.LoginActivity;
import org.storymaker.app.ui.MyCard;
import info.guardianproject.onionkit.ui.OrbotHelper;
import scal.io.liger.Constants;
import scal.io.liger.DownloadHelper;
import scal.io.liger.JsonHelper;
import scal.io.liger.MainActivity;
import scal.io.liger.StoryPathLibraryDeserializer;
import scal.io.liger.ZipHelper;
import scal.io.liger.model.StoryPathLibrary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.views.CardUI;
//import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.api.client.json.Json;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.viewpagerindicator.CirclePageIndicator;

public class HomeActivity extends BaseActivity {
    private final static String TAG = "HomeActivity";

    private ProgressDialog mLoading;
    private ArrayList<Lesson> mLessonsCompleted;
    private ArrayList<Project> mListProjects;


	CardUI mCardView;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        try {
            String pkg = getPackageName();
            String vers= getPackageManager().getPackageInfo(pkg, 0).versionName;
            setTitle(getTitle() + " v" + vers);
                    
        } catch (NameNotFoundException e) {
           
        }
        
        setContentView(R.layout.activity_home);
        
        // action bar stuff
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        checkForTor();
        
        // FIXME remove this in play store builds!!
        checkForUpdates();
        
    }
    
    
    
    @Override
	public void onResume() {
		super.onResume();
		
        initActivityList(); // FIXME this needs to wait til download is complete, that should somehow trigger a refresh of this
		
		boolean isExternalStorageReady = Utils.Files.isExternalStorageReady();
		
		if (!isExternalStorageReady)
		{
			//show storage error message
			new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setMessage(R.string.err_storage_not_ready)
            .show();
			
		}
		
		 checkForCrashes();
	}

    public static String parseInstanceDate(String filename) {
//        String jsonFilePath = storyPath.buildTargetPath(storyPath.getId() + "-instance-" + timeStamp.getTime() + ".json");
        String[] splits = FilenameUtils.removeExtension(filename).split("-");
        return splits[splits.length-1]; // FIXME make more robust and move into liger
    }

    // copied this as a short term fix until we get loading cleanly split out from the liger sample app ui stuff
    private StoryPathLibrary initSPLFromJson(String json, String jsonPath) {
        if (json == null || json.equals("")) {
            Toast.makeText(this, "Was not able to load this lesson, content was missing!", Toast.LENGTH_LONG).show();
            finish();
            return null;
        }

        ArrayList<String> referencedFiles = null;

        // should not need to insert dependencies into a saved instance
        if (jsonPath.contains("instance")) {
            referencedFiles = new ArrayList<String>();
        } else {
            referencedFiles = JsonHelper.getInstancePaths();
        }

        StoryPathLibrary storyPathLibrary = JsonHelper.deserializeStoryPathLibrary(json, jsonPath, referencedFiles, this);

        if ((storyPathLibrary != null) && (storyPathLibrary.getCurrentStoryPathFile() != null)) {
            storyPathLibrary.loadStoryPathTemplate("CURRENT");
        }

        return storyPathLibrary;
    }

    private void initActivityList () {
        mCardView = (CardUI) findViewById(R.id.cardsview);

        if (mCardView == null) {
            return;
        }

        if (!DownloadHelper.checkExpansionFiles(this, Constants.MAIN, Constants.MAIN_VERSION)) { // FIXME the app should define these, not the library
            Toast.makeText(this, "Please wait for the content pack to finish downloading and reload the app", Toast.LENGTH_LONG).show(); // FIXME move to strings.xml
            return;
        }

        mCardView.clearCards();
        mCardView.setSwipeable(false);

        ArrayList<ActivityEntry> alActivity = new ArrayList<ActivityEntry>();
        JsonHelper.setupFileStructure(this);
        ArrayList<File> instances = JsonHelper.getLibraryInstanceFiles();
        for (final File f: instances) {
            Log.d(TAG, "loading instance " + f);
            String jsonString = JsonHelper.loadJSON(f, "en"); // FIXME don't hardcode "en"
            MyCard card = null;
                StoryPathLibrary spl = initSPLFromJson(jsonString, f.getAbsolutePath());

                String title = "(no title)";
                if (spl.getCurrentStoryPath() != null) {
                    title = spl.getCurrentStoryPath().getTitle();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                Date date = new Date(Long.parseLong(parseInstanceDate(f.getName())));
                card = new MyCard(title + " " + sdf.format(date), "Last modified" + ": " + sdf.format(new Date(f.lastModified()))); // FIXME move into strings

                Bitmap coverImageThumbnail = spl.getCoverImageThumbnail();
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                if (coverImageThumbnail != null) {
                    card.setImage(new BitmapDrawable(coverImageThumbnail));//, options));
                }

            if (card != null) {
                card.setIcon(R.drawable.ic_home_project);

                card.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchLiger(HomeActivity.this, null, f.getAbsolutePath());
                    }
                });

                ActivityEntry ae = new ActivityEntry(card, new Date(f.lastModified()));
                alActivity.add(ae);
            }
        }

        Collections.sort(alActivity);

        for (ActivityEntry ae : alActivity) {
            mCardView.addCard(ae.card);
        }

        MyCard card = new MyCard("Learning Guide 1", "Build a compelling narrative."); // FIXME move into strings
        card.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLiger(HomeActivity.this, "learning_guide_1_library", null);
            }
        });
        card.setIcon(R.drawable.ic_home_lesson);
        mCardView.addCard(card);

        // draw cards
        mCardView.refresh();
    }

    public static class ActivityEntry implements Comparable<HomeActivity.ActivityEntry> {

    	  public Date dateTime;
    	  public MyCard card;
    	  
    	  public ActivityEntry (MyCard card, Date dateTime)
    	  {
    		  this.card = card;
    		  this.dateTime = dateTime;
    	  }

    	  @Override
    	  public int compareTo(ActivityEntry o) {
    	    return dateTime.compareTo(o.dateTime)*-1;//let's flip the compare output around
    	  }
    	}
    
    private void initIntroActivityList ()
    {
      	setContentView(R.layout.activity_home_intro);
      	initSlidingMenu();
      	
		int[] titles1 =
			{(R.string.tutorial_title_1),
				(R.string.tutorial_title_2),
				(R.string.tutorial_title_3),
				(R.string.tutorial_title_4),
				(R.string.tutorial_title_5)
				};
		int[] messages1 =
			{(R.string.tutorial_text_1),
				(R.string.tutorial_text_2),
				(R.string.tutorial_text_3),
				(R.string.tutorial_text_4),
				(R.string.tutorial_text_5)
				};
		

    	

		MyAdapter adapter = new MyAdapter(getSupportFragmentManager(), titles1,messages1);
		ViewPager pager = ((ViewPager)findViewById(R.id.pager1));
		
		pager.setId((int)(Math.random()*10000));
		pager.setOffscreenPageLimit(5);
			
		pager.setAdapter(adapter);
			 
		//Bind the title indicator to the adapter
         CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.circles1);
         indicator.setViewPager(pager);
         indicator.setSnap(true);
         
         
         final float density = getResources().getDisplayMetrics().density;
         
         indicator.setRadius(5 * density);
         indicator.setFillColor(0xFFFF0000);
         indicator.setPageColor(0xFFaaaaaa);
         //indicator.setStrokeColor(0xFF000000);
         //indicator.setStrokeWidth(2 * density);
		    		
         
         View button = findViewById(R.id.cardButton1);
         button.setOnClickListener(new OnClickListener()
         {

			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(HomeActivity.this, LessonsActivity.class);
				startActivity(intent);
			}
        	 
         });
    	
    		
    		int[] titles2 =
			{(R.string.tutorial_title_7),
				(R.string.tutorial_title_8),
				(R.string.tutorial_title_9),
				(R.string.tutorial_title_10),
				(R.string.tutorial_title_11)
				};
		int[] messages2 =
			{(R.string.tutorial_text_7),
				(R.string.tutorial_text_8),
				(R.string.tutorial_text_9),
				(R.string.tutorial_text_10),
				(R.string.tutorial_text_11)
				};
		
		MyAdapter adapter2 = new MyAdapter(getSupportFragmentManager(), titles2,messages2);
		ViewPager pager2 = ((ViewPager)findViewById(R.id.pager2));
		
		pager2.setId((int)(Math.random()*10000));
		pager2.setOffscreenPageLimit(5);
			
		pager2.setAdapter(adapter2);
			 
		//Bind the title indicator to the adapter
         CirclePageIndicator indicator2 = (CirclePageIndicator)findViewById(R.id.circles2);
         indicator2.setViewPager(pager2);
         indicator2.setSnap(true);
      
         indicator2.setRadius(5 * density);
         indicator2.setFillColor(0xFFFF0000);
         indicator2.setPageColor(0xFFaaaaaa);
         //indicator.setStrokeColor(0xFF000000);
         //indicator.setStrokeWidth(2 * density);
	
         button = findViewById(R.id.cardButton2);
         button.setOnClickListener(new OnClickListener()
         {

			@Override
			public void onClick(View v) {
				//Intent intent = new Intent(HomeActivity.this, StoryNewActivity.class);
				//startActivity(intent);
                launchLiger(HomeActivity.this, "learning_guide_1_library", null);
			}
        	 
         });
    }

    private void showProject(int id) {
        if (id >= mListProjects.size()) {
            return; // sometimes we get a long random number here - n8fr8
        }
        
        Project project = mListProjects.get(id);
        Intent intent = new Intent(this, SceneEditorActivity.class);
        if (project.getScenesAsArray().length > 1) {
            intent = new Intent(this, StoryTemplateActivity.class);
        }

        intent.putExtra("story_mode", project.getStoryType());
        intent.putExtra("pid", project.getId());
        intent.putExtra("title", project.getTitle());
        String templateJsonPath = Project.getSimpleTemplateForMode(getApplicationContext(), project.getStoryType()); // FIXME opt: this is redundant as the  SceneEditorActivity does this again itself
        intent.putExtra("template_path", templateJsonPath);

        startActivity(intent);
    }
    
    private ArrayList<Lesson> getLessonsCompleted (Context context)
    {
    	ArrayList<Lesson> result = new ArrayList<Lesson>();
    	
    	Locale locale = StoryMakerApp.getCurrentLocale();
    	
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
    			//else if (lesson.mStatus == Lesson.STATUS_IN_PROGRESS)
    			//	result.add(lesson);        		
			
    	}
    	
    	return result;
    	
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


	private void checkForTor ()
    {
    	 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

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
    	
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
       
        String user = settings.getString("user",null);
        
        if (user == null)
        {
        	Intent intent = new Intent(this,LoginActivity.class);
        	startActivity(intent);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
        {
            mSlidingMenu.toggle();
            return true;
        }
        else if (item.getItemId() == R.id.menu_logs)
        {
            collectAndSendLog();
            return true;
        }
        else if (item.getItemId() == R.id.menu_new_project)
        {
            launchLiger(this, "default_library", null);
            return true;
        }
        else if (item.getItemId() == R.id.menu_bug_report)
        {
            String url = "https://docs.google.com/forms/d/1KrsTg-NNr8gtQWTCjo-7Fv2L5cml84EcmIuGGNiC4fY/viewform";

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        }
        else if (item.getItemId() == R.id.menu_about)
        {
            String url = "https://storymaker.cc";

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void launchLiger(Context context, String splId, String instancePath) {
        if (!DownloadHelper.checkExpansionFiles(context, Constants.MAIN, Constants.MAIN_VERSION)) { // FIXME the app should define these, not the library
            Toast.makeText(context, "Please wait for the content pack to finish downloading", Toast.LENGTH_LONG).show(); // FIXME move to strings.xml
            return;
        }
        //        startActivity(new Intent(this, StoryNewActivity.class));
        Intent ligerIntent = new Intent(context, MainActivity.class);
        ligerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        ligerIntent.putExtra(MainActivity.INTENT_KEY_WINDOW_TITLE, context.getString(R.string.app_name));
        String lang = StoryMakerApp.getCurrentLocale().getLanguage();
        ligerIntent.putExtra("lang", lang);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        int pslideduration = Integer.parseInt(settings.getString("pslideduration", "5"));
        ligerIntent.putExtra("photo_essay_slide_duration", pslideduration * 1000);
        if (splId != null && !splId.isEmpty()) {
            ligerIntent.putExtra(MainActivity.INTENT_KEY_STORYPATH_LIBRARY_ID, splId);
        } else if (instancePath != null && !instancePath.isEmpty()) {
            ligerIntent.putExtra(MainActivity.INTENT_KEY_STORYPATH_INSTANCE_PATH, instancePath);
        }
        context.startActivity(ligerIntent);
    }
    
	void collectAndSendLog(){
		
		File fileLog = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"storymakerlog.txt");
		fileLog.getParentFile().mkdirs();
		
		try
		{
			writeLogToDisk("StoryMaker",fileLog);
			writeLogToDisk("FFMPEG",fileLog);
			writeLogToDisk("SOX",fileLog);
			
			Intent i = new Intent(Intent.ACTION_SEND);
			i.putExtra(Intent.EXTRA_EMAIL, "help@guardianproject.info");
			i.putExtra(Intent.EXTRA_SUBJECT, "StoryMaker Log");
			i.putExtra(Intent.EXTRA_TEXT, "StoryMaker log email: " + new Date().toGMTString());
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileLog));
			i.setType("text/plain");
			startActivity(Intent.createChooser(i, "Send mail"));
		}
		catch (IOException e)
		{
			
		}
    }
	
	private void showPreferences ()
	{
		Intent intent = new Intent(this,SimplePreferences.class);
		this.startActivityForResult(intent, 9999);
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
			finish();
			startActivity(new Intent(this,HomeActivity.class));
			
		}
	}
	
	public class MyAdapter extends FragmentPagerAdapter {
		 
		 int[] mMessages;
		 int[] mTitles;
		 
	        public MyAdapter(FragmentManager fm, int[] titles, int[] messages) {
	            super(fm);
	            mTitles = titles;
	            mMessages = messages;
	        }

	        @Override
	        public int getCount() {
	            return mMessages.length;
	        }

	        @Override
	        public Fragment getItem(int position) {
	        	Bundle bundle = new Bundle();
	        	bundle.putString("title",getString(mTitles[position]));
	        	bundle.putString("msg", getString(mMessages[position]));
	        	
	        	Fragment f = new MyFragment();
	        	f.setArguments(bundle);
	        	
	            return f;
	        }
	    }
	
	public static final class MyFragment extends Fragment {
	
		String mMessage;
		String mTitle;
		
		 /**
       * When creating, retrieve this instance's number from its arguments.
       */
      @Override
      public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);

          mTitle = getArguments().getString("title");
          mMessage = getArguments().getString("msg");
      }

      /**
       * The Fragment's UI is just a simple text view showing its
       * instance number.
       */
      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container,
              Bundle savedInstanceState) {
          
          ViewGroup root = (ViewGroup) inflater.inflate(R.layout.card_pager_textview, null);
          
          ((TextView)root.findViewById(R.id.title)).setText(mTitle);
          
          ((TextView)root.findViewById(R.id.description)).setText(mMessage);
          
          return root;
      }
	
	}
	
	private void checkForCrashes() {
	   CrashManager.register(this, AppConstants.HOCKEY_APP_ID);
	 }

	 private void checkForUpdates() {
	   // Remove this for store builds!
	   UpdateManager.register(this, AppConstants.HOCKEY_APP_ID);
	 }

    
	 private void writeLogToDisk (String tag, File fileLog) throws IOException
	 {
		 
		FileWriter fos = new FileWriter(fileLog,true);
		BufferedWriter writer = new BufferedWriter(fos);

		      Process process = Runtime.getRuntime().exec("logcat -d " + tag + ":D *:S");
		      BufferedReader bufferedReader = 
		        new BufferedReader(new InputStreamReader(process.getInputStream()));

		     
		      String line;
		      while ((line = bufferedReader.readLine()) != null) {
		    	  
		    	  writer.write(line);
		    	  writer.write('\n');
		      }
		      bufferedReader.close();

		      writer.close();
	 }
    
}
