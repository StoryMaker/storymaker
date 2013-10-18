package info.guardianproject.mrapp.ui;

import info.guardianproject.mrapp.BaseActivity;
import info.guardianproject.mrapp.R;
import android.content.Context;
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

import com.fima.cardsui.objects.Card;
import com.viewpagerindicator.CirclePageIndicator;

public class MyCardPager extends Card {

	private String mDesc = "";
	private View mView;
	private TextView mTextViewDesc;
	private int mId = -1;
	
	private ViewPager mPager = null;
	private MyAdapter mAdapter = null;
	private BaseActivity mActivity = null;
	
	private String[] mMessages = null;
	private String[] mTitles = null;
	
	public MyCardPager(String desc, String[] titles, String[] messages, BaseActivity activity){
		super(desc);
		mDesc = desc;
		mTitles = titles;
		mMessages = messages;
		mActivity = activity;
	}

	

	private OnClickListener mListener;
	
	@Override
	public void setOnClickListener(OnClickListener listener) {
		
		mListener = listener;
		
	}

	@Override
	public View getCardContent(Context context) {
		
		mView = LayoutInflater.from(context).inflate(R.layout.card_pager, null);

		
		mTextViewDesc = ( (TextView) mView.findViewById(R.id.description));
		mTextViewDesc.setText(mDesc);
		mTextViewDesc.setOnClickListener(mListener);
		
		if (mId != -1)
		{
			mView.setId(mId);
			mTextViewDesc.setId(mId);
		}
		
        mAdapter = new MyAdapter(mActivity.getSupportFragmentManager(), mTitles,mMessages);
		mPager = ((ViewPager)mView.findViewById(R.id.pager));
		mPager.setId((int)(Math.random()*10000));
		mPager.setOffscreenPageLimit(5);
		
		 mPager.setAdapter(mAdapter);
		 
		//Bind the title indicator to the adapter
         CirclePageIndicator indicator = (CirclePageIndicator)mView.findViewById(R.id.circles);
         indicator.setViewPager(mPager);
         indicator.setSnap(true);
         
         final float density = context.getResources().getDisplayMetrics().density;
         
         indicator.setRadius(5 * density);
         indicator.setFillColor(0xFFFF0000);
         indicator.setPageColor(0xFFaaaaaa);
         //indicator.setStrokeColor(0xFF000000);
         //indicator.setStrokeWidth(2 * density);
		
		return mView;
	}
	
	
	public void setId (int id)
	{
		mId = id;
	}

	
	 public class MyAdapter extends FragmentPagerAdapter {
		 
		 String[] mMessages;
		 String[] mTitles;
		 
	        public MyAdapter(FragmentManager fm, String[] titles, String[] messages) {
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
	        	bundle.putString("title",mTitles[position]);
	        	bundle.putString("msg", mMessages[position]);
	        	
	        	Fragment f = new MyFragment();
	        	f.setArguments(bundle);
	        	
	            return f;
	        }
	    }
	
	public class MyFragment extends Fragment {
	
		String mMessage;
		String mTitle;
		
		 /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mTitle = getArguments().getString("title");
            mMessage = getArguments().getString("msg");
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
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
