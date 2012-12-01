package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.LessonsActivity;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.StoryMakerApp;
import info.guardianproject.mrapp.model.Lesson;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.ListAdapter;

import com.WazaBe.HoloEverywhere.ArrayAdapter;
import com.WazaBe.HoloEverywhere.app.ProgressDialog;
import com.WazaBe.HoloEverywhere.widget.AdapterView;
import com.WazaBe.HoloEverywhere.widget.ListView;
import com.WazaBe.HoloEverywhere.widget.Toast;
import com.WazaBe.HoloEverywhere.widget.View;


public class LessonListView extends ListView implements LessonManagerListener {

		
	private LessonManager mLessonManager;
	private ArrayList<Lesson> mListLessons;
	
	private String mSubFolder = null; 
	private LessonsActivity mActivity;
	
    public LessonListView (Context context, LessonsActivity activity) {
        
    	super (context);
    	
    	mActivity = activity;
    	
        mLessonManager = StoryMakerApp.getLessonManager();
        mLessonManager.setListener(this);
        
        if (mSubFolder == null)
        {
        	//show lesson categories
        	String[] folders = getResources().getStringArray(R.array.lesson_sections);
        	
        	setAdapter(new LessonSectionArrayAdapter(context,R.layout.list_lesson_row, folders));
        	
        	
        }
        else
        {
        	changeLessonFolder(mSubFolder);
        }
        

        setOnItemClickListener(new OnItemClickListener ()
        {

			
			@Override
			public void onItemClick(android.widget.AdapterView<?> arg0,
					android.view.View arg1, int selIdx, long arg3) {
				
				
				if (mSubFolder == null)
				{
					mSubFolder = selIdx+1+"";
					changeLessonFolder(mSubFolder);
				}
				else
				{
					Lesson lesson = mListLessons.get(selIdx);
					accessLesson(lesson);
				}
			}
        	
        });
         
    }
    
    public boolean handleBack ()
    {
    	if (mSubFolder != null)
    	{
    		mSubFolder = null;
    		String[] folders = getResources().getStringArray(R.array.lesson_sections);
        	
        	setAdapter(new LessonSectionArrayAdapter(getContext(),R.layout.list_lesson_row, folders));
        
    		
    		return true;
    	}
    	else
    		return false;
    }
    
    public void changeLessonFolder (String subFolder)
    {
    	mSubFolder = subFolder;
    	mLessonManager.setSubFolder(mSubFolder);
    	
    	mListLessons = mLessonManager.loadLessonList();
    	
    	if (mListLessons.size() == 0)
    	{

	        mActivity.setProgressBarIndeterminateVisibility (true);
    		mLessonManager.updateLessonsFromRemote();
    	}
    	else
    	{
    		loadLessonListAdapter();
    	}
    }
    
    
	private void accessLesson (Lesson lesson)
	{
		
		//if yes, then display here!

		Intent intent = new Intent(getContext(),LessonViewActivity.class);
		intent.putExtra("title", lesson.mTitle);
		intent.putExtra("url", lesson.mResourcePath);
		
		getContext().startActivity(intent);
		
	}
	
	

  
    Handler mHandler = new Handler ()
    {

		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			switch (msg.what)
			{
				case 0:
					
						Toast.makeText(getContext(), msg.getData().getString("status"),Toast.LENGTH_SHORT).show();
					
				 break;
				case 1:
					loadLessonListAdapter();
					

			        mActivity.setProgressBarIndeterminateVisibility (false);
					
				break;
				case 2:

			        mActivity.setProgressBarIndeterminateVisibility (false);					
				default:
				
			}
			
			
		}
    
    	
    	
    };
    
    
    private void loadLessonListAdapter ()
    {
    	   setAdapter(new LessonArrayAdapter(getContext(), 
    	   R.layout.list_lesson_row, mListLessons));
    }
    
     
    
    
	@Override
	public void lessonsLoadedFromServer() {
		
		
    	mListLessons = mLessonManager.loadLessonList();

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
		mHandler.sendEmptyMessage(2);
	}

	
}
