package org.storymaker.app;

/**
 * Created by admin on 12/11/15.
 */


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import org.storymaker.app.ui.SwipelessViewPager;

/**
 * Created by admin on 12/8/15.
 */
public class CatalogActivity extends BaseActivity {

    //private final static String TAG = "CatalogActivity";

    //private boolean loggedIn;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private SwipelessViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_catalog);

        //setupDrawerLayout();

        //loggedIn = false;
        //TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        //mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (SwipelessViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        mViewPager.setPagingEnabled(false);
        //mTabLayout.setupWithViewPager(mViewPager);

        //PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
        //pagerTabStrip.setDrawFullUnderline(true);
        //pagerTabStrip.setTabIndicatorColor(Color.RED);

    }

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {

        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new StoryListFragment();
            Bundle args = new Bundle();
            args.putInt(StoryListFragment.ARG_OBJECT, i + 1); // Our object is just an integer :-P
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            return 100;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Category " + (position + 1);
        }
    }


}
