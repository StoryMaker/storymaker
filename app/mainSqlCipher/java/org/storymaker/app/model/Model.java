package org.storymaker.app.model;

import timber.log.Timber;

import org.storymaker.app.db.ProjectsProvider;
import net.sqlcipher.database.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public abstract class Model {

    protected Context context;

    protected int id;
    
    protected abstract ContentValues getValues();
    protected abstract Table getTable();
    protected Table mTable;
    
    protected SQLiteDatabase mDB = null;

    /**
     * Create a new, blank record via the Content Provider interface
     * 
     * @param context
     */
    protected Model(Context context) {
        this.context = context;  
    }
    
    /**
     * Create a new, blank record via direct db access.  
     * 
     * This should be used within DB Migrations and Model or Table classes
     *  
     * @param db
     * @param context
     */
    protected Model(SQLiteDatabase db, Context context) {
        this.context = context;
        mDB = db;
    }

    // getters and setters
    
    /**
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }
    
    // update database row with current values
    public void update() {
        ContentValues values = getValues();
        
        String selection = getTable().getIDColumnName() + "=?";
        String[] selectionArgs = new String[] { "" + id };
        
        if (mDB == null) {
            Uri uri = getTable().getURI().buildUpon().appendPath("" + id).build();
            int count = context.getContentResolver().update(uri, values, selection, selectionArgs);
        } else {
            int count = mDB.update(getTable().getTableName(), values, selection, selectionArgs);
        }
        // FIXME confirm update?
    }
    
    // delete database row with current values
    public void delete() {
        String selection = getTable().getIDColumnName() + "=?";
        String[] selectionArgs = new String[] { "" + id };
        
        if (mDB == null) {
            Uri uri = getTable().getURI().buildUpon().appendPath("" + id).build();
            int count = context.getContentResolver().delete(uri, selection, selectionArgs);
        } else {
            int count = mDB.delete(getTable().getTableName(), selection, selectionArgs);
        }
        
        // FIXME confirm delete?
    }
    
    // insert database row with current values
    public void insert() {
        ContentValues values = getValues();

        if (mDB == null) {
            Uri uri = context.getContentResolver().insert(getTable().getURI(), values);
            String lastSegment = uri.getLastPathSegment();
            this.setId(Integer.parseInt(lastSegment));
        } else {
            int newId = (int)mDB.insert(getTable().getTableName(), null, values);
            this.setId(newId);
        }
    }

    // insert/update current record
    public void save() {
        Cursor cursor = getTable().getAsCursor(context, id);
        if (cursor.getCount() == 0) {
            cursor.close();
            insert();
        } else {
            cursor.close();
            update();            
        }
    }
    
//    protected abstract static String getTableName();
//    
//    public static Cursor queryOne(Context context, SQLiteDatabase db, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
//        queryBuilder.setTables(getTableName());
//        queryBuilder.appendWhere(StoryMakerDB.Schema.Auth.ID + "=" + uri.getLastPathSegment());
//        
//        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
//        cursor.setNotificationUri(context.getContentResolver(), uri);
//        return cursor;
//    }
//
//    public static Cursor queryAll(Context context, SQLiteDatabase db, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
//        queryBuilder.setTables(tableName);
//        
//        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
//        cursor.setNotificationUri(context.getContentResolver(), uri);
//        return cursor;
//    }
//
//    public static Uri insert(Context context, SQLiteDatabase db, Uri uri, ContentValues values) {
//        long newId;
//        newId = db.insertOrThrow(tableName, null, values);
//        context.getContentResolver().notifyChange(uri, null);
//        return ProjectsProvider.AUTH_CONTENT_URI.buildUpon().appendPath(ProjectsProvider.AUTH_BASE_PATH).appendPath("" + newId).build();
//    }
//    
//    public static int delete(Context context, SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs) {
//        int count = db.delete(tableName, selection, selectionArgs);
//        context.getContentResolver().notifyChange(uri, null);
//        return count;
//    }
//
//    public static int update(Context context, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//        int count;
//        count = db.update(tableName, values, selection, selectionArgs);
//        context.getContentResolver().notifyChange(uri, null);
//        return count;
//    }
//    
//    // get result cursor for specified row id
//    public static Cursor getAsCursor(Context context, int id) {
//        String selection = StoryMakerDB.Schema.Auth.ID + "=?";
//        String[] selectionArgs = new String[] { "" + id };
//        
//        return context.getContentResolver().query(ProjectsProvider.AUTH_CONTENT_URI, null, selection, selectionArgs, null);
//    }
//
//    // get result cursor for specified row id
//    public static Cursor getAsCursor(SQLiteDatabase db, Context context, int id) {
//        String selection = StoryMakerDB.Schema.Auth.ID + "=?";
//        String[] selectionArgs = new String[] { "" + id };
//        
//        return queryOne(context, db, ProjectsProvider.AUTH_CONTENT_URI, null, selection, selectionArgs, null);
//    }
}
