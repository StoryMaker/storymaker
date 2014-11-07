package org.storymaker.app.model;

import net.sqlcipher.database.SQLiteDatabase;
import org.storymaker.app.db.ProjectsProvider;
import org.storymaker.app.db.StoryMakerDB;
import android.net.Uri;

public class LessonTable extends Table {
    private final static String TAG = "LessonTable";
    
    public LessonTable() {
        
    }
    
    public LessonTable(SQLiteDatabase db) {
        super(db);
    }

    @Override
    protected String getTableName() {
        return StoryMakerDB.Schema.Lessons.NAME;
    }
    
    @Override
    protected String getIDColumnName() {
        return StoryMakerDB.Schema.Lessons.ID;
    }

    @Override
    protected Uri getURI() {
        return ProjectsProvider.LESSONS_CONTENT_URI;
    }

    @Override
    protected String getProviderBasePath() {
        return ProjectsProvider.LESSONS_BASE_PATH;
    }
}
