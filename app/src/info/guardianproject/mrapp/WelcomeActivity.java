package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.ui.BigImageLabelView;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


public class WelcomeActivity extends SherlockActivity {

	private ViewPager pager;
	private AwesomePagerAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);
        setTitle(getString(R.string.app_name));
        
        adapter = new AwesomePagerAdapter();
        
        pager = (ViewPager) findViewById(R.id.awesomepager);
        pager.setAdapter(adapter);
        
        addDefaultView ();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_welcome, menu);
        return true;
    }
    
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if (item.getItemId() == R.id.menu_projects)
		{
			Intent intent = new Intent (this, ProjectListActivity.class);
			startActivity(intent);
			
			return true;
		}
		else if (item.getItemId() == R.id.menu_lessons)
		{
			Intent intent = new Intent (this, LessonListActivity.class);
			startActivity(intent);
			
			return true;
		}
		else
			return super.onMenuItemSelected(featureId, item);
	}
	

	
	private void addDefaultView ()
	{
		
		TextView view = new TextView(this);
		view.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		view.setTextSize(30);
		view.setSingleLine(false);
		view.setText(R.string.welcome_message);
		view.setTextColor(Color.DKGRAY);
		view.setBackgroundColor(Color.LTGRAY);
		view.setPadding(6,6,6,6);
		
		view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent (WelcomeActivity.this, ProjectListActivity.class);
				startActivity(intent);
				
			}
			
			
		});
		
		adapter.addProjectView(view);
		adapter.notifyDataSetChanged();
		
		pager.setCurrentItem(adapter.getCount()-1, true);
	}
	
	

    private class AwesomePagerAdapter extends PagerAdapter{

    	private ArrayList<View> listProjectViews;
	 	
    	public AwesomePagerAdapter ()
		{
			listProjectViews = new ArrayList<View>();
		}
		
    	public void addProjectView (View view)
    	{
    		listProjectViews.add(view);
    	}
    	
    	public void removeProjectView (View view)
    	{
    		listProjectViews.remove(view);
    	}
    	
    	public void removeProjectView (int viewIdx)
    	{
    		listProjectViews.remove(viewIdx);
    	}
	 	
		@Override
		public int getCount() {
			
			return listProjectViews.size();
			
		}

	    /**
	     * Create the page for the given position.  The adapter is responsible
	     * for adding the view to the container given here, although it only
	     * must ensure this is done by the time it returns from
	     * {@link #finishUpdate()}.
	     *
	     * @param container The containing View in which the page will be shown.
	     * @param position The page position to be instantiated.
	     * @return Returns an Object representing the new page.  This does not
	     * need to be a View, but can be some other container of the page.
	     */
		@Override
		public Object instantiateItem(View collection, int position) {
			
			((ViewPager) collection).addView(listProjectViews.get(position));
			
			return listProjectViews.get(position);
		}

	    /**
	     * Remove a page for the given position.  The adapter is responsible
	     * for removing the view from its container, although it only must ensure
	     * this is done by the time it returns from {@link #finishUpdate()}.
	     *
	     * @param container The containing View from which the page will be removed.
	     * @param position The page position to be removed.
	     * @param object The same object that was returned by
	     * {@link #instantiateItem(View, int)}.
	     */
		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager) collection).removeView((View) view);
		}

		
		
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view==((View)object);
		}

		
	    /**
	     * Called when the a change in the shown pages has been completed.  At this
	     * point you must ensure that all of the pages have actually been added or
	     * removed from the container as appropriate.
	     * @param container The containing View which is displaying this adapter's
	     * page views.
	     */
		@Override
		public void finishUpdate(View arg0) {}
		

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {}
    	
    }

}
