package info.guardianproject.mrapp;


import info.guardianproject.mrapp.model.Project;

import java.util.ArrayList;
import java.util.Locale;

import org.holoeverywhere.app.AlertDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ProjectsListView extends ListView implements Runnable {

	
	private ArrayList<Project> mListProjects;
	
    public ProjectsListView (Context context) {
        
    	super (context);
    
    	this.setOnItemLongClickListener(new OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                
                
                final Project project = mListProjects.get(arg2);


            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.delete_project_)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteProject (project);
                        }
                        
                    })
                    .setNegativeButton(R.string.no, null).show();
            
            
                
                return false;
            }
    	    
    	});
    	
        setOnItemClickListener(new OnItemClickListener ()
        {

			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
				Project project = mListProjects.get(position);
				
				Intent intent = new Intent(getContext(), SceneEditorActivity.class);
		    	intent.putExtra("story_mode", project.getStoryType());
		    	intent.putExtra("pid", project.getId());
		    	intent.putExtra("title", project.getTitle());
		    	
		    	String templateJsonPath = null;
		    	String lang = StoryMakerApp.getCurrentLocale().getLanguage();
		    	
		    	if (project.getStoryType() == Project.STORY_TYPE_VIDEO)
		    	{
		    		//video
		    		templateJsonPath = "story/templates/" + lang + "/video_simple.json";
		    	
		    		
		    	}
		    	else if (project.getStoryType() == Project.STORY_TYPE_PHOTO)
		    	{
		
		    		//photo
		    	
		    		templateJsonPath = "story/templates/" + lang + "/photo_simple.json";
		    	}
		    	else if (project.getStoryType() == Project.STORY_TYPE_AUDIO)
		    	{
		
		    		//audio
		    	
		    		templateJsonPath = "story/templates/" + lang + "/audio_simple.json";
		    	}
		    	else if (project.getStoryType() == Project.STORY_TYPE_ESSAY)
		    	{
		    		//essay
		    		templateJsonPath = "story/templates/" + lang + "/essay_simple.json";
		    		
		    	}
		    	
		    	intent.putExtra("template_path", templateJsonPath);
		    	
		        getContext().startActivity(intent);
			}
        	
        });
        
        mListProjects = Project.getAllAsList(getContext());
        
        setAdapter(new ProjectArrayAdapter(getContext(), 
         	   R.layout.list_lesson_row, mListProjects));
        
         
        
    }
    
    private void deleteProject (Project project)
    {
        project.delete();
        

        mListProjects = Project.getAllAsList(getContext());
        
        setAdapter(new ProjectArrayAdapter(getContext(), 
               R.layout.list_lesson_row, mListProjects));
    }
    
    public void refresh ()
    {
    	mListProjects = Project.getAllAsList(getContext());
         
         setAdapter(new ProjectArrayAdapter(getContext(), 
          	   R.layout.list_lesson_row, mListProjects));	
    }
    
     
    
    class ProjectArrayAdapter extends ArrayAdapter {
    	
    	Context context; 
        int layoutResourceId;    
        ArrayList<Project> projects;
        
        public ProjectArrayAdapter(Context context, int layoutResourceId,ArrayList<Project> projects) {
            super(context, layoutResourceId, projects);        
            
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.projects = projects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            
            TextView tv;
            
            if(row == null)
            {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                
                
            }
            
            tv = (TextView)row.findViewById(R.id.lessonRowTitle);
            
            tv.setText(projects.get(position).getTitle());        
            
            
            return row;
        }
        
    }




	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	
}
