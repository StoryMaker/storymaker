package org.storymaker.app;

/**
 * Created by admin on 12/11/15.
 */


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

import org.storymaker.app.ui.SlidingTabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.functions.Action1;
import scal.io.liger.JsonHelper;
import scal.io.liger.StorymakerIndexManager;
import scal.io.liger.model.ContentPackMetadata;
import scal.io.liger.model.sqlbrite.AvailableIndexItem;
import scal.io.liger.model.sqlbrite.BaseIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItem;
import scal.io.liger.model.sqlbrite.InstanceIndexItem;
import timber.log.Timber;

/**
 * Created by admin on 12/8/15.
 */

public class CatalogActivity extends BaseHomeActivity {

    private InstanceIndexItemAdapter myGuidesInstanceIndexItemAdapter;
    private InstanceIndexItemAdapter myLessonsInstanceIndexItemAdapter;
    private InstanceIndexItemAdapter myTemplatesInstanceIndexItemAdapter;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;

    private String mIntentMessage;

    private final static String TAG = "CatalogActivity";
    private int currentPage = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_catalog);

        mTabMenu = getMenu("catalog");

        // Get the message from the intent
        Intent intent = getIntent();
        mIntentMessage = intent.getStringExtra(StoryListFragment.EXTRA_MESSAGE);
        if (mIntentMessage == null) {
            mIntentMessage = "null";
        }
        //Log.d("CatalogActivity", intent.toString()+" "+intent.getStringExtra(StoryListFragment.EXTRA_MESSAGE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadMessageReceiver,
                new IntentFilter("download-complete"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mDeleteMessageReceiver,
                new IntentFilter("delete-complete"));

    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mDownloadMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            final String[] expansionId = new String[1];
            expansionId[0] = intent.getStringExtra("expansionid");
            Log.d("receiver", "catalog download expansion id: " + expansionId + " " + this.toString());

            //updates thumbnail image when item finishes downloading
            availableIndexItemDao.getAvailableIndexItemByKey(expansionId[0]).subscribe(new Action1<List<AvailableIndexItem>>() {

                @Override
                public void call(List<AvailableIndexItem> availableIndexItems) {

                    ArrayList<scal.io.liger.model.sqlbrite.ExpansionIndexItem> indexList = new ArrayList<scal.io.liger.model.sqlbrite.ExpansionIndexItem>();

                    if (availableIndexItems.size() != 1) {
                        Timber.e("can't update thumbnail, unexpected number of available index entries (" + availableIndexItems.size() + ") for " + expansionId[0]);
                    } else {
                        AvailableIndexItem item = availableIndexItems.get(0);

                        ContentPackMetadata metadata = scal.io.liger.IndexManager.loadContentMetadata(CatalogActivity.this,
                                item.getPackageName(),
                                item.getExpansionId(),
                                StoryMakerApp.getCurrentLocale().getLanguage());

                        if (metadata == null) {
                            Timber.e("can't update thumbnail, failed to load metadata for " + expansionId[0]);
                        } else if ((item.getThumbnailPath() == null) || (!item.getThumbnailPath().equals(metadata.getContentPackThumbnailPath()))) {

                            item.setThumbnailPath(metadata.getContentPackThumbnailPath());

                            // un-installed AvailableIndexItems need to be converted to InstalledIndexItems
                            InstalledIndexItem iItem = new InstalledIndexItem(item);
                            java.util.Date thisDate = new java.util.Date();
                            iItem.setCreationDate(thisDate);
                            iItem.setLastModifiedDate(thisDate);
                            iItem.setCreationDate(thisDate);

                            // i think this should be set here, we should only reach this if a download finished and checked out
                            iItem.setInstalledFlag(true);

                            Timber.d("CatalogActivity - updated thumbnail and flagged installed index item for " + iItem.getExpansionId());
                            StorymakerIndexManager.installedIndexAdd(CatalogActivity.this, iItem, installedIndexItemDao);
                        }
                    }
                }
            });

            initActivityList();            //To-do: consider modifying the ViewPager-RecyclerViewers with notifyDataSetChanged() rather than a full page refresh

        }
    };

    private BroadcastReceiver mDeleteMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String expansionId = intent.getStringExtra("expansionid");
            Log.d("receiver", "catalog delete expansion id: " + expansionId + " " + this.toString());

            removeThreads(expansionId);
            initActivityList();             //To-do: consider modifying the ViewPager-RecyclerViewers with notifyDataSetChanged() rather than a full page refresh
        }
    };

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<InstanceIndexItemAdapter> myInstanceIndexItemAdapters;

        public DemoCollectionPagerAdapter(FragmentManager fm, ArrayList<InstanceIndexItemAdapter> iiias) {
            super(fm);

            myInstanceIndexItemAdapters = iiias;

        }

        @Override
        public Fragment getItem(int i) {

            StoryListFragment fragment = new StoryListFragment();
            fragment.setMyInstanceIndexItemAdapter(myInstanceIndexItemAdapters.get(i));
            Bundle args = new Bundle();
            args.putInt(StoryListFragment.ARG_OBJECT, i + 1); // Our object is just an integer :-P
            args.putInt(StoryListFragment.LIST_COUNT, 1);
            args.putString(StoryListFragment.LIST_NAME, "");
            args.putBoolean(StoryListFragment.HOME_FLAG, false);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {

            return mTabMenu.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return "Category " + (position + 1);
            return mTabMenu[position];
        }
    }

//    private class IndexTask extends AsyncTask<Void, Void, Boolean> {
//
//        // TODO: ADJUST THIS TO ACCOUNT FOR STORING AVAILABLE INDEX IN DB
//
//        private Context mContext;
//        private boolean forceDownload;
//
//        public IndexTask(Context context, boolean forceDownload) {
//            this.mContext = context;
//            this.forceDownload = forceDownload;
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//
//            Timber.d("IndexTask.doInBackground IS RUNNING");
//
//            boolean loginRequest = false;
//
//            ServerManager sm = StoryMakerApp.getServerManager();
//
//            if (sm.hasCreds()) {
//                // user is logged in, update status flag if necessary
//                if (!loggedIn) {
//                    loggedIn = true;
//                    loginRequest = true; // user just logged in, need to check server
//                }
//            } else {
//                // user is not logged in, update status flag if necessary
//                if (loggedIn) {
//                    loggedIn = false;
//                }
//            }
//
//            // check server if user just logged in
//            if (loginRequest) {
//                Timber.d("USER LOGGED IN, CHECK SERVER");
//
//                // reset available index
//                StorymakerIndexManager.copyAvailableIndex(mContext, false);
//
//                // attempt to download new assignments
//                return Boolean.valueOf(sm.index());
//            }
//
//            // check server if user insists
//            if (forceDownload) {
//                Timber.d("UPDATE REQUIRED, CHECK SERVER");
//
//                // reset available index
//                StorymakerIndexManager.copyAvailableIndex(mContext, false);
//
//                // attempt to download new assignments
//                return Boolean.valueOf(sm.index());
//            }
//
//            // no-op
//            return false;
//        }
//
//        protected void onPostExecute(Boolean result) {
//            if (result.booleanValue()) {
//                Timber.d("DOWNLOADED ASSIGNMENTS AND UPDATED AVAILABLE INDEX");
//            } else {
//                Timber.d("DID NOT DOWNLOAD ASSIGNMENTS OR UPDATE AVAILABLE INDEX");
//            }
//
//            //RES
//            //mSwipeRefreshLayout.setRefreshing(false);
//
//            // resolve available/installed conflicts and grab updates if needed
//            if (!StorymakerDownloadHelper.checkAndDownload(mContext, availableIndexItemDao, installedIndexItemDao, queueItemDao)) {
//                Toast.makeText(mContext, getString(R.string.home_downloading_content), Toast.LENGTH_LONG).show();
//            }
//            // refresh regardless (called from onResume and OnRefreshListener)
//            initActivityList();
//        }
//    }


    public void initActivityList () {
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
        boolean fileAddedFlag = StorymakerIndexManager.fillInstalledIndex(CatalogActivity.this, StorymakerIndexManager.loadInstalledFileIndex(CatalogActivity.this, installedIndexItemDao), StorymakerIndexManager.loadAvailableFileIndex(CatalogActivity.this, availableIndexItemDao), lang, installedIndexItemDao);
        if (fileAddedFlag) {
            Log.d("CatalogActivity", "file added");
        }

        ArrayList<BaseIndexItem> guides = new ArrayList<BaseIndexItem>();
        ArrayList<BaseIndexItem> lessons = new ArrayList<BaseIndexItem>();
        ArrayList<BaseIndexItem> templates = new ArrayList<BaseIndexItem>();

        StorymakerIndexManager.IndexKeyMap availableIndexKeyMap = StorymakerIndexManager.loadAvailableIdIndexKeyMap(this, availableIndexItemDao);
        HashMap<String, scal.io.liger.model.sqlbrite.ExpansionIndexItem> availableIds = availableIndexKeyMap.getIndexMap();
        ArrayList<String> availableKeys = availableIndexKeyMap.getIndexKeys();
        ArrayList<String> availableGuideIds = getIndexItemIdsByType(availableIds, "guide");
        ArrayList<String> availableLessonIds = getIndexItemIdsByType(availableIds, "lesson");
        ArrayList<String> availableTemplateIds = getIndexItemIdsByType(availableIds, "template");

        StorymakerIndexManager.IndexKeyMap installedIndexKeyMap = StorymakerIndexManager.loadInstalledIdIndexKeyMap(this, installedIndexItemDao);
        HashMap<String, scal.io.liger.model.sqlbrite.ExpansionIndexItem> installedIds = installedIndexKeyMap.getIndexMap();
        ArrayList<String> installedKeys = installedIndexKeyMap.getIndexKeys();
        ArrayList<String> installedGuideIds = getIndexItemIdsByType(installedIds, "guide");
        ArrayList<String> installedLessonIds = getIndexItemIdsByType(installedIds, "lesson");
        ArrayList<String> installedTemplateIds = getIndexItemIdsByType(installedIds, "template");

        for (String id : availableKeys) {

            if (installedKeys.contains(id)) {
                // if the available item has been installed, add the corresponding item from the installed index

                if (installedGuideIds.contains(id)) {
                    guides.add(installedIds.get(id));
                } else if (installedLessonIds.contains(id)) {
                    lessons.add(installedIds.get(id));
                } else if (installedTemplateIds.contains(id)) {
                    templates.add(installedIds.get(id));
                }

            } else {
                // if the available item has not been installed, add the item from the available index
                if (availableGuideIds.contains(id)) {
                    guides.add(availableIds.get(id));
                } else if (availableLessonIds.contains(id)) {
                    lessons.add(availableIds.get(id));
                } else if (availableTemplateIds.contains(id)) {
                    templates.add(availableIds.get(id));
                }

            }
        }

        //Collections.sort(instances, Collections.reverseOrder()); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList

        //Collections.sort(lessons); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList
        //Collections.sort(guides); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList
        //Collections.sort(templates); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList

        //mRecyclerView.setAdapter(new InstanceIndexItemAdapter(instances, new InstanceIndexItemAdapter.BaseIndexItemSelectedListener() {

        InstanceIndexItemAdapter.BaseIndexItemSelectedListener myBaseIndexItemSelectedListener = new InstanceIndexItemAdapter.BaseIndexItemSelectedListener() {

            @Override
            public void onStorySelected(BaseIndexItem selectedItem) {

                if (selectedItem instanceof InstanceIndexItem) {
                    launchLiger(CatalogActivity.this, null, ((InstanceIndexItem) selectedItem).getInstanceFilePath(), null);
                } else {

                    Timber.d("CLICKED AN ITEM");

                    // get clicked item
                    final scal.io.liger.model.sqlbrite.ExpansionIndexItem eItem = ((scal.io.liger.model.sqlbrite.ExpansionIndexItem)selectedItem);

                    // get installed items
                    final HashMap<String, scal.io.liger.model.sqlbrite.ExpansionIndexItem> installedIds = StorymakerIndexManager.loadInstalledIdIndex(CatalogActivity.this, installedIndexItemDao);

                    // this isn't ideal but pushing an alert dialog down into the check/download process is difficult

                    if ((downloadThreads.get(eItem.getExpansionId()) != null)) {
                        Timber.d("DIALOG - FOUND THREADS: " + downloadThreads.get(eItem.getExpansionId()).size());
                    }

                    if (!installedIds.containsKey(eItem.getExpansionId()) && (downloadThreads.get(eItem.getExpansionId()) == null)) {

                        Timber.d("DIALOG - START");

                        // this item is not installed and there are no saved threads for it

                        double size = ((eItem.getExpansionFileSize() + eItem.getPatchFileSize()) / 1048576.0);
                        size = Utils.round(size, 2);
                        new AlertDialog.Builder(CatalogActivity.this)
                                .setTitle(R.string.download_content_pack)
                                .setMessage(eItem.getTitle() + " (" + size + " MB)") // FIXME we need to flip this for RTL
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
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(CatalogActivity.this);
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

                                new AlertDialog.Builder(CatalogActivity.this)
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
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(CatalogActivity.this);
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
        };


        myGuidesInstanceIndexItemAdapter = new InstanceIndexItemAdapter(guides, myBaseIndexItemSelectedListener, installedIndexItemDao, instanceIndexItemDao);
        myLessonsInstanceIndexItemAdapter = new InstanceIndexItemAdapter(lessons, myBaseIndexItemSelectedListener, installedIndexItemDao, instanceIndexItemDao);
        myTemplatesInstanceIndexItemAdapter = new InstanceIndexItemAdapter(templates, myBaseIndexItemSelectedListener, installedIndexItemDao, instanceIndexItemDao);

        //final CatalogActivity activity = this;
        ArrayList<InstanceIndexItemAdapter> myInstanceIndexItemAdapters = new ArrayList<InstanceIndexItemAdapter>();
        //myInstanceIndexItemAdapters.add(myInstancesInstanceIndexItemAdapter);
        myInstanceIndexItemAdapters.add(myGuidesInstanceIndexItemAdapter);
        myInstanceIndexItemAdapters.add(myLessonsInstanceIndexItemAdapter);
        myInstanceIndexItemAdapters.add(myTemplatesInstanceIndexItemAdapter);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager(), myInstanceIndexItemAdapters);

        // Set up the ViewPager with the sections adapter.
        //mViewPager = (SwipelessViewPager) findViewById(R.id.pager);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

               // currentScroll = positionOffsetPixels;

            }
        });
        goToCatalogTab(currentPage);
        //mViewPager.setPagingEnabled(false);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setSelectedIndicatorColors(
                getResources().getColor(R.color.white));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    public void goToCatalogTab(int currentPage) {

        int i;

        if (currentPage != -1) {
            mViewPager.setCurrentItem(currentPage);
        } else {

            switch (mIntentMessage) {
                case "home":
                    i = 0;
                    break;
                case "guides":
                    i = 0;
                    break;
                case "lessons":
                    i = 1;
                    break;
                case "templates":
                    i = 2;
                    break;
                case "null":
                    i = 0;
                    break;
                default:
                    i = 0;
                    break;
            }
            mViewPager.setCurrentItem(i);
        }

    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {

        super.onActivityResult(arg0, arg1, arg2);

        boolean changed = ((StoryMakerApp)getApplication()).checkLocale();
        if (changed)
        {
            finish();
            startActivity(new Intent(this,CatalogActivity.class));

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDeleteMessageReceiver);
    }
}
