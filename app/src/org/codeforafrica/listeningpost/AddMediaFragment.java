package org.codeforafrica.listeningpost;


import org.codeforafrica.listeningpost.R;
import org.codeforafrica.listeningpost.ReportEditorActivity.MyAdapter;

import com.animoto.android.views.DraggableGridView;
import com.viewpagerindicator.CirclePageIndicator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AddMediaFragment extends Fragment {
    View mView = null;
    
    private EditorBaseActivity mActivity;
    private SharedPreferences mSettings = null;
    
    protected DraggableGridView mOrderClipsDGV;

    private void initFragment ()
    {
    	mActivity = (EditorBaseActivity)getActivity();
    	
        mSettings = PreferenceManager
        .getDefaultSharedPreferences(getActivity().getApplicationContext());
	
    }
    
    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

    	initFragment ();
    	
    	int layout = getArguments().getInt("layout");
    	
        mView = inflater.inflate(layout, null);
        
        if (layout == R.layout.fragment_add_media) {
        	     	
        			
        		int[] titles2 =
        			{(R.string.tutorial_title_7),
        				(R.string.tutorial_title_8),
        				(R.string.tutorial_title_9),
        				(R.string.tutorial_title_10),
        				(R.string.tutorial_title_11)
        				};
        			
        		int[] messages2 =
        			{(R.string.tutorial_text_7),
        				(R.string.tutorial_text_8),
        				(R.string.tutorial_text_9),
        				(R.string.tutorial_text_10),
        				(R.string.tutorial_text_11)
        				};

        		MyAdapter adapter2 = new MyAdapter(getFragmentManager(), titles2,messages2);
        		ViewPager pager2 = ((ViewPager)mView.findViewById(R.id.pager2));

        		pager2.setId((int)(Math.random()*10000));
        		pager2.setOffscreenPageLimit(5);

        		pager2.setAdapter(adapter2);

        		//Bind the title indicator to the adapter
        	     CirclePageIndicator indicator2 = (CirclePageIndicator)mView.findViewById(R.id.circles2);
        	     indicator2.setViewPager(pager2);
        	     indicator2.setSnap(true);
                 final float density = getResources().getDisplayMetrics().density;

        	     indicator2.setRadius(5 * density);
        	     indicator2.setFillColor(0xFFFF0000);
        	     indicator2.setPageColor(0xFFaaaaaa);
        	     //indicator.setStrokeColor(0xFF000000);
        	     //indicator.setStrokeWidth(2 * density);
        	     /*
        	     button = mView.findViewById(R.id.cardButton2);
        	     button.setOnClickListener(new OnClickListener()
        	     {

        			@Override
        			public void onClick(View v) {

        			}
        	    	 
        	     });*/
        }
        return mView;
    }   
}