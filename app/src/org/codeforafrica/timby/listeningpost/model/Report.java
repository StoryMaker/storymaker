package org.codeforafrica.timby.listeningpost.model;

import java.util.ArrayList;

import org.codeforafrica.timby.listeningpost.db.ProjectsProvider;
import org.codeforafrica.timby.listeningpost.db.StoryMakerDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Report {
	final private String TAG = "Report";
    protected Context context;
    protected int id;
    protected String title;
    protected String _sector;
    protected String _issue;
    protected String _entity;
    protected String _description;
    protected String _location;
    protected String _serverid;
    protected String _date;
    protected String _exported;
    protected String _synced;
    
    
    public Report(Context context, int id, String title, String _sector, String _issue,String _entity, String _description, String _location, String _serverid, String _date, String _exported, String _synced) {
        super();
        this.context = context;
        this.id = id;
        this.title = title;
        this._sector = _sector;
        this._issue = _issue;
        this._entity = _entity;
        this._description = _description;
        this._location = _location;
        this._serverid = _serverid;
        this._date = _date;
        this._exported = _exported;
        this._synced = _synced;
    }

    public Report(Context context, Cursor cursor) {
        // FIXME use column id's directly to optimize this one schema stabilizes
        this(
                context,
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Reports.ID)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Reports.COL_TITLE)),
                cursor.getString(cursor
                         .getColumnIndex(StoryMakerDB.Schema.Reports.COL_SECTOR)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Reports.COL_ISSUE)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Reports.COL_ENTITY)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Reports.COL_DESCRIPTION)),
               cursor.getString(cursor
                          .getColumnIndex(StoryMakerDB.Schema.Reports.COL_LOCATION)),
              cursor.getString(cursor
                          .getColumnIndex(StoryMakerDB.Schema.Reports.COL_SERVERID)),
        		cursor.getString(cursor
        				.getColumnIndex(StoryMakerDB.Schema.Reports.COL_DATE)),
        		cursor.getString(cursor
        				.getColumnIndex(StoryMakerDB.Schema.Reports.COL_EXPORTED)),
				cursor.getString(cursor
        				.getColumnIndex(StoryMakerDB.Schema.Reports.COL_SYNCED)));
               // cursor.close();

    }

	/***** Table level static methods *****/

    public static Cursor getAsCursor(Context context, int id) {
        String selection = StoryMakerDB.Schema.Reports.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
        
        return context.getContentResolver().query(
                ProjectsProvider.REPORTS_CONTENT_URI, null, selection,
                selectionArgs, null);
    }

    public static Report get(Context context, int id) {
        Cursor cursor = Report.getAsCursor(context, id);
        Report report = null;
        
        if (cursor.moveToFirst()) {
            report = new Report(context, cursor);
           
        } 
        
        cursor.close();
        return report;
    }

    public static Cursor getAllAsCursor(Context context) {
        return context.getContentResolver().query(
                ProjectsProvider.REPORTS_CONTENT_URI, null, null, null, StoryMakerDB.Schema.Reports.ID+" DESC");
    }
    public static Cursor getAllAsCursor_EI(Context context) {
    	String selection = StoryMakerDB.Schema.Reports.COL_EXPORTED + "=?";
        String[] selectionArgs = new String[] { "" + "0" };
        
        return context.getContentResolver().query(
                ProjectsProvider.REPORTS_CONTENT_URI, null, selection, selectionArgs, StoryMakerDB.Schema.Reports.ID+" DESC");
    }
    public static ArrayList<Report> getAllAsList_EI(Context context, String eI) {
        ArrayList<Report> reports = new ArrayList<Report>();
        Cursor cursor = getAllAsCursor_EI(context);
        
        if (cursor.moveToFirst()) {
            do {
                reports.add(new Report(context, cursor));
            } while (cursor.moveToNext());
        }        
        cursor.close();
        return reports;
    }
    
    public static ArrayList<Report> getAllAsList(Context context) {
        ArrayList<Report> reports = new ArrayList<Report>();
        Cursor cursor = getAllAsCursor(context);
        if (cursor.moveToFirst()) {
            do {
                reports.add(new Report(context, cursor));
            } while (cursor.moveToNext());
        }        
        cursor.close();
        return reports;
    }

    /***** Object level methods *****/

    public void save() {
    	Cursor cursor = getAsCursor(context, id);
    	if (cursor.getCount() == 0) {
    		insert();
    	} else {
    		update();
    	}
    	
    	cursor.close();
    }
    
    private ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.Reports.COL_TITLE, title);
        values.put(StoryMakerDB.Schema.Reports.COL_SECTOR, _sector);
        values.put(StoryMakerDB.Schema.Reports.COL_ISSUE, _issue);
        values.put(StoryMakerDB.Schema.Reports.COL_ENTITY, _entity);
        values.put(StoryMakerDB.Schema.Reports.COL_DESCRIPTION, _description);
        values.put(StoryMakerDB.Schema.Reports.COL_LOCATION, _location);
        values.put(StoryMakerDB.Schema.Reports.COL_SERVERID, _serverid);
        values.put(StoryMakerDB.Schema.Reports.COL_DATE, _date);
        values.put(StoryMakerDB.Schema.Reports.COL_EXPORTED, _exported);
        values.put(StoryMakerDB.Schema.Reports.COL_SYNCED, _synced);
        return values;
    }
    private void insert() {
        ContentValues values = getValues();
        Uri uri = context.getContentResolver().insert(
                ProjectsProvider.REPORTS_CONTENT_URI, values);
        String lastSegment = uri.getLastPathSegment();
        int newId = Integer.parseInt(lastSegment);
        this.setId(newId);
    }
    
    private void update() {
    	Uri uri = ProjectsProvider.REPORTS_CONTENT_URI.buildUpon().appendPath("" + id).build();
        String selection = StoryMakerDB.Schema.Reports.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
    	ContentValues values = getValues();
        int count = context.getContentResolver().update(
                uri, values, selection, selectionArgs);
        // FIXME make sure 1 row updated
    }
    
    public void delete() {
    	Uri uri = ProjectsProvider.REPORTS_CONTENT_URI.buildUpon().appendPath("" + id).build();
        String selection = StoryMakerDB.Schema.Reports.ID + "=?";
        String[] selectionArgs = new String[] { "" + id };
        int count = context.getContentResolver().delete(
                uri, selection, selectionArgs);
        Log.d(TAG, "deleted report: " + id + ", rows deleted: " + count);
        // FIXME make sure 1 row updated
        
        
        
        //TODO should we also delete all media files associated with this report?
        
    }

   
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    public String getIssue() {
        return _issue;
    }
    public String getSector() {
        return _sector;
    }
    public String getEntity() {
        return _entity;
    }
    public String getDescription() {
        return _description;
    }
    public String getLocation() {
        return _location;
    }
    public String getServerId() {
        return _serverid;
    }
    public String getDate() {
        return _date;
    }
    public String getExported() {
        return _exported;
    }
    public String getSynced() {
        return _synced;
    }
    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    public void setIssue(String _issue) {
        this._issue = _issue;
    }
    public void setSector(String _sector) {
        this._sector = _sector;
    }
    public void setDescription(String _description) {
        this._description = _description;
    }
    public void setEntity(String _entity) {
        this._entity = _entity;
    }
    public void setLocation(String _location) {
        this._location = _location;
    }
    public void setServerId(String _serverid) {
        this._serverid = _serverid;
    }
    public void setDate(String _date) {
        this._date = _date;
    }    
    public void setExported(String _exported) {
        this._exported = _exported;
    }
    public void setSynced(String _synced) {
        this._synced = _synced;
    }

}
