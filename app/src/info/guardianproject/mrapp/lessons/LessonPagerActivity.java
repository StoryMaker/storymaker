package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.MediaAppConstants;
import info.guardianproject.mrapp.ProjectListActivity;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.R.id;
import info.guardianproject.mrapp.R.layout;
import info.guardianproject.mrapp.R.menu;
import info.guardianproject.mrapp.R.string;
import info.guardianproject.mrapp.model.Lesson;
import info.guardianproject.mrapp.ui.BigImageLabelView;
import info.guardianproject.mrapp.ui.OverlayCamera;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class LessonPagerActivity extends SherlockActivity {

	private ViewPager pager;
	private AwesomePagerAdapter adapter;
	
	private ArrayList<Lesson> alLessons;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);
        setTitle(getString(R.string.title_lessons));
        
        adapter = new AwesomePagerAdapter();
        
        pager = (ViewPager) findViewById(R.id.awesomepager);
        pager.setAdapter(adapter);
        
        
        initTestLessons();
        
    }
    
    private void showOverlayCamera ()
	{

		Intent intent = new Intent(this, OverlayCamera.class);
		startActivity(intent);
	}
    
    private void initTestLessons ()
    {
    	alLessons = new ArrayList<Lesson>();
    	
    	try {

    		for (int i = 1; i < 100; i++)
    		{
    			InputStream is = getAssets().open("lessons/" + i + "/lesson.json");
    			Lesson lesson = Lesson.parse(IOUtils.toString(is));
    			addNewLesson (lesson);
    		}
	    	
    	} catch (Exception e) {
			Log.e(MediaAppConstants.TAG,"error parsing Lesson",e);
		}
    	
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_lesson_list, menu);
        return true;
    }
    

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		 if (item.getItemId() == R.id.menu_overlay_camera)
         {
			 
			 showOverlayCamera();
         }	
		 else if (item.getItemId() == R.id.menu_projects)
         {
			 Intent intent = new Intent (this, ProjectListActivity.class);
				startActivity(intent);
         }	
		  
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void loadLessons ()
	{
		//access remote XML/RSS for lessons
		
		//add list of available lessons
		
		
		
		
	}
	
	private void accessLesson ()
	{
		//is lesson downloaded? if no, then download
		
		//if yes, then display here!
		Lesson lesson = alLessons.get(pager.getCurrentItem());

		Intent intent = new Intent(this,LessonViewActivity.class);
		intent.putExtra("title", lesson.mTitle);
		intent.putExtra("url", lesson.mResourcePath);
		
		startActivity(intent);
		
	}
	
	private void addNewLesson (Lesson lesson)
	{
		alLessons.add(lesson);
		TextView view = new TextView(this);
		view.setTextSize(36);
		view.setText(lesson.mTitle);
		view.setTextColor(Color.WHITE);
		view.setBackgroundColor(Color.DKGRAY);
		view.setPadding(6,6,6,6);
		
		view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				accessLesson();
			}
			
			
		});
	
		adapter.addProjectView(view);
		adapter.notifyDataSetChanged();
		
	}

    private class AwesomePagerAdapter extends PagerAdapter{

    	private ArrayList<View> listViews;
	 	
    	public AwesomePagerAdapter ()
		{
    		listViews = new ArrayList<View>();
		}
		
    	public void addProjectView (View view)
    	{
    		listViews.add(view);
    	}
    	
    	public void removeProjectView (View view)
    	{
    		listViews.remove(view);
    	}
    	
    	public void removeProjectView (int viewIdx)
    	{
    		listViews.remove(viewIdx);
    	}
	 	
		@Override
		public int getCount() {
			
			return listViews.size();
			
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
			
			((ViewPager) collection).addView(listViews.get(position));
			
			return listViews.get(position);
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
