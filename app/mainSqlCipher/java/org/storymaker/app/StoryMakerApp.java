package org.storymaker.app;

import org.storymaker.app.media.MediaProjectManager;
import org.storymaker.app.server.ServerManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.StringTokenizer;

//import com.google.analytics.tracking.android.GoogleAnalytics;

import net.sqlcipher.database.SQLiteDatabase;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import scal.io.liger.DownloadHelper;
import scal.io.liger.IndexManager;

public class StoryMakerApp extends MultiDexApplication {

	
	private static ServerManager mServerManager;

	private final static String PREF_LOCALE = "plocale";
	private final static String LOCALE_DEFAULT = "en";//need to force english for now as default
	private final static String LOCALE_ARABIC = "ar";//need to carry over settings from previous installed version
	private final static String LOCALE_SOUTH_AFRICAN = "sa";
	private static Locale mLocale = new Locale(LOCALE_DEFAULT);

    public final static String STORYMAKER_DEFAULT_SERVER_URL = "https://storymaker.org/";
    public final static String STORYMAKER_SERVER_URL_PREFS_KEY = "pserver";
	private static String mBaseUrl = null;
	
	 public void InitializeSQLCipher(String dbName, String passphrase) {

		 File databaseFile = getDatabasePath(dbName);
	     databaseFile.mkdirs();
	     SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, passphrase, null);

	  }
	 
	 public static String initServerUrls (Context context)
	 {
		 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		 mBaseUrl = settings.getString(STORYMAKER_SERVER_URL_PREFS_KEY, STORYMAKER_DEFAULT_SERVER_URL) ;
		 return mBaseUrl;
	 }
	 
	@Override
	public void onCreate() {
		super.onCreate();

		checkLocale ();
		
		SQLiteDatabase.loadLibs(this);

//		boolean optOut = true;
//		final SharedPreferences prefsAnalytics = getSharedPreferences(Constants.PREFERENCES_ANALYTICS, Activity.MODE_PRIVATE);
//		optOut = !(prefsAnalytics.getBoolean(Constants.PREFERENCE_ANALYTICS_OPTIN, false));
//		GoogleAnalytics.getInstance(this).setAppOptOut(optOut);
		
		initApp();
		 
	}
	
	private void initApp ()
	{
		try
		{

			clearRenderTmpFolders(getApplicationContext());
			
			initServerUrls(this);
	
			Utils.Proc.killZombieProcs(this);

            mServerManager = new ServerManager (getApplicationContext());

            //moved this to HomeActivity.OnCreate() so it's redundant here
            //DownloadHelper.checkAndDownload(this);
		}
		catch (Exception e)
		{
			Log.e(AppConstants.TAG,"error init app",e);
		}
	}
		
	@Override
    public void onConfigurationChanged(Configuration newConfig)
    {
		super.onConfigurationChanged(newConfig);
        if (mLocale != null)
        {
            Locale.setDefault(mLocale);
            Configuration config = new Configuration();
            config.locale = mLocale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }

	public void updateLocale (String newLocale)
	{
        mLocale = new Locale(newLocale);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        settings.edit().putString(PREF_LOCALE,newLocale);
        settings.edit().commit();
        checkLocale();
        
        //need to reload lesson manager for new locale
        initServerUrls(this);

	}
	
	private boolean isLocaleValid(String language) {
		
		if(!language.equals(LOCALE_DEFAULT) && !language.equals(LOCALE_ARABIC) && !language.equals(LOCALE_SOUTH_AFRICAN)) {
    		return false;
    	}
		
		return true;
	}
	
	public boolean isExternalStorageReady ()
	{
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		
		return mExternalStorageAvailable && mExternalStorageWriteable;
	}
	
	public static Locale getCurrentLocale ()
	{
		return mLocale;
	}
	
	 public boolean checkLocale ()
	    {
	        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

	        Configuration config = getResources().getConfiguration();

	        //String lang = settings.getString("pintlanguage", LOCALE_DEFAULT);
	        String lang = settings.getString("pintlanguage", null);
	        
	        if (lang == null) {
	            // check for previous version settings, use if found
	            if (settings.getBoolean("plocalear", false)) {
	                lang = LOCALE_ARABIC;
	            }
	            else {
	                lang = LOCALE_DEFAULT;
	            }
	        }  

	        boolean updatedLocale = false;
	        
	        // if the language string is not empty, 
	        // and the current config/locale/language is not the selected language, 
	        // set locale to selected language and update default
	        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
	            mLocale = new Locale(lang);
	    		Locale.setDefault(mLocale);
	            config.locale = mLocale;
	            getResources().updateConfiguration(config, getResources().getDisplayMetrics());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    getResources().getConfiguration().setLayoutDirection(mLocale);

	            updatedLocale = true;
	            lang = config.locale.getLanguage();
	        }
	        // otherwise, if the default locale/language is the selected language, 
	        // set locale to default language (is this necessary?)
	        else if (Locale.getDefault().getLanguage().equalsIgnoreCase(lang))
	        {
	        	  mLocale = Locale.getDefault();         
		            config.locale = mLocale;
		            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
		            updatedLocale = true;
		            lang = config.locale.getLanguage();
	        }
	        
	        return updatedLocale;
	    }
	
	public static ServerManager getServerManager ()
	{
		return mServerManager;
	}
	
	public static boolean isRootPossible()
	{
		
		StringBuilder log = new StringBuilder();
		
		try {
			
			// Check if Superuser.apk exists
			File fileSU = new File("/system/app/Superuser.apk");
			if (fileSU.exists())
				return true;
			
			fileSU = new File("/system/app/superuser.apk");
			if (fileSU.exists())
				return true;
			
			fileSU = new File("/system/bin/su");
			if (fileSU.exists())
			{
				String[] cmd = {"su"};
				int exitCode = Utils.Proc.doShellCommand(cmd, log, false, true);
				if (exitCode != 0)
					return false;
				else
					return true;
			}
			
			//Check for 'su' binary 
			String[] cmd = {"which su"};
			int exitCode = Utils.Proc.doShellCommand(cmd, log, false, true);
			
			if (exitCode == 0) {
				Log.d(AppConstants.TAG,"root exists, but not sure about permissions");
		    	 return true;
		     
		    }
		      
		} catch (IOException e) {
			//this means that there is no root to be had (normally) so we won't log anything
			Log.e(AppConstants.TAG,"Error checking for root access",e);
			
		}
		catch (Exception e) {
			Log.e(AppConstants.TAG,"Error checking for root access",e);
			//this means that there is no root to be had (normally)
		}
		
		Log.e(AppConstants.TAG,"Could not acquire root permissions");
		
		
		return false;
	}
	
	
	
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		long availableMegs = mi.availMem / 1048576L;
		
		Log.e(AppConstants.TAG,"LOW MEMORY WARNING/ MEMORY AVAIL=" + availableMegs);
		
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		
		clearRenderTmpFolders(getApplicationContext());
	}

	public void clearRenderTmpFolders (Context context)
	{
		try
		{
		 File fileRenderTmpDir = MediaProjectManager.getRenderPath(context);
		// deleteRecursive(fileRenderTmpDir,false);
		 Runtime.getRuntime().exec("rm -rf " + fileRenderTmpDir.getCanonicalPath());
		}
		catch (IOException ioe)
		{
			Log.w(AppConstants.TAG,"error deleting render tmp on exit",ioe);
		}
	}
	
	 void deleteRecursive(File fileOrDirectory, boolean onExit) throws IOException {
	        if (fileOrDirectory.isDirectory())
	            for (File child : fileOrDirectory.listFiles())
	            	deleteRecursive(child, onExit);

	        if (!onExit)
	        {
	        	fileOrDirectory.delete();
	        }
	        else
	        	fileOrDirectory.deleteOnExit();
	    }
}
