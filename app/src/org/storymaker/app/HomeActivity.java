package org.storymaker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;

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
            Timber.d("receiver", "home download expansion id: " + expansionId + " " + this.toString());

            initActivityList();     // TODO: consider modifying the ViewPager-RecyclerViewers with notifyDataSetChanged() rather than a full page refresh
        }
    };
    private BroadcastReceiver mDeleteMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String expansionId = intent.getStringExtra("expansionid");
            Timber.d("receiver", "home delete expansion id: " + expansionId + " " + this.toString());

            initActivityList();     // TODO: consider modifying the ViewPager-RecyclerViewers with notifyDataSetChanged() rather than a full page refresh
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
            Timber.d("HomeActivity", "file added");
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

        for (String id : installedKeys) {

            // we don't want to populate the home screen with anything that hasn't finished downloading
            InstalledIndexItem checkItem = (InstalledIndexItem)installedIds.get(id);

            if (checkItem.isInstalled()) {

                // if the available item has been installed, add the corresponding item from the installed index
                installations.add(installedIds.get(id));

                if (installedGuideIds.contains(id)) {
                    guides.add(checkItem);
                } else if (installedLessonIds.contains(id)) {
                    lessons.add(checkItem);
                } else if (installedTemplateIds.contains(id)) {
                    templates.add(checkItem);
                }
            } else {
                Timber.d("HomeActivity - " + checkItem.getExpansionId() + " has not finished downloading, it will be skipped");
            }

        }

        Collections.sort(instances, Collections.reverseOrder()); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList
        Collections.sort(lessons, Collections.reverseOrder()); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList
        Collections.sort(guides, Collections.reverseOrder()); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList
        Collections.sort(templates, Collections.reverseOrder()); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList
        Collections.sort(installations, Collections.reverseOrder()); // FIXME we should sort this down a layer, perhaps in loadInstanceIndexAsList

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

                // the more complex logic for downloading was moved to CatalogActivity, so this method can be streamlined

                if (selectedItem instanceof InstanceIndexItem) {
                    updateInstanceIndexItemLastOpenedDate((InstanceIndexItem) selectedItem);
                    launchLiger(HomeActivity.this, null, ((InstanceIndexItem) selectedItem).getInstanceFilePath(), null);
                } else {

                    // get clicked item
                    final ExpansionIndexItem eItem = ((ExpansionIndexItem) selectedItem);

                    Timber.d("HomeActivity - clicked an item: " + eItem.getExpansionId());

                    // get installed items
                    final HashMap<String, ExpansionIndexItem> installedIds = StorymakerIndexManager.loadInstalledIdIndex(HomeActivity.this, installedIndexItemDao);

                    if (!installedIds.containsKey(eItem.getExpansionId())) {

                        // this state should be unreachable, not sure what to do here
                        Timber.e("HomeActivity - " + eItem.getExpansionId() + " was not found in the installed index, something went wrong");

                    } else {

                        // proceed as usual
                        Timber.d("HomeActivity - handle click for item: " + eItem.getExpansionId());
                        handleClick(eItem, installedIds, true);

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

    // FIXME this should probably be moved into BaseActivity.  also, the activities in SecureShare and Liger should probably have handlers which raise an intent that we catch here to prevetn tight coupling
    @Override
    public void onCacheWordLocked() {
        // if there has been no first lock and pin prompt, use default pin to unlock
        SharedPreferences sp = getSharedPreferences("appPrefs", MODE_PRIVATE);
        String cachewordStatus = sp.getString("cacheword_status", "default");
        // if the user is trying to set a pin, we need to show the lock screen
        if (cachewordStatus.equals(BaseActivity.CACHEWORD_UNSET) && !setPin) {
            try {
                CharSequence defaultPinSequence = getText(R.string.cacheword_default_pin);
                char[] defaultPin = defaultPinSequence.toString().toCharArray();
                mCacheWordHandler.setPassphrase(defaultPin);
                Timber.d("used default cacheword pin");
            } catch (GeneralSecurityException gse) {
                Timber.e(gse, "CACHEWORD", "failed to use default cacheword pin: " + gse.getMessage());
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

        // notify the activity if the user is trying to set a pin
        if (setPin) {
            Timber.d("set flag for cacheword pin initialization");
            intent.putExtra(CACHEWORD_FIRST_LOCK, setPin);
            // now that we've initiated the activity, clear the flag so the user won't get stuck
            setPin = false;
        }

        startActivity(intent);
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDeleteMessageReceiver);
    }
}
