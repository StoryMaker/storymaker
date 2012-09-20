package info.guardianproject.mrapp.db;

import net.sqlcipher.database.SQLiteQueryBuilder;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class LessonsProvider extends ContentProvider {
  
	private StoryMakerDB mDB;
	
    private String mPassphrase = null; //how and when do we set this??
    		
    @Override
    public boolean onCreate() {
        mDB = new StoryMakerDB(getContext());
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(StoryMakerDB.Schema.Lessons.NAME);
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
        case LESSON_ID:
            queryBuilder.appendWhere(StoryMakerDB.Schema.Lessons.ID + "="
                    + uri.getLastPathSegment());
            break;
        case LESSONS:
            // no filter
            break;
        default:
            throw new IllegalArgumentException("Unknown URI");
        }
        
        Cursor cursor = queryBuilder.query(mDB.getReadableDatabase(mPassphrase),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }
    
    private static final String AUTHORITY = "info.guardianproject.mrapp.db.LessonsProvider";
    public static final int LESSONS = 100;
    public static final int LESSON_ID = 110;
    private static final String LESSONS_BASE_PATH = "lessons";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + LESSONS_BASE_PATH);
    
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/mt-lessons";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/mt-lessons";
    
    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, LESSONS_BASE_PATH, LESSONS);
        sURIMatcher.addURI(AUTHORITY, LESSONS_BASE_PATH + "/#", LESSON_ID);
    }
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}
