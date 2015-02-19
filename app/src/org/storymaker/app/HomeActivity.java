package org.storymaker.app;

import org.apache.commons.io.FilenameUtils;
import org.storymaker.app.model.Lesson;
import org.storymaker.app.model.LessonGroup;
import org.storymaker.app.model.Project;
import org.storymaker.app.server.LoginActivity;
import org.storymaker.app.ui.MyCard;
import info.guardianproject.onionkit.ui.OrbotHelper;
import scal.io.liger.Constants;
import scal.io.liger.DownloadHelper;
import scal.io.liger.IndexManager;
import scal.io.liger.JsonHelper;
import scal.io.liger.MainActivity;
import scal.io.liger.model.BaseIndexItem;
import scal.io.liger.model.ContentPackMetadata;
import scal.io.liger.model.ExpansionIndexItem;
import scal.io.liger.model.InstanceIndexItem;
import scal.io.liger.model.StoryPath;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.viewpagerindicator.CirclePageIndicator;

public class HomeActivity extends BaseActivity {
    private final static String TAG = "HomeActivity";

    private ProgressDialog mLoading;
    private ArrayList<Project> mListProjects;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        // copy index files
        IndexManager.copyAvailableIndex(this);
        IndexManager.copyInstalledIndex(this);

        try {
            String pkg = getPackageName();
            String vers= getPackageManager().getPackageInfo(pkg, 0).versionName;
            setTitle(getTitle() + " v" + vers);
                    
        } catch (NameNotFoundException ignored) {}
        
        setContentView(R.layout.activity_home);
        mRecyclerView = (RecyclerView) findViewById(scal.io.liger.R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // action bar stuff
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        checkForTor();
        
        checkForUpdates();
        
    }
    
    @Override
	public void onResume() {
		super.onResume();

        if (!DownloadHelper.checkExpansionFiles(this, Constants.MAIN, Constants.MAIN_VERSION)) {
            DownloadPoller poller = new DownloadPoller();
            poller.execute("foo");
        } else {
            initActivityList();
        }
		
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
            Toast.makeText(this, "Was not able to load this path, content is missing!", Toast.LENGTH_LONG).show();
            finish();
            return null;
        }

        ArrayList<String> referencedFiles = null;

        // should not need to insert dependencies into a saved instance
        if (jsonPath.contains("instance")) {
            referencedFiles = new ArrayList<String>();
        } else {
            referencedFiles = JsonHelper.getInstancePaths(this);
        }

        StoryPathLibrary storyPathLibrary = JsonHelper.deserializeStoryPathLibrary(json, jsonPath, referencedFiles, this, StoryMakerApp.getCurrentLocale().getLanguage());

        if ((storyPathLibrary != null) && (storyPathLibrary.getCurrentStoryPathFile() != null)) {
            storyPathLibrary.loadStoryPathTemplate("CURRENT", false);
        }

        return storyPathLibrary;
    }

    private void initActivityList () {
        if (!DownloadHelper.checkExpansionFiles(this, Constants.MAIN, Constants.MAIN_VERSION)) { // FIXME the app should define these, not the library
            Toast.makeText(this, "Please wait for the content pack to finish downloading and reload the app", Toast.LENGTH_LONG).show(); // FIXME move to strings.xml
            return;
        }

        JsonHelper.setupFileStructure(this);

        // NEW: load instance index
        HashMap<String, InstanceIndexItem> instanceIndex = IndexManager.fillInstanceIndex(HomeActivity.this, IndexManager.loadInstanceIndex(HomeActivity.this),StoryMakerApp.getCurrentLocale().getLanguage());

        // TEMP
        if (instanceIndex.size() > 0) {
            Log.d(TAG, "INITACTIVITYLIST - FOUND INSTANCE INDEX WITH " + instanceIndex.size() + " ITEMS");
        } else {
            Log.d(TAG, "INITACTIVITYLIST - FOUND INSTANCE INDEX WITH NO ITEMS");
        }

        ArrayList<BaseIndexItem> instances = new ArrayList<BaseIndexItem>(instanceIndex.values());

        HashMap<String, ExpansionIndexItem> availableIds = IndexManager.loadAvailableIdIndex(this);
        HashMap<String, ExpansionIndexItem> installedIds = IndexManager.loadInstalledIdIndex(this);

        for (String id : availableIds.keySet()) {
            if (installedIds.keySet().contains(id)) {
                // if the available item has been installed, add the corresponding item from the installed index
                instances.add(installedIds.get(id));
            } else {
                // if the available item has not been installed, add the item from the available index
                instances.add(availableIds.get(id));
            }
        }

        Collections.sort(instances, Collections.reverseOrder());

        mRecyclerView.setAdapter(new InstanceIndexItemAdapter(instances, new InstanceIndexItemAdapter.BaseIndexItemSelectedListener() {
            @Override
            public void onStorySelected(BaseIndexItem selectedItem) {
//                if (!TextUtils.isEmpty(selectedItem.getStoryType()) &&
//                    selectedItem.getStoryType().equals("learningGuide")) {
                if (selectedItem instanceof InstanceIndexItem) {
                    launchLiger(HomeActivity.this, null, ((InstanceIndexItem) selectedItem).getInstanceFilePath(), null);
                } else {
                    ExpansionIndexItem eItem = ((ExpansionIndexItem)selectedItem);

                    HashMap<String, ExpansionIndexItem> installedIds = IndexManager.loadInstalledIdIndex(HomeActivity.this);

                    if (installedIds.containsKey(eItem.getExpansionId())) {

                        // fall through if file has not yet been downloaded
                        File checkFile = new File(Environment.getExternalStorageDirectory() + File.separator + eItem.getExpansionFilePath() + eItem.getExpansionFileName());
                        if (!checkFile.exists()) {
                            Log.d("CHECKING FILE", "FILE " + checkFile.getPath() + " WAS NOT FOUND (NO-OP)");
                        } else {
                            Log.d("CHECKING FILE", "FILE " + checkFile.getPath() + " WAS FOUND");

                            // update with new thumbnail path
                            // move this somewhere that it can be triggered by completed download?
                            ContentPackMetadata metadata = IndexManager.loadContentMetadata(HomeActivity.this, eItem.getPackageName(), eItem.getExpansionId());
                            eItem.setThumbnailPath(metadata.getContentPackThumbnailPath());
                            IndexManager.registerInstalledIndexItem(HomeActivity.this, eItem);
                            try {
                                synchronized (this) {
                                    wait(1000);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            HashMap<String, InstanceIndexItem> contentIndex = IndexManager.loadContentIndex(HomeActivity.this, eItem.getPackageName(), eItem.getExpansionId());
                            String[] names = new String[contentIndex.size()];
                            String[] paths = new String[contentIndex.size()];
                            Iterator it = contentIndex.entrySet().iterator();
                            int i = 0;
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry) it.next();
                                InstanceIndexItem item = (InstanceIndexItem) pair.getValue();
                                names[i] = item.getTitle();
                                paths[i] = item.getInstanceFilePath();
                                i++;
                            }
                            showSPLSelectorPopup(names, paths);
                            // TODO prompt user with all the SPLs within this content pack, then open by passing the path from the content index as the 3rd param to launchLiger
                            //                    launchLiger(HomeActivity.this, null, null, .getExpansionFilePath());
                            // TODO check if this is installed already, if not trigger a download. if it is, launch the spl selection ui
                        }
                    } else {
                        IndexManager.registerInstalledIndexItem(HomeActivity.this, eItem);
                        try {
                            synchronized (this) {
                                wait(1000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        DownloadHelper.checkAndDownload(HomeActivity.this);
                    }
                }
            }
        }));
    }

    private void showSPLSelectorPopup(final String[] names, final String[] paths) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Choose Story File(SdCard/Liger/)").setItems(names, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                launchLiger(HomeActivity.this, null, null, paths[index]);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

//    // TODO repurpose this to act as the download a content ui
//    private void initIntroActivityList()
//    {
//      	setContentView(R.layout.activity_home_intro);
//      	setupDrawerLayout();
//
//		int[] titles1 =
//			{(R.string.tutorial_title_1),
//				(R.string.tutorial_title_2),
//				(R.string.tutorial_title_3),
//				(R.string.tutorial_title_4),
//				(R.string.tutorial_title_5)
//				};
//		int[] messages1 =
//			{(R.string.tutorial_text_1),
//				(R.string.tutorial_text_2),
//				(R.string.tutorial_text_3),
//				(R.string.tutorial_text_4),
//				(R.string.tutorial_text_5)
//				};
//
//
//
//
//		MyAdapter adapter = new MyAdapter(getSupportFragmentManager(), titles1,messages1);
//		ViewPager pager = ((ViewPager)findViewById(R.id.pager1));
//
//		pager.setId((int)(Math.random()*10000));
//		pager.setOffscreenPageLimit(5);
//
//		pager.setAdapter(adapter);
//
//		//Bind the title indicator to the adapter
//         CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.circles1);
//         indicator.setViewPager(pager);
//         indicator.setSnap(true);
//
//
//         final float density = getResources().getDisplayMetrics().density;
//
//         indicator.setRadius(5 * density);
//         indicator.setFillColor(0xFFFF0000);
//         indicator.setPageColor(0xFFaaaaaa);
//         //indicator.setStrokeColor(0xFF000000);
//         //indicator.setStrokeWidth(2 * density);
//
//
//         View button = findViewById(R.id.cardButton1);
//         button.setOnClickListener(new OnClickListener()
//         {
//
//			@Override
//			public void onClick(View v) {
//
//				Intent intent = new Intent(HomeActivity.this, LessonsActivity.class);
//				startActivity(intent);
//			}
//
//         });
//
//
//    		int[] titles2 =
//			{(R.string.tutorial_title_7),
//				(R.string.tutorial_title_8),
//				(R.string.tutorial_title_9),
//				(R.string.tutorial_title_10),
//				(R.string.tutorial_title_11)
//				};
//		int[] messages2 =
//			{(R.string.tutorial_text_7),
//				(R.string.tutorial_text_8),
//				(R.string.tutorial_text_9),
//				(R.string.tutorial_text_10),
//				(R.string.tutorial_text_11)
//				};
//
//		MyAdapter adapter2 = new MyAdapter(getSupportFragmentManager(), titles2,messages2);
//		ViewPager pager2 = ((ViewPager)findViewById(R.id.pager2));
//
//		pager2.setId((int)(Math.random()*10000));
//		pager2.setOffscreenPageLimit(5);
//
//		pager2.setAdapter(adapter2);
//
//		//Bind the title indicator to the adapter
//         CirclePageIndicator indicator2 = (CirclePageIndicator)findViewById(R.id.circles2);
//         indicator2.setViewPager(pager2);
//         indicator2.setSnap(true);
//
//         indicator2.setRadius(5 * density);
//         indicator2.setFillColor(0xFFFF0000);
//         indicator2.setPageColor(0xFFaaaaaa);
//         //indicator.setStrokeColor(0xFF000000);
//         //indicator.setStrokeWidth(2 * density);
//
//         button = findViewById(R.id.cardButton2);
//         button.setOnClickListener(new OnClickListener()
//         {
//
//			@Override
//			public void onClick(View v) {
//				//Intent intent = new Intent(HomeActivity.this, StoryNewActivity.class);
//				//startActivity(intent);
//                launchLiger(HomeActivity.this, "learning_guide_1_library", null, null);
//			}
//
//         });
//    }

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
       
        String user = settings.getString("user", null);
        
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
            toggleDrawer();
            return true;
        }
        else if (item.getItemId() == R.id.menu_new_project)
        {
            launchLiger(this, "default_library", null, null);
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

    public static void launchLiger(Context context, String splId, String instancePath, String splPath) {
        if (!DownloadHelper.checkExpansionFiles(context, Constants.MAIN, Constants.MAIN_VERSION)) { // FIXME the app should define these, not the library
            Toast.makeText(context, "Please wait for the content pack to finish downloading", Toast.LENGTH_LONG).show(); // FIXME move to strings.xml
            return;
        }
        //        startActivity(new Intent(this, StoryNewActivity.class));
        Intent ligerIntent = new Intent(context, MainActivity.class);
        ligerIntent.putExtra(MainActivity.INTENT_KEY_WINDOW_TITLE, context.getString(R.string.app_name));
        String lang = StoryMakerApp.getCurrentLocale().getLanguage();
        ligerIntent.putExtra("lang", lang);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        int pslideduration = Integer.parseInt(settings.getString("pslideduration", "5"));
        ligerIntent.putExtra("photo_essay_slide_duration", pslideduration * 1000);
        if (splId != null && !splId.isEmpty()) {
            ligerIntent.putExtra(MainActivity.INTENT_KEY_STORYPATH_LIBRARY_ID, splId);
        } else if (splPath != null && !splPath.isEmpty()) {
            ligerIntent.putExtra(MainActivity.INTENT_KEY_STORYPATH_LIBRARY_PATH, splPath);
        } else if (instancePath != null && !instancePath.isEmpty()) {
            ligerIntent.putExtra(MainActivity.INTENT_KEY_STORYPATH_INSTANCE_PATH, instancePath);
        }
        context.startActivity(ligerIntent);
    }

	private void showPreferences ()
	{
		Intent intent = new Intent(this,SimplePreferences.class);
		this.startActivityForResult(intent, 9999);
	}

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
        if (BuildConfig.DEBUG) {
            UpdateManager.register(this, AppConstants.HOCKEY_APP_ID);
        }
    }

    public void downloadComplete() {
        initActivityList();
        // http://stackoverflow.com/questions/2745061/java-lang-illegalargumentexception-view-not-attached-to-window-manager
        try {
            if ((this.mLoading != null) && this.mLoading.isShowing()) {
                this.mLoading.dismiss();
            }
        } catch (final IllegalArgumentException e) {
            // Handle or log or ignore
        } catch (final Exception e) {
            // Handle or log or ignore
        } finally {
            this.mLoading = null;
        }
    }

    // FIXME once we have a patch as well as a main file this gets a little more complex
    class DownloadPoller extends AsyncTask<String, Long, Integer> {

        protected void onPreExecute() {
            super.onPreExecute();

            if (mLoading == null || (!mLoading.isShowing()))
                mLoading = ProgressDialog.show(HomeActivity.this, null, "Downloading content...", true, true);
        }

        protected Integer doInBackground(String... params) {
            while (!DownloadHelper.checkExpansionFiles(HomeActivity.this, Constants.MAIN, Constants.MAIN_VERSION)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            HomeActivity.this.downloadComplete();
        }
    }
}
