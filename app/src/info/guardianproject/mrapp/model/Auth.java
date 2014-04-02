package info.guardianproject.mrapp.model;

import info.guardianproject.mrapp.db.StoryMakerDB;
import info.guardianproject.mrapp.db.ProjectsProvider;

import java.util.ArrayList;
import java.util.Date;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Auth extends Model {
    private static final String TAG = "Auth";
    
    protected String name;
    protected String site;
    protected String userName;
    protected String credentials;
    protected Date expires; // long stored in database as 8-bit int; null or 0 means no expiration
    protected Date lastLogin; // long stored in database as 8-bit int; null or 0 means never logged in
    
    public static final String STORYMAKER = "storymaker";

    // context constructor
    public Auth(Context context) {
        super(context);
    }

    // context + data constructor
    public Auth(Context context, int id, String name, String site, String userName, String credentials, Date expires, Date lastLogin ) {
        super(context);
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
    public Auth(Context context, Cursor cursor) {
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

    @Override
    protected Table getTable() {
        if (mTable == null) {
            mTable = new AuthTable();
        }
        
        return mTable;
    }
    
    // utility method to check if login credentials have expired
    public static boolean getExpired(Context context, String site, String userName) {
        Cursor expCursor = AuthTable.getAsCursor(context, site, userName);
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
    public static void updateLastLogin(Context context, String site, String userName) {
        Cursor updCursor = AuthTable.getAsCursor(context, site, userName);
        if ((updCursor.getCount() > 0) && updCursor.moveToFirst()) {
            do {
                Auth updateAuth = new Auth(context, updCursor);
                updateAuth.setLastLogin(new Date()); // assumes method called at time of login
                updateAuth.update();
            } while (updCursor.moveToNext()); // is there a more elegant way to handle a single record result set?
        }
        updCursor.close();
    }
    
    /**
     * @return true if credentials exist and are not passed their expiry date
     */
    public boolean credentialsAreValid() {
        return ((!(getUserName() == null) || getUserName() == "") 
                && (!(getCredentials() == null) || getCredentials() == "") 
                && !credentialsExpired());
    }
    
    /**
     * 
     * @return true if Now is passed the expiry date for the credentials
     */
    public boolean credentialsExpired() {
        if (getExpires() == null) {
            return false;
        } 
        return (new Date()).after(getExpires());
    }
    
    // build values set from current record
    @Override
    protected ContentValues getValues() {
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

    // FIXME testme, make sure we preserver the single record per site+user
    // FIXME make a db only version of this
    /**
     *  insert database row with current values, allowing only one record per site+username
     */
    @Override
    public void insert() {
        // check for duplicates and delete
        Cursor dupCursor = AuthTable.getAsCursor(context, site, userName);
        if ((dupCursor.getCount() > 0) && dupCursor.moveToFirst()) {
            do {
                (new Auth(context, dupCursor)).delete();
            } while (dupCursor.moveToNext()); // is there a more elegant way to handle a single record result set?
        }
        super.insert();
    }
    
    /**
     * @param context
     * @param site
     * @return first item in the list that matches this site, null if none do
     */
    public static Auth getAuthDefault(Context context, String site) {
        ArrayList<Auth> results = AuthTable.getAuthsAsList(context, site);
        if (results.isEmpty()) {
            Log.w(TAG,"no username/password found for \"storymaker\"");
        } else if (results.size() > 1) {
            Log.e(TAG,results.size() + " usernames/passwords found for \"storymaker\"");
        } else {
            return results.get(0);
        }
        return null;
    }

    // getters and setters
    
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
    