package org.storymaker.app.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hannesdorfmann.sqlbrite.dao.Dao;
import com.squareup.sqlbrite.SqlBrite;

import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by mnbogner on 8/20/15.
 */
public class InstanceIndexItemDao extends Dao {

    Random r = new Random();

    @Override
    public void createTable(SQLiteDatabase sqLiteDatabase) {

        CREATE_TABLE(InstanceIndexItem.TABLE_NAME,
                InstanceIndexItem.COLUMN_ID + " INTEGER", // change to auto-increment?
                InstanceIndexItem.COLUMN_TITLE + " TEXT",
                InstanceIndexItem.COLUMN_DESCRIPTION + " TEXT",
                InstanceIndexItem.COLUMN_THUMBNAILPATH + " TEXT",
                InstanceIndexItem.COLUMN_INSTANCEFILEPATH + " TEXT PRIMARY KEY NOT NULL",
                InstanceIndexItem.COLUMN_STORYCREATIONDATE + " INTEGER",
                InstanceIndexItem.COLUMN_STORYSAVEDATE + " INTEGER",
                InstanceIndexItem.COLUMN_STORYTYPE + " TEXT",
                InstanceIndexItem.COLUMN_LANGUAGE + " TEXT",
                InstanceIndexItem.COLUMN_STORYPATHID + " TEXT",
                InstanceIndexItem.COLUMN_STORYPATHPREREQUISITES + " TEXT",
                InstanceIndexItem.COLUMN_STORYCOMPLETIONDATE + " INTEGER")
                .execute(sqLiteDatabase);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        // foo

    }

    public Observable<List<InstanceIndexItem>> getInstanceIndexItems() {

        // select all rows

        return query(SELECT(InstanceIndexItem.COLUMN_ID,
                InstanceIndexItem.COLUMN_TITLE,
                InstanceIndexItem.COLUMN_DESCRIPTION,
                InstanceIndexItem.COLUMN_THUMBNAILPATH,
                InstanceIndexItem.COLUMN_INSTANCEFILEPATH,
                InstanceIndexItem.COLUMN_STORYCREATIONDATE,
                InstanceIndexItem.COLUMN_STORYSAVEDATE,
                InstanceIndexItem.COLUMN_STORYTYPE,
                InstanceIndexItem.COLUMN_LANGUAGE,
                InstanceIndexItem.COLUMN_STORYPATHID,
                InstanceIndexItem.COLUMN_STORYPATHPREREQUISITES,
                InstanceIndexItem.COLUMN_STORYCOMPLETIONDATE)
                .FROM(InstanceIndexItem.TABLE_NAME))
                .map(new Func1<SqlBrite.Query, List<InstanceIndexItem>>() {

                    @Override
                    public List<InstanceIndexItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return InstanceIndexItemMapper.list(cursor);
                    }
                });
    }

    public Observable<Long> addInstanceIndexItem(long id, String title, String description, String thumbnailPath, String instanceFilePath, long storyCreationDate, long storySaveDate, String storyType, String language, String storyPathId, String storyPathPrerequisites, long storyCompletionDate) {

        Observable<Long> rowId = null;

        ContentValues values = InstanceIndexItemMapper.contentValues()
                .id(r.nextLong())
                .title(title)
                .description(description)
                .thumbnailPath(thumbnailPath)
                .instanceFilePath(instanceFilePath)
                .storyCreationDate(storyCreationDate)
                .storySaveDate(storySaveDate)
                .storyType(storyType)
                .language(language)
                .storyPathId(storyPathId)
                .storyPathPrerequisites(storyPathPrerequisites)
                .storyCompletionDate(storyCompletionDate)
                .build();

        try {
            rowId = insert(InstanceIndexItem.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLiteConstraintException sce) {
            Log.d("RX_DB", "INSERT FAILED: " + sce.getMessage());
        }

        return rowId;
    }

    public Observable<Long> addInstanceIndexItem(scal.io.liger.model.InstanceIndexItem item) {

        String sppString = null;

        if (item.getStoryPathPrerequisites() != null) {
            sppString = item.getStoryPathPrerequisites().toString();
            Log.d("RX_DB", "WHAT DOES THIS LOOK LIKE? " + sppString);
        }

        return addInstanceIndexItem(r.nextLong(),
                item.getTitle(),
                item.getDescription(),
                item.getThumbnailPath(),
                item.getInstanceFilePath(),
                item.getStoryCreationDate(),
                item.getStorySaveDate(),
                item.getStoryType(),
                item.getLanguage(),
                item.getStoryPathId(),
                sppString,
                item.getStoryCompletionDate());
    }
}
