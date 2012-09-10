package info.guardianproject.mrapp;

import info.guardianproject.mrapp.ui.ProjectSummaryView;

import java.util.ArrayList;
import java.util.Collection;

import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ProjectListActivity extends Activity {

	private ViewPager pager;
	private Context cxt;
	private AwesomePagerAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);
        setTitle(getString(R.string.title_projects));
        
        adapter = new AwesomePagerAdapter();
        
       // for (int i = 0; i < 10; i++)
       // 	adapter.addProjectView(new ProjectSummaryView(this,"Project " + i));
        
        pager = (ViewPager) findViewById(R.id.awesomepager);
        pager.setAdapter(adapter);
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_project_list, menu);
        return true;
    }
    

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if (item.getItemId() == R.id.menu_new_project)
		{
			addNewProject ();
			
			
			return true;
		}
		else
			return super.onMenuItemSelected(featureId, item);
	}
	
	private void addNewProject ()
	{
		//startActivity(new Intent(getBaseContext(), StorylineActivity.class));
	 
		ProjectSummaryView view = new ProjectSummaryView(this,"New Project");
		
		view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getBaseContext(), StorylineActivity.class));
				
			}
			
		});
		
		adapter.addProjectView(view);
		adapter.notifyDataSetChanged();
		
	}

    private class AwesomePagerAdapter extends PagerAdapter{

    	private ArrayList<ProjectSummaryView> listProjectViews;
	 	
    	public AwesomePagerAdapter ()
		{
			listProjectViews = new ArrayList<ProjectSummaryView>();
		}
		
    	public void addProjectView (ProjectSummaryView view)
    	{
    		listProjectViews.add(view);
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
			((ViewPager) collection).removeView((ProjectSummaryView) view);
		}

		
		
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view==((ProjectSummaryView)object);
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
