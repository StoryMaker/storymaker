package org.storymaker.app;

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
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

import org.storymaker.app.ui.SlidingTabLayout;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import scal.io.liger.JsonHelper;
import scal.io.liger.StorymakerIndexManager;
import scal.io.liger.model.sqlbrite.BaseIndexItem;
import scal.io.liger.model.sqlbrite.ExpansionIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItem;
import scal.io.liger.model.sqlbrite.InstanceIndexItem;
import timber.log.Timber;

public class HomeActivity extends BaseHomeActivity {

    private final static String TAG = "HomeActivity";

    private InstanceIndexItemAdapter myHomeItemsInstanceIndexItemAdapter;
    private InstanceIndexItemAdapter myInstancesInstanceIndexItemAdapter;
    private InstanceIndexItemAdapter myGuidesInstanceIndexItemAdapter;
    private InstanceIndexItemAdapter myLessonsInstanceIndexItemAdapter;
    private InstanceIndexItemAdapter myTemplatesInstanceIndexItemAdapter;
    private int mHomeTabInstanceCount;
    private int mHomeTabInstallationCount;

    private DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    private int currentPage = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle(Utils.getAppName(this));

        setContentView(R.layout.activity_home);

        mHomeTabInstanceCount = 0;
        mHomeTabInstallationCount = 0;

        mTabMenu = getMenu("home");

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
            String expansionId = intent.getStringExtra("expansionid");
            Log.d("receiver", "home download expansion id: " + expansionId + " " + this.toString());

            initActivityList();     //To-do: consider modifying the ViewPager-RecyclerViewers with notifyDataSetChanged() rather than a full page refresh
        }
    };
    private BroadcastReceiver mDeleteMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String expansionId = intent.getStringExtra("expansionid");
            Log.d("receiver", "home delete expansion id: " + expansionId + " " + this.toString());

            initActivityList();     //To-do: consider modifying the ViewPager-RecyclerViewers with notifyDataSetChanged() rather than a full page refresh
            removeThreads(expansionId);
        }
    };


    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<InstanceIndexItemAdapter> myInstanceIndexItemAdapters;
        private ArrayList<Integer> myListLengths;
        private ArrayList<String> myListNames;
        private int myHomeTabInstanceCount;
        private int myHomeTabInstallationCount;

        public DemoCollectionPagerAdapter(FragmentManager fm, ArrayList<InstanceIndexItemAdapter> iiias, ArrayList<Integer> ls, ArrayList<String> ns, int instanceCount, int installationCount) {
            super(fm);

            myInstanceIndexItemAdapters = iiias;
            myListLengths = ls;
            myListNames = ns;
            myHomeTabInstanceCount = instanceCount;
            myHomeTabInstallationCount = installationCount;
        }

        @Override
        public Fragment getItem(int i) {
            StoryListFragment fragment = new StoryListFragment();
            fragment.setContext(HomeActivity.this);
            fragment.setMyInstanceIndexItemAdapter(myInstanceIndexItemAdapters.get(i));
            Bundle args = new Bundle();
            args.putInt(StoryListFragment.ARG_OBJECT, i + 1); // Our object is just an integer :-P
            args.putInt(StoryListFragment.LIST_COUNT, myListLengths.get(i));
            args.putString(StoryListFragment.LIST_NAME, myListNames.get(i));
            args.putBoolean(StoryListFragment.HOME_FLAG, true);
            args.putInt(StoryListFragment.HOME_TAB_INSTALLATION_COUNT, myHomeTabInstallationCount);
            args.putInt(StoryListFragment.HOME_TAB_INSTANCE_COUNT, myHomeTabInstanceCount);
            if (i == 0) {
                args.putBoolean(StoryListFragment.HOME_TAB_FLAG, true);
                args.putBoolean(StoryListFragment.SECTION_FLAG, true);
            } else {
                args.putBoolean(StoryListFragment.HOME_TAB_FLAG, false);
                args.putBoolean(StoryListFragment.SECTION_FLAG, false);
            }
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            //return 100;
            //return categories.size();

            return mTabMenu.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return "Category " + (position + 1);
            return mTabMenu[position];
        }
    }

    public void initActivityList() {
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
        boolean fileAddedFlag = StorymakerIndexManager.fillInstalledIndex(HomeActivity.this, StorymakerIndexManager.loadInstalledFileIndex(HomeActivity.this, installedIndexItemDao), StorymakerIndexManager.loadAvailableFileIndex(HomeActivity.this, availableIndexItemDao), lang, installedIndexItemDao);
        if (fileAddedFlag) {
            Log.d("HomeActivity", "file added");
        }


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
        ArrayList<BaseIndexItem> installations = new ArrayList<BaseIndexItem>();
        ArrayList<BaseIndexItem> guides = new ArrayList<BaseIndexItem>();
        ArrayList<BaseIndexItem> lessons = new ArrayList<BaseIndexItem>();
        ArrayList<BaseIndexItem> templates = new ArrayList<BaseIndexItem>();
        ArrayList<BaseIndexItem> homeitems = new ArrayList<BaseIndexItem>();


        //HashMap<String, ExpansionIndexItem> availableIds = StorymakerIndexManager.loadAvailableIdIndex(this, availableIndexItemDao);
        //ArrayList<String> availableGuideIds = getIndexItemIdsByType(availableIndexItemDao, "guide");
        //ArrayList<String> availableLessonIds = getIndexItemIdsByType(availableIndexItemDao, "lesson");
        //ArrayList<String> availableTemplateIds = getIndexItemIdsByType(availableIndexItemDao, "template");

        //HashMap<String, ExpansionIndexItem> installedIds = StorymakerIndexManager.loadInstalledIdIndex(this, installedIndexItemDao);
        //ArrayList<String> installedIdList = StorymakerIndexManager.loadInstalledIdIndexList(this, installedIndexItemDao);

        StorymakerIndexManager.IndexKeyMap installedIndexKeyMap = StorymakerIndexManager.loadInstalledIdIndexKeyMap(this, installedIndexItemDao);
        HashMap<String, scal.io.liger.model.sqlbrite.ExpansionIndexItem> installedIds = installedIndexKeyMap.getIndexMap();
        ArrayList<String> installedKeys = installedIndexKeyMap.getIndexKeys();
        ArrayList<String> installedGuideIds = getIndexItemIdsByType(installedIds, "guide");
        ArrayList<String> installedLessonIds = getIndexItemIdsByType(installedIds, "lesson");
        ArrayList<String> installedTemplateIds = getIndexItemIdsByType(installedIds, "template");

        int k = 0;

        for (String id : installedKeys) {

            Log.d("InstalledIndex", "installed "+k+" "+id+" "+((InstalledIndexItem) installedIds.get(id)).getCreationDate());

            // if the available item has been installed, add the corresponding item from the installed index
            installations.add(installedIds.get(id));

            if (installedGuideIds.contains(id)) {
                guides.add(installedIds.get(id));
            } else if (installedLessonIds.contains(id)) {
                lessons.add(installedIds.get(id));
            } else if (installedTemplateIds.contains(id)) {
                templates.add(installedIds.get(id));
            }

            k++;
        }

        Collections.sort(instances, Collections.reverseOrder()); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList
        //Collections.sort(lessons, Collections.reverseOrder()); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList
        //Collections.sort(guides, Collections.reverseOrder()); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList
        //Collections.sort(templates, Collections.reverseOrder()); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList
        //Collections.sort(installations, Collections.reverseOrder()); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList

        mHomeTabInstanceCount = 0;
        mHomeTabInstallationCount = 0;

        for (int i = 0; i < instances.size(); i++) {
            //System.out.println(list.get(i));
            homeitems.add(instances.get(i));
            mHomeTabInstanceCount++;
            if (i == 1) {
                break;
            }
        }

        if (installations.size() > 0) {
            homeitems.add(installations.get(0));
            mHomeTabInstallationCount++;
        }
        
        //mRecyclerView.setAdapter(new InstanceIndexItemAdapter(instances, new InstanceIndexItemAdapter.BaseIndexItemSelectedListener() {

        InstanceIndexItemAdapter.BaseIndexItemSelectedListener myBaseIndexItemSelectedListener = new InstanceIndexItemAdapter.BaseIndexItemSelectedListener() {

            @Override
            public void onStorySelected(BaseIndexItem selectedItem) {

                if (selectedItem instanceof InstanceIndexItem) {
                    updateInstanceIndexItemLastOpenedDate((InstanceIndexItem) selectedItem);
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
        };

        myHomeItemsInstanceIndexItemAdapter = new InstanceIndexItemAdapter(homeitems, myBaseIndexItemSelectedListener, installedIndexItemDao, instanceIndexItemDao);
        myInstancesInstanceIndexItemAdapter = new InstanceIndexItemAdapter(instances, myBaseIndexItemSelectedListener, installedIndexItemDao, instanceIndexItemDao);
        myGuidesInstanceIndexItemAdapter = new InstanceIndexItemAdapter(guides, myBaseIndexItemSelectedListener, installedIndexItemDao, instanceIndexItemDao);
        myLessonsInstanceIndexItemAdapter = new InstanceIndexItemAdapter(lessons, myBaseIndexItemSelectedListener, installedIndexItemDao, instanceIndexItemDao);
        myTemplatesInstanceIndexItemAdapter = new InstanceIndexItemAdapter(templates, myBaseIndexItemSelectedListener, installedIndexItemDao, instanceIndexItemDao);

        ArrayList<InstanceIndexItemAdapter> myInstanceIndexItemAdapters = new ArrayList<InstanceIndexItemAdapter>();
        myInstanceIndexItemAdapters.add(myHomeItemsInstanceIndexItemAdapter);
        myInstanceIndexItemAdapters.add(myInstancesInstanceIndexItemAdapter);
        myInstanceIndexItemAdapters.add(myGuidesInstanceIndexItemAdapter);
        myInstanceIndexItemAdapters.add(myLessonsInstanceIndexItemAdapter);
        myInstanceIndexItemAdapters.add(myTemplatesInstanceIndexItemAdapter);

        ArrayList<Integer> myListLengths = new ArrayList<Integer>();
        myListLengths.add(homeitems.size());
        myListLengths.add(instances.size());
        myListLengths.add(guides.size());
        myListLengths.add(lessons.size());
        myListLengths.add(templates.size());

        ArrayList<String> myListNames = new ArrayList<String>();
        myListNames.add("home");
        myListNames.add("stories");
        myListNames.add("guides");
        myListNames.add("lessons");
        myListNames.add("templates");


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager(), myInstanceIndexItemAdapters, myListLengths, myListNames, mHomeTabInstanceCount, mHomeTabInstallationCount);

        // Set up the ViewPager with the sections adapter.
        //mViewPager = (SwipelessViewPager) findViewById(R.id.pager);
        mViewPager = (ViewPager) findViewById(R.id.home_pager);
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
            }
        });
        //mViewPager.setPagingEnabled(false);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.home_sliding_tabs);
        mSlidingTabLayout.setSelectedIndicatorColors(
                getResources().getColor(R.color.white));
        //mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        if (currentPage != -1) {
            mViewPager.setCurrentItem(currentPage);
        }


        //} else {
        // empty list
        //    TextView textView = (TextView) findViewById(R.id.textViewEmptyState);
        //    textView.setVisibility(View.VISIBLE);
        //mSwipeRefreshLayout.setVisibility(View.GONE);
        //}
    }


    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {

        super.onActivityResult(arg0, arg1, arg2);

        boolean changed = ((StoryMakerApp) getApplication()).checkLocale();
        if (changed) {
            finish();
            startActivity(new Intent(this, HomeActivity.class));

        }
    }

    @Override
    public void onCacheWordUninitialized() {
        // set default pin, prompt for actual pin on first lock
        try {
            CharSequence defaultPinSequence = getText(R.string.cacheword_default_pin);
            char[] defaultPin = defaultPinSequence.toString().toCharArray();
            mCacheWordHandler.setPassphrase(defaultPin);
            SharedPreferences sp = getSharedPreferences("appPrefs", MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            e.putString("cacheword_status", BaseActivity.CACHEWORD_UNSET);
            e.commit();
            Timber.d("set default cacheword pin");
        } catch (GeneralSecurityException gse) {
            Log.e("CACHEWORD", "failed to set default cacheword pin: " + gse.getMessage());
            gse.printStackTrace();
        }
    }

    @Override
    public void onCacheWordLocked() {
        // if there has been no first lock and pin prompt, use default pin to unlock
        SharedPreferences sp = getSharedPreferences("appPrefs", MODE_PRIVATE);
        String cachewordStatus = sp.getString("cacheword_status", "default");
        if (cachewordStatus.equals(BaseActivity.CACHEWORD_UNSET)) {
            try {
                CharSequence defaultPinSequence = getText(R.string.cacheword_default_pin);
                char[] defaultPin = defaultPinSequence.toString().toCharArray();
                mCacheWordHandler.setPassphrase(defaultPin);
                Timber.d("used default cacheword pin");
            } catch (GeneralSecurityException gse) {
                Log.e("CACHEWORD", "failed to use default cacheword pin: " + gse.getMessage());
                gse.printStackTrace();
            }
        } else {
            Timber.d("prompt for cacheword pin");
            showLockScreen();
        }
    }

    // NEW/CACHEWORD
    void showLockScreen() {
        // set aside current activity and prompt for cacheword pin
        Intent intent = new Intent(this, CacheWordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("originalIntent", getIntent());
        startActivity(intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDeleteMessageReceiver);
        super.onDestroy();
    }
}
