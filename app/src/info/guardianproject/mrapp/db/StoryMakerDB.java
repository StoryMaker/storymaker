package info.guardianproject.mrapp.db;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.Context;

public class StoryMakerDB extends SQLiteOpenHelper {
	
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "sm.db";
    
    public StoryMakerDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
    	
    	 db.execSQL(Schema.DB_SCHEMA);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    
    public class Schema 
    {
    	
    	public class Lessons
    	{
    		public static final String NAME = "tutorials";
        	
	    	public static final String ID = "_id";
	    	public static final String COL_TITLE = "title";
	    	public static final String COL_URL = "url";
	    	private static final String CREATE_TABLE_TUTORIALS = "create table " + NAME
	    	+ " (" + ID + " integer primary key autoincrement, " + COL_TITLE
	    	+ " text not null, " + COL_URL + " text not null);";
    	}
    	
    	private static final String DB_SCHEMA = Lessons.CREATE_TABLE_TUTORIALS;
    }
    
}