package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.ProjectListActivity;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.StoryMakerApp;
import info.guardianproject.mrapp.model.Lesson;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class LessonPagerActivity extends SherlockActivity implements Runnable, LessonManagerListener {

	private ViewPager pager;
	private AwesomePagerAdapter adapter;
	
	private LessonManager mLessonManager;
	private ArrayList<Lesson> mListLessons;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);
        setTitle(getString(R.string.title_lessons));
        
        adapter = new AwesomePagerAdapter();
        
        pager = (ViewPager) findViewById(R.id.awesomepager);
        pager.setAdapter(adapter);
        
        mLessonManager = ((StoryMakerApp)getApplication()).getLessonManager();
        mLessonManager.setListener(this);
        
    	mListLessons = mLessonManager.loadLessonList(true);
    	if (mListLessons.size() == 0)
    	{

			 showMessage("Downloading lessons from server...");
			 mLessonManager.updateLessonsFromRemote();
    	}
    	else
    	{
    		new Thread(this).start();
    	}
    }
    
    public void run ()
    {
    	mListLessons = mLessonManager.loadLessonList(true);
    	mHandler.sendEmptyMessage(1);
    	
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_lesson_list, menu);
        return true;
    }
    

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		 if (item.getItemId() == R.id.menu_projects)
         {
			 Intent intent = new Intent (this, ProjectListActivity.class);
				startActivity(intent);
         }	
		 else if (item.getItemId() == R.id.menu_update)
         {
			 
			 adapter.clearViews();
			 showMessage("Updating lessons from server...");
			 mLessonManager.updateLessonsFromRemote();
         }	
		  
		  
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void accessLesson ()
	{
		
		//if yes, then display here!
		Lesson lesson = mListLessons.get(pager.getCurrentItem());

		Intent intent = new Intent(this,LessonViewActivity.class);
		intent.putExtra("title", lesson.mTitle);
		intent.putExtra("url", lesson.mResourcePath);
		
		startActivity(intent);
		
	}
	
	private void addNewLessonView (Lesson lesson)
	{
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
    	
    	public void clearViews ()
    	{
    		listViews.clear();
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

    Handler mHandler = new Handler ()
    {

		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			switch (msg.what)
			{
				case 0:
				 Toast.makeText(LessonPagerActivity.this, msg.getData().getString("status"),Toast.LENGTH_SHORT).show();
				 break;
				case 1:
				
					adapter.clearViews();
					//reload lessons in list
			    	for (Lesson lesson : mListLessons)
			    		addNewLessonView(lesson);
			    		
					
				break;
				
				default:
				
			}
			
			
		}
    
    	
    	
    };
    
	@Override
	public void lessonsLoadedFromServer() {
		
    	mListLessons = mLessonManager.loadLessonList(true);

		mHandler.sendEmptyMessage(1);
		
	}

	private void showMessage (String msgText)
	{
		Message msg = new Message();
		msg.what = 0;
		msg.getData().putString("status", msgText);
		mHandler.sendMessage(msg);

	}

	@Override
	public void loadingLessonFromServer(String lessonTitle) {
		showMessage("Loading lesson: " +  lessonTitle);
		
	}

	@Override
	public void errorLoadingLessons(String msg) {
		
		showMessage("There was a problem loading the lessons: " + msg);
	}
}
