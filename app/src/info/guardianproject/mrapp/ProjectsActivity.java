package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.Media;
import info.guardianproject.mrapp.model.Project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.ffmpeg.android.MediaUtils;
import org.holoeverywhere.app.AlertDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ProjectsActivity extends BaseActivity {


	ListView mListView;

	private ArrayList<Project> mListProjects;
	private ProjectArrayAdapter aaProjects;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.activity_projects);
        
        // action bar stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
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
        getSupportMenuInflater().inflate(R.menu.activity_projects, menu);
        return true;
    }


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
         case android.R.id.home:

	        	NavUtils.navigateUpFromSameTask(this);
	        	
             return true;
         case R.id.menu_new_project:
 		
			 startActivity(new Intent(this, StoryNewActivity.class));

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
    	 mListProjects = Project.getAllAsList(this);
         aaProjects = new ProjectArrayAdapter(this, 
           	   R.layout.list_project_row, mListProjects);
         
         mListView.setAdapter(aaProjects);
    }
    
    
    private void deleteProject (Project project)
    {
    	mListProjects.remove(project);
        aaProjects.notifyDataSetChanged();
        
    	project.delete();
    	
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
            			ivIcon.setImageBitmap(getThumbnail(media));
            			break;
            		}
            }
            
            String projectType = project.getScenesAsList().size() + " scene(s)";

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
	    	
            tv.setText(projectType);
            
            return row;
        }
        
    }


    public Bitmap getThumbnail(Media media)
    {
    	if (media == null)
    		return null;
    	
        String path = media.getPath();

        if (media.getMimeType() == null)
        {
            return null;
        }
        else if (media.getMimeType().startsWith("video"))
        {
            File fileThumb = new File(path + ".jpg");
            if (fileThumb.exists())
            {

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                return BitmapFactory.decodeFile(fileThumb.getAbsolutePath(), options);
            }
            else
            {
                Bitmap bmp = MediaUtils.getVideoFrame(path, -1);
                try {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 70, new FileOutputStream(fileThumb));
                } catch (FileNotFoundException e) {
                    Log.e(AppConstants.TAG, "could not cache video thumb", e);
                }

                return bmp;
            }
        }
        else if (media.getMimeType().startsWith("image"))
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;

            return BitmapFactory.decodeFile(path, options);
        }
        else if (media.getMimeType().startsWith("audio"))
        {
            return BitmapFactory.decodeResource(getResources(), R.drawable.thumb_audio);
        }
        else 
        {
            return BitmapFactory.decodeResource(getResources(), R.drawable.thumb_complete);
        }
    }
    
    
}
