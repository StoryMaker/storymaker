package org.storymaker.app;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hannesdorfmann.sqlbrite.dao.DaoManager;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.http.conn.ConnectTimeoutException;
import org.storymaker.app.db.ExpansionIndexItem;
import org.storymaker.app.db.ExpansionIndexItemDao;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import info.guardianproject.onionkit.ui.OrbotHelper;
import rx.functions.Action1;
import scal.io.liger.StorageHelper;
import scal.io.liger.ZipHelper;

/**
 * Created by mnbogner on 8/24/15.
 */
public class DownloadManager implements Runnable {

    boolean foundInQueue;

    private final static String TAG = "DownloadManager";

    // store in manager to skip db lookups
    private ExpansionIndexItem indexItem = null;


    // i think we need a clean db item to set/unset flags, DownloadHelper will update the actual content of the db item
    private ExpansionIndexItem cleanItem = null;


    private String fileName;
    private Context context;

    private NotificationManager nManager;

    private String mAppTitle = "StoryMaker";  // fix later to pull from context/preferences

    // db stuff
    private ExpansionIndexItemDao expansionIndexItemDao;

    // pass in dao for more predictable db interaction?
    public DownloadManager(String fileName, ExpansionIndexItem indexItem, Context context, ExpansionIndexItemDao expansionIndexItemDao) {
        this.fileName = fileName;
        this.indexItem = indexItem;
        this.context = context;
        this.expansionIndexItemDao = expansionIndexItemDao;
    }

    @Override
    public void run() {

        boolean downloadRequired = false;

        // NOTE: if whatever process was waiting for the download has died, but the download is still underway
        //       it may require a second click  or restart to get to this point.  if we end up here, with a
        //       finished download and no visible file progress, we'll manage the file and return without
        //       starting another download.

        if (checkDownloadFlag()) {
            Log.d("DOWNLOAD", "ANOTHER PROCESS IS ALREADY DOWNLOADING " + fileName + ", WILL NOT START DOWNLOAD");
        } else {
            Log.d("DOWNLOAD", "NO OTHER PROCESS IS DOWNLOADING " + fileName + ", CHECKING FOR FILES");

            File tempFile = new File(StorageHelper.getActualStorageDirectory(context), fileName + ".tmp");

            if (tempFile.exists()) {

                File partFile = managePartialFile(tempFile);

                if (partFile == null) {
                    Log.d("DOWNLOAD", tempFile.getPath().replace(".tmp", ".part") + " DOES NOT EXIST");
                    downloadRequired = true;
                } else {
                    Log.d("DOWNLOAD", partFile.getPath() + " FOUND, CHECKING");

                    // file exists, check size/hash (TODO: hash check)

                    if (partFile.length() == 0) {
                        Log.d("DOWNLOAD", partFile.getPath() + " IS A ZERO BYTE FILE ");
                        downloadRequired = true;
                    } else {
                        if (partFile.getPath().contains(Constants.MAIN)) {
                            if ((indexItem.getExpansionFileSize() > 0) && (indexItem.getExpansionFileSize() > partFile.length())) {
                                Log.d("DOWNLOAD", partFile.getPath() + " IS TOO SMALL (" + partFile.length() + "/" + indexItem.getExpansionFileSize() + ")");
                                downloadRequired = true;
                            } else {

                                // hash check?

                                // partial file is correct size, rename

                                File actualFile = new File(partFile.getPath().replace(".part", ""));

                                try {
                                    FileUtils.moveFile(partFile, actualFile);
                                    FileUtils.deleteQuietly(partFile);
                                    Log.d("DOWNLOAD", "MOVED COMPLETED FILE " + partFile.getPath() + " TO " + actualFile.getPath());
                                } catch (IOException ioe) {
                                    Log.e("DOWNLOAD", "FAILED TO MOVE COMPLETED FILE " + partFile.getPath() + " TO " + actualFile.getPath());
                                    ioe.printStackTrace();
                                    downloadRequired = true;
                                }
                            }
                        } else if (partFile.getPath().contains(Constants.PATCH)) {
                            if ((indexItem.getPatchFileSize() > 0) && (indexItem.getPatchFileSize() > partFile.length())) {
                                Log.d("DOWNLOAD", partFile.getPath() + " IS TOO SMALL (" + partFile.length() + "/" + indexItem.getPatchFileSize() + ")");
                                downloadRequired = true;
                            } else {

                                // hash check?

                                // partial file is correct size, rename

                                File actualFile = new File(partFile.getPath().replace(".part", ""));

                                try {
                                    FileUtils.moveFile(partFile, actualFile);
                                    FileUtils.deleteQuietly(partFile);
                                    Log.d("DOWNLOAD", "MOVED COMPLETED FILE " + partFile.getPath() + " TO " + actualFile.getPath());
                                } catch (IOException ioe) {
                                    Log.e("DOWNLOAD", "FAILED TO MOVE COMPLETED FILE " + partFile.getPath() + " TO " + actualFile.getPath());
                                    ioe.printStackTrace();
                                    downloadRequired = true;
                                }
                            }
                        } else {
                            Log.d("DOWNLOAD", "CAN'T DETERMINE FILE SIZE FOR " + partFile.getPath());
                            downloadRequired = true;
                        }
                    }
                }
            } else {
                Log.d("DOWNLOAD", tempFile.getPath() + " DOES NOT EXIST");
                downloadRequired = true;
            }
        }

        if (downloadRequired) {
            Log.d("DOWNLOAD", fileName + " MUST BE DOWNLOADED");
            download();
        } else {
            Log.d("DOWNLOAD", fileName + " WILL NOT BE DOWNLOADED");
        }

        return;
    }


    public boolean checkDownloadFlag() {

        // set default
        foundInQueue = false;

        final File checkFile = new File(StorageHelper.getActualStorageDirectory(context), fileName + ".tmp");

        // need to check if a download has already begun for this file
        // check if corresponding db item has been flagged

        expansionIndexItemDao.getExpansionIndexItem(indexItem).subscribe(new Action1<List<ExpansionIndexItem>>() {

            @Override
            public void call(List<org.storymaker.app.db.ExpansionIndexItem> expansionIndexItems) {

                if (expansionIndexItems.size() == 0) {
                    Log.e("DOWNLOAD", "NO RECORD FOUND IN THE DB FOR " + checkFile.getName());
                    foundInQueue = true;  // error state, returning true will prevent a download
                }

                if (expansionIndexItems.size() > 1) {
                    Log.e("DOWNLOAD", "MULTIPLE RECORDS FOUND IN THE DB FOR " + checkFile.getName());
                    foundInQueue = true;  // error state, returning true will prevent a download
                }

                cleanItem = expansionIndexItems.get(0);

                if (cleanItem.isDownloading()) {
                    if (checkFileProgress()) {
                        Log.d("QUEUE", "DOWNLOAD FLAG SET FOR " + checkFile.getName() + " AND DOWNLOAD PROGRESS OBSERVED, LEAVING DOWNLOAD FLAG");
                        foundInQueue = true;
                    } else {
                        Log.d("QUEUE", "DOWNLOAD FLAG SET FOR " + checkFile.getName() + " BUT NO DOWNLOAD PROGRESS OBSERVED, REMOVING DOWNLOAD FLAG");

                        cleanItem.setDownloadFlag(false);
                        expansionIndexItemDao.addExpansionIndexItem(cleanItem);
                    }
                }
            }
        });

        return foundInQueue;
    }

    public boolean checkFileProgress() {

        // not a great solution, but should indicate if file is being actively downloaded
        // only .tmp files should be download targets

        File checkFile = new File(StorageHelper.getActualStorageDirectory(context), fileName + ".tmp");
        if (checkFile.exists()) {
            long firstSize = checkFile.length();

            // wait for download progress
            try {
                synchronized (this) {
                    wait(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long secondSize = checkFile.length();

            if (secondSize > firstSize) {
                Log.d("DOWNLOAD", "DOWNLOAD IN PROGRESS FOR " + checkFile.getPath() + "(" + firstSize + " -> " + secondSize + ")");
                return true;
            } else {
                Log.d("DOWNLOAD", "NO DOWNLOAD PROGRESS FOR " + checkFile.getPath() + "(" + firstSize + " -> " + secondSize + ")");
                return false;
            }
        } else {
            Log.d("DOWNLOAD", "NO FILE FOUND FOR " + checkFile.getPath());
            return false;
        }
    }

    private File managePartialFile (File tempFile) {

        // return null if an error occurs
        // otherwise return .part file name

        File partFile = new File(tempFile.getPath().replace(".tmp", ".part"));

        // if there is no current partial file, rename .tmp file
        if (!partFile.exists()) {
            try {
                FileUtils.moveFile(tempFile, partFile);
                FileUtils.deleteQuietly(tempFile);
                Log.d("DOWNLOAD", "MOVED INCOMPLETE FILE " + tempFile.getPath() + " TO " + partFile.getPath());
                return partFile;
            } catch (IOException ioe) {
                Log.e("DOWNLOAD", "FAILED TO MOVE INCOMPLETE FILE " + tempFile.getPath() + " TO " + partFile.getPath());
                ioe.printStackTrace();
                return null;
            }
        } else {
            // if there is a current partial file, append .tmp file contents and remove .tmp file
            try {
                Log.d("APPEND", "MAKE FILE INPUT STREAM FOR " + tempFile.getPath());
                BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(tempFile));

                Log.d("APPEND", "MAKE FILE OUTPUT STREAM FOR " + partFile.getPath());
                FileOutputStream fileOutput = new FileOutputStream(partFile, true);

                byte[] buf = new byte[1024];
                int i;
                while ((i = fileInput.read(buf)) > 0) {
                    fileOutput.write(buf, 0, i);
                }

                // cleanup
                fileOutput.flush();
                fileOutput.close();
                fileOutput = null;

                fileInput.close();
                fileInput = null;

                FileUtils.deleteQuietly(tempFile);
                Log.d("DOWNLOAD", "APPENDED " + tempFile.getPath() + " TO " + partFile.getPath());
                return partFile;
            } catch (IOException ioe) {
                Log.e("DOWNLOAD", "FAILED TO APPENDED " + tempFile.getPath() + " TO " + partFile.getPath());
                ioe.printStackTrace();
                return null;
            }
        }
    }

    private void download() {

        String sourceUrl = indexItem.getExpansionFileUrl();
        String targetPathName = StorageHelper.getActualStorageDirectory(context).getPath();
        String targetFileName = fileName;

        Log.d("DOWNLOAD", "DOWNLOADING " + targetFileName + " FROM " + sourceUrl + " TO " + targetPathName);

        // set download flag (unset on failure)
        cleanItem.setDownloadFlag(true);
        expansionIndexItemDao.addExpansionIndexItem(cleanItem);

        try {
            File targetPath = new File(targetPathName);

            String nameFilter = "";

            if ((targetFileName.contains(Constants.MAIN)) && (targetFileName.contains(indexItem.getExpansionFileVersion()))) {
                nameFilter = targetFileName.replace(indexItem.getExpansionFileVersion(), "*") + "*.tmp";
            }
            if ((targetFileName.contains(Constants.PATCH)) && (targetFileName.contains(indexItem.getPatchFileVersion()))) {
                nameFilter = targetFileName.replace(indexItem.getPatchFileVersion(), "*") + "*.tmp";
            }

            Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + targetPath.getPath());

            WildcardFileFilter oldFileFilter = new WildcardFileFilter(nameFilter);
            for (File oldFile : FileUtils.listFiles(targetPath, oldFileFilter, null)) {
                Log.d("DOWNLOAD", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
                FileUtils.deleteQuietly(oldFile);
            }

            File targetFile = new File(targetPath, targetFileName + ".tmp");

            // if there is no connectivity, do not queue item (no longer seems to pause if connection is unavailable)
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();

            if ((ni != null) && (ni.isConnectedOrConnecting())) {

                if (context instanceof Activity) {
                    Utils.toastOnUiThread((Activity) context, "Starting download of " + indexItem.getTitle() + ".", false); // FIXME move to strings
                }

                // check preferences.  will also need to check whether tor is active within method
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                boolean useTor = settings.getBoolean("pusetor", false);
                boolean useManager = settings.getBoolean("pusedownloadmanager", false);

                if (useTor && useManager) {
                    Log.e("DOWNLOAD", "ANDROID DOWNLOAD MANAGER IS NOT COMPATABLE WITH TOR");

                    // download aborted, unset flag
                    cleanItem.setDownloadFlag(false);
                    expansionIndexItemDao.addExpansionIndexItem(cleanItem);

                    if (context instanceof Activity) {
                        Utils.toastOnUiThread((Activity) context, "Check settings, can't use download manager and tor", true); // FIXME move to strings
                    }
                } else if (useTor || !useManager) {
                    useStorymakerDownloader(useTor, Uri.parse(sourceUrl + targetFileName), mAppTitle + " content download", targetFileName, targetFile);
                } else {
                    Log.e("DOWNLOAD", "ANDROID DOWNLOAD MANAGER IS NOT YET SUPPORTED");

                    // download aborted, unset flag
                    cleanItem.setDownloadFlag(false);
                    expansionIndexItemDao.addExpansionIndexItem(cleanItem);

                    if (context instanceof Activity) {
                        Utils.toastOnUiThread((Activity) context, "Check settings, can't use download manager at this time", true); // FIXME move to strings
                    }
                }
            } else {
                Log.d("DOWNLOAD", "NO CONNECTION, NOT QUEUEING DOWNLOAD: " + sourceUrl + targetFileName + " -> " + targetFile.getPath());

                // download aborted, unset flag
                cleanItem.setDownloadFlag(false);
                expansionIndexItemDao.addExpansionIndexItem(cleanItem);

                if (context instanceof Activity) {
                    Utils.toastOnUiThread((Activity) context, "Check settings, no connection, can't start download", true); // FIXME move to strings
                }
            }
        } catch (Exception e) {
            Log.e("DOWNLOAD", "DOWNLOAD ERROR: " + sourceUrl + targetFileName + " -> " + e.getMessage());
            e.printStackTrace();

            // download aborted, unset flag
            cleanItem.setDownloadFlag(false);
            expansionIndexItemDao.addExpansionIndexItem(cleanItem);
        }
    }

    private void useStorymakerDownloader(boolean useTor, Uri uri, String title, String desc, File targetFile) {

        // set up notifications

        initNotificationManager();

        String nTag = indexItem.getExpansionId();
        int nId = 0;
        if (fileName.contains(Constants.MAIN)) {
            nId = Integer.parseInt(indexItem.getExpansionFileVersion());
        } else if (fileName.contains(Constants.PATCH)) {
            nId = Integer.parseInt(indexItem.getPatchFileVersion());
        }

        OkHttpClient httpClient = new OkHttpClient();

        // check tor settings and configure proxy if needed
        if (useTor) {
            if (checkTor()) {
                Log.d("DOWNLOAD/TOR", "DOWNLOAD WITH TOR PROXY: " + Constants.TOR_PROXY_HOST + "/" + Constants.TOR_PROXY_PORT);

                SocketAddress torSocket = new InetSocketAddress(Constants.TOR_PROXY_HOST, Constants.TOR_PROXY_PORT);
                Proxy torProxy = new Proxy(Proxy.Type.HTTP, torSocket);
                httpClient.setProxy(torProxy);
            } else {
                Log.e("DOWNLOAD/TOR", "CANNOT DOWNLOAD WITH TOR, TOR IS NOT ACTIVE");

                if (context instanceof Activity) {
                    Utils.toastOnUiThread((Activity) context, "Check settings, can't use tor if orbot isn't running", true); // FIXME move to strings
                }

                // download aborted, unset flag
                cleanItem.setDownloadFlag(false);
                expansionIndexItemDao.addExpansionIndexItem(cleanItem);

                return;
            }
        }

        // disable attempts to retry (more retries ties up connection and prevents failure handling)
        httpClient.setRetryOnConnectionFailure(false);

        // set modest timeout (longer timeout ties up connection and prevents failure handling)
        httpClient.setConnectTimeout(3000, TimeUnit.MILLISECONDS);

        String actualFileName = targetFile.getName().substring(0, targetFile.getName().lastIndexOf("."));

        Log.d("DOWNLOAD/TOR", "CHECKING URI: " + uri.toString());

        try {
            Request request = new Request.Builder().url(uri.toString()).build();

            // check for partially downloaded file
            File partFile = new File(targetFile.getPath().replace(".tmp", ".part"));

            if (partFile.exists()) {
                long partBytes = partFile.length();
                Log.d("DOWNLOAD", "PARTIAL FILE " + partFile.getPath() + " FOUND, SETTING RANGE HEADER: " + "Range" + " / " + "bytes=" + Long.toString(partBytes) + "-");
                request = new Request.Builder().url(uri.toString()).addHeader("Range", "bytes=" + Long.toString(partBytes) + "-").build();
            } else {
                Log.d("DOWNLOAD", "PARTIAL FILE " + partFile.getPath() + " NOT FOUND, STARTING AT BYTE 0");
            }

            Response response = httpClient.newCall(request).execute();
            int statusCode = response.code();

            if ((statusCode == 200) || (statusCode == 206)) {

                Log.d("DOWNLOAD/TOR", "DOWNLOAD SUCCEEDED, STATUS CODE: " + statusCode + ", GETTING ENTITY...");

                targetFile.getParentFile().mkdirs();
                BufferedInputStream responseInput = new BufferedInputStream(response.body().byteStream());

                try {
                    FileOutputStream targetOutput = new FileOutputStream(targetFile);
                    byte[] buf = new byte[1024];
                    int i;
                    int oldPercent = 0;

                    Date startTime = new Date();

                    while ((i = responseInput.read(buf)) > 0) {

                        // create status bar notification
                        int nPercent = getDownloadPercent();

                        if (oldPercent == nPercent) {
                            // need to cut back on notification traffic
                        } else {
                            oldPercent = nPercent;
                            Notification nProgress = new Notification.Builder(context)
                                    .setContentTitle(mAppTitle + " content download")
                                    .setContentText(indexItem.getTitle() + " - " + (nPercent / 10.0) + "%") // assignment file names are meaningless uuids
                                    .setSmallIcon(android.R.drawable.arrow_down_float)
                                    .setProgress(100, (nPercent / 10), false)
                                    .setWhen(startTime.getTime())
                                    .build();
                            nManager.notify(nTag, nId, nProgress);
                        }

                        targetOutput.write(buf, 0, i);
                    }
                    targetOutput.close();
                    targetOutput = null;
                    responseInput.close();
                    responseInput = null;
                    Log.d("DOWNLOAD/TOR", "SAVED DOWNLOAD TO " + targetFile);
                } catch (ConnectTimeoutException cte) {
                    Log.e("DOWNLOAD/TOR", "FAILED TO SAVE DOWNLOAD TO " + actualFileName + " (CONNECTION EXCEPTION)");
                    cte.printStackTrace();
                } catch (SocketTimeoutException ste) {
                    Log.e("DOWNLOAD/TOR", "FAILED TO SAVE DOWNLOAD TO " + actualFileName + " (SOCKET EXCEPTION)");
                    ste.printStackTrace();
                } catch (IOException ioe) {
                    Log.e("DOWNLOAD/TOR", "FAILED TO SAVE DOWNLOAD TO " + actualFileName + " (IO EXCEPTION)");
                    ioe.printStackTrace();
                }

                // unset flag here, regardless of success
                cleanItem.setDownloadFlag(false);
                expansionIndexItemDao.addExpansionIndexItem(cleanItem);

                // remove notification, regardless of success
                nManager.cancel(nTag, nId);

                // handle file here, regardless of success
                // (assumes .tmp file will exist if download is interrupted)
                if (!handleFile(targetFile)) {
                    Log.e("DOWNLOAD/TOR", "ERROR DURING FILE PROCESSING FOR " + actualFileName);
                }
            } else {
                Log.e("DOWNLOAD/TOR", "DOWNLOAD FAILED FOR " + actualFileName + ", STATUS CODE: " + statusCode);

                // download failed, unset flag
                cleanItem.setDownloadFlag(false);
                expansionIndexItemDao.addExpansionIndexItem(cleanItem);
            }
        } catch (IOException ioe) {
            Log.e("DOWNLOAD/TOR", "DOWNLOAD FAILED FOR " + actualFileName + ", EXCEPTION THROWN");
            ioe.printStackTrace();

            // download failed, unset flag
            cleanItem.setDownloadFlag(false);
            expansionIndexItemDao.addExpansionIndexItem(cleanItem);        }
    }

    private synchronized void initNotificationManager() {
        if (nManager == null) {
            nManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        }
    }

    public boolean checkTor() {
        OrbotHelper orbotHelper = new OrbotHelper(context);

        if(orbotHelper.isOrbotRunning()) {
            Log.d("DOWNLOAD/TOR", "ORBOT RUNNING, USE TOR");
            return true;
        } else {
            Log.d("DOWNLOAD/TOR", "ORBOT NOT RUNNING, DON'T USE TOR");
            return false;
        }
    }

    // return extra digits for greater precision in notification
    private int getDownloadPercent() {
        float percentFloat = getDownloadProgress();
        int percentInt = (int) (percentFloat * 1000);
        return percentInt;
    }

    private float getDownloadProgress() {

        long expectedSize = 0;
        long currentSize = 0;
        boolean sizeUndefined = false;

        if (fileName.contains(Constants.MAIN)) {
            expectedSize = indexItem.getExpansionFileSize();
        } else if (fileName.contains(Constants.PATCH)) {
            expectedSize = indexItem.getPatchFileSize();
        } else {
            // no file information found, can't evaluate
            return -1;
        }

        if (expectedSize == 0) {
            // this seems like an error state
            return -1;
        } else {
            File contentPackFile = new File(StorageHelper.getActualStorageDirectory(context), fileName);

            if (!contentPackFile.exists()) {
                // actual file doesn't exist, check for temp file
                File contentPackFileTemp = new File(contentPackFile.getPath() + ".tmp");

                if (!contentPackFileTemp.exists()) {
                    // still no file, add nothing to current size
                } else {
                    currentSize = currentSize + contentPackFileTemp.length();
                }

                // also check for partial file
                File contentPackFilePart = new File(contentPackFile.getPath() + ".part");

                if (!contentPackFilePart.exists()) {
                    // still no file, add nothing to current size
                } else {
                    currentSize = currentSize + contentPackFilePart.length();
                }
            } else {
                currentSize = currentSize + contentPackFile.length();
            }

            if (currentSize > 0) {
                return (float) currentSize / (float) expectedSize;
            } else {
                // this seems like an error state
                return -1;
            }
        }
    }

    private boolean handleFile (File tempFile) {

        File appendedFile = null;

        File actualFile = new File(tempFile.getPath().substring(0, tempFile.getPath().lastIndexOf(".")));
        Log.d("DOWNLOAD", "ACTUAL FILE: " + actualFile.getAbsolutePath());

        long fileSize = 0;

        if (tempFile.getName().contains(Constants.MAIN)) {
            fileSize = indexItem.getExpansionFileSize();
        } else if (tempFile.getName().contains(Constants.PATCH)) {
            fileSize = indexItem.getPatchFileSize();
        } else {
            Log.e("DOWNLOAD", "CAN'T DETERMINE FILE SIZE FOR " + tempFile.getName() + " (NOT A MAIN OR PATCH FILE)");
            return false;
        }

        // additional error checking
        if (tempFile.exists()) {
            if (tempFile.length() == 0) {
                Log.e("DOWNLOAD", "FINISHED DOWNLOAD OF " + tempFile.getPath() + " BUT IT IS A ZERO BYTE FILE");
                return false;
            } else if (tempFile.length() < fileSize) {
                Log.d("DOWNLOAD", "FINISHED DOWNLOAD OF " + tempFile.getPath() + " BUT IT IS TOO SMALL: " + Long.toString(tempFile.length()) + "/" + Long.toString(fileSize));

                // if file is too small, managePartialFile
                appendedFile = managePartialFile(tempFile);

                // if appended file is still too small, fail (leave .part file for next download
                if (appendedFile == null) {
                    Log.e("DOWNLOAD", "ERROR WHILE APPENDING TO PARTIAL FILE FOR " + tempFile.getPath());
                    return false;
                } else if (appendedFile.length() < fileSize) {
                    Log.e("DOWNLOAD", "APPENDED FILE " + appendedFile.getPath() + " IS STILL TOO SMALL: " + Long.toString(appendedFile.length()) + "/" + Long.toString(fileSize));
                    return false;
                } else {
                    Log.d("DOWNLOAD", "APPENDED FILE " + appendedFile.getPath() + " IS COMPLETE!");
                }
            } else {
                Log.d("DOWNLOAD", "FINISHED DOWNLOAD OF " + tempFile.getPath() + " AND FILE LOOKS OK");

                // show notification
                Utils.toastOnUiThread((Activity) context, "Finished downloading " + indexItem.getTitle() + ".", false); // FIXME move to strings
            }
        } else {
            Log.e("DOWNLOAD", "FINISHED DOWNLOAD OF " + tempFile.getPath() + " BUT IT DOES NOT EXIST");
            return false;
        }

        try {
            // clean up old obbs before renaming new file
            File directory = new File(actualFile.getParent());

            String nameFilter = actualFile.getName();

            if ((actualFile.getName().contains(Constants.MAIN)) && (actualFile.getName().contains(indexItem.getExpansionFileVersion()))) {
                nameFilter = actualFile.getName().replace(indexItem.getExpansionFileVersion(), "*");
            }
            if ((actualFile.getName().contains(Constants.PATCH)) && (actualFile.getName().contains(indexItem.getPatchFileVersion()))) {
                nameFilter = actualFile.getName().replace(indexItem.getPatchFileVersion(), "*");
            }

            Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + directory.getPath());

            WildcardFileFilter oldFileFilter = new WildcardFileFilter(nameFilter);
            for (File oldFile : FileUtils.listFiles(directory, oldFileFilter, null)) {
                Log.d("DOWNLOAD", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
                FileUtils.deleteQuietly(oldFile);
            }

            if ((appendedFile != null) && appendedFile.exists()) {
                FileUtils.moveFile(appendedFile, actualFile); // moved to commons-io from using exec and mv because we were getting 0kb obb files on some devices
                FileUtils.deleteQuietly(appendedFile); // for some reason I was getting an 0kb .tmp file lingereing
                FileUtils.deleteQuietly(tempFile); // for some reason I was getting an 0kb .tmp file lingereing
                Log.d("DOWNLOAD", "MOVED PART FILE " + appendedFile.getPath() + " TO " + actualFile.getPath());
            } else if (tempFile.exists()) {
                FileUtils.moveFile(tempFile, actualFile); // moved to commons-io from using exec and mv because we were getting 0kb obb files on some devices
                FileUtils.deleteQuietly(tempFile); // for some reason I was getting an 0kb .tmp file lingereing
                Log.d("DOWNLOAD", "MOVED TEMP FILE " + tempFile.getPath() + " TO " + actualFile.getPath());
            } else {
                // not sure how we get here but this is a failure state
                Log.e("DOWNLOAD", ".TMP AND .PART FILES DO NOT EXIST FOR " + tempFile.getPath());
                return false;
            }
        } catch (IOException ioe) {
            Log.e("DOWNLOAD", "ERROR DURING CLEANUP/MOVING TEMP FILE: " + ioe.getMessage());
            return false;
        }

        // download finished, must clear ZipHelper cache
        ZipHelper.clearCache();

        return true;
    }
}
