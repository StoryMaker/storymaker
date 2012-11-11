package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.ProjectListActivity;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.StoryMakerApp;
import info.guardianproject.mrapp.model.Lesson;

import java.util.ArrayList;

import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class LessonListActivity extends SherlockListActivity implements Runnable, LessonManagerListener {

		
	private LessonManager mLessonManager;
	private ArrayList<Lesson> mListLessons;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);
        setTitle(getString(R.string.title_lessons));
        
        this.getListView().setOnItemClickListener(new OnItemClickListener ()
        {

			

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				accessLesson(arg2);
				
			}
        	
        });
         
        mLessonManager = ((StoryMakerApp)getApplication()).getLessonManager();
        mLessonManager.setListener(this);
        
    	mListLessons = mLessonManager.loadLessonList(true);
    	if (mListLessons.size() == 0)
    	{

    		loadLessonsFromServer();
    	}
    	else
    	{
    		new Thread(this).start();
    	}
    }
    
    ProgressDialog progressDialog;
    
    private void loadLessonsFromServer ()
    {
    		
    	progressDialog = new ProgressDialog(this);
    	progressDialog.setTitle("Downloading lessons from server...");
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
        progressDialog.show();

		mLessonManager.updateLessonsFromRemote();
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
			 
			 
			 loadLessonsFromServer();
         }	
		  
		  
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void accessLesson (int lessonIdx)
	{
		
		//if yes, then display here!
		Lesson lesson = mListLessons.get(lessonIdx);

		Intent intent = new Intent(this,LessonViewActivity.class);
		intent.putExtra("title", lesson.mTitle);
		intent.putExtra("url", lesson.mResourcePath);
		
		startActivity(intent);
		
	}
	
	

  
    Handler mHandler = new Handler ()
    {

		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			switch (msg.what)
			{
				case 0:
					
					if (progressDialog != null)
					{
						progressDialog.setMessage( msg.getData().getString("status"));
					}
					else
					{
						Toast.makeText(LessonListActivity.this, msg.getData().getString("status"),Toast.LENGTH_SHORT).show();
					}
				 break;
				case 1:
					
					if (progressDialog != null)						
						progressDialog.dismiss();
					
				//	adapter.clearViews();
					//reload lessons in list
			    	
					loadListAdapter();
					
					
				break;
				
				default:
				
			}
			
			
		}
    
    	
    	
    };
    
    private void loadListAdapter ()
    {
    	
    	   setListAdapter(new ArrayAdapter(this, 
    	   android.R.layout.simple_list_item_1, mListLessons));
    }
    
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
