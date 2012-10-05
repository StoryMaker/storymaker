package info.guardianproject.mrapp;

import info.guardianproject.mrapp.db.StoryMakerDB;
import info.guardianproject.mrapp.db.StoryMakerDB.Schema;

import java.io.File;

import net.sqlcipher.database.SQLiteDatabase;
import android.app.Application;

public class StoryMakerApp extends Application {

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
//		InitializeSQLCipher("test.db", "");
	}
}
