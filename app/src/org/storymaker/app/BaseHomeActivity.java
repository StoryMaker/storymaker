package org.storymaker.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.hannesdorfmann.sqlbrite.dao.DaoManager;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.UpdateManager;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.storymaker.app.model.Project;
import org.storymaker.app.server.LoginActivity;
import org.storymaker.app.server.ServerManager;
import org.storymaker.app.ui.SlidingTabLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import info.guardianproject.netcipher.proxy.OrbotHelper;
import rx.functions.Action1;
import scal.io.liger.JsonHelper;
import scal.io.liger.MainActivity;
import scal.io.liger.StorageHelper;
import scal.io.liger.StorymakerIndexManager;
import scal.io.liger.model.ContentPackMetadata;
import scal.io.liger.model.StoryPathLibrary;
import scal.io.liger.model.sqlbrite.AvailableIndexItem;
import scal.io.liger.model.sqlbrite.AvailableIndexItemDao;
import scal.io.liger.model.sqlbrite.ExpansionIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItemDao;
import scal.io.liger.model.sqlbrite.InstanceIndexItem;
import scal.io.liger.model.sqlbrite.InstanceIndexItemDao;
import scal.io.liger.model.sqlbrite.QueueItemDao;
import timber.log.Timber;

/**
 * Created by josh on 12/18/15.
 */
public abstract class BaseHomeActivity extends BaseActivity {

    protected ProgressDialog mLoading;
    protected ArrayList<Project> mListProjects;
    protected RecyclerView mRecyclerView;

    protected ViewPager mViewPager;
    protected SlidingTabLayout mSlidingTabLayout;
    protected String[] mTabMenu;

    protected boolean loggedIn;

    // new stuff
    protected InstanceIndexItemDao instanceIndexItemDao;
    protected AvailableIndexItemDao availableIndexItemDao;
    protected InstalledIndexItemDao installedIndexItemDao;
    protected QueueItemDao queueItemDao;
    protected DaoManager daoManager;
    protected int dbVersion = 2;

    protected HashMap<String, ArrayList<Thread>> downloadThreads = new HashMap<String, ArrayList<Thread>>();

    public void removeThreads(String id) {
        if (downloadThreads.containsKey(id)) {
            downloadThreads.remove(id);
        }
    }

    public BaseHomeActivity() {

        instanceIndexItemDao = new InstanceIndexItemDao();
        availableIndexItemDao = new AvailableIndexItemDao();
        installedIndexItemDao = new InstalledIndexItemDao();
        queueItemDao = new QueueItemDao();

        daoManager = new DaoManager(BaseHomeActivity.this, "Storymaker.db", dbVersion, instanceIndexItemDao, availableIndexItemDao, installedIndexItemDao, queueItemDao);
        daoManager.setLogging(false);

        //Log.d("BaseHomeActivity", "DB version "+daoManager.getVersion());


    }

    public String[] getMenu(String menu_type) {

        //This Method Transposes between an arrays.xml array of ids to build the catalog menu
        //      and their strings.xml display name counterparts
        //      the idea is the ids can be consistent (they will be part of database queries),
        //      whereas the Menu tab names can be localized
        //
        //      ex: in arrays.xml
        //
        //              <string-array name="catalog_menu_ids">
        //                  <item>catalog</item>
        //                  <item>guides</item>
        //                  <item>lessons</item>
        //                  <item>templates</item>
        //              </string-array>
        //
        //      ex: in strings.xml
        //
        //              <string name="catalog_menu_catalog">Catalog</string>
        //              <string name="catalog_menu_guides">Guides</string>
        //              <string name="catalog_menu_lessons">Lessons</string>
        //              <string name="catalog_menu_templates">Templates</string>
        //

        int menuId = getResources().getIdentifier(menu_type + "_menu_ids", "array", getApplicationContext().getPackageName());
        String[] menu_ids = getResources().getStringArray(menuId);
        String[] menu_names = new String[menu_ids.length];

        for (int i=0; i<menu_ids.length; i++) {
            int id = getResources().getIdentifier("home_menu_" + menu_ids[i], "string", getApplicationContext().getPackageName());
            String menu_name = getResources().getString(id);
            menu_names[i] = menu_name;
        }

        //String s = getString(R.string.finished_downloading);

        return menu_names;

    }

    // added for testing
    public void scroll(int position) {
        Timber.d("Scrolling to index item " + position);
        mRecyclerView.scrollToPosition(position);
    }

    public static String parseInstanceDate(String filename) {
//        String jsonFilePath = storyPath.buildTargetPath(storyPath.getId() + "-instance-" + timeStamp.getTime() + ".json");
        String[] splits = FilenameUtils.removeExtension(filename).split("-");
        return splits[splits.length - 1]; // FIXME make more robust and move into liger
    }

    // copied this as a short term fix until we get loading cleanly split out from the liger sample app ui stuff
    protected StoryPathLibrary initSPLFromJson(String json, String jsonPath) {
        if (json == null || json.equals("")) {
            Toast.makeText(this, getString(R.string.home_content_missing), Toast.LENGTH_LONG).show();
            finish();
            return null;
        }

        ArrayList<String> referencedFiles = null;

        // should not need to insert dependencies into a saved instance
        if (jsonPath.contains("instance")) {
            referencedFiles = new ArrayList<String>();
        } else {
            referencedFiles = JsonHelper.getInstancePaths(this);
        }

        StoryPathLibrary storyPathLibrary = JsonHelper.deserializeStoryPathLibrary(json, jsonPath, referencedFiles, this, StoryMakerApp.getCurrentLocale().getLanguage());

        if ((storyPathLibrary != null) && (storyPathLibrary.getCurrentStoryPathFile() != null)) {
            storyPathLibrary.loadStoryPathTemplate("CURRENT", false);
        }

        return storyPathLibrary;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // copy index file
        StorymakerIndexManager.copyAvailableIndex(this, false); // TODO: REPLACE THIS WITH INDEX DOWNLOAD (IF LOGGED IN) <- NEED TO COPY FILE FOR BASELINE CONTENT

        // initialize db

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // version check (sqlite upgrade requires migration)

        int appMigrationVersion = preferences.getInt("APP_MIGRATION_VERSION", 0);

        Timber.d("MIGRATION CHECK: " + appMigrationVersion + " vs. " + Constants.APP_MIGRATION_VERSION);

        if (appMigrationVersion != Constants.APP_MIGRATION_VERSION) {

            Timber.d("MIGRATION REQUIRED, RE-ENCRYPTING DATABASE");

            final boolean[] dbStatus = {false};
            try {
                SQLiteDatabaseHook dbHook = new SQLiteDatabaseHook() {
                    public void preKey(SQLiteDatabase database) {
                    }
                    public void postKey(SQLiteDatabase database) {
                        Cursor cursor = database.rawQuery("PRAGMA cipher_migrate", new String[]{});
                        String value = "";
                        if (cursor != null) {
                            cursor.moveToFirst();
                            value = cursor.getString(0);
                            cursor.close();
                        }

                        // this result is currently ignored, checking if db is null instead
                        dbStatus[0] = Integer.valueOf(value) == 0;
                    }
                };

                File dbPath = getDatabasePath("sm.db");
                Timber.d("MIGRATING DATABASE AT " + dbPath.getPath());

                SQLiteDatabase sqldb = SQLiteDatabase.openOrCreateDatabase(dbPath, "foo", null, dbHook);
                if (sqldb != null) {
                    Timber.d("MIGRATED DATABASE NOT NULL");
                    sqldb.close();

                    // update preferences if migration succeeded

                    preferences.edit().putInt("APP_MIGRATION_VERSION", Constants.APP_MIGRATION_VERSION).commit();
                } else {
                    Timber.e("MIGRATED DATABASE IS NULL");
                }
            } catch (Exception ex) {
                Timber.e("EXCEPTION WHILE MIGRATING DATABASE: " + ex.getMessage());
            }
        }

        int availableIndexVersion = preferences.getInt("AVAILABLE_INDEX_VERSION", 0);

        Timber.d("VERSION CHECK: " + availableIndexVersion + " vs. " + scal.io.liger.Constants.AVAILABLE_INDEX_VERSION);

        if (availableIndexVersion != scal.io.liger.Constants.AVAILABLE_INDEX_VERSION) {

            // load db from file

            HashMap<String, scal.io.liger.model.ExpansionIndexItem> availableItemsFromFile = scal.io.liger.IndexManager.loadAvailableIdIndex(this);

            if (availableItemsFromFile.size() == 0) {
                Timber.d("NOTHING LOADED FROM AVAILABLE FILE");
            } else {
                for (scal.io.liger.model.ExpansionIndexItem item : availableItemsFromFile.values()) {
                    Timber.d("ADDING " + item.getExpansionId() + " TO DATABASE (AVAILABLE)");
                    availableIndexItemDao.addAvailableIndexItem(item, true); // replaces existing items, should trigger updates to installed items and table as needed

                    // ugly solution to deal with the fact that the popup menu assumes there will be threads for an item we tried to download/install
                    ArrayList<Thread> noThreads = new ArrayList<Thread>();
                    downloadThreads.put(item.getExpansionId(), noThreads);

                }
            }

            // the following migration stuff is currently piggy-backing on the index update stuff

            // if found, migrate installed index

            File installedFile = new File(StorageHelper.getActualStorageDirectory(this), "installed_index.json");

            if (installedFile.exists()) {
                HashMap<String, scal.io.liger.model.ExpansionIndexItem> installedItemsFromFile = scal.io.liger.IndexManager.loadInstalledIdIndex(this);

                if (installedItemsFromFile.size() == 0) {
                    Timber.d("NOTHING LOADED FROM INSTALLED INDEX FILE");
                } else {
                    for (scal.io.liger.model.ExpansionIndexItem item : installedItemsFromFile.values()) {
                        Timber.d("ADDING " + item.getExpansionId() + " TO DATABASE (INSTALLED)");
                        installedIndexItemDao.addInstalledIndexItem(item, true); // replaces existing items, should trigger updates to installed items and table as needed
                    }
                }

                installedFile.delete();
            } else {
                Timber.d("NO INSTALLED INDEX FILE");
            }

            // if found, migrate instance index

            File instanceFile = new File(StorageHelper.getActualStorageDirectory(this), "instance_index.json");

            if (instanceFile.exists()) {
                HashMap<String, scal.io.liger.model.InstanceIndexItem> instanceItemsFromFile = scal.io.liger.IndexManager.loadInstanceIndex(this);

                if (instanceItemsFromFile.size() == 0) {
                    Timber.d("NOTHING LOADED FROM INSTANCE INDEX FILE");
                } else {
                    for (scal.io.liger.model.InstanceIndexItem item : instanceItemsFromFile.values()) {
                        Timber.d("ADDING " + item.getInstanceFilePath() + " TO DATABASE (INSTANCE)");
                        instanceIndexItemDao.addInstanceIndexItem(item, true); // replaces existing items, should trigger updates to installed items and table as needed
                    }
                }

                instanceFile.delete();
            } else {
                Timber.d("NO INSTANCE INDEX FILE");
            }

            // update preferences

            preferences.edit().putInt("AVAILABLE_INDEX_VERSION", scal.io.liger.Constants.AVAILABLE_INDEX_VERSION).commit();

            if (getIntent() != null && getIntent().hasExtra("showlauncher")) {
                if (getIntent().getBooleanExtra("showlauncher", false)) {
                    showLauncherIcon();
                }
            }
        }



        // dumb test

        // check values
        availableIndexItemDao.getAvailableIndexItems().take(1).subscribe(new Action1<List<AvailableIndexItem>>() {

            @Override
            public void call(List<AvailableIndexItem> expansionIndexItems) {

                // just process the list

                for (ExpansionIndexItem item : expansionIndexItems) {
                    Timber.d("AVAILABLE ITEM " + item.getExpansionId() + ", TITLE: " + item.getTitle());
                }
            }
        });

        installedIndexItemDao.getInstalledIndexItems().take(1).subscribe(new Action1<List<InstalledIndexItem>>() {

            @Override
            public void call(List<InstalledIndexItem> expansionIndexItems) {

                // just process the list

                for (ExpansionIndexItem item : expansionIndexItems) {
                    Timber.d("INSTALLED ITEM " + item.getExpansionId() + ", TITLE: " + item.getTitle());
                }
            }
        });



        // file cleanup
        File actualStorageDirectory = StorageHelper.getActualStorageDirectory(this);

        if (actualStorageDirectory != null) {
            JsonHelper.cleanup(actualStorageDirectory.getPath());
        } else {
            // this is an error, will deal with it below
        }

        // default
        loggedIn = false;

        // set title bar as a reminder if test server is specified
        //getActionBar().setTitle(Utils.getAppName(this));

        if (actualStorageDirectory != null) {
            // NEW/TEMP
            // DOWNLOAD AVAILABE INDEX FOR CURRENT USER AND SAVE TO TARGET FILE
            // NEED TO ACCOUNT FOR POSSIBLE MISSING INDEX
            IndexTask iTask = new IndexTask(this, true); // force download at startup (maybe only force on a timetable?)
            iTask.execute();
        } else {
            //show storage error message
            new AlertDialog.Builder(this)
                    .setTitle(Utils.getAppName(this))
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setMessage(R.string.err_storage_not_available)
                    .show();
        }

        // we want to grab required updates without restarting the app
        // integrate with index task
        // if (!DownloadHelper.checkAndDownload(this)) {
        //     Toast.makeText(this, "Downloading content and/or updating installed files", Toast.LENGTH_LONG).show(); // FIXME move to strings.xml
        // }

        // i don't think we ever want to do this
        // IndexManager.copyInstalledIndex(this);

        //setContentView(R.layout.activity_home);

        //mRecyclerView = (RecyclerView) findViewById(scal.io.liger.R.id.recyclerView);
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        //mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        //    @Override
        //    public void onRefresh() {
        //        IndexTask iTask = new IndexTask(HomeActivity.this, true); // force download on manual refresh
        //        iTask.execute();
        //    }
        //});


        //mTabMenu = getMenu("home");
        // action bar stuff
        getActionBar().setDisplayHomeAsUpEnabled(true);

        checkForTor();

        checkForUpdates();

    }
    

    public static void launchLiger(Context context, String splId, String instancePath, String splPath) {

        // TEMP - do we need to check files for anything besides the default library?
        /*
        if (!DownloadHelper.checkAllFiles(context)) { // FIXME the app should define these, not the library
            Toast.makeText(context, "Please wait for the content pack to finish downloading", Toast.LENGTH_LONG).show(); // FIXME move to strings.xml
            return;
        }
        */

        if ((splId != null) && (splId.equals("default_library"))) {

            // initiate check/download for main/patch expansion files
            boolean readyToOpen = StorymakerDownloadHelper.checkAndDownloadNew(context);

            if (!readyToOpen) {
                // if file is being downloaded, don't open
                Timber.d("CURRENTLY DOWNLOADING FILE");

                Toast.makeText(context, context.getString(R.string.home_please_wait), Toast.LENGTH_LONG).show();
                return;
            }

        }

        Intent ligerIntent = new Intent(context, MainActivity.class);
        ligerIntent.putExtra(MainActivity.INTENT_KEY_WINDOW_TITLE, Utils.getAppName(context));
        String lang = StoryMakerApp.getCurrentLocale().getLanguage();
        ligerIntent.putExtra("lang", lang);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        int pslideduration = Integer.parseInt(settings.getString("pslideduration", "5"));
        ligerIntent.putExtra("photo_essay_slide_duration", pslideduration * 1000);
        if (splId != null && !splId.isEmpty()) {
            ligerIntent.putExtra(MainActivity.INTENT_KEY_STORYPATH_LIBRARY_ID, splId);
        } else if (splPath != null && !splPath.isEmpty()) {
            ligerIntent.putExtra(MainActivity.INTENT_KEY_STORYPATH_LIBRARY_PATH, splPath);
        } else if (instancePath != null && !instancePath.isEmpty()) {
            ligerIntent.putExtra(MainActivity.INTENT_KEY_STORYPATH_INSTANCE_PATH, instancePath);
        }
        context.startActivity(ligerIntent);
    }

    protected class IndexTask extends AsyncTask<Void, Void, Boolean> {

        // TODO: ADJUST THIS TO ACCOUNT FOR STORING AVAILABLE INDEX IN DB

        private Context mContext;
        private boolean forceDownload;

        public IndexTask(Context context, boolean forceDownload) {
            this.mContext = context;
            this.forceDownload = forceDownload;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Timber.d("IndexTask.doInBackground IS RUNNING");

            boolean loginRequest = false;

            ServerManager sm = StoryMakerApp.getServerManager();

            if (mCacheWordHandler.isLocked()) {

                // prevent credential check attempt if database is locked
                Timber.d("cacheword locked, skipping index credential check");
                // user is not logged in, update status flag if necessary
                if (loggedIn) {
                    loggedIn = false;
                }

            } else if (sm.hasCreds()) {
                // user is logged in, update status flag if necessary
                if (!loggedIn) {
                    loggedIn = true;
                    loginRequest = true; // user just logged in, need to check server
                }
            } else {
                // user is not logged in, update status flag if necessary
                if (loggedIn) {
                    loggedIn = false;
                }
            }

            // check server if user just logged in
            if (loginRequest) {
                Timber.d("USER LOGGED IN, CHECK SERVER");

                // reset available index
                StorymakerIndexManager.copyAvailableIndex(mContext, false);

                // attempt to download new assignments
                return Boolean.valueOf(sm.index());
            }

            // check server if user insists (if database is unlocked)
            if (forceDownload && !mCacheWordHandler.isLocked()) {
                Timber.d("UPDATE REQUIRED, CHECK SERVER");

                // reset available index
                StorymakerIndexManager.copyAvailableIndex(mContext, false);

                // attempt to download new assignments
                return Boolean.valueOf(sm.index());
            }

            // no-op
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (result.booleanValue()) {
                Timber.d("DOWNLOADED ASSIGNMENTS AND UPDATED AVAILABLE INDEX");
            } else {
                Timber.d("DID NOT DOWNLOAD ASSIGNMENTS OR UPDATE AVAILABLE INDEX");
            }

            //RES
            //mSwipeRefreshLayout.setRefreshing(false);

            // resolve available/installed conflicts and grab updates if needed
            if (!StorymakerDownloadHelper.checkAndDownload(mContext, availableIndexItemDao, installedIndexItemDao, queueItemDao)) {
                Toast.makeText(mContext, getString(R.string.home_downloading_content), Toast.LENGTH_LONG).show();
            }
            // refresh regardless (called from onResume and OnRefreshListener)
            initActivityList();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    //if the user hasn't registered with the user, show the login screen
    private void checkCreds() {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String user = settings.getString("user", null);

        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }


    public void launchNewProject() {
        // need to check this to determine whether there is a storage issue that will cause a crash
        File actualStorageDirectory = StorageHelper.getActualStorageDirectory(this);

        if (actualStorageDirectory != null) {
            launchLiger(this, "default_library", null, null);
        } else {
            //show storage error message
            new AlertDialog.Builder(this)
                    .setTitle(Utils.getAppName(this))
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setMessage(R.string.err_storage_not_available)
                    .show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            toggleDrawer();
            return true;
//        } else if (item.getItemId() == R.id.menu_new_project) {
//            // need to check this to determine whether there is a storage issue that will cause a crash
//            File actualStorageDirectory = StorageHelper.getActualStorageDirectory(this);
//
//            if (actualStorageDirectory != null) {
//                launchNewProject();
//            } else {
//                //show storage error message
//                new AlertDialog.Builder(this)
//                        .setTitle(Utils.getAppName(this))
//                        .setIcon(android.R.drawable.ic_dialog_info)
//                        .setMessage(R.string.err_storage_not_available)
//                        .show();
//            }
//
//            return true;
        } else if (item.getItemId() == R.id.menu_about) {
            String url = "https://storymaker.org";

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        } else if (item.getItemId() == R.id.menu_hide) {
            hideLauncherIcon();
        }

        return super.onOptionsItemSelected(item);
    }

    protected abstract void initActivityList();

    protected void checkForUpdates() {
        if (BuildConfig.DEBUG) {
            UpdateManager.register(this, AppConstants.HOCKEY_APP_ID);
        }
    }

    protected void checkForTor() {
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

         boolean useTor = settings.getBoolean("pusetor", false);

         if (useTor) {

             if (!OrbotHelper.isOrbotInstalled(this)) {
                startActivity(OrbotHelper.getOrbotInstallIntent(this));
             } else if (!OrbotHelper.isOrbotRunning(this)) {
                OrbotHelper.requestStartTor(this);
             }
         }
    }

    protected static final ComponentName LAUNCHER_COMPONENT_NAME = new ComponentName(
            "org.storymaker.app", "org.storymaker.app.Launcher");

    protected void hideLauncherIcon() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important!");
        builder.setMessage("This will hide the app's icon in the launcher.\n\nTo show the app again, dial phone number 98765.");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                getPackageManager().setComponentEnabledSetting(LAUNCHER_COMPONENT_NAME,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);

                finish();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        builder.setCancelable(true);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }

    protected void showLauncherIcon() {
        getPackageManager().setComponentEnabledSetting(LAUNCHER_COMPONENT_NAME,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    protected boolean isLauncherIconVisible() {
        int enabledSetting = getPackageManager()
                .getComponentEnabledSetting(LAUNCHER_COMPONENT_NAME);
        return enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    }

    protected void checkForCrashes() {
        //CrashManager.register(this, AppConstants.HOCKEY_APP_ID);
        CrashManager.register(this, AppConstants.HOCKEY_APP_ID, new CrashManagerListener() {
            public String getDescription() {
                String description = "";

                try {
                    //Process process = Runtime.getRuntime().exec("logcat -d HockeyApp:D *:S");
                    Process process = Runtime.getRuntime().exec("logcat -d");
                    BufferedReader bufferedReader =
                            new BufferedReader(new InputStreamReader(process.getInputStream()));

                    StringBuilder log = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        log.append(line);
                        log.append(System.getProperty("line.separator"));
                    }
                    bufferedReader.close();

                    description = log.toString();
                } catch (IOException e) {
                }

                return description;
            }
        });
    }

    protected void showPreferences ()
    {
        Intent intent = new Intent(this,SimplePreferences.class);
        this.startActivityForResult(intent, 9999);
    }

    public class PauseListener implements DialogInterface.OnClickListener {

        private scal.io.liger.model.sqlbrite.ExpansionIndexItem eItem;

        public PauseListener(scal.io.liger.model.sqlbrite.ExpansionIndexItem eItem) {
            super();

            this.eItem = eItem;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {

            Timber.d("PAUSE...");

            // stop associated threads

            ArrayList<Thread> currentThreads = downloadThreads.get(eItem.getExpansionId());

            if (currentThreads != null) {
                for (Thread thread : currentThreads) {
                    Timber.d("STOPPING THREAD " + thread.getId());
                    thread.interrupt();
                }
            }

            downloadThreads.remove(eItem.getExpansionId());

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getActionBar().setTitle(Utils.getAppName(this));

        checkForCrashes();

        //if (!DownloadHelper.checkAllFiles(this) && downloadPoller == null) {
        // integrate with index task
        //if (!DownloadHelper.checkAndDownload(this)) {
        // don't poll, just pop up message if a download was initiated
        //downloadPoller = new DownloadPoller();
        //downloadPoller.execute("foo");
        //    Toast.makeText(this, "Downloading content and/or updating installed files", Toast.LENGTH_LONG).show(); // FIXME move to strings.xml
        //} //else {
        // merge this with index task
        //   initActivityList();

        // need to check this to determine whether there is a storage issue that will cause a crash
        File actualStorageDirectory = StorageHelper.getActualStorageDirectory(this);

        if (actualStorageDirectory != null) {
            IndexTask iTask = new IndexTask(this, false); // don't force download on resume (currently triggers only on login)
            iTask.execute();
        } else {
            //show storage error message
            new AlertDialog.Builder(this)
                    .setTitle(Utils.getAppName(this))
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setMessage(R.string.err_storage_not_available)
                    .show();
        }

        //}

        boolean isExternalStorageReady = Utils.Files.isExternalStorageReady();

        if (!isExternalStorageReady) {
            //show storage error message
            new AlertDialog.Builder(this)
                    .setTitle(Utils.getAppName(this))
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setMessage(R.string.err_storage_not_ready)
                    .show();

        }

        if (getIntent() != null && getIntent().hasExtra("showlauncher")) {
            if (getIntent().getBooleanExtra("showlauncher", false)) {
                showLauncherIcon();
            }
        }
    }

    protected void showSPLSelectorPopup(final String[] names, final String[] paths) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Choose Story File(SdCard/Liger/)").setItems(names, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                launchLiger(BaseHomeActivity.this, null, null, paths[index]);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    // HAD TO SPLIT OUT INTO A METHOD
    public void handleClick(ExpansionIndexItem eItem, HashMap<String, ExpansionIndexItem> installedIds, boolean showDialog) {

        // initiate check/download whether installed or not
        HashMap<String, Thread> newThreads = StorymakerDownloadHelper.checkAndDownload(BaseHomeActivity.this, eItem, installedIndexItemDao, queueItemDao, true); // <- THIS SHOULD PICK UP EXISTING PARTIAL FILES
        // <- THIS ALSO NEEDS TO NOT INTERACT WITH THE INDEX
        // <- METADATA UPDATE SHOULD HAPPEN WHEN APP IS INITIALIZED

        // if any download threads were initiated, item is not ready to open

        boolean readyToOpen = true;

        if (newThreads.size() > 0) {
            readyToOpen = false;

            // update stored threads for index item

            ArrayList<Thread> currentThreads = downloadThreads.get(eItem.getExpansionId());

            if (currentThreads == null) {
                currentThreads = new ArrayList<Thread>();
            }

            for (Thread thread : newThreads.values()) {
                currentThreads.add(thread);
            }

            downloadThreads.put(eItem.getExpansionId(), currentThreads);
        }

        if (!installedIds.containsKey(eItem.getExpansionId())) {

            // if clicked item is not installed, update index
            // un-installed AvailableIndexItems need to be converted to InstalledIndexItems
            InstalledIndexItem iItem = new InstalledIndexItem(eItem);

            java.util.Date thisDate = new java.util.Date();
            iItem.setCreationDate(thisDate);
            iItem.setLastModifiedDate(thisDate);
            iItem.setCreationDate(thisDate);

            StorymakerIndexManager.installedIndexAdd(BaseHomeActivity.this, iItem, installedIndexItemDao);

            Timber.d(eItem.getExpansionId() + " NOT INSTALLED, ADDING ITEM TO INDEX");

            // wait for index serialization
            try {
                synchronized (this) {
                    wait(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {

            Timber.d(eItem.getExpansionId() + " INSTALLED, CHECKING FILE");

            // if clicked item is installed, check state
            if (readyToOpen) {

                // clear saved threads
                if (downloadThreads.get(eItem.getExpansionId()) != null) {
                    downloadThreads.remove(eItem.getExpansionId());
                }

                // update db record with flag
                if (!eItem.isInstalled()) {
                    Timber.d("SET INSTALLED FLAG FOR " + eItem.getExpansionId());
                    eItem.setInstalledFlag(true);
                    InstalledIndexItem iItem = new InstalledIndexItem(eItem);
                    StorymakerIndexManager.installedIndexAdd(this, iItem, installedIndexItemDao);
                }

                // if file has been downloaded, open file
                Timber.d(eItem.getExpansionId() + " INSTALLED, FILE OK");

                // update with new thumbnail path
                // move this somewhere that it can be triggered by completed download?
                ContentPackMetadata metadata = scal.io.liger.IndexManager.loadContentMetadata(BaseHomeActivity.this,
                        eItem.getPackageName(),
                        eItem.getExpansionId(),
                        StoryMakerApp.getCurrentLocale().getLanguage());

                if (metadata == null) {
                    Toast.makeText(BaseHomeActivity.this, getString(R.string.home_metadata_missing), Toast.LENGTH_LONG).show();
                    Timber.e("failed to load content metadata");
                } else if ((eItem.getThumbnailPath() == null) || (!eItem.getThumbnailPath().equals(metadata.getContentPackThumbnailPath()))) {

                    Timber.d(eItem.getExpansionId() + " FIRST OPEN, UPDATING THUMBNAIL PATH");

                    eItem.setThumbnailPath(metadata.getContentPackThumbnailPath());

                    // un-installed AvailableIndexItems need to be converted to InstalledIndexItems
                    InstalledIndexItem iItem = new InstalledIndexItem(eItem);
                    StorymakerIndexManager.installedIndexAdd(BaseHomeActivity.this, iItem, installedIndexItemDao);

                    // wait for index serialization
                    try {
                        synchronized (this) {
                            wait(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                ArrayList<scal.io.liger.model.InstanceIndexItem> contentIndex = scal.io.liger.IndexManager.loadContentIndexAsList(BaseHomeActivity.this,
                        eItem.getPackageName(),
                        eItem.getExpansionId(),
                        StoryMakerApp.getCurrentLocale().getLanguage());

                if ((contentIndex == null) || (contentIndex.size() < 1)) {
                    Toast.makeText(BaseHomeActivity.this, getString(R.string.home_index_missing), Toast.LENGTH_LONG).show();
                    Timber.e("failed to load content index");
                } else if (contentIndex.size() == 1) {
                    launchLiger(BaseHomeActivity.this, null, null, contentIndex.get(0).getInstanceFilePath());
                } else {
                    String[] names = new String[contentIndex.size()];
                    String[] paths = new String[contentIndex.size()];
                    int i = 0;
                    for (scal.io.liger.model.InstanceIndexItem item : contentIndex) {
                        names[i] = item.getTitle();
                        paths[i] = item.getInstanceFilePath();
                        i++;
                    }
                    showSPLSelectorPopup(names, paths);
                }
            } else {
                // if file is being downloaded, don't open
                Timber.d(eItem.getExpansionId() + " INSTALLED, CURRENTLY DOWNLOADING FILE");

                // if necessary, un-flag db record (this probably indicates an installed file that is being patched
                if (eItem.isInstalled()) {
                    Timber.d("UN-SET INSTALLED FLAG FOR " + eItem.getExpansionId());
                    eItem.setInstalledFlag(false);
                    InstalledIndexItem iItem = new InstalledIndexItem(eItem);
                    StorymakerIndexManager.installedIndexAdd(this, iItem, installedIndexItemDao);
                }

                // create pause/cancel dialog

                if (showDialog) {
                    new AlertDialog.Builder(BaseHomeActivity.this)
                            .setTitle(R.string.stop_download)
                            .setMessage(eItem.getTitle())
                            .setNegativeButton(getString(R.string.cancel), null)
                            .setNeutralButton(getString(R.string.pause), new PauseListener(eItem))
                            .setPositiveButton(getString(R.string.stop), new CancelListener(eItem))
                            .show();
                }

                // Toast.makeText(HomeActivity.this, "Please wait for this content pack to finish downloading", Toast.LENGTH_LONG).show(); // FIXME move to strings.xml
            }
        }


    }


    public class CancelListener implements DialogInterface.OnClickListener {

        private ExpansionIndexItem eItem;

        public CancelListener(ExpansionIndexItem eItem) {
            super();

            this.eItem = eItem;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {

            Timber.d("CANCEL...");
            // remove from installed index

            // un-installed AvailableIndexItems need to be converted to InstalledIndexItems
            InstalledIndexItem iItem = new InstalledIndexItem(eItem);
            StorymakerIndexManager.installedIndexRemove(BaseHomeActivity.this, iItem, installedIndexItemDao);

            // stop associated threads and delete associated files

            ArrayList<Thread> currentThreads = downloadThreads.get(eItem.getExpansionId());

            if (currentThreads != null) {
                for (Thread thread : currentThreads) {
                    Timber.d("STOPPING THREAD " + thread.getId());
                    thread.interrupt();
                }
            }

            downloadThreads.remove(eItem.getExpansionId());

            Timber.d("DELETE STUFF?");

            File fileDirectory = StorageHelper.getActualStorageDirectory(BaseHomeActivity.this);
            WildcardFileFilter fileFilter = new WildcardFileFilter(eItem.getExpansionId() + ".*");
            for (File foundFile : FileUtils.listFiles(fileDirectory, fileFilter, null)) {
                Timber.d("STOPPED THREAD: FOUND " + foundFile.getPath() + ", DELETING");
                FileUtils.deleteQuietly(foundFile);
            }
        }
    }

    public void updateInstanceIndexItemLastOpenedDate(InstanceIndexItem item) {
        java.util.Date thisDate = new java.util.Date();
        //Log.d("BaseHomeActivity", "setLastOpenedDate " + thisDate.toString());
        instanceIndexItemDao.updateInstanceItemLastOpenedDate(item, thisDate);
    }

    //this method loops through a HashMap of ExpansionIndexItems
    //      and returns the ones that have a certain content type i.e. "guide", "lesson", "template"
    //      used to filter an existing query set rather than running extra queries with getAvailableIndexItemsByType() or getInstalledIndexItemsByType()
    public static ArrayList<String> getIndexItemIdsByType(HashMap<String, ExpansionIndexItem> installedIds, String type) {

        ArrayList<String> indexItemIds = new ArrayList<String>();

        for (String key : installedIds.keySet()) {

            ExpansionIndexItem item = installedIds.get(key);

            if (item.getContentType().equals(type)) {
                indexItemIds.add(key);
            }

        }

        return indexItemIds;

    }


}
