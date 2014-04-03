package info.guardianproject.mrapp.model;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;

import java.io.File;
import java.util.Date;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


public class Lesson extends Model {

    public String mTitle;
	public String mDescription;
	public String mResourcePath; //file, http, asset
	public String mImage;
	public int mStatus;
	public File mLocalPath;
	public Integer mSortIdx;

	public final static int STATUS_NOT_STARTED = 0;
	public final static int STATUS_IN_PROGRESS = 1;
	public final static int STATUS_COMPLETE = 2;

	public Date mStatusModified;

    Lesson(Context context) {
        super(context);
    }
    
    Lesson(SQLiteDatabase db, Context context) {
        super(db, context);
    }

    @Override
    protected Table getTable() {
        if (mTable == null) {
            mTable = new LessonTable(mDB);
        }
        return mTable;
    }

    // FIXME probably should move this to LessonTable
	public static Lesson parse (Context context, String jsonTxt) throws Exception
	{
		Lesson result = new Lesson(context);
		
		JSONObject jobj= new JSONObject(jsonTxt);
		jobj = jobj.getJSONObject("lesson");
		
		result.mTitle = jobj.getString("title");
		result.mDescription = jobj.getString("description");
		//result.mImage = jobj.getString("image");
		
		result.mResourcePath = jobj.getJSONObject("resource").getString("url");
		
	    return result;

	}
	
	public String toString()
	{
		return mTitle;
	}
    
    protected ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.Lessons.COL_TITLE, mTitle); // FIXME i don't think the lessons module really uses the model right
//        values.put(StoryMakerDB.Schema.Lessons.COL_URL, mUrl);
        
        return values;
    }
}
/**
{"lesson": {
"title": "Journalism Introduction",
"description": "what you need to know in order to know what you need",
"image": "journalism.jpg",
"published": "2012/09/01",
"author": "Joe Someone",
"resource": {
    "url": "index.html"        
}       
}}    
*/