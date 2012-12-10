package info.guardianproject.mrapp;

import info.guardianproject.mrapp.R;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class SceneEditorActivity extends org.holoeverywhere.app.Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_editor);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.requestDisallowInterceptTouchEvent(true);
        
        // When swiping between different sections, select the corresponding tab.
        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
        // Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_scene_editor, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            int layout = R.layout.fragment_add_clips;
            if (i == 1) {
                layout = R.layout.fragment_order_clips;
            } else if (i == 2) {
                layout = R.layout.fragment_story_publish;
            } 
            Fragment fragment = new DummySectionFragment(layout, getSupportFragmentManager());
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
            fragment.setArguments(args);
            
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.tab_add_clips).toUpperCase();
                case 1: return getString(R.string.tab_order).toUpperCase();
                case 2: return getString(R.string.tab_publish).toUpperCase();
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
    	private final static String TAG = "DummySectionFragment";
        int layout;
        
        /**
         * The {@link mClipViewPager} that will host the clips.
         */
        ViewPager mClipViewPager;
        ClipPagerAdapter mClipPagerAdapter;
        
        public DummySectionFragment(int layout, FragmentManager fm) {
            this.layout = layout;
            mClipPagerAdapter = new ClipPagerAdapter(fm);
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View view = inflater.inflate(layout, null);
            if (this.layout == R.layout.fragment_add_clips) {

              // Set up the clip ViewPager with the clip adapter.
              mClipViewPager = (ViewPager) view.findViewById(R.id.viewPager);
              (new AsyncTask<Void, Void, Void>() {
                  @Override
                  protected void onPostExecute(Void result) {
                      mClipViewPager.setAdapter(mClipPagerAdapter);
                  }

                @Override
                protected Void doInBackground(Void... params) {
                    // TODO Auto-generated method stub
                    return null;
                }
              }).execute();
              
            } else if (this.layout == R.layout.fragment_order_clips) {
            	
            } else if (this.layout == R.layout.fragment_story_publish) {
                
            }
            return view;
        }

        /**
         * A {@link FragmentPagerAdapter} that returns a fragment corresponding to the clips we are editing
         */
        public class ClipPagerAdapter extends FragmentPagerAdapter {

            public ClipPagerAdapter(FragmentManager fm) {
                super(fm);
            }

            @Override
            public Fragment getItem(int i) {
                Fragment fragment = new ClipThumbnailFragment();
                Bundle args = new Bundle();
                args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i);
                fragment.setArguments(args);
                return fragment;
            }

            @Override
            public int getCount() {
                return 5;
            }
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class ClipThumbnailFragment extends Fragment {
        public ClipThumbnailFragment() {
        }

        public static final String ARG_CLIP_TYPE_ID = "clip_type_id";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int i = getArguments().getInt(ARG_CLIP_TYPE_ID, 0);
            
            LinearLayout ll = new LinearLayout(getActivity());
            ImageView iv = new ImageView(getActivity());

            TypedArray drawableIds = getActivity().getResources().obtainTypedArray(R.array.cliptype_thumbnails);
            int drawableId = drawableIds.getResourceId(i, -1); // FIXME handle -1
            Drawable d = getActivity().getResources().getDrawable(drawableId);
            
            iv.setImageDrawable(d);
            ll.addView(iv);
            
            return (View) ll;
        }
    }
}
