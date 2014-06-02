package info.guardianproject.mrapp.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import net.sqlcipher.database.SQLiteDatabase;

import info.guardianproject.mrapp.StoryMakerApp;
import info.guardianproject.mrapp.Utils;
import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import info.guardianproject.mrapp.media.MediaProjectManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class Project extends Model {
	final private String TAG = "Project";
    protected String title;
    protected String thumbnailPath;
    protected int storyType;
    protected String templatePath;
    protected Date createdAt; // long stored in database as 8-bit int
    protected Date updatedAt; // long stored in database as 8-bit int
    protected String section;
    protected String location;
    
    public final static int STORY_TYPE_VIDEO = 0;
    public final static int STORY_TYPE_AUDIO = 1;
    public final static int STORY_TYPE_PHOTO = 2;
    public final static int STORY_TYPE_ESSAY = 3;
    
    // event, breaking-news, issue, feature. match category tag on server
    public final static String STORY_TEMPLATE_TYPE_EVENT = "event";
    public final static String STORY_TEMPLATE_TYPE_BREAKINGNEWS = "breaking-news";
    public final static String STORY_TEMPLATE_TYPE_ISSUE = "issue";
    public final static String STORY_TEMPLATE_TYPE_FEATURE = "feature";
    
    public int mSceneCount = -1;
    
    /**
     * Create new Project with a predetermined sceneCount via Provider Interface
     * 
     * @param context
     * @param sceneCount
     */
    public Project(Context context, int sceneCount) {
        super(context);
        mSceneCount = sceneCount;
    }
    
    /** 
     * Create new Project with a predetermined sceneCount via direct db access.
     * 
     * This should be used within DB Migrations and Model or Table classes
     *
     * @param db
     * @param context
     * @param sceneCount
     */
    public Project(SQLiteDatabase db, Context context, int sceneCount) {
        this(context, sceneCount);
        this.mDB = db;
    }

    /**
     * Create a Model object via direct params
     * 
     * @param context
     * @param id
     * @param title
     * @param thumbnailPath
     * @param storyType
     * @param templatePath
     * @param createdAt
     * @param updatedAt
     * @param section
     * @param location
     */
    public Project(Context context, int id, String title, String thumbnailPath, int storyType, String templatePath, Date createdAt, Date updatedAt, String section, String location) {
        super(context);
        this.id = id;
        this.title = title;
        this.thumbnailPath = thumbnailPath;
        this.storyType = storyType;
        this.templatePath = templatePath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.section = section;
        this.location = location;
    }
    
    /**
     * Create a Model object via direct params via direct db access.
     * 
     * This should be used within DB Migrations and Model or Table classes
     *
     * @param db
     * @param context
     * @param id
     * @param title
     * @param thumbnailPath
     * @param storyType
     * @param templatePath
     * @param createdAt
     * @param updatedAt
     * @param section
     * @param location
     */
    public Project(SQLiteDatabase db, Context context, int id, String title, String thumbnailPath, int storyType, String templatePath, Date createdAt, Date updatedAt, String section, String location) {
        this(context, id, title, thumbnailPath, storyType, templatePath, createdAt, updatedAt, section, location);
        this.mDB = db;
    }

    /**
     * Inflate record from a cursor via the Content Provider
     * 
     * @param context
     * @param cursor
     */
    public Project(Context context, Cursor cursor) {
        // FIXME use column id's directly to optimize this one schema stabilizes
        this(
                context,
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.ID)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_TITLE)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_THUMBNAIL_PATH)),
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_STORY_TYPE)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_TEMPLATE_PATH)),
                (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.COL_CREATED_AT)) ?
                        new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.COL_CREATED_AT))) : null),
                (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.COL_UPDATED_AT)) ?
                        new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.COL_UPDATED_AT))) : null),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_SECTION)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_LOCATION)));

        calculateMaxSceneCount();

    }
    
    /**
     * Inflate record from a cursor via direct db access.  
     * 
     * This should be used within DB Migrations and Model or Table classes
     * 
     * @param db
     * @param context
     * @param cursor
     */
    public Project(SQLiteDatabase db, Context context, Cursor cursor) {
        // FIXME use column id's directly to optimize this one schema stabilizes
        this(
                context,
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.ID)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_TITLE)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_THUMBNAIL_PATH)),
                cursor.getInt(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_STORY_TYPE)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_TEMPLATE_PATH)),
                (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.COL_CREATED_AT)) ?
                        new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.COL_CREATED_AT))) : null),
                (!cursor.isNull(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.COL_UPDATED_AT)) ?
                        new Date(cursor.getLong(cursor.getColumnIndex(StoryMakerDB.Schema.Projects.COL_UPDATED_AT))) : null),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_SECTION)),
                cursor.getString(cursor
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_LOCATION)));
        this.mDB = db;
        calculateMaxSceneCount(); // had to dupe the Project(context, cursor) constructor in here because of this call being fired before we set mDB 
    }
    
    public String getTemplateTag ()
    {
        String path = getTemplatePath();
        
        if (path != null)
        {
            if (path.contains("event"))
            {
                return STORY_TEMPLATE_TYPE_EVENT;
            }
            else if (path.contains("issue"))
            {
                return STORY_TEMPLATE_TYPE_ISSUE;
            }
            else if (path.contains("profile"))
            {
                return STORY_TEMPLATE_TYPE_FEATURE;
            }
            else if (path.contains("news"))
            {
                return STORY_TEMPLATE_TYPE_BREAKINGNEWS;
            }
        }
        
        return null;
    }

    @Override
    protected Table getTable() {
        if (mTable == null) {
            mTable = new ProjectTable(mDB);
        }
        
        return mTable;
    }
    
    private void calculateMaxSceneCount ()
    {
        Cursor cursor = getScenesAsCursor();
        
        int projectIndex = 0;
        
        mSceneCount = cursor.getCount();
        
        cursor.close();
        
    }
    
    protected ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(StoryMakerDB.Schema.Projects.COL_TITLE, title);
        values.put(StoryMakerDB.Schema.Projects.COL_THUMBNAIL_PATH, thumbnailPath);
        values.put(StoryMakerDB.Schema.Projects.COL_STORY_TYPE, storyType);
        values.put(StoryMakerDB.Schema.Projects.COL_TEMPLATE_PATH, templatePath);
        if (createdAt != null) {
            values.put(StoryMakerDB.Schema.Projects.COL_CREATED_AT, createdAt.getTime());
        }
        if (updatedAt != null) {
            values.put(StoryMakerDB.Schema.Projects.COL_UPDATED_AT, updatedAt.getTime());
        }
        values.put(StoryMakerDB.Schema.Projects.COL_SECTION, section);
        values.put(StoryMakerDB.Schema.Projects.COL_LOCATION, location);
        // store dates as longs(8-bit ints)
        // can't put null in values set, so only add entry if non-null
        
        return values;
    }
    
    // insert/update current record
    // need to set created at/updated at date
    @Override
    public void save() {
        Cursor cursor = getTable().getAsCursor(context, id);
        if (cursor.getCount() == 0) {
            cursor.close();
            setCreatedAt(new Date());
            insert();
        } else {
            cursor.close();
            setUpdatedAt(new Date());
            update();            
        }
    }
    
    public ArrayList<Scene> getScenesAsList() {
        Cursor cursor = getScenesAsCursor();
        
        ArrayList<Scene> scenes = new ArrayList<Scene>(mSceneCount);
        
        for (int i = 0; i < mSceneCount; i++) {
            scenes.add(null);
        }
        
        if (cursor.moveToFirst()) {
            do {
                Scene scene = new Scene(mDB, context, cursor);
                scenes.set(scene.getProjectIndex(), scene);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return scenes;
    }
    
    public Scene[] getScenesAsArray() {
        ArrayList<Scene> scenes = getScenesAsList();
        return scenes.toArray(new Scene[] {});
    }
    
    public Cursor getScenesAsCursor() {
        String selection = "project_id=?";
        String[] selectionArgs = new String[] { "" + getId() };
        String orderBy = "project_index";
        if (mDB == null) {
            return context.getContentResolver().query(ProjectsProvider.SCENES_CONTENT_URI, null, selection, selectionArgs, orderBy);
        } else {
            return mDB.query((new SceneTable(mDB)).getTableName(), null, selection, selectionArgs, null, null, orderBy);
        }
    }
    
    public ArrayList<Media> getMediaAsList() {
        ArrayList<Media> mediaList = null;
        mediaList = new ArrayList<Media>();
        for (Scene s : getScenesAsArray()) {
            mediaList.addAll(s.getMediaAsList());
        }
        return mediaList;
    }
    
    public String[] getMediaAsPathArray() {
        ArrayList<Media> mediaList = getMediaAsList();

        // purge nulls
        mediaList.removeAll(Collections.singleton(null));
        
        String[] pathArray = new String[mediaList.size()];
        for (int i = 0 ; i < mediaList.size() ; i++) {
            pathArray[i] = mediaList.get(i).getPath(); // how this makes me long for python
        }
        return pathArray;
    }

    /**
     * @param media insert this scene into the projects scene list at index 
     */
    public void setScene(int projectIndex, Scene scene) {
        scene.setProjectIndex(projectIndex);
        scene.setProjectId(getId()); // need created/updated?
        scene.save();
        
        mSceneCount = Math.max((projectIndex+1), mSceneCount);
    }
    
    public Cursor getTagsAsCursor() 
    {
        String selection = StoryMakerDB.Schema.Tags.COL_PROJECT_ID + " = ? ";
        String[] selectionArgs = new String[] { "" + getId() };

        if (mDB == null) 
            return context.getContentResolver().query(ProjectsProvider.TAGS_CONTENT_URI, null, selection, selectionArgs, null);
        else 
            return mDB.query((new TagTable(mDB)).getTableName(), null, selection, selectionArgs, null, null, null);
    }
    
    public ArrayList<Tag> getTagsAsList() 
    {
        ArrayList<Tag> tagList = new ArrayList<Tag>();
        
        Cursor cursor = getTagsAsCursor();
            
        if (cursor.moveToFirst()) 
        {
            do 
            {
                Tag tag = new Tag(mDB, context, cursor);
                tagList.add(tag);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tagList;
    }

    public boolean isTemplateStory() {
        return (templatePath != null) && !templatePath.equals(""); 
    }
    /***** getters and setters *****/

    /**
     * @return the id
     */
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

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the thumbnailPath
     */
    public String getThumbnailPath() {
        return thumbnailPath;
    }

    /**
     * @param thumbnailPath
     *            the thumbnailPath to set
     */
    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    /**
     * @return createdAt
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt 
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return updatedAt
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt 
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return section
     */
    public String getSection() {
        return section;
    }

    /**
     * @param section
     */
    public void setSection(String section) {
        this.section = section;
    }

    /**
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    public int getStoryType() {
        return storyType;
    }

    public void setStoryType(int storyType) {
        this.storyType = storyType;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String template) {
        this.templatePath = template;
    }
    
    public static String getSimpleTemplateForMode (Context context, int storyMode)
    {
    	 String lang = StoryMakerApp.getCurrentLessonsLocale().getLanguage();

         String templateJsonPath = "story/templates/" + lang + "/simple/";
         
         // if templates folder for this lang don't exist, fallback to english
         if (!Utils.assetExists(context, templateJsonPath + "audio_simple.json")) {
             templateJsonPath = "story/templates/en/simple/";
         }
         
         switch (storyMode)
         {
         
         case Project.STORY_TYPE_VIDEO:
             templateJsonPath += "video_simple.json";
             break;
         case Project.STORY_TYPE_AUDIO:
             templateJsonPath += "audio_simple.json";
             break;
         case Project.STORY_TYPE_PHOTO:
             templateJsonPath += "photo_simple.json";
             break;
         case Project.STORY_TYPE_ESSAY:
             templateJsonPath += "essay_simple.json";
             break;
             
         }
         
         return templateJsonPath;
    }
    
    public static ArrayList<Project> migrate(Context context, SQLiteDatabase db) // returns array of records that could not be migrated
    {
    	ArrayList<Project> projects = (ArrayList<Project>)(new ProjectTable(db)).getAllAsList(context); //cast necessary?
        ArrayList<Scene> allScenes = new ArrayList<Scene>();
    	ArrayList<Media> allMedia = new ArrayList<Media>();
    	ArrayList<Project> failed = new ArrayList<Project>();
    	boolean failure;
    	
    	for (Project project : projects)
    	{
    		failure = false;
    		
    		File projectDir = MediaProjectManager.getExternalProjectFolderOld(project, context);
    		if (projectDir.exists())
    		{
    			Date projectDate = new Date(projectDir.lastModified()); // creation time not stored with file
                project.setCreatedAt(projectDate);
                project.setUpdatedAt(projectDate);
    			
                allScenes = project.getScenesAsList();
                for (Scene scene : allScenes)
                {
                    if (!scene.migrate(project, projectDate))
                    {
                        Log.e("PROJECT MIGRATION", "failed to migrate scene " + scene.getId());
                        failure = true;
                    }
                }
                
    			allMedia = project.getMediaAsList();
    		    for (Media media : allMedia)
    		    {
                    if (!media.migrate(project, projectDate))
                    {
                    	Log.e("PROJECT MIGRATION", "failed to migrate media " + media.getId());
    			        failure = true;
                    }
    		    }
    		    
    		    if (!failure)
    		    {
   			        if (!MediaProjectManager.migrateProjectFiles(project, context))
   			        {
   			        	Log.e("PROJECT MIGRATION", "failed to migrate files");
   			    	    failure = true;
   			        }
    		    }
    		}
    		else
    		{
    			try {
    			    Log.e("PROJECT MIGRATION", projectDir.getCanonicalPath() + "does not exist");
    			} catch (Exception e) {
    				Log.e("PROJECT MIGRATION", "unexpected exception: " + e.getMessage());
    			}
    			failure = true;
    		}

    		if (!failure)
    		{ 
    			project.update();
    		}
    		else
    		{
    			Log.e("PROJECT MIGRATION", "failed to migrate project " + project.getId());
    			// if migration failed in some way, add project to result list for error handling upstream
    			failed.add(project);
    		}
    	}
    	
    	return null;
    }			
}
