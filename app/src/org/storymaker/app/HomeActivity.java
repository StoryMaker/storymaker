






package org.storymaker.app;

import timber.log.Timber;

import timber.log.Timber;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hannesdorfmann.sqlbrite.dao.DaoManager;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.UpdateManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.storymaker.app.model.Project;
import org.storymaker.app.server.LoginActivity;
import org.storymaker.app.server.ServerManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import info.guardianproject.onionkit.ui.OrbotHelper;
import rx.functions.Action1;
import scal.io.liger.IndexManager;
import scal.io.liger.JsonHelper;
import scal.io.liger.MainActivity;
import scal.io.liger.StorageHelper;
import scal.io.liger.StorymakerIndexManager;
import scal.io.liger.model.ContentPackMetadata;
import scal.io.liger.model.StoryPathLibrary;
import scal.io.liger.model.sqlbrite.AvailableIndexItem;
import scal.io.liger.model.sqlbrite.AvailableIndexItemDao;
import scal.io.liger.model.sqlbrite.BaseIndexItem;
import scal.io.liger.model.sqlbrite.ExpansionIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItemDao;
import scal.io.liger.model.sqlbrite.InstanceIndexItem;
import scal.io.liger.model.sqlbrite.InstanceIndexItemDao;
import scal.io.liger.model.sqlbrite.QueueItemDao;

//import scal.io.liger.DownloadHelper;
//import scal.io.liger.IndexManager;
//import scal.io.liger.model.BaseIndexItem;
//import scal.io.liger.model.ExpansionIndexItem;
//import scal.io.liger.model.InstanceIndexItem;
//import com.google.analytics.tracking.android.GoogleAnalytics;

public class HomeActivity extends BaseActivity {

    private final static String TAG = "HomeActivity";

    private ProgressDialog mLoading;
    private ArrayList<Project> mListProjects;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    // private DownloadPoller downloadPoller = null;

    private boolean loggedIn;

    // new stuff
    private InstanceIndexItemDao instanceIndexItemDao;
    private AvailableIndexItemDao availableIndexItemDao;
    private InstalledIndexItemDao installedIndexItemDao;
    private QueueItemDao queueItemDao;
    private DaoManager daoManager;
    private int dbVersion = 1;

    private HashMap<String, ArrayList<Thread>> downloadThreads = new HashMap<String, ArrayList<Thread>>();

    public void removeThreads(String id) {
        if (downloadThreads.containsKey(id)) {
            downloadThreads.remove(id);
        }
    }

    // must set dao stuff in constructor?
    public HomeActivity() {

        instanceIndexItemDao = new InstanceIndexItemDao();
        availableIndexItemDao = new AvailableIndexItemDao();
        installedIndexItemDao = new InstalledIndexItemDao();
        queueItemDao = new QueueItemDao();

        daoManager = new DaoManager(HomeActivity.this, "Storymaker.db", dbVersion, instanceIndexItemDao, availableIndexItemDao, installedIndexItemDao, queueItemDao);
        daoManager.setLogging(false);

    }

    // added for testing
    public void scroll(int position) {
        Timber.d("Scrolling to index item " + position);
        mRecyclerView.scrollToPosition(position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // copy index file
        StorymakerIndexManager.copyAvailableIndex(this, false); // TODO: REPLACE THIS WITH INDEX DOWNLOAD (IF LOGGED IN) <- NEED TO COPY FILE FOR BASELINE CONTENT

        // initialize db

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

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
        getActionBar().setTitle(Utils.getAppName(this));

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

        setContentView(R.layout.activity_home);
        mRecyclerView = (RecyclerView) findViewById(scal.io.liger.R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                IndexTask iTask = new IndexTask(HomeActivity.this, true); // force download on manual refresh
                iTask.execute();
            }
        });

        // action bar stuff
        getActionBar().setDisplayHomeAsUpEnabled(true);

        checkForTor();

        checkForUpdates();

    }

    private class IndexTask extends AsyncTask<Void, Void, Boolean> {

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

            if (sm.hasCreds()) {
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

            // check server if user insists
            if (forceDownload) {
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

            mSwipeRefreshLayout.setRefreshing(false);
            // resolve available/installed conflicts and grab updates if needed
            if (!StorymakerDownloadHelper.checkAndDownload(mContext, availableIndexItemDao, installedIndexItemDao, queueItemDao)) {
                Toast.makeText(mContext, getString(R.string.home_downloading_content), Toast.LENGTH_LONG).show();
            }
            // refresh regardless (called from onResume and OnRefreshListener)
            initActivityList();
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
		
		if (!isExternalStorageReady)
		{
			//show storage error message
			new AlertDialog.Builder(this)
            .setTitle(Utils.getAppName(this))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setMessage(R.string.err_storage_not_ready)
            .show();
			
		}
	}

    public static String parseInstanceDate(String filename) {
//        String jsonFilePath = storyPath.buildTargetPath(storyPath.getId() + "-instance-" + timeStamp.getTime() + ".json");
        String[] splits = FilenameUtils.removeExtension(filename).split("-");
        return splits[splits.length-1]; // FIXME make more robust and move into liger
    }

    // copied this as a short term fix until we get loading cleanly split out from the liger sample app ui stuff
    private StoryPathLibrary initSPLFromJson(String json, String jsonPath) {
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

    private void initActivityList () {
        // menu items now locked during downloads, i think this can be removed
        /*
        if (!DownloadHelper.checkAllFiles(this)) { // FIXME the app should define these, not the library
            Toast.makeText(this, "Please wait for the content pack to finish downloading and reload the app", Toast.LENGTH_LONG).show(); // FIXME move to strings.xml
            return;
        }
        */

        JsonHelper.setupFileStructure(this);

        // NEW: load instance index
        String lang = StoryMakerApp.getCurrentLocale().getLanguage();
        Timber.d("lang returned from getCurrentLocale: " + lang);
        HashMap<String, InstanceIndexItem> instanceIndex = StorymakerIndexManager.fillInstanceIndex(HomeActivity.this, StorymakerIndexManager.loadInstanceIndex(HomeActivity.this, instanceIndexItemDao), lang, instanceIndexItemDao);

        // FIXME --- this should only happen on app updates in a migration
        if (instanceIndex.size() > 0) {
            Timber.d("INITACTIVITYLIST - FOUND INSTANCE INDEX WITH " + instanceIndex.size() + " ITEMS");


            // dumb test

            // put in values
            /*
            for (InstanceIndexItem item : instanceIndex.values()) {
                instanceIndexItemDao.addInstanceIndexItem(item);
            }

            // read out values
            instanceIndexItemDao.getInstanceIndexItems().subscribe(new Action1<List<org.storymaker.app.db.InstanceIndexItem>>() {

                @Override
                public void call(List<org.storymaker.app.db.InstanceIndexItem> instanceIndexItems) {

                    // just process the list

                    for (org.storymaker.app.db.InstanceIndexItem item : instanceIndexItems) {
                        Timber.d("GOT ITEM " + item.getId() + ", TITLE: " + item.getTitle());
                    }
                }
            });
            */


        } else {
            Timber.d("INITACTIVITYLIST - FOUND INSTANCE INDEX WITH NO ITEMS");
        }

        ArrayList<BaseIndexItem> instances = new ArrayList<BaseIndexItem>(instanceIndex.values());

        HashMap<String, ExpansionIndexItem> availableIds = StorymakerIndexManager.loadAvailableIdIndex(this, availableIndexItemDao);
        HashMap<String, ExpansionIndexItem> installedIds = StorymakerIndexManager.loadInstalledIdIndex(this, installedIndexItemDao);

        for (String id : availableIds.keySet()) {
            if (installedIds.keySet().contains(id)) {
                // if the available item has been installed, add the corresponding item from the installed index
                instances.add(installedIds.get(id));
            } else {
                // if the available item has not been installed, add the item from the available index
                //instances.add(availableIds.get(id)); // FIXME temporarily commenting this out, we could much more gracefully do this now that we only care about installed items and stories
            }
        }

        if (instances.size() > 0) {

            Collections.sort(instances, Collections.reverseOrder()); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList

            mRecyclerView.setAdapter(new InstanceIndexItemAdapter(instances, new InstanceIndexItemAdapter.BaseIndexItemSelectedListener() {
                @Override
                public void onStorySelected(BaseIndexItem selectedItem) {

                    if (selectedItem instanceof InstanceIndexItem) {
                        launchLiger(HomeActivity.this, null, ((InstanceIndexItem) selectedItem).getInstanceFilePath(), null);
                    } else {

                        Timber.d("CLICKED AN ITEM");

                        // get clicked item
                        final ExpansionIndexItem eItem = ((ExpansionIndexItem) selectedItem);

                        // get installed items
                        final HashMap<String, ExpansionIndexItem> installedIds = StorymakerIndexManager.loadInstalledIdIndex(HomeActivity.this, installedIndexItemDao);

                        // this isn't ideal but pushing an alert dialog down into the check/download process is difficult

                        if ((downloadThreads.get(eItem.getExpansionId()) != null)) {
                            Timber.d("DIALOG - FOUND THREADS: " + downloadThreads.get(eItem.getExpansionId()).size());
                        }

                        if (!installedIds.containsKey(eItem.getExpansionId()) && (downloadThreads.get(eItem.getExpansionId()) == null)) {

                            Timber.d("DIALOG - START");

                            // this item is not installed and there are no saved threads for it

                            new AlertDialog.Builder(HomeActivity.this)
                                    .setTitle(R.string.download_content_pack)
                                    .setMessage(eItem.getTitle() + " (" + ((eItem.getExpansionFileSize() + eItem.getPatchFileSize()) / 1048576) + " MB)") // FIXME we need to flip this for RTL
                                            // using negative button to account for fixed order
                                    .setPositiveButton(getString(R.string.download), new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            Timber.d("START...");

                                            handleClick(eItem, installedIds, false);

                                        }
                                    })
                                    .setNegativeButton(getString(R.string.cancel), null)
                                    .show();
                        } else if (installedIds.containsKey(eItem.getExpansionId()) && (downloadThreads.get(eItem.getExpansionId()) == null)) {

                            // do not display dialog options if user selected "use manager"
                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
                            boolean useManager = settings.getBoolean("pusedownloadmanager", false);

                            if (useManager) {

                                Timber.d("USING MANAGER - NO DIALOG");

                                handleClick(eItem, installedIds, false);

                            } else {

                                Timber.d("DIALOG - RESUME");

                                // this item is installed and there are no saved threads for it

                                // if item is flagged, download finished, do not prompt to resume
                                if (eItem.isInstalled()) {

                                    // proceed as usual

                                    Timber.d("DO STUFF");

                                    handleClick(eItem, installedIds, true);
                                } else {

                                    new AlertDialog.Builder(HomeActivity.this)
                                            .setTitle(getString(R.string.resume_download))
                                            .setMessage(eItem.getTitle())
                                            .setPositiveButton(getString(R.string.resume), new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Timber.d("RESUME...");
                                                    handleClick(eItem, installedIds, false);
                                                }
                                            })
                                            .setNegativeButton(getString(R.string.cancel), null)
                                            .setNeutralButton(getString(R.string.stop), new CancelListener(eItem))
                                            .show();
                                }
                            }
                        } else {

                            // proceed as usual

                            // do not display dialog options if user selected "use manager"
                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
                            boolean useManager = settings.getBoolean("pusedownloadmanager", false);

                            if (useManager) {

                                Timber.d("USING MANAGER - NO DIALOG");

                                handleClick(eItem, installedIds, false);

                            } else {

                                Timber.d("CONTINUE...");

                                handleClick(eItem, installedIds, true);

                            }
                        }
                    }
                }
            }, installedIndexItemDao));
        } else {
            // empty list
            TextView textView = (TextView) findViewById(R.id.textViewEmptyState);
            textView.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setVisibility(View.GONE);
        }
    }

    // HAD TO SPLIT OUT INTO A METHOD
    public void handleClick (ExpansionIndexItem eItem, HashMap<String, ExpansionIndexItem> installedIds, boolean showDialog) {

        // initiate check/download whether installed or not
        HashMap<String, Thread> newThreads = StorymakerDownloadHelper.checkAndDownload(HomeActivity.this, eItem, installedIndexItemDao, queueItemDao, true); // <- THIS SHOULD PICK UP EXISTING PARTIAL FILES
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
            StorymakerIndexManager.installedIndexAdd(HomeActivity.this, iItem, installedIndexItemDao);

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
                ContentPackMetadata metadata = scal.io.liger.IndexManager.loadContentMetadata(HomeActivity.this,
                        eItem.getPackageName(),
                        eItem.getExpansionId(),
                        StoryMakerApp.getCurrentLocale().getLanguage());

                if (metadata == null) {
                    Toast.makeText(HomeActivity.this, getString(R.string.home_metadata_missing), Toast.LENGTH_LONG).show();
                    Timber.e("failed to load content metadata");
                } else if ((eItem.getThumbnailPath() == null) || (!eItem.getThumbnailPath().equals(metadata.getContentPackThumbnailPath()))) {

                    Timber.d(eItem.getExpansionId() + " FIRST OPEN, UPDATING THUMBNAIL PATH");

                    eItem.setThumbnailPath(metadata.getContentPackThumbnailPath());

                    // un-installed AvailableIndexItems need to be converted to InstalledIndexItems
                    InstalledIndexItem iItem = new InstalledIndexItem(eItem);
                    StorymakerIndexManager.installedIndexAdd(HomeActivity.this, iItem, installedIndexItemDao);

                    // wait for index serialization
                    try {
                        synchronized (this) {
                            wait(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                ArrayList<scal.io.liger.model.InstanceIndexItem> contentIndex = scal.io.liger.IndexManager.loadContentIndexAsList(HomeActivity.this,
                        eItem.getPackageName(),
                        eItem.getExpansionId(),
                        StoryMakerApp.getCurrentLocale().getLanguage());

                if ((contentIndex == null) || (contentIndex.size() < 1)) {
                    Toast.makeText(HomeActivity.this, getString(R.string.home_index_missing), Toast.LENGTH_LONG).show();
                    Timber.e("failed to load content index");
                } else if (contentIndex.size() == 1) {
                    launchLiger(HomeActivity.this, null, null, contentIndex.get(0).getInstanceFilePath());
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
                    new AlertDialog.Builder(HomeActivity.this)
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





//
//
//    private String buildZipFilePath(String filePath)
//    {
//        //create datestamp
//        Date date = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
//
//        int index = filePath.lastIndexOf('/');
//        filePath = filePath.substring(0, index + 1);
//
//        return String.format("%sstorymaker_project_%s_%s.zip", filePath, mMPM.mProject.getId(), dateFormat.format(date));
//    }
//
//    private void exportProjectFiles()
//    {
//        try
//        {
//            File fileProjectSrc = MediaProjectManager.getExternalProjectFolder(mMPM.mProject, mMPM.getContext());
//            ArrayList<File> fileList= new ArrayList<File>();
//            String mZipFileName = buildZipFilePath(fileProjectSrc.getAbsolutePath());
//
//            //if not enough space
//            if(!mMPM.checkStorageSpace())
//            {
//                return;
//            }
//
//            String[] mMediaPaths = mMPM.mProject.getMediaAsPathArray();
//
//            //add videos
//            for (String path : mMediaPaths)
//            {
//                fileList.add(new File(path));
//            }
//
//            //add thumbnails
//            fileList.addAll(Arrays.asList(fileProjectSrc.listFiles()));
//
//            //add database file
//            fileList.add(getDatabasePath("sm.db"));
//
//            FileOutputStream fos = new FileOutputStream(mZipFileName);
//            ZipOutputStream zos = new ZipOutputStream(fos);
//
//            exportProjectFiles(zos, fileList.toArray(new File[fileList.size()]));
//
//            zos.close();
//
////            onExportProjectSuccess(mZipFileName); // FIXME TODO
//        }
//        catch (IOException ioe)
//        {
//            Timber.e("Error creating zip file:", ioe);
//        }
//    }
//
//
//    private void exportProjectFiles(ZipOutputStream zos, File[] fileList)
//    {
//        final int BUFFER = 2048;
//
//        for (int i = 0; i < fileList.length; i++)
//        {
//            try
//            {
//                byte[] data = new byte[BUFFER];
//
//                FileInputStream fis = new FileInputStream(fileList[i]);
//                zos.putNextEntry(new ZipEntry(fileList[i].getName()));
//
//                int count;
//                while ((count = fis.read(data, 0, BUFFER)) != -1)
//                {
//                    zos.write(data, 0, count);
//                }
//
//                //close steams
//                zos.closeEntry();
//                fis.close();
//
//            }
//            catch (IOException ioe)
//            {
//                Timber.e("Error creating zip file:", ioe);
//            }
//        }
//    }

    private void showSPLSelectorPopup(final String[] names, final String[] paths) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Choose Story File(SdCard/Liger/)").setItems(names, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                launchLiger(HomeActivity.this, null, null, paths[index]);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


	private void checkForTor ()
    {
    	 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

	     boolean useTor = settings.getBoolean("pusetor", false);
	     
	     if (useTor)
	     {
	    	 OrbotHelper oh = new OrbotHelper(this);
	    	 
	    	 if (!oh.isOrbotInstalled())
	    	 {
	    		 oh.promptToInstall(this);
	    	 }
	    	 else if (!oh.isOrbotRunning())
	    	 {
	    		 oh.requestOrbotStart(this);
	    	 }
	    	 
	     }
    }

    //if the user hasn't registered with the user, show the login screen
    private void checkCreds ()
    {
    	
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
       
        String user = settings.getString("user", null);
        
        if (user == null)
        {
        	Intent intent = new Intent(this,LoginActivity.class);
        	startActivity(intent);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
        {
            toggleDrawer();
            return true;
        }
        else if (item.getItemId() == R.id.menu_new_project)
        {
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

            return true;
        }
        else if (item.getItemId() == R.id.menu_about)
        {
            String url = "https://storymaker.org";

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

	private void showPreferences ()
	{
		Intent intent = new Intent(this,SimplePreferences.class);
		this.startActivityForResult(intent, 9999);
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		
		super.onActivityResult(arg0, arg1, arg2);

		boolean changed = ((StoryMakerApp)getApplication()).checkLocale();
		if (changed)
		{
			finish();
			startActivity(new Intent(this,HomeActivity.class));
			
		}
	}
	
	public class MyAdapter extends FragmentPagerAdapter {
		 
		 int[] mMessages;
		 int[] mTitles;
		 
	        public MyAdapter(FragmentManager fm, int[] titles, int[] messages) {
	            super(fm);
	            mTitles = titles;
	            mMessages = messages;
	        }

	        @Override
	        public int getCount() {
	            return mMessages.length;
	        }

	        @Override
	        public Fragment getItem(int position) {
	        	Bundle bundle = new Bundle();
	        	bundle.putString("title",getString(mTitles[position]));
	        	bundle.putString("msg", getString(mMessages[position]));
	        	
	        	Fragment f = new MyFragment();
	        	f.setArguments(bundle);
	        	
	            return f;
	        }
	    }
	
	public static final class MyFragment extends Fragment {
	
		String mMessage;
		String mTitle;
		
		 /**
       * When creating, retrieve this instance's number from its arguments.
       */
      @Override
      public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);

          mTitle = getArguments().getString("title");
          mMessage = getArguments().getString("msg");
      }

      /**
       * The Fragment's UI is just a simple text view showing its
       * instance number.
       */
      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container,
              Bundle savedInstanceState) {
          
          ViewGroup root = (ViewGroup) inflater.inflate(R.layout.card_pager_textview, null);
          
          ((TextView)root.findViewById(R.id.title)).setText(mTitle);
          
          ((TextView)root.findViewById(R.id.description)).setText(mMessage);
          
          return root;
      }
	
	}
	
	private void checkForCrashes() {
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
                }
                catch (IOException e) {
                }

                return description;
            }
        });
	 }

    private void checkForUpdates() {
        if (BuildConfig.DEBUG) {
            UpdateManager.register(this, AppConstants.HOCKEY_APP_ID);
        }
    }

    public void downloadComplete() {
        //this.downloadPoller = null;
        initActivityList();
        // http://stackoverflow.com/questions/2745061/java-lang-illegalargumentexception-view-not-attached-to-window-manager
        try {
            if ((this.mLoading != null) && this.mLoading.isShowing()) {
                this.mLoading.dismiss();
            }
        } catch (final IllegalArgumentException e) {
            // Handle or log or ignore
        } catch (final Exception e) {
            // Handle or log or ignore
        } finally {
            this.mLoading = null;
        }
    }

    // FIXME once we have a patch as well as a main file this gets a little more complex
    // i think this can be removed, individual menu items are now locked during downloads
    /*
    class DownloadPoller extends AsyncTask<String, Long, Integer> {

        protected void onPreExecute() {
            super.onPreExecute();

            if (mLoading == null || (!mLoading.isShowing())) {
                boolean indeterminate= true;
                float prog = DownloadHelper.getDownloadProgress(HomeActivity.this);
                if (prog != -1.0) {
                    indeterminate = false;
                }
                mLoading = ProgressDialog.show(HomeActivity.this, null, "Downloading content...", indeterminate, true);
                mLoading.setIndeterminate(indeterminate);
                mLoading.setCancelable(false);
                mLoading.setCanceledOnTouchOutside(false);
            }
        }

        protected Integer doInBackground(String... params) {
            while (!DownloadHelper.checkAllFiles(HomeActivity.this)) {
                // TODO add progress
                float prog = DownloadHelper.getDownloadProgress(HomeActivity.this);
                boolean indeterminate = mLoading.isIndeterminate();
                if (!indeterminate && prog >= 0f) {
                    int dialogProg = Math.round(10000 * prog);
                    mLoading.setProgress(dialogProg);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            HomeActivity.this.downloadComplete();
        }
    }
    */

    public class PauseListener implements DialogInterface.OnClickListener {

        private ExpansionIndexItem eItem;

        public PauseListener(ExpansionIndexItem eItem) {
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
            StorymakerIndexManager.installedIndexRemove(HomeActivity.this, iItem, installedIndexItemDao);

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

            File fileDirectory = StorageHelper.getActualStorageDirectory(HomeActivity.this);
            WildcardFileFilter fileFilter = new WildcardFileFilter(eItem.getExpansionId() + ".*");
            for (File foundFile : FileUtils.listFiles(fileDirectory, fileFilter, null)) {
                Timber.d("STOPPED THREAD: FOUND " + foundFile.getPath() + ", DELETING");
                FileUtils.deleteQuietly(foundFile);
            }
        }
    }
}
