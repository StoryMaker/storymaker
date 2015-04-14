package org.storymaker.app.model;

import org.storymaker.app.db.StoryMakerDB;
import io.scal.secureshareui.model.Account;

import java.util.Date;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

public class Auth extends Model {
    private static final String TAG = "Auth";
    
    protected String name;
    protected String site;
    protected String userName;
    protected String credentials;
    protected String data; // a string blob of whatever this service needs to store
    protected Date expires; // long stored in database as 8-bit int; null or 0 means no expiration
    protected Date lastLogin; // long stored in database as 8-bit int; null or 0 means never logged in
    
    public static final String SITE_STORYMAKER = "storymaker"; // FIXME move them to a better place
    public static final String SITE_YOUTUBE = "youtube";
    public static final String SITE_SOUNDCLOUD = "soundcloud";
    public static final String SITE_FACEBOOK = "facebook";
    public static final String SITE_FLICKR = "flickr";
    public static final String SITE_SSH = "ssh";
    public static final String SITE_ARCHIVE = "archive";
    public static final String SITE_S3 = "s3";

    /**
     * Create a new, blank record via the Content Provider interface
     * 
     * @param context
     */
    public Auth(Context context) {
        super(context);
    }
    
    /**
     * Create a new, blank record via direct db access.  
     * 
     * This should be used within DB Migrations and Model or Table classes
     * 
     * @param db
     * @param context
     */
    public Auth(SQLiteDatabase db, Context context) {
        super(db, context);
    }

    /**
     * Create a Model object via direct params, using Content Provider interface.
     * 
     * @param context
     * @param id
     * @param name
     * @param site
     * @param userName
     * @param credentials
     * @param data
     * @param expires
     * @param lastLogin
     */
    public Auth(Context context, int id, String name, String site, String userName, String credentials, String data, Date expires, Date lastLogin ) {
        super(context);
        this.context = context;
        this.id = id;
        this.name = name;
        this.site = site;
        this.userName = userName;
        this.credentials = credentials;
        this.data = data;
        this.expires = expires;
        this.lastLogin = lastLogin;
    }
    
    /**
     * Create a Model object via direct params, except for auto-incremented primary key, using Content Provider interface.
     * 
     * @param context
     * @param name
     * @param site
     * @param userName
     * @param credentials
     * @param data
     * @param expires
     * @param lastLogin
     */
    public Auth(Context context, String name, String site, String userName, String credentials, String data, Date expires, Date lastLogin ) {
        super(context);
        this.context = context;
        this.name = name;
        this.site = site;
        this.userName = userName;
        this.credentials = credentials;
        this.data = data;
        this.expires = expires;
        this.lastLogin = lastLogin;
    }
    
    /**
     * Create a Model object via direct params via direct db access.
     * 
     * This should be used within DB Migrations and Model or Table classes
     * 
     * @param db
     * @param context
     * @param id
     * @param name
     * @param site
     * @param userName
     * @param credentials
     * @param data
     * @param expires
     * @param lastLogin
     */
    public Auth(SQLiteDatabase db, Context context, int id, String name, String site, String userName, String credentials, String data, Date expires, Date lastLogin ) {
        this(context, id, name, site, userName, credentials, data, expires, lastLogin);
        this.mDB = db;
    }
    
    /**
     * Create a Model object via direct params, except for auto-incremented primary key, via direct db access.
     * 
     * This should be used within DB Migrations and Model or Table classes
     * 
     * @param db
     * @param context
     * @param name
     * @param site
     * @param userName
     * @param credentials
     * @param expires
     * @param lastLogin
     */
    public Auth(SQLiteDatabase db, Context context, String name, String site, String userName, String credentials, String data, Date expires, Date lastLogin ) {
        this(context, name, site, userName, credentials, data, expires, lastLogin);
        this.mDB = db;
    }

    /**
     * Inflate record from a cursor
     *  
     * @param context
     * @param cursor
     */
    public Auth(Context context, Cursor cursor) {
        this(context,
             cursor.getInt(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.ID)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_NAME)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_SITE)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_USER_NAME)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_CREDENTIALS)),
             cursor.getString(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_DATA)),
             (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_EXPIRES)) ?
               new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_EXPIRES))) : null),
             (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_LAST_LOGIN)) ?
               new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Auth.COL_LAST_LOGIN))) : null));
        // expires/last_login columns are nullable, need to avoid errors creating Date objects
    }

    /**
     * Default constructor to inflate record from a cursor via direct db access.  This should be used within DB Migrations and within an Model or Tabel classes
     * @param db
     * @param context
     */
    public Auth(SQLiteDatabase db, Context context, Cursor cursor) {
        this(context, cursor);
        this.mDB = db;
    }

    @Override
    protected Table getTable() {
        if (mTable == null) {
            mTable = new AuthTable(mDB);
        }
        
        return mTable;
    }
    
    /**
     * @return true if credentials exist
     */
    public boolean credentialsExist() {
        // TODO this needs to defer to the site package authenication validator method (TBD)
        return (!((getUserName() == null) || getUserName() == "") 
                && (!(getCredentials() == null) || getCredentials() == ""));
    }
    
    /**
     * @return true if credentials exist and are not passed their expire date
     */
    public boolean credentialsAreValid() 
    {
        // validation may eventually be handled differently for each site
        if (site.equals(SITE_STORYMAKER))
        {
            return (credentialsExist() && !credentialsExpired());
        }
        if (site.equals(SITE_YOUTUBE))
        {
            return (credentialsExist() && !credentialsExpired());
        }
        if (site.equals(SITE_SOUNDCLOUD))
        {
            return (credentialsExist() && !credentialsExpired());
        }
        if (site.equals(SITE_FACEBOOK))
        {
            return (credentialsExist() && !credentialsExpired());
        }
        if (site.equals(SITE_FLICKR))
        {
            return (credentialsExist() && !credentialsExpired());
        }
        
        // default
        return (credentialsExist() && !credentialsExpired());
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
        values.put(StoryMakerDB.Schema.Auth.COL_DATA, data);
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
        Cursor dupCursor = (new AuthTable(mDB)).getAsCursor(context, site, userName);
        if ((dupCursor.getCount() > 0) && dupCursor.moveToFirst()) {
            do {
                (new Auth(mDB, context, dupCursor)).delete();
            } while (dupCursor.moveToNext()); // is there a more elegant way to handle a single record result set?
        }
        super.insert();
        dupCursor.close();
    }
    
    public static boolean migrate(Context context, SQLiteDatabase db) // returns true/false
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String user = settings.getString("user",null);
        String pass = settings.getString("pass",null);
        
        if ((user != null) && (pass != null))
        {
            Auth storymakerAuth = new Auth(db,
                                           context,
                                           "StoryMaker.cc",
                                           Auth.SITE_STORYMAKER,
                                           user,
                                           pass,
                                           null,
                                           null,
                                           new Date());
            storymakerAuth.insert();
            return true;
        }

        return false;
    }
    
    public Account convertToAccountObject() {
        return new Account(this.id, this.name, this.site, this.userName, this.credentials, this.data, this.credentialsExist(), this.credentialsAreValid());
    }

    // getters and setters //////////////////////////
    
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
     * @param data, a string blob that a site can store arbitrary data in 
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * @return data, a string blob that a site can store arbitrary data in
     */
    public String getData() {
        return data;
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
    
