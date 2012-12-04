package info.guardianproject.mrapp.db;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.Context;

public class StoryMakerDB extends SQLiteOpenHelper {
	
    private static final int DB_VERSION = 2;
    private static final String DB_NAME = "sm.db";
    
    public StoryMakerDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
		db.execSQL(StoryMakerDB.Schema.Projects.CREATE_TABLE_PROJECTS);
		db.execSQL(StoryMakerDB.Schema.Lessons.CREATE_TABLE_LESSONS);
		db.execSQL(StoryMakerDB.Schema.Media.CREATE_TABLE_MEDIA);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	
    	db.execSQL(StoryMakerDB.Schema.Projects.UPDATE_TABLE_PROJECTS);
//		db.execSQL(StoryMakerDB.Schema.Lessons.CREATE_TABLE_LESSONS);
//		db.execSQL(StoryMakerDB.Schema.Media.CREATE_TABLE_MEDIA);
    	
    }
    
    public class Schema 
    {
    	
    	public class Lessons
    	{
    		public static final String NAME = "lessons";
        	
	    	public static final String ID = "_id";
	    	public static final String COL_TITLE = "title";
	    	public static final String COL_URL = "url";
	    	
	    	private static final String CREATE_TABLE_LESSONS = "create table " + NAME + " (" 
	    			+ ID + " integer primary key autoincrement, " 
	    			+ COL_TITLE + " text not null, " 
	    			+ COL_URL + " text not null"
	    			+ "); ";
    	}
    	
    	public class Projects
    	{
    		public static final String NAME = "projects";
        	
	    	public static final String ID = "_id";
	    	public static final String COL_TITLE = "title";
	    	public static final String COL_THUMBNAIL_PATH = "thumbnail_path";
	    	public static final String COL_STORY_TYPE = "story_type";
	    	
	    	private static final String CREATE_TABLE_PROJECTS = "create table " + NAME + " (" 
	    			+ ID + " integer primary key autoincrement, " 
	    			+ COL_TITLE + " text not null, " 
	    			+ COL_THUMBNAIL_PATH + " text,"
	    			+ COL_STORY_TYPE + " integer"
					+ "); ";
	    	
	    	private static final String UPDATE_TABLE_PROJECTS = "alter table " + NAME + " " 
	    			+ "ADD COLUMN "
	    			+ COL_STORY_TYPE + " integer"
	    			+ " DEFAULT 0";
    	}
    	
    	public class Media
    	{
    		public static final String NAME = "media";
        	
	    	public static final String ID = "_id";
	    	public static final String COL_PROJECT_ID = "project_id"; // foreign key
	    	public static final String COL_PATH = "path";
	    	public static final String COL_MIME_TYPE = "mime_type";
	    	public static final String COL_CLIP_TYPE = "clip_type";
	    	public static final String COL_CLIP_INDEX = "clip_index";
	    	
	    	private static final String CREATE_TABLE_MEDIA = "create table " + NAME + " ("
	    			+ ID + " integer primary key autoincrement, "
	    			+ COL_PROJECT_ID + " text not null, "
	    			+ COL_PATH + " text not null, "
	    			+ COL_MIME_TYPE + " text not null, " 
	    			+ COL_CLIP_TYPE + " text not null, " 
	    			+ COL_CLIP_INDEX + " integer not null" 
	    			+ "); ";
    	}
    	
//    	public static final String DB_SCHEMA = Lessons.CREATE_TABLE_LESSONS 
//    			+ Projects.CREATE_TABLE_PROJECTS 
//    			+ Medias.CREATE_TABLE_MEDIAS;
    }
    
}