package org.codeforafrica.timby.listeningpost.spy;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.codeforafrica.timby.listeningpost.AppConstants;
import org.codeforafrica.timby.listeningpost.model.Project;
import org.codeforafrica.timby.listeningpost.model.Report;
import org.codeforafrica.timby.listeningpost.model.Scene;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;


public class StoryNewService extends Service{
	private int rid;
	private int quickstory;
	private int storymode;
	
	@Override
    public void onCreate() {
          super.onCreate();
          
          
    }
	  @Override
		public int onStartCommand(Intent intent, int flags, int startId){
			super.onStartCommand(intent, flags, startId);
	       Bundle extras = intent.getExtras(); 
	       
	       quickstory = intent.getIntExtra("quickstory", 0);
	       storymode = intent.getIntExtra("storymode", -1);

	       launchSimpleStory("", storymode, false, quickstory);

	       return startId;
	       
	  }
	  private int createReport() {
	      	
	      	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	      	String currentdate = dateFormat.format(new Date());
	      	
	      	String pLocation = "0, 0";
	      	
	      	String title = "Captured at "+currentdate;
	      	
	      	Report report = new Report (getApplicationContext(), 0, title, "0", "0", "", "", pLocation, "0", currentdate, "0", "0");

	        report.save();
	          
	        rid = report.getId();
	        
	        return rid;
	          
	   }
	   
	    
	   public void launchSimpleStory(String pName, int storyMode, boolean autoCapture, int quickstory) {
	        int clipCount = AppConstants.DEFAULT_CLIP_COUNT;
	        
	        rid = createReport();
	        
	        
	        Project project = new Project (getBaseContext(), clipCount);
	        project.setTitle(pName);
	        project.setReport_Id(rid);
	        project.save();
	        
	        Scene scene = new Scene(this, clipCount);
	        scene.setProjectIndex(0);
	        scene.setProjectId(project.getId());
	        scene.save();
	    
	        String templateJsonPath = Project.getSimpleTemplateForMode(getApplicationContext(), storyMode);
	       
	        project.setStoryType(storyMode);
	        project.save();
	        
	        Intent intent = new Intent(getBaseContext(), SceneEditorActivity_spy.class);
	        intent.putExtra("story_mode", storyMode);
	        intent.putExtra("template_path", templateJsonPath);
	        intent.putExtra("title", project.getTitle());
	        intent.putExtra("pid", project.getId());
	        intent.putExtra("scene", 0);
	        intent.putExtra("quickstory", quickstory);
	        intent.putExtra("auto_capture", true);
	        startActivity(intent);
	        
	    }
			
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		
		return null;
	}

}
