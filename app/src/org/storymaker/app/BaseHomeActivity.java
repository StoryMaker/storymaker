package org.storymaker.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.hannesdorfmann.sqlbrite.dao.DaoManager;

import net.hockeyapp.android.UpdateManager;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import org.storymaker.app.model.Project;
import org.storymaker.app.server.ServerManager;
import org.storymaker.app.ui.SlidingTabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import info.guardianproject.netcipher.proxy.OrbotHelper;
import rx.functions.Action1;
import scal.io.liger.JsonHelper;
import scal.io.liger.MainActivity;
import scal.io.liger.StorageHelper;
import scal.io.liger.StorymakerIndexManager;
import scal.io.liger.model.sqlbrite.AvailableIndexItem;
import scal.io.liger.model.sqlbrite.AvailableIndexItemDao;
import scal.io.liger.model.sqlbrite.ExpansionIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItemDao;
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
    protected int dbVersion = 1;

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

    protected abstract void initActivityList();

    private void checkForUpdates() {
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
}
