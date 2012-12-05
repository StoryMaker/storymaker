package info.guardianproject.mrapp.db;

import net.sqlcipher.database.SQLiteQueryBuilder;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

// FIXME rename this to SMProvier and get rid of LessonsProvider
public class ProjectsProvider extends ContentProvider {  
	private StoryMakerDB mDB;
    private String mPassphrase = "foo"; //how and when do we set this??
    
    private static final String AUTHORITY = "info.guardianproject.mrapp.db.ProjectsProvider";
    public static final int PROJECTS = 101;
    public static final int PROJECT_ID = 111;
    public static final int LESSONS = 102;
    public static final int LESSON_ID = 112;
    public static final int MEDIA = 103;
    public static final int MEDIA_ID = 113;
    public static final String PROJECTS_BASE_PATH = "projects";
    public static final String LESSONS_BASE_PATH = "lessons";
    public static final String MEDIA_BASE_PATH = "media";
    public static final Uri PROJECTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PROJECTS_BASE_PATH);
    public static final Uri LESSONS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + LESSONS_BASE_PATH);
    public static final Uri MEDIA_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + MEDIA_BASE_PATH);

    public static final String PROJECTS_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/projects";
    public static final String PROJECTS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/projects";
    public static final String LESSONS_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/lessons";
    public static final String LESSONS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/lessons";
    public static final String MEDIA_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/media";
    public static final String MEDIA_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/media";
    
    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    
    static {
        sURIMatcher.addURI(AUTHORITY, PROJECTS_BASE_PATH, PROJECTS);
        sURIMatcher.addURI(AUTHORITY, PROJECTS_BASE_PATH + "/#", PROJECT_ID);
        sURIMatcher.addURI(AUTHORITY, LESSONS_BASE_PATH, LESSONS);
        sURIMatcher.addURI(AUTHORITY, LESSONS_BASE_PATH + "/#", LESSON_ID);
        sURIMatcher.addURI(AUTHORITY, MEDIA_BASE_PATH, MEDIA);
        sURIMatcher.addURI(AUTHORITY, MEDIA_BASE_PATH + "/#", MEDIA_ID);
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
            queryBuilder.appendWhere(StoryMakerDB.Schema.Projects.ID + "="
                    + uri.getLastPathSegment());
            break;
        case PROJECTS:
        	queryBuilder.setTables(StoryMakerDB.Schema.Projects.NAME);
            break;
        case LESSON_ID:
            queryBuilder.setTables(StoryMakerDB.Schema.Lessons.NAME);
            queryBuilder.appendWhere(StoryMakerDB.Schema.Lessons.ID + "="
                    + uri.getLastPathSegment());
            break;
        case LESSONS:
        	queryBuilder.setTables(StoryMakerDB.Schema.Lessons.NAME);
            break;
        case MEDIA_ID:
            queryBuilder.setTables(StoryMakerDB.Schema.Media.NAME);
            queryBuilder.appendWhere(StoryMakerDB.Schema.Media.ID + "="
                    + uri.getLastPathSegment());
            break;
        case MEDIA:
        	queryBuilder.setTables(StoryMakerDB.Schema.Media.NAME);
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
		long newId;
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case PROJECTS:
            newId = mDB.getWritableDatabase(mPassphrase)
            	.insertOrThrow(StoryMakerDB.Schema.Projects.NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return PROJECTS_CONTENT_URI.buildUpon().appendPath(PROJECTS_BASE_PATH).appendPath("" + newId).build();
		case LESSONS:
            newId = mDB.getWritableDatabase(mPassphrase)
            	.insertOrThrow(StoryMakerDB.Schema.Lessons.NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return LESSONS_CONTENT_URI.buildUpon().appendPath(LESSONS_BASE_PATH).appendPath("" + newId).build();
		case MEDIA:
            newId = mDB.getWritableDatabase(mPassphrase)
            	.insertOrThrow(StoryMakerDB.Schema.Media.NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return MEDIA_CONTENT_URI.buildUpon().appendPath(MEDIA_BASE_PATH).appendPath("" + newId).build();
		default:
			throw new IllegalArgumentException("Unknown URI");
		}
	}
    
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		int count;
		String table;
		switch (uriType) {
		case PROJECTS:
		case PROJECT_ID:
			table = StoryMakerDB.Schema.Projects.NAME;
			break;
		case LESSONS:
		case LESSON_ID:
			table = StoryMakerDB.Schema.Lessons.NAME;
			break;
		case MEDIA:
		case MEDIA_ID:
			table = StoryMakerDB.Schema.Media.NAME;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI");
		}
		count = mDB.getWritableDatabase(mPassphrase).delete(table, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		int count;
		String table;
		switch (uriType) {
		case PROJECTS:
		case PROJECT_ID:
			table = StoryMakerDB.Schema.Projects.NAME;
			break;
		case LESSONS:
		case LESSON_ID:
			table = StoryMakerDB.Schema.Lessons.NAME;
			break;
		case MEDIA:
		case MEDIA_ID:
			table = StoryMakerDB.Schema.Media.NAME;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI");
		}
		count = mDB.getWritableDatabase(mPassphrase).update(table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
