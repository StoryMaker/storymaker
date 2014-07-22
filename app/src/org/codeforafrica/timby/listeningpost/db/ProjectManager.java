package org.codeforafrica.timby.listeningpost.db;

import java.util.ArrayList;

import org.codeforafrica.timby.listeningpost.model.Project;

/*
 * This should handle persistence of all project data into a SQLCipher instance
 * 
 */
public abstract class ProjectManager 
{
	/*
	 * @returns database index id
	 */
	public abstract String persistProject (Project project);
	
	public abstract boolean removeProject (Project project);

	public abstract ArrayList<Project> loadProjects ();
	
}
