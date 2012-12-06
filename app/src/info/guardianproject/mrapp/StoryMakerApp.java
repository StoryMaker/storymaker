package info.guardianproject.mrapp;

import info.guardianproject.mrapp.lessons.LessonManager;
import info.guardianproject.mrapp.server.ServerManager;
import info.guardianproject.mrapp.db.StoryMakerDB;
import info.guardianproject.mrapp.db.StoryMakerDB.Schema;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import net.sqlcipher.database.SQLiteDatabase;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

public class StoryMakerApp extends Application {

	
	private static ServerManager mServerManager;
	private static LessonManager mLessonManager;
	
	private final static String LOCALE_DEFAULT = "en";//need to force english for now as default
	private final static String LANG_ARABIC = "ar";
	
	private static Locale mLocale = new Locale(LOCALE_DEFAULT);
	
	private final static String PREF_LOCALE = "plocale";
	
	//just throwing some test tfiles up here for now 
	private String bootstrapUrlString = "https://guardianproject.info/private/storymaker/content/lessons/";
	
	 public void InitializeSQLCipher(String dbName, String passphrase) {
	        	      
		 File databaseFile = getDatabasePath(dbName);
	     databaseFile.mkdirs();
	     SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, passphrase, null);

	  }
	 
//	public void InitializeSQLCipher(String dbName, String passphrase) {
//
//		File databaseFile = getDatabasePath(dbName);
//		databaseFile.mkdirs();
//		databaseFile.delete();
//		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, passphrase, null);
//		database.execSQL(StoryMakerDB.Schema.Projects.CREATE_TABLE_PROJECTS);
//		database.execSQL(StoryMakerDB.Schema.Lessons.CREATE_TABLE_LESSONS);
//		database.execSQL(StoryMakerDB.Schema.Medias.CREATE_TABLE_MEDIAS);
//		database.close();
//
//	}

	@Override
	public void onCreate() {
		super.onCreate();

		checkLocale ();
		
		SQLiteDatabase.loadLibs(this);

		initApp();
		 
	}
	
	private void initApp ()
	{
		try
		{
			
			mLessonManager = new LessonManager (this, bootstrapUrlString + mLocale.getLanguage() + "/", new File(getExternalFilesDir(null), "lessons/" + mLocale.getLanguage()));
			mServerManager = new ServerManager (getBaseContext());
		}
		catch (Exception e)
		{
			Log.e(AppConstants.TAG,"error init app",e);
		}
	}

	public void updateLocale (String newLocale)
	{
        Configuration config = getResources().getConfiguration();
		mLocale = new Locale(newLocale);
		
		Locale.setDefault(mLocale);
		config.locale = mLocale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.edit().putString(PREF_LOCALE,newLocale);
        settings.edit().commit();
        //need to reload lesson manager for new locale
		mLessonManager = new LessonManager (this, bootstrapUrlString+ mLocale.getLanguage() + "/", new File(getExternalFilesDir(null), "lessons/" + newLocale));

	}
	
	public Locale getCurrentLocale ()
	{
		return mLocale;
	}
	
	 public boolean checkLocale ()
	    {
	        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

	        Configuration config = getResources().getConfiguration();

	        boolean useLangAr = settings.getBoolean("plocalear", false);
	        String lang = settings.getString(PREF_LOCALE, "");
	        
	        if (useLangAr)
	        	lang = LANG_ARABIC;
	        
	        boolean updatedLocale = false;
	        
	        //if we have an arabic preference stored, then use it
	        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
	            mLocale = new Locale(lang);            
	            config.locale = mLocale;
	            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
	            updatedLocale = true;
	        }
	        else if (Locale.getDefault().getLanguage().equalsIgnoreCase(LANG_ARABIC))
	        {
	        	//if device is default arabic, then switch the app to it
	        	  mLocale = Locale.getDefault();         
		            config.locale = mLocale;
		            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
		            updatedLocale = true;
	        }
	        
	        if (updatedLocale)
	        {
	            //need to reload lesson manager for new locale
				mLessonManager = new LessonManager (this, bootstrapUrlString+ mLocale.getLanguage() + "/", new File(getExternalFilesDir(null), "lessons/" + mLocale.getLanguage()));

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
}
