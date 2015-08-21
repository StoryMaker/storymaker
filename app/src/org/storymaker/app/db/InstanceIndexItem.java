package org.storymaker.app.db;

import com.hannesdorfmann.sqlbrite.objectmapper.annotation.Column;
import com.hannesdorfmann.sqlbrite.objectmapper.annotation.ObjectMappable;

/**
 * Created by mnbogner on 8/20/15.
 */

@ObjectMappable
public class InstanceIndexItem extends BaseIndexItem {

    public static final String TABLE_NAME = "InstanceIndexItem";
    public static final String COLUMN_INSTANCEFILEPATH = "instanceFilePath";
    public static final String COLUMN_STORYCREATIONDATE = "storyCreationDate";
    public static final String COLUMN_STORYSAVEDATE = "storySaveDate";
    public static final String COLUMN_STORYTYPE = "storyType";
    public static final String COLUMN_LANGUAGE = "language";
    public static final String COLUMN_STORYPATHID = "storyPathId";
    public static final String COLUMN_STORYPATHPREREQUISITES = "storyPathPrerequisites";
    public static final String COLUMN_STORYCOMPLETIONDATE = "storyCompletionDate";

    @Column(COLUMN_INSTANCEFILEPATH) public String instanceFilePath;
    @Column(COLUMN_STORYCREATIONDATE) public long storyCreationDate;
    @Column(COLUMN_STORYSAVEDATE) public long storySaveDate;
    @Column(COLUMN_STORYTYPE) public String storyType;
    @Column(COLUMN_LANGUAGE) public String language;

    // additional fields for supporting sequences of lessons
    @Column(COLUMN_STORYPATHID) public String storyPathId;
    @Column(COLUMN_STORYPATHPREREQUISITES) public String storyPathPrerequisites; // comma-delimited list, need access methods that will construct an ArrayList<String>
    @Column(COLUMN_STORYCOMPLETIONDATE) public long storyCompletionDate;

    public InstanceIndexItem() {
        super();

    }

    public InstanceIndexItem(long id, String title, String description, String thumbnailPath, String instanceFilePath, long storyCreationDate, long storySaveDate, String storyType, String language, String storyPathId, String storyPathPrerequisites, long storyCompletionDate) {
        super(id, title, description, thumbnailPath);
        this.instanceFilePath = instanceFilePath;
        this.storyCreationDate = storyCreationDate;
        this.storySaveDate = storySaveDate;
        this.storyType = storyType;
        this.language = language;
        this.storyPathId = storyPathId;
        this.storyPathPrerequisites = storyPathPrerequisites;
        this.storyCompletionDate = storyCompletionDate;
    }

    public String getInstanceFilePath() {
        return instanceFilePath;
    }

    public long getStoryCreationDate() {
        return storyCreationDate;
    }

    public long getStorySaveDate() {
        return storySaveDate;
    }

    public String getStoryType() {
        return storyType;
    }

    public String getLanguage() {
        return language;
    }

    public String getStoryPathId() {
        return storyPathId;
    }

    public String getStoryPathPrerequisites() {
        return storyPathPrerequisites;
    }

    public long getStoryCompletionDate() {
        return storyCompletionDate;
    }
}
