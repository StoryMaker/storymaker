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

//    public class Category {
//        public String id, name;
//
//        public Category(String id, String name){
//            this.id = id;
//            this.name = name;
//        }
//    }
//    private List<Category> categories = new ArrayList<>();
//    categories.add(new Category("catalog", "Catalog"));
//    categories.add(new Category("guides", "Guides"));
//    categories.add(new Category("lessons", "Lessons"));
//    categories.add(new Category("templates", "Templates"));

//    ArrayList<String> category_keys = new ArrayList<String>() {{
//        add("catalog");
//        add("guides");
//        add("lessons");
//        add("templates");
//    }};
//    ArrayList<String> category_names = new ArrayList<String>() {{
//        add("Catalog");
//        add("Guides");
//        add("Lessons");
//        add("Templates");
//    }};

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

    private String[] mCatalogMenu;

    public String[] getCatalogMenu() {

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

        String[] catalog_ids = getResources().getStringArray(R.array.catalog_menu_ids);
        String[] catalog_names = new String[catalog_ids.length];

        for (int i=0; i<catalog_ids.length; i++) {
            int id = getResources().getIdentifier("catalog_menu_" + catalog_ids[i], "string", getApplicationContext().getPackageName());
            String catalog_name = getResources().getString(id);
            catalog_names[i] = catalog_name;
        }

        return catalog_names;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_catalog);

        //setupDrawerLayout();

        //loggedIn = false;
        //TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        //mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        mCatalogMenu = getCatalogMenu();

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
    public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {

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
            //return 100;
            //return categories.size();

            //Log.d("CatalogActivity", menu.size());
            return mCatalogMenu.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return "Category " + (position + 1);
            return mCatalogMenu[position];
        }
    }


}
