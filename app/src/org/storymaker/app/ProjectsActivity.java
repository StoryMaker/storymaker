package org.storymaker.app;

import org.storymaker.app.model.Media;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.ProjectTable;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ProjectsActivity extends BaseActivity {


	ListView mListView;

	private ArrayList<Project> mListProjects;
	private ProjectArrayAdapter aaProjects;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.activity_projects);
        
        // action bar stuff
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        mListView = (ListView)findViewById(R.id.projectslist);
        initListView(mListView);
    }
    
    
    
    @Override
	protected void onResume() {
		super.onResume();
		refreshProjects();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_projects, menu);
        return true;
    }


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
         case android.R.id.home:

	        	NavUtils.navigateUpFromSameTask(this);
	        	
             return true;
         case R.id.menu_new_project:
 		
//			 startActivity(new Intent(this, StoryNewActivity.class));
             HomeActivity.launchLiger(this, "default_library", null);

             return true;
     }
 		
     return super.onOptionsItemSelected(item);
  
	}
    
	
	private void showPreferences ()
	{
		Intent intent = new Intent(this,SimplePreferences.class);
		this.startActivityForResult(intent, 9999);
	}

    
    
 

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		
		super.onActivityResult(arg0, arg1, arg2);
		

		boolean changed = ((StoryMakerApp)getApplication()).checkLocale();
		if (changed)
		{
			startActivity(new Intent(this,ProjectsActivity.class));
			
			finish();
			
		}
	}

	
    public void initListView (ListView list) {
    
    	list.setOnItemLongClickListener(new OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                
                
                final Project project = mListProjects.get(arg2);


            AlertDialog.Builder builder = new AlertDialog.Builder(ProjectsActivity.this);
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
    	
        list.setOnItemClickListener(new OnItemClickListener ()
        {

			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
				Project project = mListProjects.get(position);
				Intent intent = null;

			    
				if (project.getScenesAsArray().length > 1) {
					
				    intent = new Intent(ProjectsActivity.this, StoryTemplateActivity.class);
				    
				    
			    }else {
    				intent = new Intent(ProjectsActivity.this, SceneEditorActivity.class);
    		    }
				
				intent.putExtra("template_path",project.getTemplatePath());
				intent.putExtra("story_mode", project.getStoryType());
                intent.putExtra("pid", project.getId());
                intent.putExtra("title", project.getTitle());
		        startActivity(intent);
			}
        	
        });
        
       refreshProjects();
        
    }
    
    public void refreshProjects ()
    {
    	 mListProjects = (ArrayList<Project>) (new ProjectTable()).getAllAsList(this); // FIXME ugly
         aaProjects = new ProjectArrayAdapter(this, 
           	   R.layout.list_project_row, mListProjects);
         
         mListView.setAdapter(aaProjects);
    }
    
    
    private void deleteProject (Project project)
    {
    	mListProjects.remove(project);
        aaProjects.notifyDataSetChanged();
        
    	project.delete(); // FIXME this is leaving orphaned records for associated Scenes and Media.
    	
    	//should we delete project folders here too?
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
            
            tv = (TextView)row.findViewById(R.id.title);
            
            Project project = projects.get(position);
            
            tv.setText(project.getTitle());       
            
            tv = (TextView)row.findViewById(R.id.description);
            
            ImageView ivType = (ImageView)row.findViewById(R.id.cardIcon);
            ImageView ivIcon = (ImageView)row.findViewById(R.id.imageView1);
            
            // FIXME default to use first scene
            Media[] mediaList = project.getScenesAsArray()[0].getMediaAsArray();
            
            if (mediaList != null && mediaList.length > 0)    
            {
            	for (Media media: mediaList)
            		if (media != null)
            		{
            			Bitmap bmp = Media.getThumbnail(ProjectsActivity.this,media,project);
            			if (bmp != null)
            				ivIcon.setImageBitmap(bmp);
            			break;
            		}
            }
            
            int sceneCount = project.getScenesAsList().size();
            int clipCount = project.getMediaAsList().size();
            
            String projectDesc = sceneCount + " " + getContext().getString(R.string.scene_s_) + ", " + clipCount + ' ' + getContext().getString(R.string.clip_s_);

            tv.setText(projectDesc);
            
            if (project.getStoryType() == Project.STORY_TYPE_VIDEO)
	    	{
	    		//video
	    		ivType.setImageDrawable(getContext().getResources().getDrawable(R.drawable.btn_toggle_ic_list_video));
	    	}
	    	else if (project.getStoryType() == Project.STORY_TYPE_PHOTO)
	    	{	
	    		//photo	    	
	    		ivType.setImageDrawable(getContext().getResources().getDrawable(R.drawable.btn_toggle_ic_list_photo));

	    	}
	    	else if (project.getStoryType() == Project.STORY_TYPE_AUDIO)
	    	{
	
	    		//audio	    	
	    		ivType.setImageDrawable(getContext().getResources().getDrawable(R.drawable.btn_toggle_ic_list_audio));

	    	}
	    	else if (project.getStoryType() == Project.STORY_TYPE_ESSAY)
	    	{
	    		//essay
	    		ivType.setImageDrawable(getContext().getResources().getDrawable(R.drawable.btn_toggle_ic_list_essay));
	
	    	}
	    	
            
            return row;
        }
        
    }


   
    
    
}
