package info.guardianproject.mrapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.holoeverywhere.widget.Spinner;

import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class StoryTemplateActivity extends org.holoeverywhere.app.Activity implements ActionBar.TabListener {

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
        setContentView(R.layout.activity_story_template);
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
        getSupportMenuInflater().inflate(R.menu.activity_story_template, menu);
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
            int layout = R.layout.fragment_make;
            if (i == 1) {
                layout = R.layout.fragment_story_review;
            } else if (i == 2) {
                layout = R.layout.fragment_story_publish;
            } 
            Fragment fragment = new DummySectionFragment(layout);
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
                case 0: return getString(R.string.tab_make).toUpperCase();
                case 1: return getString(R.string.tab_review).toUpperCase();
                case 2: return getString(R.string.tab_publish).toUpperCase();
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        int layout;
        public DummySectionFragment(int layout) {
            this.layout = layout;
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View view = inflater.inflate(layout, null);
            if (this.layout == R.layout.fragment_make) {
                String[] egTitles = getResources().getStringArray(R.array.eg_scene_titles);
                String[] egDescriptions = getResources().getStringArray(R.array.eg_scene_descriptions);
                String[] egStatuses = getResources().getStringArray(R.array.eg_scene_statuses);
                
                // create the item mapping
                String[] from = new String[] {"title", "description", "status" };
                int[] to = new int[] { R.id.textViewTitle, R.id.textViewDescription, R.id.textViewStatus  };

                List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
                for(int i = 0; i < egTitles.length; i++){
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("title", egTitles[i]);
                    map.put("description", egDescriptions[i]);
                    map.put("status", egStatuses[i]);
                    fillMaps.add(map);
                }
                
                SimpleAdapter adapter = new SimpleAdapter(getActivity(), fillMaps, R.layout.list_item_scene, from, to);
                ListView lv = (ListView) view.findViewById(R.id.listView1);
                lv.setAdapter(adapter);
                
                lv.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                            int arg2, long arg3) {
                    	Intent i = new Intent(getActivity(), SceneEditorActivity.class);
                    	i.putExtra("template_story", true);
                        getActivity().startActivity(i);
                        
                    }
                });
            } else if (this.layout == R.layout.fragment_story_review) {
            } else if (this.layout == R.layout.fragment_story_publish) {
                /*
                Spinner spinnerSections = (Spinner) getActivity().findViewById(R.id.spinnerSections);
                Spinner spinnerTopics = (Spinner) view.findViewById(R.id.spinnerTopics);
                
                ArrayAdapter<CharSequence> adapterTopics = ArrayAdapter.createFromResource(getActivity(),
                        R.array.story_topics, android.R.layout.simple_spinner_item);
                adapterTopics.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTopics.setAdapter(adapterTopics);
                
                ArrayAdapter<CharSequence> adapterSections = ArrayAdapter.createFromResource(getActivity(),
                        R.array.story_sections, android.R.layout.simple_spinner_item);
                adapterSections.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTopics.setAdapter(adapterSections);*/
            }
            return view;
        }
    }
}
