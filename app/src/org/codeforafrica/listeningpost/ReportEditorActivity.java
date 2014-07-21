package org.codeforafrica.listeningpost;

import org.codeforafrica.listeningpost.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;

public class ReportEditorActivity extends EditorBaseActivity implements ActionBar.TabListener {
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	

    public Fragment mFragmentTab0, mFragmentTab1, mLastTabFrag;
    public ReportPublishFragment rPublishFragment;
    public AddMediaFragment addMediaFragment;
    
    private int rid;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_scene_editor_no_swipe);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        actionBar.setTitle("Create Story");
     
        // For each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText("Add Media").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Publish").setTabListener(this));
        
        Intent i = getIntent();
        rid = i.getIntExtra("rid", -1);
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
	public void onTabSelected(Tab tab, FragmentTransaction ft){
        int layout = R.layout.fragment_report_publish;
        FragmentManager fm = getSupportFragmentManager();

            if (tab.getPosition() == 1) {
                layout = R.layout.fragment_report_publish;
                if (rPublishFragment == null)
                {
                	rPublishFragment = new ReportPublishFragment();
                    Bundle args = new Bundle();
                    args.putInt(ReportPublishFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
                	args.putInt("layout",layout);
                	args.putInt("rid", rid);
                	rPublishFragment.setArguments(args);
                        

                    fm.beginTransaction()
                            .add(R.id.container, rPublishFragment, layout + "")
                            .commit();

                } else {

                    fm.beginTransaction()
                            .show(rPublishFragment)
                            .commit();
                }

                mLastTabFrag = rPublishFragment;
            } else  if (tab.getPosition() == 0) {
                
                layout = R.layout.fragment_add_media;
            	
            	if (mFragmentTab0 == null){
               
                mFragmentTab0 = new AddMediaFragment();

                Bundle args = new Bundle();
                args.putInt(AddMediaFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
                args.putInt("rid", rid);
                args.putInt("layout", layout);
                mFragmentTab0.setArguments(args);
             
                fm.beginTransaction()
                        .add(R.id.container, mFragmentTab0, layout + "")
                        .commit();

            }else {
                fm.beginTransaction()
                        .show(mFragmentTab0)
                        .commit();
            }
            mLastTabFrag = mFragmentTab0;
          //Edit Tab
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 
    }

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().hide(mLastTabFrag).commit();
	} 
	public static class MyAdapter extends FragmentPagerAdapter {

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
	        	bundle.putString("title","some title");//getString(mTitles[position]));
	        	bundle.putString("msg","some message");//getString(mMessages[position]));

	        	Fragment f = new MyFragment();
	        	f.setArguments(bundle);

	            return f;
	        }
	    }
	public static final class MyFragment extends Fragment {
		
	String mMessage;
	String mTitle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);

	      mTitle = getArguments().getString("title");
	      mMessage = getArguments().getString("msg");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	      
	      ViewGroup root = (ViewGroup) inflater.inflate(R.layout.card_pager_textview, null);
	      
	      ((TextView)root.findViewById(R.id.title)).setText(mTitle);
	      
	      ((TextView)root.findViewById(R.id.description)).setText(mMessage);
	      
	      return root;
	  }
	}
}