package info.guardianproject.mrapp;

import info.guardianproject.mrapp.lessons.LessonPagerActivity;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


public class ProjectListActivity extends SherlockActivity {

	private ViewPager pager;
	private AwesomePagerAdapter adapter;
	private ArrayList<Project> alProjects;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);
        setTitle(getString(R.string.title_projects));
        
        adapter = new AwesomePagerAdapter();
        alProjects = new ArrayList<Project>();
        
        pager = (ViewPager) findViewById(R.id.awesomepager);
        pager.setAdapter(adapter);
        
        if (alProjects.size() == 0)
        {
        	addDefaultView ();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_project_list, menu);
        return true;
    }
    
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if (item.getItemId() == R.id.menu_new_project)
		{
			addNewProject ();
			
			return true;
		}
		else if (item.getItemId() == R.id.menu_lessons)
		{
			Intent intent = new Intent (this, LessonPagerActivity.class);
			startActivity(intent);
			
			return true;
		}
		else
			return super.onMenuItemSelected(featureId, item);
	}
	
	private void addNewProject ()
	{
		
		Project project = new Project ();
		project.mId =(adapter.getCount());
		project.mTitle = "Project " + project.mId;
		alProjects.add(project);
		
		Bitmap image = null;
		
		try
		{			
			image = BitmapFactory.decodeStream(getAssets().open("images/MediumShot.jpg"));
		}
		catch (Exception e){
			
			Log.e(AppConstants.TAG,"error loading image",e);
		}
		
		BigImageLabelView view = new BigImageLabelView(this,project.mTitle, image, Color.WHITE);
		
		view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				showProjectView ();
				
			}
			
			
		});
		
		view.setOnLongClickListener(new OnLongClickListener ()
		{

			@Override
			public boolean onLongClick(View v) {
				
				adapter.removeProjectView(pager.getCurrentItem());

				adapter.notifyDataSetChanged();
				
				return false;
			}
			
		});
		
		adapter.addProjectView(view);
		adapter.notifyDataSetChanged();
		
		pager.setCurrentItem(adapter.getCount(), true);
		
	}
	
	private void addDefaultView ()
	{
		Project project = new Project ();
		project.mId =-1;
		project.mTitle = "dummy project";
		alProjects.add(project);
		
		TextView view = new TextView(this);
		view.setTextSize(36);
		view.setText(R.string.default_project_list_view);
		view.setTextColor(Color.WHITE);
		view.setBackgroundColor(Color.DKGRAY);
		view.setPadding(6,6,6,6);
		
		view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				addNewProject();
				
			}
			
			
		});
		
		adapter.addProjectView(view);
		adapter.notifyDataSetChanged();
		
		pager.setCurrentItem(adapter.getCount()-1, true);
	}
	
	private void showProjectView ()
	{
		Project projectCurrent = alProjects.get(pager.getCurrentItem());
		
		Intent intent = new Intent(getBaseContext(), ProjectViewActivity.class);
		intent.putExtra("pid", projectCurrent.mId);
		intent.putExtra("title", projectCurrent.mTitle);
		
		startActivity(intent);
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
