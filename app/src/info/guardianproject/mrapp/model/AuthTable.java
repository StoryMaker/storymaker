package info.guardianproject.mrapp.model;

import java.util.ArrayList;

import net.sqlcipher.database.SQLiteDatabase;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;

public class AuthTable extends Table {

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
    


//    // get result for specified row id
//    public static Auth get(SQLiteDatabase db, Context context, int id) {
//        Model model = (new Table()).get(db, context, id);
////        Cursor cursor = Auth.getAsCursor(db, context, id);
////        Model model = null;
////        if (cursor.moveToFirst()) {
////            case getTableName():
////                switch "
////            model = new (context, cursor);
////        } 
////        cursor.close();
////        return model;
//    }

    // get result array for all rows with the specified site 
    public static Auth[] getAuthsAsArray(Context context, String site) {
        ArrayList<Auth> auths = getAuthsAsList(context, site);
        return auths.toArray(new Auth[] {});
    }

    // get result cursor for all rows with the specified site 
    public static Cursor getAuthsAsCursor(Context context, String site) {
        String selection = StoryMakerDB.Schema.Auth.COL_SITE + "=?";
        String[] selectionArgs = new String[] { "" + site };
        String orderBy = StoryMakerDB.Schema.Auth.ID;
        return context.getContentResolver().query(ProjectsProvider.AUTH_CONTENT_URI, null, selection, selectionArgs, orderBy);
    }

    // get result list for all rows with the specified site 
    public static ArrayList<Auth> getAuthsAsList(Context context, String site) {
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
    
    // get result cursor for specified site/username
    public static Cursor getAsCursor(Context context, String site, String userName) {
        String selection = StoryMakerDB.Schema.Auth.COL_SITE + "=? and " +
                           StoryMakerDB.Schema.Auth.COL_USER_NAME + "=?";
        String[] selectionArgs = new String[] { "" + site, "" + userName };
        return context.getContentResolver().query(ProjectsProvider.AUTH_CONTENT_URI, null, selection, selectionArgs, null);
    }
    
}
