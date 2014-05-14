package info.guardianproject.mrapp.model;

import java.util.ArrayList;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public abstract class Table {
    protected abstract String getTableName();
    protected abstract String getIDColumnName(); // FIXME this should just be always _id, move it into a const in here
    protected abstract Uri getURI(); // FIXME rename to getProviderURI 
    protected abstract String getProviderBasePath();
    
    protected SQLiteDatabase mDB = null;

    public Table() {
        
    }
    
    public Table(SQLiteDatabase db) {
        mDB = db;
    }
    
    public Cursor queryOne(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(getTableName());
        queryBuilder.appendWhere(getIDColumnName() + "=" + uri.getLastPathSegment());
        
        Cursor cursor = queryBuilder.query(mDB, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    public Cursor queryAll(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(getTableName());
        
        Cursor cursor = queryBuilder.query(mDB, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }
    
    public Cursor queryOneDistinct(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(getTableName());
        queryBuilder.appendWhere(getIDColumnName() + "=" + uri.getLastPathSegment());
        queryBuilder.setDistinct(true); // "true" specifies distinct results
        
        Cursor cursor = queryBuilder.query(mDB, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    public Cursor queryAllDistinct(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(getTableName());
        queryBuilder.setDistinct(true); // "true" specifies distinct results
        
        Cursor cursor = queryBuilder.query(mDB, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    public Uri insert(Context context, Uri uri, ContentValues values) {
        long newId;
        newId = mDB.insertOrThrow(getTableName(), null, values);
        context.getContentResolver().notifyChange(uri, null);
        return getURI().buildUpon().appendPath(getProviderBasePath()).appendPath("" + newId).build();
    }
    
    public int delete(Context context, Uri uri, String selection, String[] selectionArgs) {
        int count = mDB.delete(getTableName(), selection, selectionArgs);
        context.getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    // FIXME we should probably strip this from non debug builds?
    /**
     * this exists fro debugging/testing, don't use it
     */
    public void debugPurgeTable() {
        mDB.delete(getTableName(), null, null);
    }

    public int update(Context context, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        count = mDB.update(getTableName(), values, selection, selectionArgs);
        context.getContentResolver().notifyChange(uri, null);
        return count;
    }

    /** 
     * get inflated model object for specified row id 
     */
    public Model get(Context context, int id) {
        Cursor cursor = getAsCursor(context, id);
        Model model = null;
        final String name = getTableName();
        if (cursor.moveToFirst()) {
            if (name == (new AuthTable()).getTableName()) {
                model = new Auth(context, cursor);
            } else if (name == (new LessonTable()).getTableName()) {
                model = new Lesson(context);
            } else if (name == (new MediaTable()).getTableName()) {
                model = new Media(context, cursor);
            } else if (name == (new ProjectTable()).getTableName()) {
                model = new Project(context, cursor);
            } else if (name == (new SceneTable()).getTableName()) {
                model = new Scene(context, cursor);
            } else if (name == (new JobTable()).getTableName()) {
                model = new Job(context, cursor);
            } else if (name == (new PublishJobTable()).getTableName()) {
                model = new PublishJob(context, cursor);
            }
        } 
        cursor.close();
        return model;
    }
    
    // get result cursor for specified row id
    public Cursor getAsCursor(Context context, int id) {
        String selection = getIDColumnName() + "=?";
        String[] selectionArgs = new String[] { "" + id };
        ContentResolver resolver = context.getContentResolver();
        if (mDB == null) {
            return resolver.query(getURI(), null, selection, selectionArgs, null);
        } else {
            return mDB.query(getTableName(), null, selection, selectionArgs, null, null, null); 
        }
    }
    
    // get result cursor for all rows
    public Cursor getAllAsCursor(Context context) {
        if (mDB == null) {
            return context.getContentResolver().query(getURI(), null, null, null, null);
        } else {
            return mDB.query(getTableName(), null, null, null, null, null, null);
        }
    }

    // get result array for all rows
    public ArrayList<? extends Model> getAllAsList(Context context) {
        Cursor cursor = getAllAsCursor(context);
        return _getAllAsList(context, cursor);
    }
    
    private ArrayList<? extends Model> _getAllAsList(Context context, Cursor cursor) {
        ArrayList<? extends Model> models = null;
        Model model = null;
        final String name = getTableName();
        
        if (name == (new AuthTable()).getTableName()) { // FIXME would love a way to interact with tables statically
            models = new ArrayList<Auth>();
        } else if (name == (new LessonTable()).getTableName()) {
            models = new ArrayList<Lesson>();
        } else if (name == (new MediaTable()).getTableName()) {
            models = new ArrayList<Media>();
        } else if (name == (new ProjectTable()).getTableName()) {
            models = new ArrayList<Project>();
        } else if (name == (new SceneTable()).getTableName()) {
            models = new ArrayList<Scene>();
        } else if (name == (new JobTable()).getTableName()) {
            models = new ArrayList<Job>();
        } else if (name == (new PublishJobTable()).getTableName()) {
            models = new ArrayList<PublishJob>();
        }
        
        if (cursor.moveToFirst()) {
            do {
                if (name == (new AuthTable()).getTableName()) {
                    model = new Auth(mDB, context, cursor);
                    ((ArrayList<Auth>)models).add((Auth)model); // FIXME ugly again
                } else if (name == (new LessonTable()).getTableName()) {
                    model = new Lesson(mDB, context);
                    ((ArrayList<Lesson>)models).add((Lesson)model);
                } else if (name == (new MediaTable()).getTableName()) {
                    model = new Media(mDB, context, cursor);
                    ((ArrayList<Media>)models).add((Media)model);
                } else if (name == (new ProjectTable()).getTableName()) {
                    model = new Project(mDB, context, cursor);
                    ((ArrayList<Project>)models).add((Project)model);
                } else if (name == (new SceneTable()).getTableName()) {
                    model = new Scene(mDB, context, cursor);
                    ((ArrayList<Scene>)models).add((Scene)model);
                } else if (name == (new JobTable()).getTableName()) {
                    model = new Job(mDB, context, cursor);
                    ((ArrayList<Job>)models).add((Job)model);
                } else if (name == (new PublishJobTable()).getTableName()) {
                    model = new PublishJob(mDB, context, cursor);
                    ((ArrayList<PublishJob>)models).add((PublishJob)model);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return models;
    }
}
