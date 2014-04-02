package info.guardianproject.mrapp.model;

import java.util.ArrayList;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
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
    protected abstract Uri getURI();
    protected abstract String getProviderBasePath();
    

    public Cursor queryOne(Context context, SQLiteDatabase db, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(getTableName());
        queryBuilder.appendWhere(getIDColumnName() + "=" + uri.getLastPathSegment());
        
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    public Cursor queryAll(Context context, SQLiteDatabase db, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(getTableName());
        
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    public Uri insert(Context context, SQLiteDatabase db, Uri uri, ContentValues values) {
        long newId;
        newId = db.insertOrThrow(getTableName(), null, values);
        context.getContentResolver().notifyChange(uri, null);
        return getURI().buildUpon().appendPath(getProviderBasePath()).appendPath("" + newId).build();
    }
    
    public int delete(Context context, SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs) {
        int count = db.delete(getTableName(), selection, selectionArgs);
        context.getContentResolver().notifyChange(uri, null);
        return count;
    }

    public int update(Context context, SQLiteDatabase db, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        count = db.update(getTableName(), values, selection, selectionArgs);
        context.getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    // get result cursor for specified row id
    public Cursor getAsCursor(Context context, int id) {
        String selection = getIDColumnName() + "=?";
        String[] selectionArgs = new String[] { "" + id };
        ContentResolver resolver = context.getContentResolver();
        return resolver.query(getURI(), null, selection, selectionArgs, null);
    }

    // get result cursor for specified row id
    public Cursor getAsCursor(SQLiteDatabase db, Context context, int id) {
        String selection = getIDColumnName() + "=?";
        String[] selectionArgs = new String[] { "" + id };
        
        return queryOne(context, db, getURI(), null, selection, selectionArgs, null);
    }

    // get result array for all rows
    public ArrayList<? extends Model> getAllAsList(Context context) {
        ArrayList<? extends Model> models = null;
        Cursor cursor = getAllAsCursor(context);
        Model model = null;
        final String name = getTableName();
        
        if (name == (new AuthTable()).getTableName()) { // FIXME would love a way to interact with tables staticly 
            models = new ArrayList<Auth>();
        } else if (name == (new LessonTable()).getTableName()) {
            models = new ArrayList<Lesson>();
        } else if (name == (new MediaTable()).getTableName()) {
            models = new ArrayList<Media>();
        } else if (name == (new ProjectTable()).getTableName()) {
            models = new ArrayList<Project>();
        } else if (name == (new SceneTable()).getTableName()) {
            models = new ArrayList<Scene>();
        }
        
        if (cursor.moveToFirst()) {
            do {
                if (name == (new AuthTable()).getTableName()) {
                    model = new Auth(context, cursor);
                    ((ArrayList<Auth>)models).add((Auth)model); // FIXME ugly again
                } else if (name == (new LessonTable()).getTableName()) {
                    model = new Lesson(context);
                    ((ArrayList<Lesson>)models).add((Lesson)model);
                } else if (name == (new MediaTable()).getTableName()) {
                    model = new Media(context, cursor);
                    ((ArrayList<Media>)models).add((Media)model);
                } else if (name == (new ProjectTable()).getTableName()) {
                    model = new Project(context, cursor);
                    ((ArrayList<Project>)models).add((Project)model);
                } else if (name == (new SceneTable()).getTableName()) {
                    model = new Scene(context, cursor);
                    ((ArrayList<Scene>)models).add((Scene)model);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return models;
    }

    // get result for specified row id
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
            }
        } 
        cursor.close();
        return model;
    }

    // get result for specified row id
    public Model get(SQLiteDatabase db, Context context, int id) {
        Cursor cursor = getAsCursor(db, context, id);
        Model model = null;
        if (cursor.moveToFirst()) {
            model = new Auth(context, cursor);
        } 
        cursor.close();
        return model;
    }
    
    // get result cursor for all rows
    public Cursor getAllAsCursor(Context context) {
        return context.getContentResolver().query(getURI(), null, null, null, null);
    }
}
