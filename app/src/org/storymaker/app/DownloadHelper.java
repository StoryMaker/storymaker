package org.storymaker.app;

import android.content.Context;
import android.util.Log;

import com.hannesdorfmann.sqlbrite.dao.DaoManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.storymaker.app.db.ExpansionIndexItem;
import org.storymaker.app.db.ExpansionIndexItemDao;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import rx.functions.Action1;
import scal.io.liger.IndexManager;
import scal.io.liger.LigerDownloadManager;
import scal.io.liger.ZipHelper;

/**
 * Created by mnbogner on 8/25/15.
 */
public class DownloadHelper {

    boolean expansionFilesOk;
    boolean contentPacksOk;

    private static final String TAG = "DownloadHelper";

    private Context context;

    // db stuff
    private ExpansionIndexItemDao expansionIndexItemDao;

    // pass in dao for more predictable db interaction?
    public DownloadHelper(Context context, ExpansionIndexItemDao expansionIndexItemDao) {
        this.context = context;
        this.expansionIndexItemDao = expansionIndexItemDao;
    }

    public boolean checkAndDownload() {

        // set defaults
        expansionFilesOk = true;
        contentPacksOk = true;

        expansionFilesOk = checkAndDownloadExpansionFiles(context);

        final HashMap<String, scal.io.liger.model.ExpansionIndexItem> availableIndex = IndexManager.loadAvailableIdIndex(context);

        expansionIndexItemDao.getExpansionIndexItems().subscribe(new Action1<List<ExpansionIndexItem>>() {

            @Override
            public void call(List<ExpansionIndexItem> expansionIndexItems) {

                // for each db item:
                //  - if no longer in available index, remove from db
                //  - if db item is different, update
                //  - if db item is installed, download (and update)

                for (ExpansionIndexItem dbItem : expansionIndexItems) {

                    scal.io.liger.model.ExpansionIndexItem indexItem = availableIndex.get(dbItem.getExpansionId());

                    if (indexItem == null) {
                        Log.d(TAG, "item removed from available index. deleting obb file(s) and removing record from db for " + dbItem.getExpansionId());

                        // remove main file
                        String mainPath = IndexManager.buildFileAbsolutePath(dbItem.getExpansionId(), Constants.MAIN, dbItem.getExpansionFileVersion(), context);
                        new File(mainPath).delete();

                        // remove patch file (if it exists)
                        String patchPath = IndexManager.buildFileAbsolutePath(dbItem.getExpansionId(), Constants.PATCH, dbItem.getPatchFileVersion(), context);
                        if (new File(patchPath).exists()) {
                            new File(patchPath).delete();
                        }

                        // remove db record
                        expansionIndexItemDao.removeExpansionIndexItemByKey(dbItem.getId());
                    } else {

                        boolean updateFlag = false;

                        if (dbItem.getExpansionFileVersion() != indexItem.getExpansionFileVersion()) {
                            Log.d(TAG, "main file version update for " + dbItem.getExpansionId() + ": " + dbItem.getExpansionFileVersion() + " -> " + indexItem.getExpansionFileVersion());
                            updateFlag = true;
                        }
                        if (dbItem.getPatchFileVersion() != indexItem.getPatchFileVersion()) {
                            Log.d(TAG, "patch file version update for " + dbItem.getExpansionId() + ": " + dbItem.getPatchFileVersion() + " -> " + indexItem.getPatchFileVersion());
                            updateFlag = true;
                        }

                        if (updateFlag) {
                            if (dbItem.isInstalled()) {

                                Log.d(TAG, "need to download update for " + dbItem.getExpansionId());

                                // check installed file and download update if needed
                                dbItem.update(indexItem);

                                if (!checkAndDownloadContentPacks(dbItem)) {
                                    contentPacksOk = false;

                                    Log.d(TAG, "updating db record for " + dbItem.getExpansionId());

                                    // update the db record

                                    expansionIndexItemDao.addExpansionIndexItem(dbItem);
                                }

                            } else {

                                Log.d(TAG, "updating db record for " + dbItem.getExpansionId());

                                // just update the db record

                                dbItem.update(indexItem);
                                expansionIndexItemDao.addExpansionIndexItem(dbItem);
                            }
                        }
                    }
                }
            }
        });

        if (expansionFilesOk && contentPacksOk) {
            // everything is fine
            return true;
        } else {
            // something is being downloaded
            return false;
        }
    }

    public static boolean checkAndDownloadExpansionFiles(Context context) {

        boolean mainFileOk = true;
        boolean patchFileOk = true;
        boolean fileStateOk = true;

        String filePath = ZipHelper.getExpansionZipDirectory(context, Constants.MAIN, Constants.MAIN_VERSION);
        String fileName = ZipHelper.getExpansionZipFilename(context, Constants.MAIN, Constants.MAIN_VERSION);

        File expansionFile = new File(filePath + fileName);

        if (expansionFile.exists()) {
            // file exists, check size/hash (TODO: hash check)

            if (expansionFile.length() == 0) {
                Log.d("CHECK/DOWNLOAD", "MAIN EXPANSION FILE " + fileName + " IS A ZERO BYTE FILE ");
                mainFileOk = false;
            }

            if ((Constants.MAIN_SIZE > 0) && (Constants.MAIN_SIZE > expansionFile.length())) {
                Log.d("CHECK/DOWNLOAD", "MAIN EXPANSION FILE " + fileName + " IS TOO SMALL (" + expansionFile.length() + "/" + Constants.MAIN_SIZE + ")");
                mainFileOk = false;
            }

        } else {
            // file does not exist, flag for downloading

            Log.d("CHECK/DOWNLOAD", "MAIN EXPANSION FILE " + fileName + " DOES NOT EXIST ");
            mainFileOk = false;
        }

        if (mainFileOk) {
            Log.d("CHECK/DOWNLOAD", "MAIN EXPANSION FILE " + fileName + " CHECKS OUT, NO DOWNLOAD");
        } else {
            Log.d("CHECK/DOWNLOAD", "MAIN EXPANSION FILE " + fileName + " MUST BE DOWNLOADED");


            // TODO: should move this functionality into the storymaker download manager, but since it doesn't involve the db i'm leaving it alone for now


            final LigerDownloadManager expansionDownload = new LigerDownloadManager(Constants.MAIN, Constants.MAIN_VERSION, context);
            Thread expansionDownloadThread = new Thread(expansionDownload);

            expansionDownloadThread.start();

            // downloading a new main file, must clear ZipHelper cache
            ZipHelper.clearCache();

            fileStateOk = false;
        }

        // if the main file is newer than the patch file, remove the patch file rather than downloading
        if (Constants.PATCH_VERSION > 0) {
            if (Constants.PATCH_VERSION < Constants.MAIN_VERSION) {

                File obbDirectory = new File(ZipHelper.getObbFolderName(context));
                File fileDirectory = new File(ZipHelper.getFileFolderName(context));

                String nameFilter = Constants.PATCH + ".*." + context.getPackageName() + ".obb";

                Log.d("CHECK/DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + obbDirectory.getPath());

                WildcardFileFilter obbFileFilter = new WildcardFileFilter(nameFilter);
                for (File obbFile : FileUtils.listFiles(obbDirectory, obbFileFilter, null)) {
                    Log.d("CHECK/DOWNLOAD", "CLEANUP: FOUND " + obbFile.getPath() + ", DELETING");
                    FileUtils.deleteQuietly(obbFile);
                }

                Log.d("CHECK/DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + fileDirectory.getPath());

                WildcardFileFilter fileFileFilter = new WildcardFileFilter(nameFilter);
                for (File fileFile : FileUtils.listFiles(fileDirectory, fileFileFilter, null)) {
                    Log.d("CHECK/DOWNLOAD", "CLEANUP: FOUND " + fileFile.getPath() + ", DELETING");
                    FileUtils.deleteQuietly(fileFile);
                }
            } else {

                String patchName = ZipHelper.getExpansionZipFilename(context, Constants.PATCH, Constants.PATCH_VERSION);

                expansionFile = new File(filePath + patchName);

                if (expansionFile.exists()) {
                    // file exists, check size/hash (TODO: hash check)

                    if (expansionFile.length() == 0) {
                        Log.d("CHECK/DOWNLOAD", "EXPANSION FILE PATCH " + patchName + " IS A ZERO BYTE FILE ");
                        patchFileOk = false;
                    }

                    if ((Constants.PATCH_SIZE > 0) && (Constants.PATCH_SIZE > expansionFile.length())) {
                        Log.d("CHECK/DOWNLOAD", "EXPANSION FILE PATCH " + fileName + " IS TOO SMALL (" + expansionFile.length() + "/" + Constants.PATCH_SIZE + ")");
                        patchFileOk = false;
                    }

                } else {
                    // file does not exist, flag for downloading

                    Log.d("CHECK/DOWNLOAD", "EXPANSION FILE PATCH " + patchName + " DOES NOT EXIST ");
                    patchFileOk = false;
                }

                if (patchFileOk) {
                    Log.d("CHECK/DOWNLOAD", "EXPANSION FILE PATCH " + patchName + " CHECKS OUT, NO DOWNLOAD");
                } else {
                    Log.d("CHECK/DOWNLOAD", "EXPANSION FILE PATCH " + patchName + " MUST BE DOWNLOADED");

                    final LigerDownloadManager expansionDownload = new LigerDownloadManager(Constants.PATCH, Constants.PATCH_VERSION, context);
                    Thread expansionDownloadThread = new Thread(expansionDownload);

                    expansionDownloadThread.start();

                    // downloading a new patch file, must clear ZipHelper cache
                    ZipHelper.clearCache();

                    fileStateOk = false;
                }
            }
        }

        return fileStateOk;
    }

    public boolean checkAndDownloadContentPacks(ExpansionIndexItem installedItem) {

        boolean mainFileOk = true;
        boolean patchFileOk = true;
        boolean fileStateOk = true;

        String filePath = IndexManager.buildFilePath(context);
        String fileName = IndexManager.buildFileName(installedItem.getExpansionId(), Constants.MAIN, installedItem.getExpansionFileVersion());

        File expansionFile = new File(filePath, fileName);

        if (expansionFile.exists()) {
            // file exists, check size/hash (TODO: hash check)

            if (expansionFile.length() == 0) {
                Log.d("CHECK/DOWNLOAD", "CONTENT PACK FILE " + fileName + " IS A ZERO BYTE FILE ");
                mainFileOk = false;
            }

            if ((installedItem.getExpansionFileSize() > 0) && (installedItem.getExpansionFileSize() > expansionFile.length())) {
                Log.d("CHECK/DOWNLOAD", "CONTENT PACK FILE " + fileName + " IS TOO SMALL (" + expansionFile.length() + "/" + installedItem.getExpansionFileSize() + ")");
                mainFileOk = false;
            }

            // NOTE: unsure what to do in this state.  incomplete downloads should be .tmp or .part,
            //       so this is probably a broken file that should be deleted and redownloaded
        } else {
            // file does not exist, flag for downloading
            // (download process will handle .tmp and .part files)
            Log.d("CHECK/DOWNLOAD", "CONTENT PACK FILE " + fileName + " DOES NOT EXIST ");
            mainFileOk = false;
        }

        if (mainFileOk) {
            Log.d("CHECK/DOWNLOAD", "CONTENT PACK FILE " + fileName + " CHECKS OUT, NO DOWNLOAD");

        } else {
            Log.d("CHECK/DOWNLOAD", "CONTENT PACK FILE " + fileName + " MUST BE DOWNLOADED");

            final DownloadManager expansionDownload = new DownloadManager(fileName, installedItem, context, expansionIndexItemDao);
            Thread expansionDownloadThread = new Thread(expansionDownload);

            expansionDownloadThread.start();

            // downloading a new content pack file, must clear ZipHelper cache
            ZipHelper.clearCache();

            fileStateOk = false;
        }

        // if the main file is newer than the patch file, remove the patch file rather than downloading
        if (installedItem.getPatchFileVersion() != null) {
            if ((installedItem.getExpansionFileVersion() != null) &&
                    (Integer.parseInt(installedItem.getPatchFileVersion()) < Integer.parseInt(installedItem.getExpansionFileVersion()))) {

                File obbDirectory = new File(ZipHelper.getObbFolderName(context));
                File fileDirectory = new File(ZipHelper.getFileFolderName(context));

                String nameFilter = installedItem.getExpansionId() + "." + Constants.PATCH + "*" + ".obb";

                Log.d("CHECK/DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + obbDirectory.getPath());

                WildcardFileFilter obbFileFilter = new WildcardFileFilter(nameFilter);
                for (File obbFile : FileUtils.listFiles(obbDirectory, obbFileFilter, null)) {
                    Log.d("CHECK/DOWNLOAD", "CLEANUP: FOUND " + obbFile.getPath() + ", DELETING");
                    FileUtils.deleteQuietly(obbFile);
                }

                Log.d("CHECK/DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + fileDirectory.getPath());

                WildcardFileFilter fileFileFilter = new WildcardFileFilter(nameFilter);
                for (File fileFile : FileUtils.listFiles(fileDirectory, fileFileFilter, null)) {
                    Log.d("CHECK/DOWNLOAD", "CLEANUP: FOUND " + fileFile.getPath() + ", DELETING");
                    FileUtils.deleteQuietly(fileFile);
                }
            } else {

                String patchName = IndexManager.buildFileName(installedItem.getExpansionId(), Constants.PATCH, installedItem.getPatchFileVersion());

                expansionFile = new File(filePath, patchName);

                if (expansionFile.exists()) {
                    // file exists, check size/hash (TODO: hash check)

                    if (expansionFile.length() == 0) {
                        Log.d("CHECK/DOWNLOAD", "CONTENT PACK PATCH " + patchName + " IS A ZERO BYTE FILE ");
                        patchFileOk = false;
                    }

                    if ((installedItem.getPatchFileSize() > 0) && (installedItem.getPatchFileSize() > expansionFile.length())) {
                        Log.d("CHECK/DOWNLOAD", "CONTENT PACK PATCH " + patchName + " IS TOO SMALL (" + expansionFile.length() + "/" + installedItem.getPatchFileSize() + ")");
                        patchFileOk = false;
                    }

                    // NOTE: unsure what to do in this state.  incomplete downloads should be .tmp or .part,
                    //       so this is probably a broken file that should be deleted and redownloaded

                } else {
                    // file does not exist, flag for downloading
                    // (download process will handle .tmp and .part files)
                    Log.d("CHECK/DOWNLOAD", "CONTENT PACK PATCH " + patchName + " DOES NOT EXIST ");
                    patchFileOk = false;
                }

                if (patchFileOk) {
                    Log.d("CHECK/DOWNLOAD", "CONTENT PACK PATCH " + patchName + " CHECKS OUT, NO DOWNLOAD");


                } else {
                    Log.d("CHECK/DOWNLOAD", "CONTENT PACK PATCH " + patchName + " MUST BE DOWNLOADED");

                    final DownloadManager expansionDownload = new DownloadManager(patchName, installedItem, context, expansionIndexItemDao);
                    Thread expansionDownloadThread = new Thread(expansionDownload);

                    expansionDownloadThread.start();

                    // downloading a new content pack patch, must clear ZipHelper cache
                    ZipHelper.clearCache();

                    fileStateOk = false;
                }
            }
        }

        return fileStateOk;
    }
}
