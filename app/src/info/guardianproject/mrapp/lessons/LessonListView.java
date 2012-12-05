package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.AppConstants;
import info.guardianproject.mrapp.LessonsActivity;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.StoryMakerApp;
import info.guardianproject.mrapp.model.Lesson;
import info.guardianproject.mrapp.model.LessonGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
	
	private Locale mLocale = null;
	
    public LessonListView (Context context, LessonsActivity activity) {
        
    	super (context);
    	
    	mActivity = activity;
    	mLocale = ((StoryMakerApp)mActivity.getApplication()).getCurrentLocale();
    	
        mLessonManager = StoryMakerApp.getLessonManager();
        mLessonManager.setListener(this);
        
        if (mSubFolder == null)
        {
        	showLessonGroups();
        	
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
    
    private void showLessonGroups ()
    {
    	//show lesson categories
    	String[] lessonSections = getResources().getStringArray(R.array.lesson_sections);
    	String[] lessonSectionsFolder = getResources().getStringArray(R.array.lesson_sections_folder);

    	ArrayList<LessonGroup> alGroups = new ArrayList<LessonGroup>();
    	int idx = 0;
    	
    	for (String folder : lessonSections)
    	{
    		LessonGroup lg = new LessonGroup();
    		lg.mTitle = folder;
    		
    		String subFolder = lessonSectionsFolder[idx++];
    	
    		ArrayList<Lesson> lessons = LessonManager.loadLessonList(getContext(), mLessonManager.getLessonRoot(), subFolder, mLocale.getLanguage());
    		
    		int lessonsComplete = 0;
    		
    		for (Lesson lesson : lessons)
    			if (lesson.mStatus == Lesson.STATUS_COMPLETE)
    				lessonsComplete++;
    		
    		lg.mStatus = lessonsComplete + " of " + lessons.size() + " lesson complete";
    		
    		alGroups.add(lg);
    	}
    	
    	setAdapter(new LessonGroupArrayAdapter(getContext(),R.layout.list_lesson_row, alGroups));
    	
    }
    
    public boolean handleBack ()
    {
    	if (mSubFolder != null)
    	{
    		mSubFolder = null;
    		
    		showLessonGroups();
    		
        
    		
    		return true;
    	}
    	else
    		return false;
    }
    
    public void changeLessonFolder (String subFolder)
    {
    	mSubFolder = subFolder;
    	mLessonManager.setSubFolder(mSubFolder);
    	
    	mListLessons = mLessonManager.loadLessonList(getContext(), mLocale.getLanguage());
    	
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

		if (lesson.mStatus != Lesson.STATUS_COMPLETE)
		{
			try {
				mLessonManager.updateLessonStatus(lesson.mLocalPath.getAbsolutePath(), Lesson.STATUS_IN_PROGRESS);
			} catch (Exception e) {
				Log.e(AppConstants.TAG,"erorr updating lesson status",e);
			}
		}
		
		Intent intent = new Intent(getContext(),LessonViewActivity.class);
		intent.putExtra("title", lesson.mTitle);
		intent.putExtra("url", lesson.mResourcePath);
		intent.putExtra("lessonPath", lesson.mLocalPath.getAbsolutePath());
		mActivity.startActivityForResult(intent, 1);
		
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
    
    public void refreshList ()
    {
    	changeLessonFolder(mSubFolder);
    	loadLessonListAdapter ();
    }
    
    private void loadLessonListAdapter ()
    {
    	   setAdapter(new LessonArrayAdapter(getContext(), 
    	   R.layout.list_lesson_row, mListLessons));
    }
    
     
    
    
	@Override
	public void lessonsLoadedFromServer() {
		
		
    	mListLessons = mLessonManager.loadLessonList(getContext(), mLocale.getLanguage());

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
