package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.model.Lesson;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class LessonManager {
	
	private URL mUrlRemoteRepo;
	private File mLocalStorageRoot;
	
	private ArrayList<Lesson> mListLessons;
	
	public LessonManager (URL remoteRepoUrl, File localStorageRoot)
	{
		mUrlRemoteRepo = remoteRepoUrl;
		mLocalStorageRoot = localStorageRoot;
	}
	
	public ArrayList<Lesson> getLessonList (boolean refresh)
	{
		if (mListLessons == null || refresh)
		{
			mListLessons = new ArrayList<Lesson>();
			
			// load lessons.json from server
		}
		
		return mListLessons;
	}
	
	public Lesson loadCompleteLesson (Lesson lesson)
	{
		//download remote lesson archive - use LessonLoaderService
		
		//unzip, inflate, etc
		
		return lesson;
	}
	
}
