package info.guardianproject.mrapp.model;

import info.guardianproject.mrapp.db.StoryMakerDB;
import info.guardianproject.mrapp.db.ProjectsProvider;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Auth {
    private static final String TAG = "Auth";
    
    protected Context context;
    protected int id;
    protected String name;
    protected String site;
    protected String userName;
    protected String credentials;
    protected Date expires; // long stored in database as 8-bit int
    protected Date lastLogin; // long stored in database as 8-bit int

    // context constructor
    public Auth(Context context) {
        this.context = context;
    }

    // context + data constructor
    public Auth(Context context, 
                int id, 
                String name, 
                String site, 
                String userName, 
                String credentials, 
                Date expires, 
                Date lastLogin ) {
        super();
        this.context = context;
        this.id = id;
        this.name = name;
        this.site = site;
        this.userName = userName;
        this.credentials = credentials;
        this.expires = expires;
        this.lastLogin = lastLogin;
    }

    // context + cursor constructor
    public Auth(Context context, 
                Cursor cursor) {
        this(context,
             cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.ID)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_NAME)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_SITE)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_USER_NAME)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_CREDENTIALS)),
             (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_EXPIRES)) ?
               new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_EXPIRES))) : null),
             (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_LAST_LOGIN)) ?
               new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_LAST_LOGIN))) : null));
        // expires/last_login columns are nullable, need to avoid errors creating Date objects
    }

    // get result cursor for specified row id
    public static Cursor getAsCursor(Context context, 
                                     int id) {
        String selection = StoryMakerDB.Schema.Auth.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
        
        return context.getContentResolver().query(ProjectsProvider.AUTH_CONTENT_URI, 
                                                  null, 
                                                  selection,
                                                  selectionArgs, 
                                                  null);
    }

    // get result for specified row id
    public static Auth get(Context context, 
                           int id) {
        Cursor cursor = Auth.getAsCursor(context, id);
        Auth auth = null;
        if (cursor.moveToFirst()) {
            auth = new Auth(context, cursor);
        } 
        cursor.close();
        return auth;
    }
    
    // get result cursor for all rows
    public static Cursor getAllAsCursor(Context context) {
        return context.getContentResolver().query(ProjectsProvider.AUTH_CONTENT_URI, 
                                                  null, 
                                                  null, 
                                                  null, 
                                                  null);
    }

    // get result array for all rows
    public static ArrayList<Auth> getAllAsList(Context context) {
        ArrayList<Auth> auths = new ArrayList<Auth>();
        Cursor cursor = getAllAsCursor(context);
        if (cursor.moveToFirst()) {
            do {
                auths.add(new Auth(context, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return auths;
    }
    
    // utility method to check if login credentials have expired
    public static boolean getExpired(Context context, 
                                     String site, 
                                     String userName) {
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
    
    // utility method to update last login time
    public static void updateLastLogin(Context context, 
                                       String site, 
                                       String userName) {
        Cursor updCursor = getAsCursor(context, site, userName);
        if ((updCursor.getCount() > 0) && updCursor.moveToFirst()) {
            do {
                Auth updateAuth = new Auth(context, updCursor);
                updateAuth.setLastLogin(new Date()); // assumes method called at time of login
                updateAuth.update();
            } while (updCursor.moveToNext()); // is there a more elegant way to handle a single record result set?
        }
        updCursor.close();
    }

    // insert/update current record
    public void save() {
        Cursor cursor = getAsCursor(context, id);
        if (cursor.getCount() == 0) {
            cursor.close();
            insert();
        } else {
            cursor.close();
            update();            
        }
    }
    
    // build values set from current record
    private ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.Auth.COL_NAME, name);
        values.put(StoryMakerDB.Schema.Auth.COL_SITE, site);
        values.put(StoryMakerDB.Schema.Auth.COL_USER_NAME, userName);
        values.put(StoryMakerDB.Schema.Auth.COL_CREDENTIALS, credentials);
        if (expires != null) {
            values.put(StoryMakerDB.Schema.Auth.COL_EXPIRES, expires.getTime());
        }
        if (lastLogin != null) {
            values.put(StoryMakerDB.Schema.Auth.COL_LAST_LOGIN, lastLogin.getTime());
        }
        // store dates as longs(8-bit ints)
        // can't put null in values set, so only add entry if non-null
        return values;
    }
    
    // get result cursor for specified site/username
    public static Cursor getAsCursor(Context context, 
                                     String site, 
                                     String userName) {
        String selection = StoryMakerDB.Schema.Auth.COL_SITE + "=? and " +
                           StoryMakerDB.Schema.Auth.COL_USER_NAME + "=?";
        String[] selectionArgs = new String[] { "" + site, "" + userName };
        return context.getContentResolver().query(ProjectsProvider.AUTH_CONTENT_URI, 
                                                  null, 
                                                  selection,
                                                  selectionArgs, 
                                                  null);
    }

    // get result cursor for specified site/username 
    public static Auth get(Context context, 
                           String site, 
                           String userName) {
        Cursor cursor = Auth.getAsCursor(context, site, userName);
        Auth auth = null;
        if (cursor.moveToFirst()) {
            auth = new Auth(context, cursor);
        } 
        cursor.close();
        return auth;
    }

    // get result list for all rows with the specified site 
    public static ArrayList<Auth> getAuthsAsList(Context context, 
                                                 String site) {
        Cursor cursor = getAuthsAsCursor(context, site);
        ArrayList<Auth> auths = new ArrayList<Auth>();
        if (cursor.moveToFirst()) {
            do {
                auths.add(new Auth(context, cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return auths;
    }

    // get result array for all rows with the specified site 
    public static Auth[] getAuthsAsArray(Context context, 
                                         String site) {
        ArrayList<Auth> auths = getAuthsAsList(context, site);
        return auths.toArray(new Auth[] {});
    }

    // get result cursor for all rows with the specified site 
    public static Cursor getAuthsAsCursor(Context context, 
                                          String site) {
        String selection = StoryMakerDB.Schema.Auth.COL_SITE + "=?";
        String[] selectionArgs = new String[] { "" + site };
        String orderBy = StoryMakerDB.Schema.Auth.ID;
        return context.getContentResolver().query(ProjectsProvider.AUTH_CONTENT_URI, 
                                                  null, 
                                                  selection,
                                                  selectionArgs, 
                                                  orderBy);
    }

    // insert database row with current values
    private void insert() {
        // check for duplicates and delete
        Cursor dupCursor = getAsCursor(context, site, userName);
        if ((dupCursor.getCount() > 0) && dupCursor.moveToFirst()) {
            do {
                (new Auth(context, dupCursor)).delete();
            } while (dupCursor.moveToNext()); // is there a more elegant way to handle a single record result set?
        }
        ContentValues values = getValues();
        Uri uri = context.getContentResolver().insert(ProjectsProvider.AUTH_CONTENT_URI, values);
        String lastSegment = uri.getLastPathSegment();
        this.setId(Integer.parseInt(lastSegment));
        dupCursor.close();
    }
    
    // update database row with current values
    private void update() {
        Uri uri = ProjectsProvider.AUTH_CONTENT_URI.buildUpon().appendPath("" + id).build();
        String selection = StoryMakerDB.Schema.Auth.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
        ContentValues values = getValues();
        int count = context.getContentResolver().update(uri, 
                                                        values, 
                                                        selection, 
                                                        selectionArgs);
        // confirm update?
    }
    
    // delete database row with current values
    public void delete() {
        Uri uri = ProjectsProvider.AUTH_CONTENT_URI.buildUpon().appendPath("" + id).build();
        String selection = StoryMakerDB.Schema.Auth.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
        int count = context.getContentResolver().delete(uri, 
                                                        selection, 
                                                        selectionArgs);
        // confirm delete?
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

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return site
     */
    public String getSite() {
        return site;
    }

    /**
     * @param site
     */
    public void setSite(String site) {
        this.site = site;
    }

    /**
     * @return userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return credentials
     */
    public String getCredentials() {
        return credentials;
    }

    /**
     * @param credentials 
     */
    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    /**
     * @return expires
     */
    public Date getExpires() {
        return expires;
    }

    /**
     * @param expires 
     */
    public void setExpires(Date expires) {
        this.expires = expires;
    }

    /**
     * @return lastLogin
     */
    public Date getLastLogin() {
        return lastLogin;
    }

    /**
     * @param lastLogin 
     */
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
}
    