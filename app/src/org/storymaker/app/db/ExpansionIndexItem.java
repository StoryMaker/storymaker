package org.storymaker.app.db;

import android.util.Log;

import com.hannesdorfmann.sqlbrite.objectmapper.annotation.Column;
import com.hannesdorfmann.sqlbrite.objectmapper.annotation.ObjectMappable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mnbogner on 8/20/15.
 */

@ObjectMappable
public class ExpansionIndexItem extends BaseIndexItem {

    public static final String TABLE_NAME = "ExpansionIndexItem";
    public static final String COLUMN_PACKAGENAME = "packageName";
    public static final String COLUMN_EXPANSIONID = "expansionId";
    public static final String COLUMN_PATCHORDER = "patchOrder";
    public static final String COLUMN_CONTENTTYPE = "contentType";
    public static final String COLUMN_EXPANSIONFILEURL = "expansionFileUrl";
    public static final String COLUMN_EXPANSIONFILEPATH = "expansionFilePath";
    public static final String COLUMN_EXPANSIONFILEVERSION = "expansionFileVersion";
    public static final String COLUMN_EXPANSIONFILESIZE = "expansionFileSize";
    public static final String COLUMN_EXPANSIONFILECHECKSUM = "expansionFileChecksum";
    public static final String COLUMN_PATCHFILEVERSION = "patchFileVersion";
    public static final String COLUMN_PATCHFILESIZE = "patchFileSize";
    public static final String COLUMN_PATCHFILECHECKSUM = "patchFileChecksum";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_WEBSITE = "website";
    public static final String COLUMN_DATEUPDATED = "dateUpdated";
    public static final String COLUMN_LANGUAGES = "languages";
    public static final String COLUMN_TAGS = "tags";
    public static final String COLUMN_INSTALLEDFLAG = "installedFlag";
    public static final String COLUMN_DOWNLOADFLAG = "downloadFlag";


    // required
    @Column(COLUMN_PACKAGENAME) public String packageName;
    @Column(COLUMN_EXPANSIONID) public String expansionId;
    @Column(COLUMN_PATCHORDER) public String patchOrder;
    @Column(COLUMN_CONTENTTYPE) public String contentType;
    @Column(COLUMN_EXPANSIONFILEURL) public String expansionFileUrl;
    @Column(COLUMN_EXPANSIONFILEPATH) public String expansionFilePath; // relative to Context.getExternalFilesDirs()

    // not optional, but need to handle nulls
    @Column(COLUMN_EXPANSIONFILEVERSION) public String expansionFileVersion;
    @Column(COLUMN_EXPANSIONFILESIZE) public long expansionFileSize;
    @Column(COLUMN_EXPANSIONFILECHECKSUM) public String expansionFileChecksum;

    // patch stuff, optional
    @Column(COLUMN_PATCHFILEVERSION) public String patchFileVersion;
    @Column(COLUMN_PATCHFILESIZE) public long patchFileSize;
    @Column(COLUMN_PATCHFILECHECKSUM) public String patchFileChecksum;

    // optional
    @Column(COLUMN_AUTHOR) public String author;
    @Column(COLUMN_WEBSITE) public String website;
    @Column(COLUMN_DATEUPDATED) public String dateUpdated;
    @Column(COLUMN_LANGUAGES) public String languages; // comma-delimited list, need access methods that will construct an ArrayList<String>
    @Column(COLUMN_TAGS) public String tags; // comma-delimited list, need access methods that will construct an ArrayList<String>

    // HashMap<String, String> extras; <- dropping this, don't know a good way to handle hash maps

    // for internal use
    @Column(COLUMN_INSTALLEDFLAG) public int installedFlag;
    @Column(COLUMN_DOWNLOADFLAG) public int downloadFlag;


    public ExpansionIndexItem() {
        super();

    }

    public ExpansionIndexItem(long id, String title, String description, String thumbnailPath, String packageName, String expansionId, String patchOrder, String contentType, String expansionFileUrl, String expansionFilePath, String expansionFileVersion, long expansionFileSize, String expansionFileChecksum, String patchFileVersion, long patchFileSize, String patchFileChecksum, String author, String website, String dateUpdated, String languages, String tags, int installedFlag, int downloadFlag) {
        super(id, title, description, thumbnailPath);
        this.packageName = packageName;
        this.expansionId = expansionId;
        this.patchOrder = patchOrder;
        this.contentType = contentType;
        this.expansionFileUrl = expansionFileUrl;
        this.expansionFilePath = expansionFilePath;
        this.expansionFileVersion = expansionFileVersion;
        this.expansionFileSize = expansionFileSize;
        this.expansionFileChecksum = expansionFileChecksum;
        this.patchFileVersion = patchFileVersion;
        this.patchFileSize = patchFileSize;
        this.patchFileChecksum = patchFileChecksum;
        this.author = author;
        this.website = website;
        this.dateUpdated = dateUpdated;
        this.languages = languages;
        this.tags = tags;
        this.installedFlag = installedFlag;
        this.downloadFlag = downloadFlag;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getExpansionId() {
        return expansionId;
    }

    public String getPatchOrder() {
        return patchOrder;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExpansionFileUrl() {
        return expansionFileUrl;
    }

    public String getExpansionFilePath() {
        return expansionFilePath;
    }

    public String getExpansionFileVersion() {
        return expansionFileVersion;
    }

    public long getExpansionFileSize() {
        return expansionFileSize;
    }

    public String getExpansionFileChecksum() {
        return expansionFileChecksum;
    }

    public String getPatchFileVersion() {
        return patchFileVersion;
    }

    public long getPatchFileSize() {
        return patchFileSize;
    }

    public String getPatchFileChecksum() {
        return patchFileChecksum;
    }

    public String getAuthor() {
        return author;
    }

    public String getWebsite() {
        return website;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public String getLanguages() {
        return languages;
    }

    public String getTags() {
        return tags;
    }

    public int getInstalledFlag() {
        return installedFlag;
    }

    public int getDownloadFlag() {
        return downloadFlag;
    }

    // sqlite doesn't support boolean columns, so provide an interface to fake it

    public void setInstalledFlag(boolean installedFlag) {
        if (installedFlag) {
            this.installedFlag = 1;
        } else {
            this.installedFlag = 0;
        }
    }

    public boolean isInstalled() {
        if (installedFlag > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void setDownloadFlag(boolean downloadFlag) {
        if (downloadFlag) {
            this.downloadFlag = 1;
        } else {
            this.downloadFlag = 0;
        }
    }

    public boolean isDownloading() {
        if (downloadFlag > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void update(scal.io.liger.model.ExpansionIndexItem item) {

        // update db item with values from available index item

        // leave id alone

        this.title = item.getTitle();
        this.description = item.getDescription();
        this.thumbnailPath = item.getThumbnailPath();
        this.packageName = item.getPackageName();
        this.expansionId = item.getExpansionId();
        this.patchOrder = item.getPatchOrder();
        this.contentType = item.getContentType();
        this.expansionFileUrl = item.getExpansionFileUrl();
        this.expansionFilePath = item.getExpansionFilePath();
        this.expansionFileVersion = item.getExpansionFileVersion();
        this.expansionFileSize = item.getExpansionFileSize();
        this.expansionFileChecksum = item.getExpansionFileChecksum();
        this.patchFileVersion = item.getPatchFileVersion();
        this.patchFileSize = item.getPatchFileSize();
        this.patchFileChecksum = item.getPatchFileChecksum();
        this.author = item.getAuthor();
        this.website = item.getWebsite();
        this.dateUpdated = item.getDateUpdated();

        String languageString = null;
        String tagString = null;

        if (item.getLanguages() != null) {
            languageString = item.getLanguages().toString();
            Log.d("RX_DB", "WHAT DOES THIS LOOK LIKE? " + languageString);
        }
        if (item.getTags() != null) {
            tagString = item.getTags().toString();
            Log.d("RX_DB", "WHAT DOES THIS LOOK LIKE? " + tagString);
        }

        this.languages = languageString;
        this.tags = tagString;

        // leave installed flag alone
        // leave download flag alone

    }
}
