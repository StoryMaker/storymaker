package info.guardianproject.mrapp.model;

import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.net.Uri;

public class ProjectTable extends Table {
    @Override
    protected String getTableName() {
        return StoryMakerDB.Schema.Projects.NAME;
    }
    
    @Override
    protected String getIDColumnName() {
        return StoryMakerDB.Schema.Projects.ID;
    }

    @Override
    protected Uri getURI() {
        return ProjectsProvider.PROJECTS_CONTENT_URI;
    }

    @Override
    protected String getProviderBasePath() {
        return ProjectsProvider.PROJECTS_BASE_PATH;
    }
}
