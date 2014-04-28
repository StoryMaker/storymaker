package info.guardianproject.mrapp.model;

import info.guardianproject.mrapp.db.StoryMakerDB;
import io.scal.secureshareui.model.PublishAccount;

import java.util.Date;

import net.sqlcipher.database.SQLiteDatabase;
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
    protected Date expires; // long stored in database as 8-bit int; null or 0 means no expiration
    protected Date lastLogin; // long stored in database as 8-bit int; null or 0 means never logged in
    
    public static final String STORYMAKER = "storymaker"; // FIXME homogenize these and move them to a better place
    public static final String SITE_YOUTUBE = "youtube";
    public static final String SITE_SOUNDCLOUD = "soundcloud";
    public static final String SITE_FACEBOOK = "facebook";
    public static final String SITE_FLICKR = "flickr";

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
     * @param expires
     * @param lastLogin
     */
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
     * @param expires
     * @param lastLogin
     */
    public Auth(SQLiteDatabase db, Context context, int id, String name, String site, String userName, String credentials, Date expires, Date lastLogin ) {
        this(context, id, name, site, userName, credentials, expires, lastLogin);
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
        return ((!(getUserName() == null) || getUserName() == "") 
                && (!(getCredentials() == null) || getCredentials() == ""));
    }
    
    /**
     * @return true if credentials exist and are not passed their expire date
     */
    public boolean credentialsAreValid() {
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
    
    public static boolean migrate(Context context, SQLiteDatabase db) // returns true/false
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String user = settings.getString("user",null);
        String pass = settings.getString("pass",null);
        
        if ((user != null) && (pass != null))
        {
            Auth storymakerAuth = new Auth(db,
                                           context,
                                           -1, // should be set to a real value by insert method // FIXME should make a second constructor to clean this up
                                           "StoryMaker.cc",
                                           Auth.STORYMAKER,
                                           user,
                                           pass,
                                           null,
                                           new Date());
            storymakerAuth.insert();
            return true;
        }

        return false;
    }
    
    public PublishAccount convertToPublishAccountObject() {
    	return new PublishAccount(Integer.toString(this.id), this.name, this.site, this.userName, this.credentials, this.credentialsExist(), this.credentialsAreValid());
    }
}
    