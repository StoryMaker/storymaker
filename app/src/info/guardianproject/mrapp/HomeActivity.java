package info.guardianproject.mrapp;

import info.guardianproject.mrapp.StoryTemplateChooserActivity.MyAdapter;
import info.guardianproject.mrapp.StoryTemplateChooserActivity.MyFragment;
import info.guardianproject.mrapp.lessons.LessonManager;
import info.guardianproject.mrapp.model.Lesson;
import info.guardianproject.mrapp.model.LessonGroup;
import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.server.LoginActivity;
import info.guardianproject.mrapp.ui.MyCard;
import info.guardianproject.mrapp.ui.MyCardPager;
import info.guardianproject.onionkit.ui.OrbotHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.fima.cardsui.views.CardUI;
import com.slidingmenu.lib.SlidingMenu;
import com.viewpagerindicator.CirclePageIndicator;

public class HomeActivity extends BaseActivity {

    
    private ProgressDialog mLoading;
    private ArrayList<Lesson> mLessonsCompleted;
    private ArrayList<Project> mListProjects;


	CardUI mCardView;
    

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
        
        // action bar stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        Eula.show(this);
        
    }
    
    
    
    @Override
	public void onResume() {
		super.onResume();
		
		new getAsynctask().execute("");
		

	}



	class getAsynctask extends AsyncTask<String, Long, Integer> {

        protected void onPreExecute() {
            super.onPreExecute();
            mLoading = ProgressDialog.show(HomeActivity.this, null, "Please wait...");
        }
        protected Integer doInBackground(String... params) {
            try {
            	
                
            	mLessonsCompleted = getLessonsCompleted(HomeActivity.this);
            	mListProjects = Project.getAllAsList(HomeActivity.this);

            	
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

                if (mLessonsCompleted.size() == 0 && mListProjects.size() == 0)
                	initIntroActivityList();
                else
                	initActivityList();
                
            } catch (Throwable t) {
                Log.v(AppConstants.TAG, "loading.dismiss() problem", t);
            }
        }
    }
	
    private void initActivityList ()
    {

	
    	mCardView = (CardUI) findViewById(R.id.cardsview);
    	
    	if (mCardView == null)
    		return;
    	
    	mCardView.clearCards();
		
    	mCardView.setSwipeable(false);
    	
    	ArrayList<ActivityEntry> alActivity = new ArrayList<ActivityEntry>();
    	
    	for (int i = mLessonsCompleted.size()-1; i > mLessonsCompleted.size()-4 && i > -1; i--)
        {

    		Lesson lesson = mLessonsCompleted.get(i);
    		
    		MyCard card = new MyCard(getString(R.string.lessons_congratulations_you_have_completed_the_lesson_),lesson.mTitle);
    		card.setIcon(R.drawable.ic_home_lesson);
    		
    		card.setOnClickListener(new OnClickListener() {

    			@Override
    			public void onClick(View v) {
                    startActivity(new Intent(HomeActivity.this, LessonsActivity.class));


    			}
    		});
    		
    		Date cardDate = new Date();
    		if (lesson.mStatusModified != null)
    			cardDate = lesson.mStatusModified;
    		
    		ActivityEntry ae = new ActivityEntry(card,cardDate);
    		alActivity.add(ae);
    		
    		
    	}
    	
    		
    	for (int i = mListProjects.size()-1; i > mListProjects.size()-4 && i > -1; i--)
    	{
    		Project project = mListProjects.get(i);
    		
    		// FIXME default to use first scene
    	    Media[] mediaList = project.getScenesAsArray()[0].getMediaAsArray();
            
    	    Drawable img = null;
    	    
            if (mediaList != null && mediaList.length > 0)    
            {
            	for (Media media: mediaList)
            		if (media != null)
            		{
            			Bitmap bmp = getThumbnail(media);
            			 
            			if (bmp != null)
            			{
            				img = new BitmapDrawable(getResources(),bmp);
            			
            				break;
            			}
            		}
            }
            

			if (img != null)
			{
        		
        		MyCard card = new MyCard(getString(R.string.title_activity_new_story),project.getTitle());
        		card.setImage(img);
        		card.setId(i);
        		card.setOnClickListener(new OnClickListener() {

        			@Override
        			public void onClick(View v) {
        				
	                    showProject(v.getId());

        			}
        		});
        		

        		Date cardDate = new Date(new File(mediaList[0].getPath()).lastModified());
        		ActivityEntry ae = new ActivityEntry(card,cardDate);
        		alActivity.add(ae);
        		
			}
			else
			{
        		
        		MyCard card = new MyCard(getString(R.string.title_activity_new_story),project.getTitle());
        		card.setId(i);
        		
        		card.setOnClickListener(new OnClickListener() {

        			@Override
        			public void onClick(View v) {
	                    showProject(v.getId());

        			}
        		});
        		mCardView.addCard(card);
        		
        		Date cardDate = new Date();
        		
        		if (mediaList.length > 0)
        			cardDate = new Date(new File(mediaList[0].getPath()).lastModified());
        			
        		ActivityEntry ae = new ActivityEntry(card,cardDate);
        		alActivity.add(ae);
        		
        		
			}
                  
    		
    	}
    	
    	Collections.sort(alActivity);

    	for (ActivityEntry ae : alActivity)
    		mCardView.addCard(ae.card);
    	
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
				Intent intent = new Intent(HomeActivity.this, StoryNewActivity.class);
				startActivity(intent);
				
			}
        	 
         });
    }
    
    
    private void showProject (int id)
    {

		Project project = mListProjects.get(id);
		
		Intent intent = new Intent(this, SceneEditorActivity.class);
    	
		if (project.getScenesAsArray().length > 1) {
			
		    intent = new Intent(this, StoryTemplateActivity.class);
		    
		    
	    }

    	intent.putExtra("story_mode", project.getStoryType());
    	intent.putExtra("pid", project.getId());
    	intent.putExtra("title", project.getTitle());
    	
    	String lang = StoryMakerApp.getCurrentLocale().getLanguage();
    	String templateJsonPath = "story/templates/" + lang + "/simple/";
    	
    	if (project.getStoryType() == Project.STORY_TYPE_VIDEO)
    	{
    		//video
    		templateJsonPath += "video_simple.json";
    	}
    	else if (project.getStoryType() == Project.STORY_TYPE_PHOTO)
    	{

    		//photo
    	
    		templateJsonPath += "photo_simple.json";
    	}
    	else if (project.getStoryType() == Project.STORY_TYPE_AUDIO)
    	{

    		//audio
    	
    		templateJsonPath += "audio_simple.json";
    	}
    	else if (project.getStoryType() == Project.STORY_TYPE_ESSAY)
    	{
    		//essay
    		templateJsonPath += "essay_simple.json";
    		
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
    
    
    private ArrayList<Lesson> getLessonsCompleted (Context context)
    {
    	ArrayList<Lesson> result = new ArrayList<Lesson>();
    	
    	Locale locale = ((StoryMakerApp)getApplication()).getCurrentLocale();
    	
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
        	mSlidingMenu.toggle();
        }
        else if (item.getItemId() == R.id.menu_settings)
        {
			showPreferences();
		}
		else if (item.getItemId() == R.id.menu_logs)
		{
			collectAndSendLog();
		}
		else if (item.getItemId() == R.id.menu_new_project)
		{
			 startActivity(new Intent(this, StoryNewActivity.class));
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
	
	public class MyFragment extends Fragment {
	
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

    
    
}
