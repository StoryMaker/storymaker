package org.codeforafrica.timby.listeningpost;

import java.util.ArrayList;

import org.codeforafrica.timby.listeningpost.model.Media;
import org.codeforafrica.timby.listeningpost.model.Project;

//used to delete a project's media on storage
public class ProjectCleaner {
	
	public static void clean (Project project)
	{
		// FIXME default to use first scene
		ArrayList<Media> alMedia = project.getScenesAsArray()[0].getMediaAsList();
		
	}

}
