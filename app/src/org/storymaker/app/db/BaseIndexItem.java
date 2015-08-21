package org.storymaker.app.db;

import com.hannesdorfmann.sqlbrite.objectmapper.annotation.Column;
import com.hannesdorfmann.sqlbrite.objectmapper.annotation.ObjectMappable;

/**
 * Created by mnbogner on 8/20/15.
 */

@ObjectMappable
public class BaseIndexItem {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_THUMBNAILPATH = "thumbnailPath";

    public long id;
    @Column(COLUMN_TITLE) public String title;
    @Column(COLUMN_DESCRIPTION) public String description;
    @Column(COLUMN_THUMBNAILPATH) public String thumbnailPath;

    public BaseIndexItem() {

    }

    public BaseIndexItem(long id, String title, String description, String thumbnailPath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnailPath = thumbnailPath;
    }

    @Column(COLUMN_ID)
    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }
}
