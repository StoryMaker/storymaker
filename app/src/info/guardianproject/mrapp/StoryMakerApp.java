package info.guardianproject.mrapp;

import info.guardianproject.mrapp.lessons.LessonManager;
import info.guardianproject.mrapp.media.MediaProjectManager;
import info.guardianproject.mrapp.server.ServerManager;

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
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class StoryMakerApp extends Application {

	
	private static ServerManager mServerManager;
	private static LessonManager mLessonManager;
	
	private final static String PREF_LOCALE = "plocale";
	private final static String LOCALE_DEFAULT = "en";//need to force english for now as default
	private final static String LOCALE_ARABIC = "ar";//need to carry over settings from previous installed version
	private static Locale mLocale = new Locale(LOCALE_DEFAULT);
		
	private final static String URL_PATH_LESSONS = "/appdata/lessons/";
	private final static String STORYMAKER_DEFAULT_SERVER_URL = "https://storymaker.cc";
	private static String mBaseUrl = null;
	
	 public void InitializeSQLCipher(String dbName, String passphrase) {
	        	      
		 File databaseFile = getDatabasePath(dbName);
	     databaseFile.mkdirs();
	     SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, passphrase, null);

	  }
	 
	 public static String initServerUrls (Context context)
	 {
		 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		 mBaseUrl = settings.getString("pserver", STORYMAKER_DEFAULT_SERVER_URL) ;
		 return mBaseUrl;
	 }
	 
	@Override
	public void onCreate() {
		super.onCreate();

		checkLocale ();
		
		SQLiteDatabase.loadLibs(this);

//		boolean optOut = true;
//		final SharedPreferences prefsAnalytics = getSharedPreferences(Globals.PREFERENCES_ANALYTICS, Activity.MODE_PRIVATE);
//		optOut = !(prefsAnalytics.getBoolean(Globals.PREFERENCE_ANALYTICS_OPTIN, false));
//		GoogleAnalytics.getInstance(this).setAppOptOut(optOut);
		
		initApp();
		 
	}
	
	private void initApp ()
	{
		try
		{

			clearRenderTmpFolders(getApplicationContext());
			
			initServerUrls(this);
	
			killZombieProcs ();
			
            updateLessonLocation();
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
	
	public void killZombieProcs () throws Exception
	{
		int killDelayMs = 300;
    	
		int procId = -1;
		
		File fileCmd = new File(getDir("bin", Context.MODE_WORLD_READABLE),"ffmpeg");
		
		while ((procId = findProcessId(fileCmd.getAbsolutePath())) != -1)
		{
			
			Log.w(AppConstants.TAG,"Found Tor PID=" + procId + " - killing now...");
			
			String[] cmd = { SHELL_CMD_KILL + ' ' + procId + "" };
			StringBuilder log = new StringBuilder();
			doShellCommand(cmd,log, false, false);
			try { Thread.sleep(killDelayMs); }
			catch (Exception e){}
		}

	}
	
	public void updateLessonLocation ()
	{
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String customLessonLoc = settings.getString("pleslanguage", null);
        
        if (customLessonLoc == null) {
            // check for previous version settings, use if found
            customLessonLoc = settings.getString("plessonloc", null);
        }  
                
        String lessonUrlPath = mBaseUrl + URL_PATH_LESSONS + mLocale.getLanguage() + "/";
        String lessonLocalPath = "lessons/" + mLocale.getLanguage();
        
        if (customLessonLoc != null && customLessonLoc.length() > 0)
        {
            if (customLessonLoc.toLowerCase().startsWith("http"))
            {
                lessonUrlPath = customLessonLoc;
                lessonLocalPath = "lessons/" + lessonUrlPath.substring(lessonUrlPath.lastIndexOf('/')+1);
            }
            else
            {
                lessonUrlPath = mBaseUrl + URL_PATH_LESSONS + customLessonLoc + "/";
                lessonLocalPath = "lessons/" + customLessonLoc;
            }
        }
        
        File fileDirLessons = new File(getExternalFilesDir(null), lessonLocalPath);
        fileDirLessons.mkdirs();
                
        mLessonManager = new LessonManager (this, lessonUrlPath, fileDirLessons);
        mServerManager = new ServerManager (getApplicationContext());
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
	        
	        if (updatedLocale)
	        {
	            //need to reload lesson manager for new locale
	        	File fileDirLessons = new File(getExternalFilesDir(null), "lessons/" + lang);
	        	fileDirLessons.mkdirs();
				mLessonManager = new LessonManager (this, mBaseUrl + URL_PATH_LESSONS+ lang + "/", fileDirLessons);

	        }
	        
	        return updatedLocale;
	    }
	
	public static ServerManager getServerManager ()
	{
		return mServerManager;
	}
	
	public static LessonManager getLessonManager ()
	{
		return mLessonManager;
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
				int exitCode = doShellCommand(cmd, log, false, true);
				if (exitCode != 0)
					return false;
				else
					return true;
			}
			
			//Check for 'su' binary 
			String[] cmd = {"which su"};
			int exitCode = doShellCommand(cmd, log, false, true);
			
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
	
	
	public static int findProcessId(String command) 
	{
		int procId = -1;
		
		try
		{
			procId = findProcessIdWithPidOf(command);
			
			if (procId == -1)
				procId = findProcessIdWithPS(command);
		}
		catch (Exception e)
		{
			try
			{
				procId = findProcessIdWithPS(command);
			}
			catch (Exception e2)
			{
				Log.w(AppConstants.TAG,"Unable to get proc id for: " + command,e2);
			}
		}
		
		return procId;
	}
	
	//use 'pidof' command
	public static int findProcessIdWithPidOf(String command) throws Exception
	{
		
		int procId = -1;
		
		Runtime r = Runtime.getRuntime();
		    	
		Process procPs = null;
		
		String baseName = new File(command).getName();
		//fix contributed my mikos on 2010.12.10
		procPs = r.exec(new String[] {SHELL_CMD_PIDOF, baseName});
        //procPs = r.exec(SHELL_CMD_PIDOF);
            
        BufferedReader reader = new BufferedReader(new InputStreamReader(procPs.getInputStream()));
        String line = null;

        while ((line = reader.readLine())!=null)
        {
        
        	try
        	{
        		//this line should just be the process id
        		procId = Integer.parseInt(line.trim());
        		break;
        	}
        	catch (NumberFormatException e)
        	{
        		Log.e("TorServiceUtils","unable to parse process pid: " + line,e);
        	}
        }
            
       
        return procId;

	}
	
	//use 'ps' command
	public static int findProcessIdWithPS(String command) throws Exception
	{
		
		int procId = -1;
		
		Runtime r = Runtime.getRuntime();
		    	
		Process procPs = null;
		
        procPs = r.exec(SHELL_CMD_PS);
            
        BufferedReader reader = new BufferedReader(new InputStreamReader(procPs.getInputStream()));
        String line = null;
        
        while ((line = reader.readLine())!=null)
        {
        	if (line.indexOf(' ' + command)!=-1)
        	{
        		
        		StringTokenizer st = new StringTokenizer(line," ");
        		st.nextToken(); //proc owner
        		
        		procId = Integer.parseInt(st.nextToken().trim());
        		
        		break;
        	}
        }
        
       
        
        return procId;

	}
	
	
	public static int doShellCommand(String[] cmds, StringBuilder log, boolean runAsRoot, boolean waitFor) throws Exception
	{
		
		Process proc = null;
		int exitCode = -1;
		
    	if (runAsRoot)
    		proc = Runtime.getRuntime().exec("su");
    	else
    		proc = Runtime.getRuntime().exec("sh");
    
    	OutputStreamWriter out = new OutputStreamWriter(proc.getOutputStream());
        
        for (int i = 0; i < cmds.length; i++)
        {
        //	TorService.logMessage("executing shell cmd: " + cmds[i] + "; runAsRoot=" + runAsRoot + ";waitFor=" + waitFor);
    		
        	out.write(cmds[i]);
        	out.write("\n");
        }
        
        out.flush();
		out.write("exit\n");
		out.flush();
	
		if (waitFor)
		{
			
			final char buf[] = new char[10];
			
			// Consume the "stdout"
			InputStreamReader reader = new InputStreamReader(proc.getInputStream());
			int read=0;
			while ((read=reader.read(buf)) != -1) {
				if (log != null) log.append(buf, 0, read);
			}
			
			// Consume the "stderr"
			reader = new InputStreamReader(proc.getErrorStream());
			read=0;
			while ((read=reader.read(buf)) != -1) {
				if (log != null) log.append(buf, 0, read);
			}
			
			exitCode = proc.waitFor();
			
		}
        
        
        return exitCode;

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

	//various console cmds
	public final static String SHELL_CMD_CHMOD = "chmod";
	public final static String SHELL_CMD_KILL = "kill -9";
	public final static String SHELL_CMD_RM = "rm";
	public final static String SHELL_CMD_PS = "ps";
	public final static String SHELL_CMD_PIDOF = "pidof";
}
