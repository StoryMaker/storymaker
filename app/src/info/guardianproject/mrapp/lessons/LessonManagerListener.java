package info.guardianproject.mrapp.lessons;

import info.guardianproject.mrapp.model.Lesson;

public interface LessonManagerListener {

	public void loadingLessonFromServer(String subFolder, String lessonTitle);
	
	public void lessonsLoadedFromServer();
	
	public void errorLoadingLessons(String msg);
}
