package info.guardianproject.mrapp;

import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.api.SyncService;
import info.guardianproject.mrapp.encryption.EncryptionService;
import info.guardianproject.mrapp.encryption.EncryptionBackground;
import info.guardianproject.mrapp.export.Export2SDService;
import info.guardianproject.mrapp.facebook.FacebookLogin;
import info.guardianproject.mrapp.model.Lesson;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.server.LoginActivity;
import info.guardianproject.onionkit.ui.OrbotHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import net.sqlcipher.database.SQLiteDatabase;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.widget.Toast;

import com.actionbarsherlock.view.MenuItem;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HomePanelsActivity extends BaseActivity implements OnClickListener{

    
    private ProgressDialog mLoading;
    private ArrayList<Lesson> mLessonsCompleted;
    private ArrayList<Project> mListProjects;

    RelativeLayout load_new_report;
    RelativeLayout load_lessons;
    RelativeLayout load_reports;
    RelativeLayout load_sync;

    private Dialog dialog;
    
    //Connection detector class
    ConnectionDetector cd;
    //flag for Internet connection status
    Boolean isInternetPresent = false;
    
    @Override
    
    public void onCreate(Bundle savedInstanceState) {
    
    	super.onCreate(savedInstanceState);
    	
        cd = new ConnectionDetector(getApplicationContext());

    	//Get constants
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	
    	SQLiteDatabase.loadLibs(this);
        try {
            String pkg = getPackageName();
            String vers= getPackageManager().getPackageInfo(pkg, 0).versionName;
            setTitle(getTitle() + " v" + vers);
                    
        } catch (NameNotFoundException e) {
           
        }
        checkCreds();
    	//new getSectors().execute();
       // new getCategories().execute();
        setContentView(R.layout.activity_home_panels);
        
        // action bar stuff
       
         
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(false);
       
        initSlidingMenu();
        
        checkForTor();
        //check if relevant folders exist
        
        
        //start encryption
        if(!isServiceRunning(EncryptionBackground.class)){
        	startService(new Intent(HomePanelsActivity.this, EncryptionBackground.class));
        }
        
        //checkForUpdates();
       	
        load_new_report = (RelativeLayout)findViewById(R.id.load_new_report);
        load_new_report.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(),ReportActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				
			}
		});
        load_reports = (RelativeLayout)findViewById(R.id.load_reports);
        load_reports.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplicationContext(),ReportsActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});
        
        load_lessons = (RelativeLayout)findViewById(R.id.load_lessons);
        load_lessons.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(isServiceRunning(VideoTutorialsService.class)){
					Toast.makeText(getApplicationContext(), "Currently downloading lessons...", Toast.LENGTH_LONG).show();
				}else{
					File mFileExternDir = new File(Environment.getExternalStorageDirectory(), "TIMBY_Tutorials");
				    if (!mFileExternDir.exists()) {
				            Toast.makeText(getApplicationContext(), "No lessons found! Downloading...", Toast.LENGTH_LONG).show();
				            startService(new Intent(HomePanelsActivity.this,VideoTutorialsService.class));
				    }else if(mFileExternDir.listFiles().length<4){
				    		DeleteRecursive(mFileExternDir);
				    		Toast.makeText(getApplicationContext(), "No lessons found! Downloading...", Toast.LENGTH_LONG).show();
				    		startService(new Intent(HomePanelsActivity.this,VideoTutorialsService.class));
				    }else{
						Intent i = new Intent(getApplicationContext(),LessonsActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
				    }
				}
			}
		});
        
        load_sync = (RelativeLayout)findViewById(R.id.load_sync);
        load_sync.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
	            dialog = new Dialog(HomePanelsActivity.this);
	            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_sync);
                dialog.findViewById(R.id.button_sync).setOnClickListener(
                        HomePanelsActivity.this);
                dialog.findViewById(R.id.button_export).setOnClickListener(
                        HomePanelsActivity.this);
                dialog.findViewById(R.id.checkBox1).setOnClickListener(
                        HomePanelsActivity.this);
                dialog.show();
                
			}
		});
    }
    void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }

    private boolean isServiceRunning(Class<?> cls) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (cls.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isEncryptionRunning() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			       
        String encryption_running = settings.getString("encryption_running",null);
        
	        if (encryption_running == null){
	        	return false;
	        }else if(encryption_running.equals("end")){
	        	return false;
	        }else{
	        	return true;
	        }
	      
	    }
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button_sync:            
        	//check if service is already running
        	//check if encryption is running
        	//check if export is running
        	if(isServiceRunning(SyncService.class)){
  	          	Toast.makeText(getBaseContext(), "Syncing is already started!", Toast.LENGTH_LONG).show();
        	}else if (isServiceRunning(EncryptionService.class)){
  	          	Toast.makeText(getBaseContext(), "Please wait for encryption to finish!", Toast.LENGTH_LONG).show();
        	}else if(isServiceRunning(Export2SDService.class)){
  	          	Toast.makeText(getBaseContext(), "Please wait for exporting to finish!", Toast.LENGTH_LONG).show();
        	}else{
	        	isInternetPresent = cd.isConnectingToInternet();
	  	       	if(!isInternetPresent){
	  	          	Toast.makeText(getBaseContext(), "You have no connection!", Toast.LENGTH_LONG).show();
	  	        }else{
	  	        	dialog.dismiss();
	  	        	startService(new Intent(HomePanelsActivity.this,SyncService.class));
	  	        }   
        	}
        	//Intent i = new Intent(getApplicationContext(),SyncActivity.class);
			//i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//startActivity(i);
			
            break;
            
        case R.id.button_export:
        	dialog.dismiss();
        	
        	CheckBox cB = (CheckBox)dialog.findViewById(R.id.checkBox1);
        	
        	String includeExported;
        	if(cB.isChecked()){
        		includeExported = "1";
        	}else{
        		includeExported = "0";
        	}
        	
        	if(isServiceRunning(Export2SDService.class)){
  	          	Toast.makeText(getBaseContext(), "Export to SD is already started!", Toast.LENGTH_LONG).show();
        	}else if (isServiceRunning(EncryptionService.class)){
  	          	Toast.makeText(getBaseContext(), "Please wait for encryption to finish!", Toast.LENGTH_LONG).show();
        	}else if(isServiceRunning(SyncService.class)){
  	          	Toast.makeText(getBaseContext(), "Please wait for sync to finish!", Toast.LENGTH_LONG).show();
        	}else{
        		Intent eS = new Intent(HomePanelsActivity.this,Export2SDService.class);
        		eS.putExtra("includeExported", includeExported);
	  	        startService(eS); 
        	}
        	/*
        	Intent i2 = new Intent(getApplicationContext(), Export2SD.class);
        	i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i2);
            break;
            */
        }
    }

    @Override
	public void onResume() {
		super.onResume();
		
		//new getAsynctask().execute("");
		
		boolean isExternalStorageReady = ((StoryMakerApp)getApplication()).isExternalStorageReady();
		
		if (!isExternalStorageReady)
		{
			//show storage error message
			new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setMessage(R.string.err_storage_not_ready)
            .show();
			
		}
		
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
    	//if(userFunctions.isUserLoggedIn(getApplicationContext())){
    		//Do nothing
    	//}
    	//else{
    	//	Intent intent = new Intent(this,LoginPreferencesActivity.class);
        //	startActivity(intent);
    	//}
    	
    	
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
       
        String user = settings.getString("logged_in",null);
        
        if ((user == null)||(user.equals("0")))
        {
        	Intent intent = new Intent(this,FacebookLogin.class);
        	startActivity(intent);
        	//finish();
        }else{
        	//Do nothing
        	
        }
    }
    
 
    /*

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
		else if (item.getItemId() == R.id.menu_bug_report)
		{
			String url = "https://docs.google.com/forms/d/1KrsTg-NNr8gtQWTCjo-7Fv2L5cml84EcmIuGGNiC4fY/viewform";

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
		}
		else if (item.getItemId() == R.id.menu_about)
		{
			String url = "https://storymaker.cc";

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
		}
        
		return true;
	}
    */
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
			startActivity(new Intent(this,HomePanelsActivity.class));
			
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
