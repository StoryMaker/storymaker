package info.guardianproject.mrapp.model;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.net.Uri;

public class MediaTable extends Table {
    @Override
    protected String getTableName() {
        return StoryMakerDB.Schema.Media.NAME;
    }
    
    @Override
    protected String getIDColumnName() {
        return StoryMakerDB.Schema.Media.ID;
    }

    @Override
    protected Uri getURI() {
        return ProjectsProvider.MEDIA_CONTENT_URI;
    }

    @Override
    protected String getProviderBasePath() {
        return ProjectsProvider.MEDIA_BASE_PATH;
    }

}
