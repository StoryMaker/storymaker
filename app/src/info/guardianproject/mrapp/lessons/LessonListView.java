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

import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.Toast;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;



public class LessonListView extends ListView implements LessonManagerListener {

		
	private LessonManager mLessonManager;
	private ArrayList<Lesson> mListLessons;
	private ArrayList<LessonGroup> mLessonGroups;
	
	
	private String mSubFolder = null; 
	private LessonsActivity mActivity;
	
	private Locale mLocale = null;
	
	private int mLastIdx = -1;
	
    public LessonListView (Context context, LessonsActivity activity) {
        
    	super (context);
    	
    	setDivider(null);
    	setDividerHeight(0);
    	
    	mActivity = activity;
    	mLocale = ((StoryMakerApp)mActivity.getApplication()).getCurrentLocale();
    	
        mLessonManager = StoryMakerApp.getLessonManager();
        mLessonManager.setListener(this);
        
        new Thread ()
        {
        	public void run ()
        	{

        		Message msg = mHandler.obtainMessage(0);
        		mHandler.sendMessage(msg);
        		loadData();
        		mHandler.sendEmptyMessage(3);
        	}
        }.start();
    }
    
    private void loadData ()
    {
        
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
			public void onItemClick(android.widget.AdapterView<?> aview,
					android.view.View view, int selIdx, long arg3) {
				
				
				mLastIdx = selIdx;
				
				if (mSubFolder == null)
				{
					mSubFolder = selIdx+1+"";
					changeLessonFolder(mSubFolder);
				}
				else if (selIdx < mListLessons.size())
				{
					if (selIdx == 0 || mListLessons.get(selIdx-1).mStatus == Lesson.STATUS_COMPLETE)
					{
						Lesson lesson = mListLessons.get(selIdx);
						accessLesson(lesson);
					}
				}
			}
        	
        });
         
    }
    
    private void showLessonGroups ()
    {
    	//show lesson categories
    	String[] lessonSections = getResources().getStringArray(R.array.lesson_sections);
    	String[] lessonSectionsFolder = getResources().getStringArray(R.array.lesson_sections_folder);

    	mLessonGroups = new ArrayList<LessonGroup>();
    	int idx = 0;
    	
    	for (String folder : lessonSections)
    	{
    		LessonGroup lg = new LessonGroup();
    		lg.mTitle = folder;
    		
    		String subFolder = lessonSectionsFolder[idx++];
    	
    		ArrayList<Lesson> lessons = LessonManager.loadLessonList(getContext(), mLessonManager.getLessonRoot(), subFolder, mLocale.getLanguage(),-1);
    		
    		int lessonsComplete = 0;
    		
    		for (Lesson lesson : lessons)
    			if (lesson.mStatus == Lesson.STATUS_COMPLETE)
    				lessonsComplete++;
    		
    		if (lessons.size() == 0)
    		{
    			lg.mStatus = getContext().getString(
						R.string.no_lessons_available_tap_to_load_);
    		}
    		else
    		{
    			lg.mStatus = lessonsComplete + getContext().getString(R.string._of_) + lessons.size() + getContext().getString(R.string._lesson_complete);
    		}
    		mLessonGroups.add(lg);
    	}
    	
    	
    	
    }
    
   
   
    public boolean handleBack ()
    {
    	if (mSubFolder != null)
    	{
    		mSubFolder = null;
    		
    		showLessonGroups();
    		mHandler.sendEmptyMessage(3);
    		
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

	        
	        mActivity.setSupportProgressBarIndeterminateVisibility(true);
	        
    		mLessonManager.updateLessonsFromRemote();
    		
    	}
    	
    	loadLessonListAdapter();
    	
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
		
		try
		{
			LessonManager.updateLessonResource(getContext(),lesson,mLocale.getLanguage());
			
			Intent intent = new Intent(getContext(),LessonViewActivity.class);
			intent.putExtra("title", lesson.mTitle);
			intent.putExtra("url", lesson.mResourcePath);
			intent.putExtra("lessonPath", lesson.mLocalPath.getAbsolutePath());
			mActivity.startActivityForResult(intent, 1);
		}
		catch (IOException e)
		{
			Log.e(AppConstants.TAG,"error updating lesson",e);
		}
	}
	
	

  
    Handler mHandler = new Handler ()
    {

		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			switch (msg.what)
			{
				case 0:
					
						if (msg.getData().containsKey("status"))
							Toast.makeText(getContext(), msg.getData().getString("status"),Toast.LENGTH_SHORT).show();

						if (getAdapter() != null)
							((ArrayAdapter)getAdapter()).notifyDataSetChanged();

						mActivity.setSupportProgressBarIndeterminateVisibility(true);
				        
				 break;
				case 1:

			    	mListLessons = mLessonManager.loadLessonList(getContext(), mLocale.getLanguage());

					loadLessonListAdapter();
					
					
				break;
				case 2:
					mActivity.setSupportProgressBarIndeterminateVisibility(false);
			    break;
			    
				case 3: //update group list

			    	setAdapter(new LessonGroupArrayAdapter(getContext(),R.layout.list_lesson_row, mLessonGroups));
					mActivity.setSupportProgressBarIndeterminateVisibility(false);

				break;
				
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
    	mHandler.sendEmptyMessage(2);
    	
		
		
	}

	

	@Override
	public void loadingLessonFromServer(String subFolder, String lessonTitle) {
		
		/*
		int rowIdx = Integer.parseInt(subFolder)-1;
		
		Object item = this.getItemAtPosition(rowIdx);
		
		if (item instanceof LessonGroup)
		{
			((LessonGroup)item).mStatus = "loading lesson: " + lessonTitle;
		}
		*/
		
		mHandler.sendEmptyMessage(0);
		mHandler.sendEmptyMessage(1);
		
	}

	@Override
	public void errorLoadingLessons(String msg) {
		
		
		mHandler.sendEmptyMessage(2);
	}

	
}
