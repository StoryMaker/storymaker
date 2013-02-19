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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ProjectsListView extends ListView implements Runnable {

	
	private ArrayList<Project> mListProjects;
	private ProjectArrayAdapter aaProjects;
	
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
				Intent intent = null;
				if (project.getScenesAsArray().length > 1) {
				    intent = new Intent(getContext(), StoryTemplateActivity.class);
				    String lang = StoryMakerApp.getCurrentLocale().getLanguage();
				    intent.putExtra("template_path", "story/templates/" + lang + "/video_3_scene.json");
			    }else {
    				intent = new Intent(getContext(), SceneEditorActivity.class);
    		    	intent.putExtra("template_path", getSimpleTemplateJsonPath(project));
			    }
				intent.putExtra("story_mode", project.getStoryType());
                intent.putExtra("pid", project.getId());
                intent.putExtra("title", project.getTitle());
		        getContext().startActivity(intent);
			}
        	
        });
        
        mListProjects = Project.getAllAsList(getContext());
        aaProjects = new ProjectArrayAdapter(getContext(), 
          	   R.layout.list_project_row, mListProjects);
        
        setAdapter(aaProjects);
        
        
         
        
    }
    
    private String getSimpleTemplateJsonPath(Project project) {
        String templateJsonPath = null;
        String lang = StoryMakerApp.getCurrentLocale().getLanguage();
        if (project.getStoryType() == Project.STORY_TYPE_VIDEO)
        {
            templateJsonPath = "story/templates/" + lang + "/video_simple.json";
        }
        else if (project.getStoryType() == Project.STORY_TYPE_PHOTO)
        {
            templateJsonPath = "story/templates/" + lang + "/photo_simple.json";
        }
        else if (project.getStoryType() == Project.STORY_TYPE_AUDIO)
        {
            templateJsonPath = "story/templates/" + lang + "/audio_simple.json";
        }
        else if (project.getStoryType() == Project.STORY_TYPE_ESSAY)
        {
            templateJsonPath = "story/templates/" + lang + "/essay_simple.json";
        }
        
        return templateJsonPath;
    }
    
    private void deleteProject (Project project)
    {
    	mListProjects.remove(project);
        aaProjects.notifyDataSetChanged();
        
    	project.delete();
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
            
            tv = (TextView)row.findViewById(R.id.projectTitle);
            
            Project project = projects.get(position);
            
            tv.setText(project.getTitle());       
            
            tv = (TextView)row.findViewById(R.id.projectSummary);
            
            ImageView ivType = (ImageView)row.findViewById(R.id.projectIconType);
            ImageView ivIcon = (ImageView)row.findViewById(R.id.projectIcon);
            
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
            
            String projectType = "";
            
            if (project.getStoryType() == Project.STORY_TYPE_VIDEO)
	    	{
	    		//video
	    		projectType = mediaList.length + " video clips";
	    		ivType.setImageDrawable(getContext().getResources().getDrawable(R.drawable.btn_toggle_ic_list_video));
	    	}
	    	else if (project.getStoryType() == Project.STORY_TYPE_PHOTO)
	    	{	
	    		//photo	    	
	    		projectType = mediaList.length + " photos";
	    		ivType.setImageDrawable(getContext().getResources().getDrawable(R.drawable.btn_toggle_ic_list_photo));

	    	}
	    	else if (project.getStoryType() == Project.STORY_TYPE_AUDIO)
	    	{
	
	    		//audio	    	
	    		projectType = mediaList.length + " audio clips";
	    		ivType.setImageDrawable(getContext().getResources().getDrawable(R.drawable.btn_toggle_ic_list_audio));

	    	}
	    	else if (project.getStoryType() == Project.STORY_TYPE_ESSAY)
	    	{
	    		//essay
	    		projectType = mediaList.length + " essay photos";
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
        else 
        {
            return BitmapFactory.decodeResource(getResources(), R.drawable.thumb_complete);
        }
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	
}
