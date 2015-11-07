package org.storymaker.app.model;

import timber.log.Timber;

public class StoryTag {

	private int Id;
	private String Name;
	
	public StoryTag(int id, String name){
		this.Id = id;
		this.Name = name;
	}
	
	public int getId(){
	     return this.Id;
	}
	public void setId(int id){
	     this.Id = id;
	}
	
	public String getName(){
	     return this.Name;
	}
	public void setName(String name){
	     this.Name = name;
	}
}
