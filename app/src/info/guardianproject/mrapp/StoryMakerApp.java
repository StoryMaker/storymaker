package info.guardianproject.mrapp;

import info.guardianproject.mrapp.lessons.LessonManager;

import java.io.File;
import java.net.URL;

import net.sqlcipher.database.SQLiteDatabase;
import android.app.Application;
import android.util.Log;

public class StoryMakerApp extends Application {

	
	private LessonManager mLessonManager;
	
	//just throwing some test files up here for now 
	private String bootstrapUrlString = "https://guardianproject.info/downloads/storymaker/";
	
	 public void InitializeSQLCipher(String dbName, String passphrase) {
	        	      
		 File databaseFile = getDatabasePath(dbName);
	     databaseFile.mkdirs();
	     SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, passphrase, null);

	  }

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
			mLessonManager = new LessonManager (bootstrapUrlString, fileExt);
			
		}
		catch (Exception e)
		{
			Log.e(MediaAppConstants.TAG,"error init app",e);
		}
	}
	
	public LessonManager getLessonManager ()
	{
		return mLessonManager;
	}
}
