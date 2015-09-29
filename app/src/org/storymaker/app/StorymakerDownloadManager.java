package org.storymaker.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import info.guardianproject.onionkit.trust.StrongHttpsClient;
import info.guardianproject.onionkit.ui.OrbotHelper;
import scal.io.liger.StorymakerIndexManager;
import scal.io.liger.StorymakerQueueManager;
import scal.io.liger.ZipHelper;
import scal.io.liger.model.sqlbrite.ExpansionIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItemDao;
import scal.io.liger.model.sqlbrite.QueueItemDao;

/**
 * Created by mnbogner on 11/7/14.
 */
public class StorymakerDownloadManager implements Runnable {
    private final static String TAG = "LigerAltDownloadManager";

    // TODO use HTTPS
    // TODO pickup Tor settings

    // store in manager to skip index lookups
    private ExpansionIndexItem indexItem = null;
    private String fileName;
    private Context context;

    private DownloadManager dManager;
    private NotificationManager nManager;
    private long lastDownload = -1L;

    StrongHttpsClient mClient = null;

    //boolean useManager = true;
    //boolean useTor = true; // CURRENTLY SET TO TRUE, WILL USE TOR IF ORBOT IS RUNNING

    private String mAppTitle;

    InstalledIndexItemDao installedDao;
    private QueueItemDao queueDao;

    private boolean confirmDownload;

    public StorymakerDownloadManager(String fileName, ExpansionIndexItem indexItem, Context context, InstalledIndexItemDao installedDao, QueueItemDao queueDao, boolean confirmDownload) {
        this.fileName = fileName;
        this.indexItem = indexItem;
        this.context = context;

        this.installedDao = installedDao;
        this.queueDao = queueDao;

        this.confirmDownload = confirmDownload;

        this.mAppTitle = context.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE).getString(Constants.PREFS_APP_TITLE, "StoryPath");
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void run() {

        boolean downloadRequired = false;

        // NOTE: if whatever process was waiting for the download has died, but the download is still underway
        //       it may require a second click  or restart to get to this point.  if we end up here, with a
        //       finished download and no visible file progress, we'll manage the file and return without
        //       starting another download.

        if (checkQueue()) {
            Log.d("DOWNLOAD", "ANOTHER PROCESS IS ALREADY DOWNLOADING " + fileName + ", WILL NOT START DOWNLOAD");
        } else {
            Log.d("DOWNLOAD", "NO OTHER PROCESS IS DOWNLOADING " + fileName + ", CHECKING FOR FILES");

            File tempFile = new File(StorymakerIndexManager.buildFilePath(indexItem, context), fileName + ".tmp");

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

                        if (partFile.getPath().contains(scal.io.liger.Constants.MAIN)) {
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
                        } else if (partFile.getPath().contains(scal.io.liger.Constants.PATCH)) {
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

            downloadFromLigerServer();

        } else {
            Log.d("DOWNLOAD", fileName + " WILL NOT BE DOWNLOADED");
        }

        return;
    }

    public boolean checkQueue() {

        File checkFile = new File(StorymakerIndexManager.buildFilePath(indexItem, context), fileName + ".tmp");
        boolean foundInQueue = false;

        // need to check if a download has already been queued for this file

        //Log.d("QUEUE", "QUEUE ITEM IS " + queueMap.get(queueId).getQueueFile() + " LOOKING FOR " + checkFile.getName());

        Long queueId = StorymakerQueueManager.checkQueue(context, checkFile, queueDao);

        if (queueId == null) {

            // not found
            foundInQueue = false;

        } else if (queueId == StorymakerQueueManager.DUPLICATE_QUERY) {

            // not exactly in queue, but someone is already looking for this item, so avoid collision
            foundInQueue = true;

        } else if (queueId < 0) {
            // use negative numbers to flag non-manager downloads

            if (checkFileProgress()) {

                Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " AND DOWNLOAD PROGRESS OBSERVED, LEAVING " + queueId.toString() + " IN QUEUE ");
                foundInQueue = true;

            } else {

                Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " BUT NO DOWNLOAD PROGRESS OBSERVED, REMOVING " + queueId.toString() + " FROM QUEUE ");
                StorymakerQueueManager.removeFromQueue(context, Long.valueOf(queueId), queueDao);

            }

        } else {
            // use download manager ids to flag manager downloads

            // need to init download manager to check queue
            initDownloadManager();

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(queueId.longValue());
            Cursor c = dManager.query(query);
            try {
                if (c.moveToFirst()) {

                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)) {

                        Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " BUT DOWNLOAD STATUS IS FAILED, REMOVING " + queueId.toString() + " FROM QUEUE ");
                        StorymakerQueueManager.removeFromQueue(context, Long.valueOf(queueId), queueDao);

                    } else if (DownloadManager.STATUS_PAUSED == c.getInt(columnIndex)) {

                        Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " AND DOWNLOAD STATUS IS PAUSED, LEAVING " + queueId.toString() + " IN QUEUE ");
                        foundInQueue = true;

                    } else if (DownloadManager.STATUS_PENDING == c.getInt(columnIndex)) {

                        Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " AND DOWNLOAD STATUS IS PENDING, LEAVING " + queueId.toString() + " IN QUEUE ");
                        foundInQueue = true;

                    } else if (DownloadManager.STATUS_RUNNING == c.getInt(columnIndex)) {

                        Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " AND DOWNLOAD STATUS IS RUNNING, LEAVING " + queueId.toString() + " IN QUEUE ");
                        foundInQueue = true;

                    } else if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                        Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " BUT DOWNLOAD STATUS IS SUCCESSFUL, REMOVING " + queueId.toString() + " FROM QUEUE ");
                        StorymakerQueueManager.removeFromQueue(context, Long.valueOf(queueId), queueDao);

                    } else {

                        Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " BUT DOWNLOAD STATUS IS UNKNOWN, REMOVING " + queueId.toString() + " FROM QUEUE ");
                        StorymakerQueueManager.removeFromQueue(context, Long.valueOf(queueId), queueDao);

                    }
                } else {

                    Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " BUT NOTHING FOUND IN DOWNLOAD MANAGER, REMOVING " + queueId.toString() + " FROM QUEUE ");
                    StorymakerQueueManager.removeFromQueue(context, Long.valueOf(queueId), queueDao);

                }
            } finally {
                if (c != null) {
                    c.close(); // cleanup
                }
            }
        }

        return foundInQueue;
    }

    public boolean checkFileProgress() {

        // not a great solution, but should indicate if file is being actively downloaded
        // only .tmp files should be download targets

        File checkFile = new File(StorymakerIndexManager.buildFilePath(indexItem, context), fileName + ".tmp");
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

    private void downloadFromLigerServer() {

        String ligerUrl = indexItem.getExpansionFileUrl();
        String ligerPath = StorymakerIndexManager.buildFilePath(indexItem, context);
        String ligerObb = fileName;

        Log.d("DOWNLOAD", "DOWNLOADING " + ligerObb + " FROM " + ligerUrl + " TO " + ligerPath);

        try {

            URI expansionFileUri = null;
            HttpGet request = null;
            HttpResponse response = null;

            File targetFolder = new File(ligerPath);

            String nameFilter = "";

            if (ligerObb.contains(indexItem.getExpansionFileVersion())) {
                nameFilter = fileName.replace(indexItem.getExpansionFileVersion(), "*") + "*.tmp";
            } else {
                nameFilter = fileName + "*.tmp";
            }

            Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + targetFolder.getPath());

            WildcardFileFilter oldFileFilter = new WildcardFileFilter(nameFilter);
            for (File oldFile : FileUtils.listFiles(targetFolder, oldFileFilter, null)) {
                Log.d("DOWNLOAD", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
                FileUtils.deleteQuietly(oldFile);
            }

            // additional cleanup of pre-name-change files
            if (fileName.contains(scal.io.liger.Constants.MAIN)) {
                nameFilter = fileName.replace(scal.io.liger.Constants.MAIN + ".", "").replace(indexItem.getExpansionFileVersion(), "*");

                Log.d("DOWNLOAD", "CLEANUP: DELETING OLD FILES " + nameFilter + " FROM " + targetFolder.getPath());

                oldFileFilter = new WildcardFileFilter(nameFilter);
                for (File oldFile : FileUtils.listFiles(targetFolder, oldFileFilter, null)) {
                    Log.d("DOWNLOAD", "CLEANUP: FOUND OLD FILE " + oldFile.getPath() + ", DELETING");
                    FileUtils.deleteQuietly(oldFile);
                }
            }

            File targetFile = new File(targetFolder, ligerObb + ".tmp");

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

                //if (checkTor(useTor, context)) {
                if (useTor && useManager) {
                    Log.e("DOWNLOAD", "ANDROID DOWNLOAD MANAGER IS NOT COMPATABLE WITH TOR");

                    if (context instanceof Activity) {
                        Utils.toastOnUiThread((Activity) context, "Check settings, can't use download manager and tor", true); // FIXME move to strings
                    }

                    StorymakerQueueManager.checkQueueFinished(context, targetFile.getName());

                } else if (useTor || !useManager) {
                    downloadWithTor(useTor, Uri.parse(ligerUrl + ligerObb), mAppTitle + " content download", ligerObb, targetFile);
                } else {
                    downloadWithManager(Uri.parse(ligerUrl + ligerObb), mAppTitle + " content download", ligerObb, Uri.fromFile(targetFile));
                }

            } else {
                Log.d("DOWNLOAD", "NO CONNECTION, NOT QUEUEING DOWNLOAD: " + ligerUrl + ligerObb + " -> " + targetFile.getPath());

                if (context instanceof Activity) {
                    Utils.toastOnUiThread((Activity) context, "Check settings, no connection, can't start download", true); // FIXME move to strings
                }

                StorymakerQueueManager.checkQueueFinished(context, targetFile.getName());

            }

        } catch (Exception e) {
            Log.e("DOWNLOAD", "DOWNLOAD ERROR: " + ligerUrl + ligerObb + " -> " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean checkTor(Context mContext) {
        OrbotHelper orbotHelper = new OrbotHelper(mContext);

        if(orbotHelper.isOrbotRunning()) {
            Log.d("DOWNLOAD/TOR", "ORBOT RUNNING, USE TOR");
            return true;
        } else {
            Log.d("DOWNLOAD/TOR", "ORBOT NOT RUNNING, DON'T USE TOR");
            return false;
        }
    }

    private void downloadWithTor(boolean useTor, Uri uri, String title, String desc, File targetFile) {
        initNotificationManager();

        // generate id/tag for notification
        String nTag = indexItem.getExpansionId();
        int nId = 0;
        if (fileName.contains(scal.io.liger.Constants.MAIN)) {
            nId = Integer.parseInt(indexItem.getExpansionFileVersion());
        } else if (fileName.contains(scal.io.liger.Constants.PATCH)) {
            nId = Integer.parseInt(indexItem.getPatchFileVersion());
        }

        // incompatible with lungcast certificate
        // StrongHttpsClient httpClient = getHttpClientInstance();
        OkHttpClient httpClient = new OkHttpClient();

        // we're now using this method to support non-tor downloads as well, so settings must be checked
        if (useTor) {
            if (checkTor(context)) {

                Log.d("DOWNLOAD/TOR", "DOWNLOAD WITH TOR PROXY: " + scal.io.liger.Constants.TOR_PROXY_HOST + "/" + scal.io.liger.Constants.TOR_PROXY_PORT);

                SocketAddress torSocket = new InetSocketAddress(scal.io.liger.Constants.TOR_PROXY_HOST, scal.io.liger.Constants.TOR_PROXY_PORT);
                Proxy torProxy = new Proxy(Proxy.Type.HTTP, torSocket);
                httpClient.setProxy(torProxy);

            } else {
                Log.e("DOWNLOAD/TOR", "CANNOT DOWNLOAD WITH TOR, TOR IS NOT ACTIVE");

                if (context instanceof Activity) {
                    Utils.toastOnUiThread((Activity) context, "Check settings, can't use tor if orbot isn't running", true); // FIXME move to strings
                }

                StorymakerQueueManager.checkQueueFinished(context, targetFile.getName());

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
                // request.setHeader("Range", "bytes=" + Long.toString(partBytes) + "-");
                request = new Request.Builder().url(uri.toString()).addHeader("Range", "bytes=" + Long.toString(partBytes) + "-").build();
            } else {
                Log.d("DOWNLOAD", "PARTIAL FILE " + partFile.getPath() + " NOT FOUND, STARTING AT BYTE 0");
            }

            Response response = httpClient.newCall(request).execute();

            int statusCode = response.code();

            if ((statusCode == 200) || (statusCode == 206)) {

                Log.d("DOWNLOAD/TOR", "DOWNLOAD SUCCEEDED, STATUS CODE: " + statusCode);

                // queue item here, "download" doesn't start until after we get a status code

                // queue item, use date to get a unique long, subtract to get a negative number (to distinguish from download manager items)
                Date startTime = new Date();
                long queueId = 0 - startTime.getTime();
                StorymakerQueueManager.addToQueue(context, queueId, targetFile.getName(), queueDao);

                targetFile.getParentFile().mkdirs();

                Log.d("DOWNLOAD/TOR", "DOWNLOAD SUCCEEDED, GETTING ENTITY...");

                BufferedInputStream responseInput = new BufferedInputStream(response.body().byteStream());

                try {
                    FileOutputStream targetOutput = new FileOutputStream(targetFile);
                    byte[] buf = new byte[1024];
                    int i;
                    int oldPercent = 0;
                    while ((i = responseInput.read(buf)) > 0) {

                        // create status bar notification
                        int nPercent = StorymakerDownloadHelper.getDownloadPercent(context, fileName, installedDao);

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
                    responseInput.close();
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

                // remove from queue here, regardless of success
                StorymakerQueueManager.removeFromQueue(context, queueId, queueDao);

                // remove notification, regardless of success
                nManager.cancel(nTag, nId);

                // handle file here, regardless of success
                // (assumes .tmp file will exist if download is interrupted)
                if (!handleFile(targetFile)) {
                    Log.e("DOWNLOAD/TOR", "ERROR DURING FILE PROCESSING FOR " + actualFileName);
                }
            } else {
                Log.e("DOWNLOAD/TOR", "DOWNLOAD FAILED FOR " + actualFileName + ", STATUS CODE: " + statusCode);

                StorymakerQueueManager.checkQueueFinished(context, targetFile.getName());
            }

            // clean up connection
            // EntityUtils.consume(entity);
            // request.abort();
            // request.releaseConnection();

        } catch (IOException ioe) {
            Log.e("DOWNLOAD/TOR", "DOWNLOAD FAILED FOR " + actualFileName + ", EXCEPTION THROWN");
            ioe.printStackTrace();

            StorymakerQueueManager.checkQueueFinished(context, targetFile.getName());
        }
    }

    private void downloadWithManager(Uri uri, String title, String desc, Uri uriFile) {
        initDownloadManager();

        Log.d("DOWNLOAD", "QUEUEING DOWNLOAD: " + uri.toString() + " -> " + uriFile.toString());

        initReceivers();

        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(title)
                .setDescription(desc)
                .setVisibleInDownloadsUi(false)
                .setDestinationUri(uriFile);

        File partFile = new File(uriFile.toString().replace(".tmp", ".part"));

        if (partFile.exists()) {
            long partBytes = partFile.length();
            Log.d("DOWNLOAD", "PARTIAL FILE " + partFile.getPath() + " FOUND, SETTING RANGE HEADER: " + "Range" + " / " + "bytes=" + Long.toString(partBytes) + "-");
            request.addRequestHeader("Range", "bytes=" + Long.toString(partBytes) + "-");
        } else {
            Log.d("DOWNLOAD", "PARTIAL FILE " + partFile.getPath() + " NOT FOUND, STARTING AT BYTE 0");
        }

        lastDownload = dManager.enqueue(request);

        // have to enqueue first to get manager id
        String uriString = uriFile.toString();
        StorymakerQueueManager.addToQueue(context, Long.valueOf(lastDownload), uriString.substring(uriString.lastIndexOf("/") + 1), queueDao);
    }

    private synchronized void initDownloadManager() {
        if (dManager == null) {
            dManager = (DownloadManager)context.getSystemService(context.DOWNLOAD_SERVICE);
        }
    }

    private synchronized void initNotificationManager() {
        if (nManager == null) {
            nManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        }
    }

    private synchronized void initReceivers() {
        FilteredBroadcastReceiver onComplete = new FilteredBroadcastReceiver(fileName);
        BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                // ???
            }
        };

        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        context.registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
    }

    private class FilteredBroadcastReceiver extends BroadcastReceiver {

        public String fileFilter;
        public boolean fileReceived = false;

        public FilteredBroadcastReceiver(String fileFilter) {
            this.fileFilter = fileFilter;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor c = dManager.query(query);
                try {
                    if (c.moveToFirst()) {

                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);

                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                            File savedFile = new File(Uri.parse(uriString).getPath());
                            Log.d("DOWNLOAD", "PROCESSING DOWNLOADED FILE " + savedFile.getPath());

                            File fileCheck = new File(savedFile.getPath().substring(0, savedFile.getPath().lastIndexOf(".")));

                            if (fileReceived) {
                                Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " BUT THIS RECEIVER HAS ALREADY PROCESSED A FILE");
                                return;
                            } else if (!fileCheck.getName().equals(fileFilter)) {
                                Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " BUT THIS RECEIVER IS FOR " + fileFilter);
                                return;
                            } else {
                                Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " AND THIS RECEIVER IS FOR " + fileFilter + ", PROCESSING...");
                                fileReceived = true;
                            }

                            StorymakerQueueManager.removeFromQueue(context, Long.valueOf(downloadId), queueDao);

                            Log.d("QUEUE", "DOWNLOAD COMPLETE, REMOVING FROM QUEUE: " + downloadId);

                            if (!handleFile(savedFile)) {
                                Log.e("DOWNLOAD", "ERROR DURING FILE PROCESSING FOR " + fileCheck.getName());

                            } else {
                                Log.e("DOWNLOAD", "FILE PROCESSING COMPLETE FOR " + fileCheck.getName());
                            }
                        } else {

                            // COLUMN_LOCAL_URI seems to be null if download fails
                            // COLUMN_URI is the download url, not the .tmp file path
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                            String uriName = uriString.substring(uriString.lastIndexOf("/"));

                            File savedFile = new File(StorymakerIndexManager.buildFilePath(indexItem, context), uriName + ".tmp");
                            Log.d("DOWNLOAD", "PROCESSING DOWNLOADED FILE " + savedFile.getPath());

                            File fileCheck = new File(savedFile.getPath().substring(0, savedFile.getPath().lastIndexOf(".")));

                            if (fileReceived) {
                                Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " BUT THIS RECEIVER HAS ALREADY PROCESSED A FILE");
                                return;
                            } else if (!fileCheck.getName().equals(fileFilter)) {
                                Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " BUT THIS RECEIVER IS FOR " + fileFilter);
                                return;
                            } else {
                                Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " AND THIS RECEIVER IS FOR " + fileFilter + ", PROCESSING...");
                                fileReceived = true;
                            }

                            String status;
                            boolean willResume = true;

                            // improve feedback
                            if (DownloadManager.STATUS_RUNNING == c.getInt(columnIndex)) {
                                status = "RUNNING";
                            } else if (DownloadManager.STATUS_PENDING == c.getInt(columnIndex)) {
                                status = "PENDING";
                            } else if (DownloadManager.STATUS_PAUSED == c.getInt(columnIndex)) {
                                status = "PAUSED";
                            } else if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)) {
                                status = "FAILED";
                                willResume = false;
                            } else {
                                status = "UNKNOWN";
                                willResume = false;
                            }

                            Log.e("DOWNLOAD", "MANAGER FAILED AT STATUS CHECK, STATUS IS " + status);

                            if (willResume) {
                                Log.e("DOWNLOAD", "STATUS IS " + status + ", LEAVING QUEUE/FILES AS-IS FOR MANAGER TO HANDLE");
                            } else {
                                Log.e("DOWNLOAD", "STATUS IS " + status + ", CLEANING UP QUEUE/FILES, MANAGER WILL NOT RESUME");

                                Log.d("QUEUE", "DOWNLOAD STOPPED, REMOVING FROM QUEUE: " + downloadId);

                                StorymakerQueueManager.removeFromQueue(context, Long.valueOf(downloadId), queueDao);

                                if (!handleFile(savedFile)) {
                                    Log.e("DOWNLOAD", "ERROR DURING FILE PROCESSING FOR " + fileCheck.getName());
                                } else {
                                    Log.e("DOWNLOAD", "FILE PROCESSING COMPLETE FOR " + fileCheck.getName());
                                }
                            }
                        }
                    } else {
                        Log.e("DOWNLOAD", "MANAGER FAILED AT QUERY");
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            } else {
                Log.e("DOWNLOAD", "MANAGER FAILED AT COMPLETION CHECK");
            }

            // once this has done its job, make it go away
            context.unregisterReceiver(this);
        }
    }

    private boolean handleFile (File tempFile) {

        File appendedFile = null;

        File actualFile = new File(tempFile.getPath().substring(0, tempFile.getPath().lastIndexOf(".")));
        Log.d("DOWNLOAD", "ACTUAL FILE: " + actualFile.getAbsolutePath());

        long fileSize = 0;

        if (tempFile.getName().contains(scal.io.liger.Constants.MAIN)) {
            fileSize = indexItem.getExpansionFileSize();
        } else if (tempFile.getName().contains(scal.io.liger.Constants.PATCH)) {
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

                Log.e("DOWNLOAD", "FINISHED DOWNLOAD OF " + tempFile.getPath() + " BUT IT IS TOO SMALL: " + Long.toString(tempFile.length()) + "/" + Long.toString(fileSize));

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

            String nameFilter = "";
            if (actualFile.getName().contains(indexItem.getExpansionFileVersion())) {
                nameFilter = actualFile.getName().replace(indexItem.getExpansionFileVersion(), "*");
            } else {
                nameFilter = actualFile.getName();
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
