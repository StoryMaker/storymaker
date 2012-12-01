package info.guardianproject.mrapp;

import info.guardianproject.mrapp.lessons.LessonManager;
import info.guardianproject.mrapp.server.ServerManager;
import info.guardianproject.mrapp.db.StoryMakerDB;
import info.guardianproject.mrapp.db.StoryMakerDB.Schema;

import java.io.File;
import java.net.URL;

import net.sqlcipher.database.SQLiteDatabase;
import android.app.Application;
import android.util.Log;

public class StoryMakerApp extends Application {

	
	private static ServerManager mServerManager;
	private static LessonManager mLessonManager;
	
	//just throwing some test files up here for now 
	private String bootstrapUrlString = "https://guardianproject.info/downloads/storymaker/";
	
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

		
		SQLiteDatabase.loadLibs(this);

		initApp();
		 
	}
	
	private void initApp ()
	{
		try
		{
			File fileExt = getExternalFilesDir(null);
			mLessonManager = new LessonManager (this, bootstrapUrlString, new File(fileExt, "lessons"));
			mServerManager = new ServerManager (this);
		}
		catch (Exception e)
		{
			Log.e(AppConstants.TAG,"error init app",e);
		}
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
