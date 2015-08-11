package org.storymaker.app.db;

import org.storymaker.app.model.AudioClipTable;
import org.storymaker.app.model.Auth;
import org.storymaker.app.model.AuthTable;
import org.storymaker.app.model.JobTable;
import org.storymaker.app.model.LessonTable;
import org.storymaker.app.model.MediaTable;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.ProjectTable;
import org.storymaker.app.model.PublishJobTable;
import org.storymaker.app.model.Scene;
import org.storymaker.app.model.Media;
import org.storymaker.app.model.Lesson;
import org.storymaker.app.model.SceneTable;
import org.storymaker.app.model.TagTable;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ProjectsProvider extends ContentProvider {
	private StoryMakerDB mDBHelper;
	private SQLiteDatabase mDB = null;
    private String mPassphrase = "foo"; //how and when do we set this??
    
    private static final String AUTHORITY = "org.storymaker.app.db.ProjectsProvider";
    public static final int PROJECTS = 101;
    public static final int PROJECT_ID = 111;
    public static final int LESSONS = 102;
    public static final int LESSON_ID = 112;
    public static final int MEDIA = 103;
    public static final int MEDIA_ID = 113;
    public static final int SCENES = 104;
    public static final int SCENE_ID = 114;
    public static final int AUTH = 105;
    public static final int AUTH_ID = 115;
    public static final int TAGS = 106;
    public static final int TAG_ID = 116;
    public static final int DISTINCT_TAGS = 126;
    public static final int DISTINCT_TAG_ID = 136;
    public static final int JOBS = 127;
    public static final int JOB_ID = 137;
    public static final int PUBLISH_JOBS = 128;
    public static final int PUBLISH_JOB_ID = 138;
    public static final int AUDIO_CLIPS = 129;
    public static final int AUDIO_CLIP_ID = 139;
    public static final String PROJECTS_BASE_PATH = "projects";
    public static final String SCENES_BASE_PATH = "scenes";
    public static final String LESSONS_BASE_PATH = "lessons";
    public static final String MEDIA_BASE_PATH = "media";
    public static final String AUTH_BASE_PATH = "auth";
    public static final String TAGS_BASE_PATH = "tags";
    public static final String DISTINCT_TAGS_BASE_PATH = "distinct_tags";
    public static final String JOBS_BASE_PATH = "jobs";
    public static final String PUBLISH_JOBS_BASE_PATH = "publish_jobs";
    public static final String AUDIO_CLIPS_BASE_PATH = "audio_clips";
    public static final Uri PROJECTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PROJECTS_BASE_PATH);
    public static final Uri SCENES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SCENES_BASE_PATH);
    public static final Uri LESSONS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + LESSONS_BASE_PATH);
    public static final Uri MEDIA_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + MEDIA_BASE_PATH);
    public static final Uri AUTH_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + AUTH_BASE_PATH);
    public static final Uri TAGS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TAGS_BASE_PATH);
    public static final Uri DISTINCT_TAGS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DISTINCT_TAGS_BASE_PATH);
    public static final Uri JOBS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + JOBS_BASE_PATH);
    public static final Uri PUBLISH_JOBS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PUBLISH_JOBS_BASE_PATH);
    public static final Uri AUDIO_CLIPS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + AUDIO_CLIPS_BASE_PATH);

    public static final String PROJECTS_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/projects";
    public static final String PROJECTS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/projects";
    public static final String SCENES_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/scenes";
    public static final String SCENES_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/scenes";
    public static final String LESSONS_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/lessons";
    public static final String LESSONS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/lessons";
    public static final String MEDIA_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/media";
    public static final String MEDIA_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/media";
    public static final String AUTH_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/auth";
    public static final String AUTH_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/auth";
    public static final String TAGS_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/tags";
    public static final String TAGS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/tags";
    public static final String DISTINCT_TAGS_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/distinct_tags";
    public static final String DISTINCT_TAGS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/distinct_tags";
    public static final String JOBS_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/jobs";
    public static final String JOBS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/jobs";
    public static final String PUBLISH_JOBS_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/publish_jobs";
    public static final String PUBLISH_JOBS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/publish_jobs";
    public static final String AUDIO_CLIPS_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/audio_clips";
    public static final String AUDIO_CLIPS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/audio_clips";
    
    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    
    static {
        sURIMatcher.addURI(AUTHORITY, PROJECTS_BASE_PATH, PROJECTS);
        sURIMatcher.addURI(AUTHORITY, PROJECTS_BASE_PATH + "/#", PROJECT_ID);
        sURIMatcher.addURI(AUTHORITY, SCENES_BASE_PATH, SCENES);
        sURIMatcher.addURI(AUTHORITY, SCENES_BASE_PATH + "/#", SCENE_ID);
        sURIMatcher.addURI(AUTHORITY, LESSONS_BASE_PATH, LESSONS);
        sURIMatcher.addURI(AUTHORITY, LESSONS_BASE_PATH + "/#", LESSON_ID);
        sURIMatcher.addURI(AUTHORITY, MEDIA_BASE_PATH, MEDIA);
        sURIMatcher.addURI(AUTHORITY, MEDIA_BASE_PATH + "/#", MEDIA_ID);
        sURIMatcher.addURI(AUTHORITY, AUTH_BASE_PATH, AUTH);
        sURIMatcher.addURI(AUTHORITY, AUTH_BASE_PATH + "/#", AUTH_ID);
        sURIMatcher.addURI(AUTHORITY, TAGS_BASE_PATH, TAGS);
        sURIMatcher.addURI(AUTHORITY, TAGS_BASE_PATH + "/#", TAG_ID);
        sURIMatcher.addURI(AUTHORITY, DISTINCT_TAGS_BASE_PATH, DISTINCT_TAGS);
        sURIMatcher.addURI(AUTHORITY, DISTINCT_TAGS_BASE_PATH + "/#", DISTINCT_TAG_ID);
        sURIMatcher.addURI(AUTHORITY, JOBS_BASE_PATH, JOBS);
        sURIMatcher.addURI(AUTHORITY, JOBS_BASE_PATH + "/#", JOB_ID);
        sURIMatcher.addURI(AUTHORITY, PUBLISH_JOBS_BASE_PATH, PUBLISH_JOBS);
        sURIMatcher.addURI(AUTHORITY, PUBLISH_JOBS_BASE_PATH + "/#", PUBLISH_JOB_ID);
        sURIMatcher.addURI(AUTHORITY, AUDIO_CLIPS_BASE_PATH, AUDIO_CLIPS);
        sURIMatcher.addURI(AUTHORITY, AUDIO_CLIPS_BASE_PATH + "/#", AUDIO_CLIP_ID);
    }
    
    @Override
    public boolean onCreate() {
        mDBHelper = new StoryMakerDB(getContext()); 
        return true;
    }
    
    private SQLiteDatabase getDB() {
        if (mDB == null) {
            mDB = mDBHelper.getWritableDatabase();
        }
        return mDB;
    }

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
        case PROJECT_ID:
            return (new ProjectTable(getDB())).queryOne(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case PROJECTS:
            return (new ProjectTable(getDB())).queryAll(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case SCENE_ID:
            return (new SceneTable(getDB())).queryOne(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case SCENES:
            return (new SceneTable(getDB())).queryAll(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case LESSON_ID:
            return (new LessonTable(getDB())).queryOne(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case LESSONS:
            return (new LessonTable(getDB())).queryAll(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case MEDIA_ID:
            return (new MediaTable(getDB())).queryOne(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case MEDIA:
            return (new MediaTable(getDB())).queryAll(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case AUTH_ID:
            return (new AuthTable(getDB())).queryOne(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case AUTH:
            return (new AuthTable(getDB())).queryAll(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case TAG_ID:
            return (new TagTable(getDB())).queryOne(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case TAGS:
            return (new TagTable(getDB())).queryAll(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case DISTINCT_TAG_ID:
            return (new TagTable(getDB())).queryOneDistinct(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case DISTINCT_TAGS:
            return (new TagTable(getDB())).queryAllDistinct(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case JOB_ID:
            return (new JobTable(getDB())).queryOne(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case JOBS:
            return (new JobTable(getDB())).queryAll(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case PUBLISH_JOB_ID:
            return (new PublishJobTable(getDB())).queryOne(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case PUBLISH_JOBS:
            return (new PublishJobTable(getDB())).queryAll(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case AUDIO_CLIP_ID:
            return (new AudioClipTable(getDB())).queryOne(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        case AUDIO_CLIPS:
            return (new AudioClipTable(getDB())).queryAll(getContext(), uri, projection, selection, selectionArgs, sortOrder);
        default:
            throw new IllegalArgumentException("Unknown URI");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long newId;
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
        case PROJECTS:
            return (new ProjectTable(getDB())).insert(getContext(), uri, values);
        case SCENES:
            return (new SceneTable(getDB())).insert(getContext(), uri, values);
        case LESSONS:
            return (new LessonTable(getDB())).insert(getContext(), uri, values);
        case MEDIA:
            return (new MediaTable(getDB())).insert(getContext(), uri, values);
        case AUTH:
            return (new AuthTable(getDB())).insert(getContext(), uri, values);
        case TAGS:
        case DISTINCT_TAGS:
            return (new TagTable(getDB())).insert(getContext(), uri, values);
        case JOBS:
            return (new JobTable(getDB())).insert(getContext(), uri, values);
        case PUBLISH_JOBS:
            return (new PublishJobTable(getDB())).insert(getContext(), uri, values);
        case AUDIO_CLIPS:
            return (new AudioClipTable(getDB())).insert(getContext(), uri, values);
        default:
            throw new IllegalArgumentException("Unknown URI");
        }
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
        case PROJECTS:
        case PROJECT_ID:
            return (new ProjectTable(getDB())).delete(getContext(), uri, selection, selectionArgs);
        case SCENES:
        case SCENE_ID:
            return (new SceneTable(getDB())).delete(getContext(), uri, selection, selectionArgs);
        case LESSONS:
        case LESSON_ID:
            return (new LessonTable(getDB())).delete(getContext(), uri, selection, selectionArgs);
        case MEDIA:
        case MEDIA_ID:
            return (new MediaTable(getDB())).delete(getContext(), uri, selection, selectionArgs);
        case AUTH:
        case AUTH_ID:
            return (new AuthTable(getDB())).delete(getContext(), uri, selection, selectionArgs);
        case TAGS:
        case TAG_ID:
        case DISTINCT_TAGS:
        case DISTINCT_TAG_ID:
            return (new TagTable(getDB())).delete(getContext(), uri, selection, selectionArgs);
        case JOBS:
        case JOB_ID:
            return (new JobTable(getDB())).delete(getContext(), uri, selection, selectionArgs);
        case PUBLISH_JOBS:
        case PUBLISH_JOB_ID:
            return (new PublishJobTable(getDB())).delete(getContext(), uri, selection, selectionArgs);
        case AUDIO_CLIPS:
        case AUDIO_CLIP_ID:
            return (new AudioClipTable(getDB())).delete(getContext(), uri, selection, selectionArgs);
        default:
            throw new IllegalArgumentException("Unknown URI");
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
        case PROJECTS:
        case PROJECT_ID:
            return (new ProjectTable(getDB())).update(getContext(), uri, values, selection, selectionArgs);
        case SCENES:
        case SCENE_ID:
            return (new SceneTable(getDB())).update(getContext(), uri, values, selection, selectionArgs);
        case LESSONS:
        case LESSON_ID:
            return (new LessonTable(getDB())).update(getContext(), uri, values, selection, selectionArgs);
        case MEDIA:
        case MEDIA_ID:
            return (new MediaTable(getDB())).update(getContext(), uri, values, selection, selectionArgs);
        case AUTH:
        case AUTH_ID:
            return (new AuthTable(getDB())).update(getContext(), uri, values, selection, selectionArgs);
        case TAGS:
        case TAG_ID:
        case DISTINCT_TAGS:
        case DISTINCT_TAG_ID:
            return (new TagTable(getDB())).update(getContext(), uri, values, selection, selectionArgs);
        case JOBS:
        case JOB_ID:
            return (new JobTable(getDB())).update(getContext(), uri, values, selection, selectionArgs);
        case PUBLISH_JOBS:
        case PUBLISH_JOB_ID:
            return (new PublishJobTable(getDB())).update(getContext(), uri, values, selection, selectionArgs);
        case AUDIO_CLIPS:
        case AUDIO_CLIP_ID:
            return (new AudioClipTable(getDB())).update(getContext(), uri, values, selection, selectionArgs);
        default:
            throw new IllegalArgumentException("Unknown URI");
        }
    }
}
