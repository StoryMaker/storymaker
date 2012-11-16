package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.StoryMakerApp;
import info.guardianproject.mrapp.model.Lesson;

import java.util.ArrayList;

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


public class LessonListView extends ListView implements Runnable, LessonManagerListener {

		
	private LessonManager mLessonManager;
	private ArrayList<Lesson> mListLessons;
	
    public LessonListView (Context context) {
        
    	super (context);
    
        setOnItemClickListener(new OnItemClickListener ()
        {

			
			@Override
			public void onItemClick(android.widget.AdapterView<?> arg0,
					android.view.View arg1, int arg2, long arg3) {
				accessLesson(arg2);
				
			}
        	
        });
         
        mLessonManager = StoryMakerApp.getLessonManager();
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
    
    //ProgressDialog progressDialog;
    
    public void loadLessonsFromServer ()
    {
    	/*	
    	progressDialog = new ProgressDialog(getContext());
    	progressDialog.setTitle(getContext().getString(R.string.downloading_lessons_from_server_));
    	progressDialog.setMessage(getContext().getString(R.string.checking_for_updates_));
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
        progressDialog.show();
		*/
        
		mLessonManager.updateLessonsFromRemote();
    }
    
    public void run ()
    {
    	mListLessons = mLessonManager.loadLessonList(true);
    	mHandler.sendEmptyMessage(1);
    	
    }
    
    
   
	private void accessLesson (int lessonIdx)
	{
		
		//if yes, then display here!
		Lesson lesson = mListLessons.get(lessonIdx);

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
					
				//	if (progressDialog != null)						
					//	progressDialog.dismiss();
					
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
    	   setAdapter(new ArrayAdapter(getContext(), 
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
