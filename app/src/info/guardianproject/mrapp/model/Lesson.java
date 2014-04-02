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

	Lesson(Context context) {
        super(context);
    }

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


    @Override
    protected Table getTable() {
        if (mTable == null) {
            mTable = new LessonTable();
        }
        return mTable;
    }
    
//    public static Cursor queryOne(Context context, SQLiteDatabase db, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
//        queryBuilder.setTables(StoryMakerDB.Schema.Lessons.NAME);
//        queryBuilder.appendWhere(StoryMakerDB.Schema.Lessons.ID + "=" + uri.getLastPathSegment());
//        
//        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
//        cursor.setNotificationUri(context.getContentResolver(), uri);
//        return cursor;
//    }
//
//    public static Cursor queryAll(Context context, SQLiteDatabase db, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
//        queryBuilder.setTables(StoryMakerDB.Schema.Lessons.NAME);
//        
//        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
//        cursor.setNotificationUri(context.getContentResolver(), uri);
//        return cursor;
//    }

//    public static Uri insert(Context context, SQLiteDatabase db, Uri uri, ContentValues values) {
//        long newId;
//        newId = db.insertOrThrow(StoryMakerDB.Schema.Lessons.NAME, null, values);
//        context.getContentResolver().notifyChange(uri, null);
//        return ProjectsProvider.LESSONS_CONTENT_URI.buildUpon().appendPath(ProjectsProvider.LESSONS_BASE_PATH).appendPath("" + newId).build();
//    }
//    
//    public static int delete(Context context, SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs) {
//        int count = db.delete(StoryMakerDB.Schema.Lessons.NAME, selection, selectionArgs);
//        context.getContentResolver().notifyChange(uri, null);
//        return count;
//    }
//
//    public static int update(Context context, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//        int count;
//        count = db.update(StoryMakerDB.Schema.Lessons.NAME, values, selection, selectionArgs);
//        context.getContentResolver().notifyChange(uri, null);
//        return count;
//    }
    
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