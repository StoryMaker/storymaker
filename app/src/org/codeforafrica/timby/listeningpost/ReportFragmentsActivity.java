package org.codeforafrica.timby.listeningpost;

import org.codeforafrica.timby.listeningpost.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

public class ReportFragmentsActivity extends BaseActivity {
	
	public void onCreate(Bundle SavedInstance){
		setContentView(R.layout.activity_home_intro);
		initCaptureFragments();
	}
	
	public void initCaptureFragments(){
      	
		int[] titles1 =
			{(R.string.tutorial_title_1),
				(R.string.tutorial_title_2),
				(R.string.tutorial_title_3),
				(R.string.tutorial_title_4),
				(R.string.tutorial_title_5)
				};
		int[] messages1 =
			{(R.string.tutorial_text_1),
				(R.string.tutorial_text_2),
				(R.string.tutorial_text_3),
				(R.string.tutorial_text_4),
				(R.string.tutorial_text_5)
				};
		
		MyAdapter adapter = new MyAdapter(getSupportFragmentManager(), titles1,messages1);
		ViewPager pager = ((ViewPager)findViewById(R.id.pager1));

		pager.setId((int)(Math.random()*10000));
		pager.setOffscreenPageLimit(5);

		pager.setAdapter(adapter);

		//Bind the title indicator to the adapter
         CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.circles1);
         indicator.setViewPager(pager);
         indicator.setSnap(true);
         
         final float density = getResources().getDisplayMetrics().density;
         
         indicator.setRadius(5 * density);
         indicator.setFillColor(0xFFFF0000);
         indicator.setPageColor(0xFFaaaaaa);
         //indicator.setStrokeColor(0xFF000000);
         //indicator.setStrokeWidth(2 * density);
         
         View button = findViewById(R.id.cardButton1);
         button.setOnClickListener(new OnClickListener()
         {

			@Override
			public void onClick(View v) {

				//Intent intent = new Intent(HomeActivity.this, LessonsActivity.class);
				//startActivity(intent);
			}
        	 
         });    	
    		
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

		MyAdapter adapter2 = new MyAdapter(getSupportFragmentManager(), titles2,messages2);
		ViewPager pager2 = ((ViewPager)findViewById(R.id.pager2));

		pager2.setId((int)(Math.random()*10000));
		pager2.setOffscreenPageLimit(5);

		pager2.setAdapter(adapter2);

		//Bind the title indicator to the adapter
         CirclePageIndicator indicator2 = (CirclePageIndicator)findViewById(R.id.circles2);
         indicator2.setViewPager(pager2);
         indicator2.setSnap(true);
      
         indicator2.setRadius(5 * density);
         indicator2.setFillColor(0xFFFF0000);
         indicator2.setPageColor(0xFFaaaaaa);
         //indicator.setStrokeColor(0xFF000000);
         //indicator.setStrokeWidth(2 * density);

         button = findViewById(R.id.cardButton2);
         button.setOnClickListener(new OnClickListener()
         {

			@Override
			public void onClick(View v) {

			}
        	 
         });
	}
	public class MyAdapter extends FragmentPagerAdapter {

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
	        	bundle.putString("title",getString(mTitles[position]));
	        	bundle.putString("msg", getString(mMessages[position]));
	        	
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
      public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
          
          ViewGroup root = (ViewGroup) inflater.inflate(R.layout.card_pager_textview, null);
          
          ((TextView)root.findViewById(R.id.title)).setText(mTitle);
          
          ((TextView)root.findViewById(R.id.description)).setText(mMessage);
          
          return root;
      }
	
	}
	
}
