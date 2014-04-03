package info.guardianproject.mrapp.model;

import java.util.ArrayList;
import java.util.Collections;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;

import info.guardianproject.mrapp.StoryMakerApp;
import info.guardianproject.mrapp.db.ProjectsProvider;
import info.guardianproject.mrapp.db.StoryMakerDB;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Project extends Model {
	final private String TAG = "Project";
    protected String title;
    protected String description;
    protected String thumbnailPath;
    protected int storyType;
    protected String templatePath;
    protected String section;
    protected String location;
    protected ArrayList<String> tags = new ArrayList<String>();
    
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
     */
    public Project(Context context, int id, String title, String thumbnailPath, int storyType, String templatePath) {
        super(context);
        this.id = id;
        this.title = title;
        this.thumbnailPath = thumbnailPath;
        this.storyType = storyType;
        this.templatePath = templatePath;
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
     */
    public Project(SQLiteDatabase db, Context context, int id, String title, String thumbnailPath, int storyType, String templatePath) {
        this(context, id, title, thumbnailPath, storyType, templatePath);
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
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_TEMPLATE_PATH)));

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
                        .getColumnIndex(StoryMakerDB.Schema.Projects.COL_TEMPLATE_PATH)));
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
        
        return values;
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
        scene.setProjectId(getId());
        scene.save();
        
        mSceneCount = Math.max((projectIndex+1), mSceneCount);
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
    
    public String getDescription() {
	     return this.description;
	}
	public void setDescription(String description) {
	     this.description = description;
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
    
	public String getSection() {
	     return this.section;
	}
	public void setSection(String section) {
	     this.section = section;
	}
	
	public String getLocation() {
		return this.location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String[] getTagsAsArray() {	
		return tags.toArray(new String[tags.size()]);
	}
	public void addTag(String tag) {
		this.tags.add(tag);
	}
	public void removeTag(String tag) {
		this.tags.remove(tag);
	}
	
    
    public static String getSimpleTemplateForMode (int storyMode)
    {
    	 String lang = StoryMakerApp.getCurrentLocale().getLanguage();

         String templateJsonPath = "story/templates/" + lang + "/simple/";
         
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
    
}
