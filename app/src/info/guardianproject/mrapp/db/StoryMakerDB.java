package info.guardianproject.mrapp.db;

import java.util.ArrayList;

import info.guardianproject.mrapp.model.Auth;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Scene;
import info.guardianproject.mrapp.model.SceneTable;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.Context;
import android.util.Log;

public class StoryMakerDB extends SQLiteOpenHelper {
    private static final String TAG = "StoryMakerDB";
    private static final int DB_VERSION = 7;
    private static final String DB_NAME = "sm.db";
    private Context mContext;
    
    public StoryMakerDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(StoryMakerDB.Schema.Projects.CREATE_TABLE_PROJECTS);
        db.execSQL(StoryMakerDB.Schema.Scenes.CREATE_TABLE_SCENES);
		db.execSQL(StoryMakerDB.Schema.Lessons.CREATE_TABLE_LESSONS);
		db.execSQL(StoryMakerDB.Schema.Media.CREATE_TABLE_MEDIA);
		db.execSQL(StoryMakerDB.Schema.Auth.CREATE_TABLE_AUTH);
        db.execSQL(StoryMakerDB.Schema.Tags.CREATE_TABLE_TAGS);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "updating db from " + oldVersion + " to " + newVersion);
        
        if ((oldVersion < 2) && (newVersion >= 2)) {
            db.execSQL(StoryMakerDB.Schema.Projects.UPDATE_TABLE_PROJECTS);
        }
        
        if ((oldVersion < 3) && (newVersion >= 3)) {
            db.execSQL(StoryMakerDB.Schema.Media.UPDATE_TABLE_MEDIA_ADD_TRIM_START);
            db.execSQL(StoryMakerDB.Schema.Media.UPDATE_TABLE_MEDIA_ADD_TRIM_END);
            db.execSQL(StoryMakerDB.Schema.Media.UPDATE_TABLE_MEDIA_ADD_DURATION);
        } 
        
        if ((oldVersion < 6) && (newVersion >= 6)) {
            db.execSQL(StoryMakerDB.Schema.Auth.UPDATE_TABLE_AUTH);
            db.execSQL(StoryMakerDB.Schema.Projects.UPDATE_TABLE_PROJECTS_ADD_CREATED_AT);
            db.execSQL(StoryMakerDB.Schema.Projects.UPDATE_TABLE_PROJECTS_ADD_UPDATED_AT);
            db.execSQL(StoryMakerDB.Schema.Scenes.UPDATE_TABLE_SCENES_ADD_CREATED_AT);
            db.execSQL(StoryMakerDB.Schema.Scenes.UPDATE_TABLE_SCENES_ADD_UPDATED_AT);
            db.execSQL(StoryMakerDB.Schema.Media.UPDATE_TABLE_MEDIA_ADD_CREATED_AT);
            db.execSQL(StoryMakerDB.Schema.Media.UPDATE_TABLE_MEDIA_ADD_UPDATED_AT);
            db.execSQL(StoryMakerDB.Schema.Projects.UPDATE_TABLE_PROJECTS_ADD_SECTION);
            db.execSQL(StoryMakerDB.Schema.Projects.UPDATE_TABLE_PROJECTS_ADD_LOCATION);
            db.execSQL(StoryMakerDB.Schema.Tags.UPDATE_TABLE_TAGS);
            Auth.migrate(mContext, db); // migrates storymaker login credentials
            Project.migrate(mContext, db); // migrates existing database records and associated files
        }
        
        if ((oldVersion < 7) && (newVersion >= 7)) {
            @SuppressWarnings("unchecked")
            ArrayList<Scene> scenes = (ArrayList<Scene>) (new SceneTable(db)).getAllAsList(mContext);
            for (Scene scene: scenes) {
                scene.migrateDeleteDupedMedia();
            }
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
            public static final String COL_CREATED_AT = "created_at";
            public static final String COL_UPDATED_AT = "updated_at";
            public static final String COL_SECTION = "section";
            public static final String COL_LOCATION = "location";
            
            private static final String CREATE_TABLE_PROJECTS = "create table " + NAME + " (" 
                    + ID + " integer primary key autoincrement, " 
                    + COL_TITLE + " text not null, " 
                    + COL_THUMBNAIL_PATH + " text,"
                    + COL_STORY_TYPE + " integer,"
                    + COL_TEMPLATE_PATH + " text,"
                    + COL_CREATED_AT + " integer,"
                    + COL_UPDATED_AT + " integer,"
                    + COL_SECTION + " text,"
                    + COL_LOCATION + " text"
                    + "); ";
            
            private static final String UPDATE_TABLE_PROJECTS = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_STORY_TYPE + " integer"
                    + " DEFAULT 0";

            private static final String UPDATE_TABLE_PROJECTS_ADD_CREATED_AT = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_CREATED_AT + " integer;";
            
            private static final String UPDATE_TABLE_PROJECTS_ADD_UPDATED_AT = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_UPDATED_AT + " integer;"; 

            private static final String UPDATE_TABLE_PROJECTS_ADD_SECTION = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_SECTION + " text;";
            
            private static final String UPDATE_TABLE_PROJECTS_ADD_LOCATION = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_LOCATION + " text;";
        }
        
        public class Scenes
        {
            public static final String NAME = "scenes";
            
            public static final String ID = "_id";
            public static final String COL_TITLE = "title";
            public static final String COL_THUMBNAIL_PATH = "thumbnail_path";
            public static final String COL_PROJECT_INDEX = "project_index";
            public static final String COL_PROJECT_ID = "project_id";
            public static final String COL_CREATED_AT = "created_at";
            public static final String COL_UPDATED_AT = "updated_at";
            
            private static final String CREATE_TABLE_SCENES = "create table " + NAME + " (" 
                    + ID + " integer primary key autoincrement, " 
                    + COL_TITLE + " text, " 
                    + COL_THUMBNAIL_PATH + " text,"
                    + COL_PROJECT_INDEX + " integer not null,"
                    + COL_PROJECT_ID + " integer not null,"
                    + COL_CREATED_AT + " integer,"
                    + COL_UPDATED_AT + " integer" 
                    + "); ";

            private static final String UPDATE_TABLE_SCENES_ADD_CREATED_AT = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_CREATED_AT + " integer;";
            
            private static final String UPDATE_TABLE_SCENES_ADD_UPDATED_AT = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_UPDATED_AT + " integer;"; 
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
            public static final String COL_CREATED_AT = "created_at";
            public static final String COL_UPDATED_AT = "updated_at";
	    	
	    	private static final String CREATE_TABLE_MEDIA = "create table " + NAME + " ("
	    			+ ID + " integer primary key autoincrement, "
	    			+ COL_SCENE_ID + " text not null, "
	    			+ COL_PATH + " text not null, "
	    			+ COL_MIME_TYPE + " text not null, " 
	    			+ COL_CLIP_TYPE + " text not null, " 
	    			+ COL_CLIP_INDEX + " integer not null," 
                    + COL_TRIM_START + " integer," 
                    + COL_TRIM_END + " integer," 
                    + COL_DURATION + " integer,"
                    + COL_CREATED_AT + " integer,"
                    + COL_UPDATED_AT + " integer" 
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
            
            private static final String UPDATE_TABLE_MEDIA_ADD_CREATED_AT = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_CREATED_AT + " integer;";
            
            private static final String UPDATE_TABLE_MEDIA_ADD_UPDATED_AT = "alter table " + NAME + " " 
                    + "ADD COLUMN "
                    + COL_UPDATED_AT + " integer;"; 
    	}
    	
    	public class Auth
        {
            public static final String NAME = "auth";
            
            public static final String ID = "_id";
            public static final String COL_NAME = "name";
            public static final String COL_SITE = "site";
            public static final String COL_USER_NAME = "user_name";
            public static final String COL_CREDENTIALS = "credentials";
            public static final String COL_DATA = "data";
            public static final String COL_EXPIRES = "expires";
            public static final String COL_LAST_LOGIN = "last_login";
            
            private static final String CREATE_TABLE_AUTH = "create table " + NAME + " (" 
                    + ID + " integer primary key autoincrement, " 
                    + COL_NAME + " text not null, "
                    + COL_SITE + " text not null, "
                    + COL_USER_NAME + " text, "
                    + COL_CREDENTIALS + " text, "
                    + COL_DATA + " text, "
                    + COL_EXPIRES + " integer, "
                    + COL_LAST_LOGIN + " integer "
                    + "); ";
            
            private static final String UPDATE_TABLE_AUTH = CREATE_TABLE_AUTH;
        }
    	
    	public class Tags
        {
            public static final String NAME = "tags";
            
            public static final String ID = "_id";
            public static final String COL_TAG = "tag";
            public static final String COL_PROJECT_ID = "project_id";
            public static final String COL_CREATED_AT = "created_at";
            
            private static final String CREATE_TABLE_TAGS = "create table " + NAME + " (" 
                    + ID + " integer primary key autoincrement, " 
                    + COL_TAG + " text not null, "
                    + COL_PROJECT_ID + " integer not null, "
                    + COL_CREATED_AT + " integer "
                    + "); ";
            
            private static final String UPDATE_TABLE_TAGS = CREATE_TABLE_TAGS;
        }
    }
    
}
