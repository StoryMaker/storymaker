package org.storymaker.app;

import timber.log.Timber;

import org.storymaker.app.model.Media;
import org.storymaker.app.model.Project;

import java.util.ArrayList;

//used to delete a project's media on storage
public class ProjectCleaner {
	
	public static void clean (Project project)
	{
		// FIXME default to use first scene
		ArrayList<Media> alMedia = project.getScenesAsArray()[0].getMediaAsList();
		
	}

}
