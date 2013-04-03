package info.guardianproject.mrapp.db;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.Context;

public class StoryMakerDB extends SQLiteOpenHelper {
	
    private static final int DB_VERSION = 3;
    private static final String DB_NAME = "sm.db";
    
    public StoryMakerDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(StoryMakerDB.Schema.Projects.CREATE_TABLE_PROJECTS);
        db.execSQL(StoryMakerDB.Schema.Scenes.CREATE_TABLE_SCENES);
		db.execSQL(StoryMakerDB.Schema.Lessons.CREATE_TABLE_LESSONS);
		db.execSQL(StoryMakerDB.Schema.Media.CREATE_TABLE_MEDIA);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if ((oldVersion < 2) && (newVersion == 2)) {
            db.execSQL(StoryMakerDB.Schema.Projects.UPDATE_TABLE_PROJECTS);
        } 
        if ((oldVersion < 3) && (newVersion == 3)) {
            db.execSQL(StoryMakerDB.Schema.Media.UPDATE_TABLE_MEDIA_ADD_TRIM_START);
            db.execSQL(StoryMakerDB.Schema.Media.UPDATE_TABLE_MEDIA_ADD_TRIM_END);
            db.execSQL(StoryMakerDB.Schema.Media.UPDATE_TABLE_MEDIA_ADD_DURATION);
        } 
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
            public static final String COL_TEMPLATE_PATH = "template_path";
            
            private static final String CREATE_TABLE_PROJECTS = "create table " + NAME + " (" 
                    + ID + " integer primary key autoincrement, " 
                    + COL_TITLE + " text not null, " 
                    + COL_THUMBNAIL_PATH + " text,"
                    + COL_STORY_TYPE + " integer,"
                    + COL_TEMPLATE_PATH + " text"
                    + "); ";
            
            private static final String UPDATE_TABLE_PROJECTS = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_STORY_TYPE + " integer"
                    + " DEFAULT 0";
        }
        
        public class Scenes
        {
            public static final String NAME = "scenes";
            
            public static final String ID = "_id";
            public static final String COL_TITLE = "title";
            public static final String COL_THUMBNAIL_PATH = "thumbnail_path";
            public static final String COL_PROJECT_INDEX = "project_index";
            public static final String COL_PROJECT_ID = "project_id";
            
            private static final String CREATE_TABLE_SCENES = "create table " + NAME + " (" 
                    + ID + " integer primary key autoincrement, " 
                    + COL_TITLE + " text, " 
                    + COL_THUMBNAIL_PATH + " text,"
                    + COL_PROJECT_INDEX + " integer not null,"
                    + COL_PROJECT_ID + " integer not null" 
                    + "); ";
        }
    	
    	public class Media
    	{
    		public static final String NAME = "media";
        	
	    	public static final String ID = "_id";
	    	public static final String COL_SCENE_ID = "scene_id"; // foreign key
	    	public static final String COL_PATH = "path";
	    	public static final String COL_MIME_TYPE = "mime_type";
	    	public static final String COL_CLIP_TYPE = "clip_type";
	    	public static final String COL_CLIP_INDEX = "clip_index";
            public static final String COL_TRIM_START = "trim_start";
            public static final String COL_TRIM_END = "trim_end";
            public static final String COL_DURATION = "duration";
	    	
	    	private static final String CREATE_TABLE_MEDIA = "create table " + NAME + " ("
	    			+ ID + " integer primary key autoincrement, "
	    			+ COL_SCENE_ID + " text not null, "
	    			+ COL_PATH + " text not null, "
	    			+ COL_MIME_TYPE + " text not null, " 
	    			+ COL_CLIP_TYPE + " text not null, " 
	    			+ COL_CLIP_INDEX + " integer not null," 
                    + COL_TRIM_START + " integer," 
                    + COL_TRIM_END + " integer," 
                    + COL_DURATION + " integer" 
	    			+ "); ";
            
            private static final String UPDATE_TABLE_MEDIA_ADD_TRIM_START = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_TRIM_START + " integer;";

            private static final String UPDATE_TABLE_MEDIA_ADD_TRIM_END = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_TRIM_END + " integer;";

            private static final String UPDATE_TABLE_MEDIA_ADD_DURATION = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_DURATION + " integer;";
    	}
    	
//    	public static final String DB_SCHEMA = Lessons.CREATE_TABLE_LESSONS 
//    			+ Projects.CREATE_TABLE_PROJECTS 
//    			+ Medias.CREATE_TABLE_MEDIAS;
    }
    
}