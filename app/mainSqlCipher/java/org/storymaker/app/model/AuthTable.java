package org.storymaker.app.model;

import timber.log.Timber;

import java.util.ArrayList;
import java.util.Date;

import net.sqlcipher.database.SQLiteDatabase;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import org.storymaker.app.db.ProjectsProvider;
import org.storymaker.app.db.StoryMakerDB;

public class AuthTable extends Table {
    private final static String TAG = "AuthTable";
    
    public AuthTable() {
        
    }

    /**
     * Default Table constructor that uses the direct db.
     * 
     * This should be used within DB Migrations and Model or Table classes
     * 
     * @param db
     */
    public AuthTable(SQLiteDatabase db) {
        super(db);
    }

    @Override
    protected String getTableName() {
        return StoryMakerDB.Schema.Auth.NAME;
    }
    
    @Override
    protected String getIDColumnName() {
        return StoryMakerDB.Schema.Auth.ID;
    }

    @Override
    protected Uri getURI() {
        return ProjectsProvider.AUTH_CONTENT_URI;
    }

    @Override
    protected String getProviderBasePath() {
        return ProjectsProvider.AUTH_BASE_PATH;
    }

    /**
     *  get result array for all rows with the specified site 
     * @param context
     * @param site
     * @return
     */
    public Auth[] getAuthsAsArray(Context context, String site) {
        ArrayList<Auth> auths = getAuthsAsList(context, site);
        return auths.toArray(new Auth[] {});
    }

    /**
     *  get result cursor for all rows with the specified site 
     * @param context
     * @param site
     * @return
     */
    public Cursor getAuthsAsCursor(Context context, String site) {
        String selection = StoryMakerDB.Schema.Auth.COL_SITE + "=?";
        String[] selectionArgs = new String[] { "" + site };
        String orderBy = StoryMakerDB.Schema.Auth.ID;
        if (mDB == null) {
            return context.getContentResolver().query(ProjectsProvider.AUTH_CONTENT_URI, null, selection, selectionArgs, orderBy);
        } else {
            return mDB.query(getTableName(), null, selection, selectionArgs, null, null, orderBy);
        }
    }

    /**
     *  get result list for all rows with the specified site 
     * @param context
     * @param site
     * @return
     */
    public ArrayList<Auth> getAuthsAsList(Context context, String site) {
        Cursor cursor = getAuthsAsCursor(context, site);
        ArrayList<Auth> auths = new ArrayList<Auth>();

        // this needs to handle a null cursor gracefully in case cacheword prevents db access
        if (cursor == null || !cursor.moveToFirst()) {
            Timber.d("either no credentials were found or db could not be accessed");
        } else {
            do {
                auths.add(new Auth(mDB, context, cursor));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return auths;
    }
    
    /**
     *  get result cursor for specified site/username
     * @param context
     * @param site
     * @param userName
     * @return
     */
    public Cursor getAsCursor(Context context, String site, String userName) {
        String selection = StoryMakerDB.Schema.Auth.COL_SITE + "=? and " +
                           StoryMakerDB.Schema.Auth.COL_USER_NAME + "=?";
        String[] selectionArgs = new String[] { "" + site, "" + userName };
        if (mDB == null) {
            return context.getContentResolver().query(ProjectsProvider.AUTH_CONTENT_URI, null, selection, selectionArgs, null);
        } else {
            return mDB.query(getTableName(), null, selection, selectionArgs, null, null, null);
        }
    }
    
    /**
     *  utility method to update last login time
     * @param context
     * @param site
     * @param userName
     */
    // FIXME this should probably be in the Auth class
    public void updateLastLogin(Context context, String site, String userName) {
        Cursor updCursor = getAsCursor(context, site, userName);
        if ((updCursor.getCount() > 0) && updCursor.moveToFirst()) {
            do {
                Auth updateAuth = new Auth(mDB, context, updCursor);
                updateAuth.setLastLogin(new Date()); // assumes method called at time of login
                updateAuth.update();
            } while (updCursor.moveToNext()); // is there a more elegant way to handle a single record result set?
        }
        updCursor.close();
    }
    
    // utility method to check if login credentials have expired
    public boolean getExpired(Context context, String site, String userName) {
        Cursor expCursor = getAsCursor(context, site, userName);
        if ((expCursor.getCount() > 0) && expCursor.moveToFirst()) {
            do {
                if (!expCursor.isNull(expCursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_EXPIRES))) {
                    // get current time
                    Date now = new Date();
                    if (now.getTime() < expCursor.getLong(expCursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_EXPIRES))) {
                        expCursor.close();
                        return false;
                    } else {
                        expCursor.close();
                        return true;
                    }
                } 
            } while (expCursor.moveToNext()); // is there a more elegant way to handle a single record result set?
        } 
        // assumes no value = no expiration
        expCursor.close();
        return false;
    }
    
    /**
     * @param context
     * @param site
     * @return first item in the list that matches this site, null if none do
     */
    public Auth getAuthDefault(Context context, String site) {
        ArrayList<Auth> results = getAuthsAsList(context, site);
        if (results.isEmpty()) {
            Log.w(TAG,"no username/password found for \"storymaker\"");
        } else if (results.size() > 1) {
            Log.e(TAG,results.size() + " usernames/passwords found for \"storymaker\"");
        } else {
            return results.get(0);
        }
        return null;
    }
}
