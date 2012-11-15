package info.guardianproject.mrapp;

import info.guardianproject.mrapp.R;
import android.content.Intent;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.animoto.android.views.DraggableGridView;
import com.animoto.android.views.OnRearrangeListener;

public class SceneEditorNoSwipe extends com.WazaBe.HoloEverywhere.sherlock.SActivity implements ActionBar.TabListener {

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    protected boolean templateStory = false; 
    protected Menu mMenu = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_editor_no_swipe);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);


        // For each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_add_clips).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_order).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_publish).setTabListener(this));
        
        if (getIntent().hasExtra("template_story")) {
        	templateStory = true;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getSupportActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	mMenu = menu;
        getSupportMenuInflater().inflate(R.menu.activity_scene_editor, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	if (templateStory) {
            		NavUtils.navigateUpTo(this, new Intent(this, StoryTemplate.class));
            	} else {
            		NavUtils.navigateUpFromSameTask(this);
            	}
                return true;
            case R.id.itemForward:
            	int idx = getSupportActionBar().getSelectedNavigationIndex();
            	getSupportActionBar().setSelectedNavigationItem(Math.min(2, idx+1));
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, show the tab contents in the container
        int layout = R.layout.fragment_add_clips;

        if (mMenu != null) {
	        mMenu.findItem(R.id.itemInfo).setVisible(false);
	        mMenu.findItem(R.id.itemTrim).setVisible(false);
        }

        if (tab.getPosition() == 0) {
        	if (mMenu != null) {
        		mMenu.findItem(R.id.itemForward).setEnabled(true);
        	}
        } else if (tab.getPosition() == 1) {
            layout = R.layout.fragment_order_clips;

        	if (mMenu != null) {
	            mMenu.findItem(R.id.itemInfo).setVisible(true);
	            mMenu.findItem(R.id.itemTrim).setVisible(true);
		        mMenu.findItem(R.id.itemForward).setEnabled(true);
        	}
        } else if (tab.getPosition() == 2) {
            layout = R.layout.fragment_story_publish;
            mMenu.findItem(R.id.itemForward).setEnabled(false);
        }
        String tag = "" + layout;
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new DummySectionFragment(layout, fm);
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
            fragment.setArguments(args);
            fm.beginTransaction()
                    .replace(R.id.container, fragment, tag)
//                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
    	private final static String TAG = "DummySectionFragment";
        int layout;
        ViewPager mClipViewPager;
        View mView = null;
        ClipPagerAdapter mClipPagerAdapter;
        
        /**
         * The sortable grid view that contains the clips to reorder on the Order tab
         */
        protected DraggableGridView mDGV;
        
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
              mClipViewPager.setPageMargin(-200);
              mClipViewPager.setOffscreenPageLimit(5);
              
            } else if (this.layout == R.layout.fragment_order_clips) {
            	mDGV = (DraggableGridView) view.findViewById(R.id.DraggableGridView01);
            	
            	ImageView iv = new ImageView(getActivity());
            	iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptypesm_close));
            	mDGV.addView(iv);
            	
            	iv = new ImageView(getActivity());
            	iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptypesm_detail));
            	mDGV.addView(iv);
            	
            	iv = new ImageView(getActivity());
            	iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptypesm_long));
            	mDGV.addView(iv);
            	
            	iv = new ImageView(getActivity());
            	iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptypesm_medium));
            	mDGV.addView(iv);
            	
            	iv = new ImageView(getActivity());
            	iv.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cliptypesm_wide));
            	mDGV.addView(iv);
            	
            	mDGV.setOnRearrangeListener(new OnRearrangeListener() {
					
					@Override
					public void onRearrange(int arg0, int arg1) {
						// TODO Auto-generated method stub
						Log.d(TAG, "grid rearranged");
					}
				});
            	
            	mDGV.setOnItemClickListener(new OnItemClickListener() {
            		
            		@Override
        			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            			Log.d(TAG, "item clicked");
            		}
				});
            } else if (this.layout == R.layout.fragment_story_publish) {
            }
            return view;
        }
        
        @Override
        public void onResume() {
            super.onResume();
            if (this.layout == R.layout.fragment_add_clips) {
    
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
                args.putInt(ClipThumbnailFragment.ARG_CLIP_TYPE_ID, i);
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
//            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ll.getLayoutParams();
//            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
//            iv.setLayoutParams(param);
//            iv.setPadding(30, 30, 30, 30);
            TypedArray drawableIds = getActivity().getResources().obtainTypedArray(R.array.cliptype_thumbnails);
            int drawableId = drawableIds.getResourceId(i, -1); // FIXME handle -1
            Drawable d = getActivity().getResources().getDrawable(drawableId);
            
            iv.setImageDrawable(d);
            ll.addView(iv);
            
            return (View) ll;
        }
    }
}
