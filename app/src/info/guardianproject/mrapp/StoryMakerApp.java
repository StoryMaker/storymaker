package info.guardianproject.mrapp;

import java.io.File;

import net.sqlcipher.database.SQLiteDatabase;
import android.app.Application;

public class StoryMakerApp extends Application {

	
	 public void InitializeSQLCipher(String dbName, String passphrase) {
	        
	      
		 File databaseFile = getDatabasePath(dbName);
	     databaseFile.mkdirs();
	     SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, passphrase, null);

	  }

	@Override
	public void onCreate() {
		super.onCreate();
		
		 SQLiteDatabase.loadLibs(this);

	}
}
