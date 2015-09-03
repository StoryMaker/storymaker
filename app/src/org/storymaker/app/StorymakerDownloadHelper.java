package org.storymaker.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import scal.io.liger.LigerDownloadManager;
import scal.io.liger.StorymakerIndexManager;
import scal.io.liger.ZipHelper;
import scal.io.liger.model.sqlbrite.AvailableIndexItemDao;
import scal.io.liger.model.sqlbrite.ExpansionIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItemDao;
import scal.io.liger.model.sqlbrite.QueueItemDao;

/**
 * Created by mnbogner on 11/6/14.
 */
public class StorymakerDownloadHelper {

    private static final String TAG = "DownloadHelper";
    private static final Object waitObj = new Object();

    public static boolean checkAllFiles(Context context) {

        ArrayList<String> missingFiles = new ArrayList<String>();

        if (ZipHelper.getExpansionFileFolder(context, Constants.MAIN, Constants.MAIN_VERSION) == null) {
            missingFiles.add(ZipHelper.getExpansionZipFilename(context, Constants.MAIN, Constants.MAIN_VERSION));
        }

        // only check for patch file if version is newer than main file version
        if ((Constants.PATCH_VERSION > 0) && (Constants.PATCH_VERSION >= Constants.MAIN_VERSION)) {
            if (ZipHelper.getExpansionFileFolder(context, Constants.PATCH, Constants.PATCH_VERSION) == null) {
                missingFiles.add(ZipHelper.getExpansionZipFilename(context, Constants.PATCH, Constants.PATCH_VERSION));
            }
        }

        if (missingFiles.isEmpty()) {
            return true;
        } else {
            Log.e("CHECKING FILES", "THE FOLLOWING EXPECTED EXPANSION FILES ARE MISSING OR INCOMPLETE: " + missingFiles.toString());
            return false;
        }
    }

    // return extra digits for greater precision in notification
    public static int getDownloadPercent(Context context, String fileName, InstalledIndexItemDao installedDao) {
        float percentFloat = getDownloadProgress(context, fileName, installedDao);
        int percentInt = (int) (percentFloat * 1000);
        return percentInt;
    }

    public static float getDownloadProgress(Context context, String fileName, InstalledIndexItemDao installedDao) {

        //Log.d("DOWNLOAD", "CHECKING PROGRESS FOR " + fileName);

        long totalExpectedSize = 0;
        long totalCurrentSize = 0;
        boolean sizeUndefined = false;

        // omitting main and patch files for now

        // also currently omitting .part files...

        HashMap<String, ExpansionIndexItem> contentPacksMap = StorymakerIndexManager.loadInstalledFileIndex(context, installedDao);

        ExpansionIndexItem contentPack = contentPacksMap.get(fileName);

        if(contentPack != null) {
            File contentPackFile = null;

            if (fileName.contains(Constants.MAIN)) {
                contentPackFile = new File(StorymakerIndexManager.buildFileAbsolutePath(contentPack, Constants.MAIN, context));
            } else if (fileName.contains(Constants.PATCH)) {
                contentPackFile = new File(StorymakerIndexManager.buildFileAbsolutePath(contentPack, Constants.PATCH, context));
            } else {
                //Log.e("DOWNLOAD", "CAN'T DETERMINE IF " + fileName + " IS A MAIN OR PATCH FILE");
                sizeUndefined = true;
            }

            if ((contentPackFile == null) || (contentPack.getExpansionFileSize() == 0)) {
                // no size defined, can't evaluate
                //Log.e("DOWNLOAD", "NO FILE SIZE FOUND FOR " + fileName);
                sizeUndefined = true;
            } else {
                if (fileName.contains(Constants.MAIN)) {
                    totalExpectedSize = totalExpectedSize + contentPack.getExpansionFileSize();
                } else if (fileName.contains(Constants.PATCH)) {
                    totalExpectedSize = totalExpectedSize + contentPack.getPatchFileSize();
                } else {
                    //Log.e("DOWNLOAD", "CAN'T DETERMINE IF " + fileName + " IS A MAIN OR PATCH FILE");
                    sizeUndefined = true;
                }

                if (!contentPackFile.exists()) {
                    // actual file doesn't exist, check for temp file
                    File contentPackFileTemp = new File(contentPackFile.getPath() + ".tmp");

                    if (!contentPackFileTemp.exists()) {
                        // still no file, add nothing to current size
                    } else {
                        totalCurrentSize = totalCurrentSize + contentPackFileTemp.length();
                    }

                    // also check for partial file
                    File contentPackFilePart = new File(contentPackFile.getPath() + ".part");

                    if (!contentPackFilePart.exists()) {
                        // still no file, add nothing to current size
                    } else {
                        totalCurrentSize = totalCurrentSize + contentPackFilePart.length();
                    }
                } else {
                    totalCurrentSize = totalCurrentSize + contentPackFile.length();
                }
            }
        } else {
            // no file information found, can't evaluate
            //Log.e("DOWNLOAD", "NO METADATA FOUND FOR " + fileName);
            sizeUndefined = true;
        }

        if (sizeUndefined) {
            return -1;
        } else if (totalExpectedSize == 0) {
            //Log.e("CHECKING FILES", "TOTAL EXPECTED SIZE IS 0 BYTES (NO CURRENT DOWNLOADS?)");
            return -1;
        } else {
            //Log.d("CHECKING FILES", "CURRENT DOWNLOAD PROGRESS: " + totalCurrentSize + " BYTES OUT OF " + totalExpectedSize + " BYTES");
            return (float) totalCurrentSize / (float) totalExpectedSize;
        }
    }

    public static boolean checkAndDownload(Context context, AvailableIndexItemDao availableDao, InstalledIndexItemDao installedDao, QueueItemDao queueDao) {

        boolean expansionFilesOk = true;
        boolean contentPacksOk = true;

        // just call new method
        expansionFilesOk = checkAndDownloadNew(context);

        HashMap<String, ExpansionIndexItem> installedIndex = StorymakerIndexManager.loadInstalledIdIndex(context, installedDao);
        HashMap<String, ExpansionIndexItem> availableIndex = StorymakerIndexManager.loadAvailableIdIndex(context, availableDao);

        HashMap<String, ExpansionIndexItem> updatedIndex = new HashMap<String, ExpansionIndexItem>();
        ExpansionIndexItem tempIndexItem = null;
        boolean updateFlag = false;

        // loop through installed items to see if we need to update any of them
        for (String id : installedIndex.keySet()) {

            ExpansionIndexItem installedItem = installedIndex.get(id);
            ExpansionIndexItem availableItem = availableIndex.get(id);

            // if availableItem is null, this was likely removed from the available index and we should purge it from the installed and remove the obb file
            if (availableItem == null) {
                Log.d(TAG, "item removed from availabe index. deleting obb file and removing from isntalled index");
                updateFlag = true;
                String absPath = StorymakerIndexManager.buildFileAbsolutePath(installedItem, Constants.MAIN, context);
                new File(absPath).delete();

                absPath = StorymakerIndexManager.buildFileAbsolutePath(installedItem, Constants.PATCH, context);
                File file = new File(absPath);
                if (file.exists()) {
                    file.delete();
                }
            } else {
                tempIndexItem = updateItem(context, installedItem, availableItem); // DO STUFF

                // build list and update index once

                // DISABLING AUTOMATIC CHECK/DOWNLOAD OF CONTENT PACKS FOR NOW
                // CONFLICTS WITH USER-CONTROLLED CONFIRM/PAUSE/CANCEL ACTIONS
                // CONTENT PACKS WILL BE CHECKED/DOWNLOADED ON MENU CLICK

                if (tempIndexItem == null) {
                    // if (checkAndDownload(context, installedItem, installedDao, queueDao, false).size() > 0) {
                    //     contentPacksOk = false;
                    // }

                    updatedIndex.put(installedItem.getExpansionId(), installedItem);
                } else {
                    // if (checkAndDownload(context, tempIndexItem, installedDao, queueDao, false).size() > 0) {
                    //     contentPacksOk = false;
                    // }

                    updatedIndex.put(tempIndexItem.getExpansionId(), tempIndexItem);
                    updateFlag = true;
                }
            }
        }

        if (updateFlag) {
            // persist updated index
            StorymakerIndexManager.saveInstalledIndex(context, updatedIndex, installedDao);

            // need a better solution
            try {
                synchronized (waitObj) {
                    Log.d("WAITING", "PERSISTING INDEX WITH UPDATED VALUES");
                    waitObj.wait(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (expansionFilesOk && contentPacksOk) {
            // everything is fine
            return true;
        } else {
            // something is being downloaded
            return false;
        }
    }

    // TODO use HTTPS
    // TODO pickup Tor settings
    public static boolean checkExpansionFiles(Context context, String mainOrPatch, int version) {
        String expansionFilePath = ZipHelper.getExpansionFileFolder(context, mainOrPatch, version);

        if (expansionFilePath != null) {
            Log.d("DOWNLOAD", "EXPANSION FILE " + ZipHelper.getExpansionZipFilename(context, mainOrPatch, version) + " FOUND IN " + expansionFilePath);
            return true;
        } else {
            Log.d("DOWNLOAD", "EXPANSION FILE " + ZipHelper.getExpansionZipFilename(context, mainOrPatch, version) + " NOT FOUND");
            return false;
        }
    }

    // need to be able to check/download a single file (currently only supports content packs)
    public static ExpansionIndexItem updateItem(Context context, ExpansionIndexItem installedItem, ExpansionIndexItem availableItem) {

        boolean itemUpdated = false;

        // need to compare main and patch versions
        // update installed index for consistency

        if ((installedItem.getExpansionFileVersion() != null) &&
                (availableItem.getExpansionFileVersion() != null) &&
                (Integer.parseInt(availableItem.getExpansionFileVersion()) > Integer.parseInt(installedItem.getExpansionFileVersion()))) {
            Log.d("DOWNLOAD", "FOUND NEWER VERSION OF MAIN EXPANSION ITEM " + installedItem.getExpansionId() + " (" + availableItem.getExpansionFileVersion() + " vs. " + installedItem.getExpansionFileVersion() + ") UPDATING");
            installedItem.setExpansionFileVersion(availableItem.getExpansionFileVersion());
            itemUpdated = true;
        }

        // need to account for case where installed item has no defined patch version
        if (availableItem.getPatchFileVersion() != null) {
            if (installedItem.getPatchFileVersion() != null) {
                if (Integer.parseInt(availableItem.getPatchFileVersion()) > Integer.parseInt(installedItem.getPatchFileVersion())) {
                    Log.d("DOWNLOAD", "FOUND NEWER VERSION OF PATCH EXPANSION ITEM " + installedItem.getExpansionId() + " (" + availableItem.getPatchFileVersion() + " vs. " + installedItem.getPatchFileVersion() + ") UPDATING");
                    installedItem.setPatchFileVersion(availableItem.getPatchFileVersion());
                    itemUpdated = true;
                }
            } else {
                Log.d("DOWNLOAD", "FOUND NEWER VERSION OF PATCH EXPANSION ITEM " + installedItem.getExpansionId() + " (" + availableItem.getPatchFileVersion() + " vs. " + installedItem.getPatchFileVersion() + ") UPDATING");
                installedItem.setPatchFileVersion(availableItem.getPatchFileVersion());
                itemUpdated = true;
            }
        }

        ExpansionIndexItem tempItem = fixStats(installedItem, availableItem);

        if (tempItem != null) {
            Log.d("DOWNLOAD", "FOUND UPDATED STATS FOR EXPANSION ITEM " + installedItem.getExpansionId() + " UPDATING");
            installedItem = tempItem;
            itemUpdated = true;
        }

        // checkAndDownload(context, installedItem);

        if (itemUpdated) {
            return installedItem;
        } else {
            return null;
        }
    }

    public static boolean checkAndDownloadNew(Context context) {

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

    public static HashMap<String, Thread> checkAndDownload(Context context, ExpansionIndexItem installedItem, InstalledIndexItemDao installedDao, QueueItemDao queueDao, boolean confirmDownload) {

        boolean mainFileOk = true;
        boolean patchFileOk = true;
        boolean fileStateOk = true;

        String filePath = StorymakerIndexManager.buildFilePath(installedItem, context);
        String fileName = StorymakerIndexManager.buildFileName(installedItem, Constants.MAIN);

        HashMap<String, Thread> downloadThreads = new HashMap<String, Thread>();

        File expansionFile = new File(filePath + fileName);

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

            final StorymakerDownloadManager mainDownload = new StorymakerDownloadManager(fileName, installedItem, context, installedDao, queueDao, confirmDownload);
            Thread mainDownloadThread = new Thread(mainDownload);

            // Toast.makeText(context, "Starting download of " + installedItem.getExpansionId() + " content pack.", Toast.LENGTH_LONG).show(); // FIXME move to strings

            // expansionDownloadThread.start();

            downloadThreads.put(Constants.MAIN, mainDownloadThread);

            // downloading a new content pack file, must clear ZipHelper cache
            // ZipHelper.clearCache();

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

                String patchName = StorymakerIndexManager.buildFileName(installedItem, Constants.PATCH);

                expansionFile = new File(filePath + patchName);

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

                    final StorymakerDownloadManager patchDownload = new StorymakerDownloadManager(patchName, installedItem, context, installedDao, queueDao, confirmDownload);
                    Thread patchDownloadThread = new Thread(patchDownload);

                    // Toast.makeText(context, "Starting download of expansion file patch.", Toast.LENGTH_LONG).show(); // FIXME move to strings

                    // expansionDownloadThread.start();

                    downloadThreads.put(Constants.PATCH, patchDownloadThread);

                    // downloading a new content pack patch, must clear ZipHelper cache
                    // ZipHelper.clearCache();

                    fileStateOk = false;
                }
            }
        }

        // this may be confusing as we are starting threads that may not result in downloads

        boolean downloadStarted = false;

        Thread mainThread = downloadThreads.get(Constants.MAIN);

        if (mainThread != null) {
            Log.d("DOWNLOAD", "STOPPING THREAD " + mainThread.getId());
            mainThread.start();
            downloadStarted = true;
        }

        Thread patchThread = downloadThreads.get(Constants.PATCH);

        if (patchThread != null) {
            Log.d("DOWNLOAD", "STOPPING THREAD " + patchThread.getId());
            patchThread.start();
            downloadStarted = true;
        }

        if (downloadStarted) {
            ZipHelper.clearCache();
        }


        return downloadThreads;
    }

    public static ExpansionIndexItem fixStats(ExpansionIndexItem installedItem, ExpansionIndexItem availableItem) {

        ArrayList<String> updatedStats = new ArrayList<String>();

        // if size/hash don't match, defer to available index

        if ((availableItem.getExpansionFileSize() > 0) &&
                (installedItem.getExpansionFileSize() != availableItem.getExpansionFileSize())) {
            installedItem.setExpansionFileSize(availableItem.getExpansionFileSize());
            updatedStats.add("expansionFileSize");
        }

        if ((availableItem.getPatchFileSize() > 0) &&
                (installedItem.getPatchFileSize() != availableItem.getPatchFileSize())) {
            installedItem.setPatchFileSize(availableItem.getPatchFileSize());
            updatedStats.add("patchFileSize");
        }

        if (availableItem.getExpansionFileChecksum() != null) {
            if (installedItem.getExpansionFileChecksum() != null) {
                if (!installedItem.getExpansionFileChecksum().equals(availableItem.getExpansionFileChecksum())) {
                    installedItem.setExpansionFileChecksum(availableItem.getExpansionFileChecksum());
                    updatedStats.add("expansionFileChecksum");
                }
            } else {
                installedItem.setExpansionFileChecksum(availableItem.getExpansionFileChecksum());
                updatedStats.add("expansionFileChecksum");
            }
        }

        if (availableItem.getPatchFileChecksum() != null) {
            if (installedItem.getPatchFileChecksum() != null) {
                if (!installedItem.getPatchFileChecksum().equals(availableItem.getPatchFileChecksum())) {
                    installedItem.setPatchFileChecksum(availableItem.getPatchFileChecksum());
                    updatedStats.add("patchFileChecksum");
                }
            } else {
                installedItem.setPatchFileChecksum(availableItem.getPatchFileChecksum());
                updatedStats.add("patchFileChecksum");
            }
        }

        if (!updatedStats.isEmpty()) {
            Log.d("INDEX", "UPDATED STATS FOR " + installedItem.getExpansionId() + ": " + updatedStats.toString());
            return installedItem;
        } else {
            return null;
        }

    }
}