package info.guardianproject.mrapp.db;

import java.util.ArrayList;

import info.guardianproject.mrapp.model.Lesson;
import info.guardianproject.mrapp.model.Project;

/*
 * This should handle persistence of all lesson data into a SQLCipher instance
 * 
 */
public abstract class LessonManager 
{
	/*
	 * @returns database index id
	 */
	public abstract String persistLesson (Lesson lesson);
	
	public abstract boolean removeLesson (Lesson lesson);

	public abstract ArrayList<Lesson> loadLocalLessons ();
	
}
