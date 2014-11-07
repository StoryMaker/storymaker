
package org.storymaker.app.lessons;

import org.storymaker.app.AppConstants;
import org.storymaker.app.R;
import org.storymaker.app.model.Lesson;
import info.guardianproject.onionkit.trust.StrongHttpsClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.util.EntityUtils;

public class LessonManager implements Runnable {
    protected final static String TAG = "LessonManager";
    private String mUrlRemoteRepo;
    private File mLocalStorageRoot;

    private LessonManagerListener mListener;

    private Context mContext;

    private String mSubFolder;

    public final static String LESSON_METADATA_FILE = "lesson.json";
    public final static String LESSON_STATUS_FILE = "status.txt";
    // public final static String LESSON_INDEX_FILE = "index.json";'

    private final static int SO_TIMEOUT = 60000;

    private static StrongHttpsClient mHttpClient;

    public LessonManager(Context context, String remoteRepoUrl, File localStorageRoot) {
        mContext = context;

        mUrlRemoteRepo = remoteRepoUrl;
        if (!mUrlRemoteRepo.endsWith("/")) {
            mUrlRemoteRepo = mUrlRemoteRepo + "/";
        }
        
        mLocalStorageRoot = localStorageRoot;
        mLocalStorageRoot.mkdir();
    }

    public File getLessonRoot() {
        return mLocalStorageRoot;
    }

    public void setSubFolder(String subFolder) {
        mSubFolder = subFolder;
    }

    public void setLessonManagerListener(LessonManagerListener listener) {
        mListener = listener;
    }

    public void updateLessonsFromRemote() {
        new Thread(this).start();
    }

    public void updateLessonStatus(String path, int status) throws IOException {
        File fileLesson = new File(path);
        if (!fileLesson.isDirectory()) {
            fileLesson = fileLesson.getParentFile();
        }

        String strStatus = status + "";

        File fileStatus = new File(fileLesson, LESSON_STATUS_FILE);
        fileStatus.createNewFile();
        FileOutputStream fos = new FileOutputStream(fileStatus);
        fos.write(strStatus.getBytes());
    }

    public ArrayList<Lesson> loadLessonList(Context context, String lang) {
        return loadLessonList(context, mLocalStorageRoot, mSubFolder, lang, -1);
    }

    public ArrayList<Lesson> loadLessonList(Context context, String lang, int matchStatus) {
        return loadLessonList(context, mLocalStorageRoot, mSubFolder, lang, matchStatus);
    }

    public static ArrayList<Lesson> loadLessonList(Context context, File targetFolder,
            String subFolder, String lang, int matchStatus) {

        ArrayList<Lesson> lessons = new ArrayList<Lesson>();

        File lessonFolder = targetFolder;

        if (subFolder != null) {
            lessonFolder = new File(targetFolder, subFolder);
        }

        if (lessonFolder.exists()) {

            File[] fileLessons = lessonFolder.listFiles();

            for (File fileLesson : fileLessons) {
                try {
                    File fileLessonJson = new File(fileLesson, "lesson.json");

                    if (!fileLessonJson.exists()) {
                        fileLessonJson = new File(fileLesson, "Lesson.json"); // sometimes people are silly and case matters
                    }

                    File fileStatus = new File(fileLesson, LESSON_STATUS_FILE);
                    int status = -1;
                    Date statusModified = null;

                    if (fileStatus.exists()) {
                        byte[] bStatus = new byte[(int) fileStatus.length()];
                        IOUtils.readFully(new FileInputStream(fileStatus), bStatus);
                        status = Integer.parseInt(new String(bStatus).trim());
                        statusModified = new Date(fileStatus.lastModified());
                    }

                    if (matchStatus != -1 && matchStatus != status) {
                        continue; // didn't match so don't add it
                    }

                    if (fileLessonJson.exists())
                    {

                        Lesson lesson = Lesson.parse(context, IOUtils.toString(new FileInputStream( fileLessonJson)));
                        File fileIdx = new File(fileLesson, lesson.mResourcePath);

                        lesson.mTitle = fileLesson.getName() + ": " + lesson.mTitle;
                        lesson.mResourcePath = fileIdx.getAbsolutePath();
                        lesson.mStatus = status;
                        lesson.mStatusModified = statusModified;

                        lessons.add(lesson);

                        lesson.mLocalPath = fileLesson;

                        File fileImage = new File(fileLesson, "1.png");
                        if (fileImage.exists()) {
                            lesson.mImage = fileImage.getAbsolutePath();
                        }
                        
                        lesson.mSortIdx = Integer.parseInt(lesson.mTitle.substring(2, lesson.mTitle.indexOf(":")));
                    }
                } catch (FileNotFoundException fnfe) {
                    Log.w(TAG,
                            "lesson json not found: " + fileLesson.getAbsolutePath(), fnfe);
                } catch (IOException fnfe) {
                    Log.w(TAG,
                            "lesson json i/o error on loading: " + fileLesson.getAbsolutePath(),
                            fnfe);
                } catch (Exception fnfe) {
                    Log.w(TAG,
                            "lesson json general exception: " + fileLesson.getAbsolutePath(), fnfe);
                }
            }
        }

        Collections.sort(lessons, new Comparator<Lesson>() {
            public int compare(Lesson lessonA, Lesson lessonB) {
                return lessonA.mSortIdx.compareTo(lessonB.mSortIdx);
            }
        });

        return lessons;
    }

    public static void updateLessonResource(Context context, Lesson lesson, String locale)
            throws IOException {
        File fileIdx = new File(lesson.mResourcePath);

        InputStream is = context.getAssets().open("template/index.html." + locale);
        OutputStream os = new java.io.FileOutputStream(fileIdx);
        IOUtils.copy(is, os);
    }

    private synchronized StrongHttpsClient getHttpClientInstance() {
        if (mHttpClient == null) {
            mHttpClient = new StrongHttpsClient(mContext);
        }
        
        // HttpParams params = mHttpClient.getParams();
        // HttpConnectionParams.setConnectionTimeout(params, SO_TIMEOUT);
        // HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);
        // DefaultHttpClient.setDefaultHttpParams(params);

        return mHttpClient;
    }

    public void run() {
        Log.d(TAG, "loading lessons from remote");

        String sUrlLesson = null;

        try {
            File lessonFolder = mLocalStorageRoot;
            if (mSubFolder != null) {
                lessonFolder = new File(mLocalStorageRoot, mSubFolder);
            }
            lessonFolder.mkdirs();

            Log.d(TAG, "current lesson folder: " + lessonFolder.getAbsolutePath());

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
            boolean useDownloadManager = settings.getBoolean("pusedownloadmanager", true);

            // open URL and download file listing
            StrongHttpsClient httpClient = getHttpClientInstance();

            boolean useTor = settings.getBoolean("pusetor", false);
            if (useTor) {
                httpClient.useProxy(true, "SOCKS", AppConstants.TOR_PROXY_HOST, AppConstants.TOR_PROXY_PORT);
            } else {
                httpClient.useProxy(false, null, null, -1);
            }

            String urlBase = mUrlRemoteRepo;
            if (mSubFolder != null) {
                urlBase += mSubFolder + '/';
            }

            String urlIndex = urlBase + LESSON_METADATA_FILE;

            Log.d(TAG, "Loading lesson index: " + urlIndex);

            HttpGet request = new HttpGet(urlIndex);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();

            // InputStream isContent = entity.getContent();

            long conLen = entity.getContentLength();
            boolean isChunked = entity.isChunked();

            // if (conLen > -1)
            if (statusCode == 200) {
                String jsonData = EntityUtils.toString(entity);

                if (!jsonData.contains("]")) {
                    jsonData += "]}";
                }

                // Log.d(TAG,"got json data: " + jsonData);

                JSONObject jObjMain = new JSONObject(jsonData);

                JSONArray jarray = jObjMain.getJSONArray("lessons");

                for (int i = 0; i < jarray.length() && (!jarray.isNull(i)); i++) {
                    try {
                        if (mListener != null) {
                            mListener.lessonLoadingStatusMessage(String.format(
                                    "%d" + mContext.getString(R.string._of_) + "%d", (i + 1),
                                    jarray.length()));
                        }

                        JSONObject jobj = jarray.getJSONObject(i);

                        String title = jobj.getString("title");
                        String lessonUrl = jobj.getJSONObject("resource").getString("url");

                        // this should be a zip file
                        sUrlLesson = urlBase + lessonUrl;

                        Log.d(TAG, "Loading lesson zip: " + sUrlLesson);

                        if (useDownloadManager) {
                            URI urlLesson = new URI(sUrlLesson);
                            String fileName = urlLesson.getPath();
                            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
                            File fileZip = new File(lessonFolder, fileName);

                            doDownloadManager(Uri.parse(sUrlLesson), title, fileName, Uri.fromFile(fileZip));

                        } else {
                            URI urlLesson = new URI(sUrlLesson);
                            request = new HttpGet(urlLesson);
                            response = httpClient.execute(request);

                            String fileName = urlLesson.getPath();
                            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
                            File fileZip = new File(lessonFolder, fileName);
                            long remoteLen = response.getEntity().getContentLength();

                            if (fileZip.exists()) {
                                long localLen = fileZip.length();

                                if (localLen == remoteLen) {
                                    // same file, leave it be
                                    Log.d(TAG, "file already exists locally; skipping!");
                                    response.getEntity().consumeContent();
                                    continue;
                                } else {
                                    // otherwise, delete and download
                                    Log.d(TAG, "local file is out of date; updating...");
                                    fileZip.delete();
                                }
                            }

                            if (mListener != null) {
                                mListener.lessonLoadingStatusMessage("Loading " + (i + 1) + " of " + jarray.length() + " lessons" + "\nSize: " + remoteLen / 1000000 + "MB");
                            }

                            fileZip.getParentFile().mkdirs();

                            BufferedInputStream bis = new BufferedInputStream(response.getEntity() .getContent());
                            IOUtils.copy(bis, new FileOutputStream(fileZip));

                            unpack(fileZip, lessonFolder);

                            fileZip.delete();
                        }

                    } catch (Exception ioe) {
                        Log.e(TAG, "error loading lesson from server: " + sUrlLesson, ioe);

                        if (response != null) {
                            response.getEntity().consumeContent();
                        }
                        
                        if (mListener != null) {
                            mListener.errorLoadingLessons(ioe.getLocalizedMessage());
                        }
                    }
                }

                if (mListener != null) {
                    mListener.lessonsLoadedFromServer();
                }
            } else {
                Log.w(TAG, "lesson json not available on server: " + sUrlLesson);
                if (mListener != null) {
                    mListener.errorLoadingLessons("Lesson data not yet available on server");
                }
            }
        } catch (Exception ioe) {
            Log.e(TAG, "error loading lessons from server: " + sUrlLesson, ioe);

            if (mListener != null)
                mListener.errorLoadingLessons(ioe.getLocalizedMessage());
        }
    }

    /** Unpacks the give zip file using the built in Java facilities for unzip. */
    @SuppressWarnings("unchecked")
    public void unpack(File zipFile, File rootDir) throws IOException {
        ZipFile zip = new ZipFile(zipFile);
        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
        ZipEntry entry = null;

        while (entries.hasMoreElements()) {
            entry = entries.nextElement();

            java.io.File f = new java.io.File(rootDir, entry.getName());
            
            // if its a directory, create it
            if (entry.isDirectory()) { 
                continue;
            }

            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            InputStream is = zip.getInputStream(entry); // get the input stream
            OutputStream os = new java.io.FileOutputStream(f);
            IOUtils.copy(is, os);
        }

    }

    private DownloadManager mgr;
    private long lastDownload = -1L;

    private synchronized void initDownloadManager() {
        if (mgr == null) {
            mgr = (DownloadManager) mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
            mContext.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            mContext.registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
        }
    }

    private void doDownloadManager(Uri uri, String title, String desc, Uri uriFile) {
        initDownloadManager();

        lastDownload =
                mgr.enqueue(new DownloadManager.Request(uri)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false)
                        .setTitle(title)
                        .setDescription(desc)
                        .setVisibleInDownloadsUi(false)
                        .setDestinationUri(uriFile));
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                Query query = new Query();
                query.setFilterById(downloadId);
                Cursor c = mgr.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        File fileZip = new File(Uri.parse(uriString).getPath());

                        try {
                            unpack(fileZip, fileZip.getParentFile());

                            if (mListener != null) {
                                mListener.lessonsLoadedFromServer();
                            }

                            fileZip.delete();
                        } catch (IOException e) {
                            Log.e(TAG, "unable to unzip file:" + fileZip.getAbsolutePath(), e);
                        }
                    }
                }
            }
        }
    };

    BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

        }
    };
}
