package info.guardianproject.mrapp.db;

import net.sqlcipher.database.SQLiteQueryBuilder;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class ProjectsProvider extends ContentProvider {  
	private StoryMakerDB mDB;
    private String mPassphrase = "foo"; //how and when do we set this??
    
    private static final String AUTHORITY = "info.guardianproject.mrapp.db.ProjectsProvider";
    public static final int PROJECTS = 101;
    public static final int PROJECT_ID = 111;
    public static final String PROJECTS_BASE_PATH = "projects";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + PROJECTS_BASE_PATH);
    
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/projects";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/projects";
    
    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    
    static {
        sURIMatcher.addURI(AUTHORITY, PROJECTS_BASE_PATH, PROJECTS);
        sURIMatcher.addURI(AUTHORITY, PROJECTS_BASE_PATH + "/#", PROJECT_ID);
    }
    
    @Override
    public boolean onCreate() {
        mDB = new StoryMakerDB(getContext()); 
        return true;
    }

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
        case PROJECT_ID:
            queryBuilder.setTables(StoryMakerDB.Schema.Projects.NAME);
            queryBuilder.appendWhere(StoryMakerDB.Schema.Lessons.ID + "="
                    + uri.getLastPathSegment());
            break;
        case PROJECTS:
        	queryBuilder.setTables(StoryMakerDB.Schema.Projects.NAME);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI");
        }
        
        Cursor cursor = queryBuilder.query(mDB.getReadableDatabase(mPassphrase),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case PROJECTS:
            long newProjectId = mDB.getWritableDatabase(mPassphrase)
            	.insertOrThrow(StoryMakerDB.Schema.Projects.NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return CONTENT_URI.buildUpon().appendPath(PROJECTS_BASE_PATH).appendPath("" + newProjectId).build();
		default:
			throw new IllegalArgumentException("Unknown URI");
		}
	}
    
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}
