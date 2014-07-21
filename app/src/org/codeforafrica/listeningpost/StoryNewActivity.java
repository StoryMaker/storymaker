package org.codeforafrica.listeningpost;

import java.util.Date;

import org.codeforafrica.listeningpost.R;
import org.codeforafrica.listeningpost.model.Project;
import org.codeforafrica.listeningpost.model.Scene;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.Toast;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class StoryNewActivity extends BaseActivity {

	private RadioGroup rGroup;
	private TextView txtNewStoryDesc;
	private EditText editTextStoryName;
	
	int rid;
	int storymode;
	int quickstory;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_story);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        rid = intent.getIntExtra("rid", -1);
        storymode = intent.getIntExtra("storymode", -1);
        
        txtNewStoryDesc = (TextView)findViewById(R.id.txtNewStoryDesc);
        editTextStoryName = (EditText)findViewById(R.id.editTextStoryName);
        editTextStoryName.setHint("Quick caption(optional)");
        rGroup = (RadioGroup)findViewById(R.id.radioGroupStoryType);
        
		launchSimpleStory("caption", getSelectedStoryMode(), false);

        
        //storymode is already chosen     
        LinearLayout llMedium = (LinearLayout)findViewById(R.id.llMedium);
        llMedium.setVisibility(View.GONE);
        
        rGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				
				if (checkedId == R.id.radioStoryType0)
		    	{
		    		//video
					txtNewStoryDesc.setText(R.string.template_video_desc);
		    		
		    	}
		    	else if (checkedId == R.id.radioStoryType1)
		    	{

		    		//photo

					txtNewStoryDesc.setText(R.string.template_photo_desc);
		    	}
		    	else if (checkedId == R.id.radioStoryType2)
		    	{

		    		//audio

					txtNewStoryDesc.setText(R.string.template_audio_desc);
		    	}
		    	else if (checkedId == R.id.radioStoryType3)
		    	{
		    		//essay

					txtNewStoryDesc.setText(R.string.template_essay_desc);
		    		
		    	}
				
			}
        	
        });
        
        ((Button) findViewById(R.id.buttonStartStory)).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
            	
            	if (formValid()) {

                	RadioGroup view = ((RadioGroup)findViewById(R.id.radioGroupStoryLevel));
                	if (view.getCheckedRadioButtonId() == R.id.radioStoryType0)
                		launchSimpleStory(editTextStoryName.getText().toString(), getSelectedStoryMode(), false);
                	else
                		launchTemplateChooser();
            	}
            	
            }
        });
        
        
        
        if (intent.hasExtra("story_name") && intent.hasExtra("story_type"))
        {
        	String storyName = intent.getExtras().getString("story_name");
        	int storyType = intent.getExtras().getInt("story_type");
        	boolean autoCapture = false;
        	
        	if (intent.hasExtra("auto_capture"))
        			autoCapture = intent.getExtras().getBoolean("auto_capture");
        	
        	storyName += " " + new Date().toLocaleString();
        	
        	launchSimpleStory(storyName, storyType, autoCapture);
        	
        }
    }
    
    private boolean formValid ()
    {
    	//Caption is optional
    	return true;
    	/*
    	String pName = editTextStoryName.getText().toString();
    	    
    	if (pName == null || pName.length() == 0)
    	{
    		Toast.makeText(this, R.string.you_must_enter_a_project_name, Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	else
    	{
    		return true;
    	}
    	*/
    	
    }
    
    private int getSelectedStoryMode ()
    {		
    	
    	   //storymode got from intent extras
    	   return storymode;
    	   /*
    	   int checkedId = rGroup.getCheckedRadioButtonId();
    	   int resultMode = -1;
    	   
    	   switch (checkedId)
    	   {
    	   case R.id.radioStoryType0:
    		   resultMode = Project.STORY_TYPE_VIDEO;
    		   break;
    	   case R.id.radioStoryType1:
    		   resultMode = Project.STORY_TYPE_PHOTO;
    		   break;
    		   
    	   case R.id.radioStoryType2:
    		   resultMode = Project.STORY_TYPE_AUDIO;
    		   break;
    		   
    	   case R.id.radioStoryType3:
    		   resultMode = Project.STORY_TYPE_ESSAY;
    		   break;
    		   
    	   }
    	   
    	   return resultMode;
    	   */
    		
    }
    		
    private void launchTemplateChooser ()
    {
        int storyMode = getSelectedStoryMode();

        String templateJsonPath = Project.getSimpleTemplateForMode(getApplicationContext(), storyMode);
        
        Intent i = new Intent(getBaseContext(), StoryTemplateChooserActivity.class);

        i.putExtra("project_title", editTextStoryName.getText().toString());
        i.putExtra("story_mode", storyMode);
        i.putExtra("rid", rid);
        i.putExtra("story_mode_template", templateJsonPath);
        
        startActivity(i);
        finish();
    }
    
   
    
    private void launchSimpleStory(String pName, int storyMode, boolean autoCapture) {
        int clipCount = AppConstants.DEFAULT_CLIP_COUNT;
        
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
        
        Intent intent = new Intent(getBaseContext(), SceneEditorActivity.class);
        intent.putExtra("story_mode", storyMode);
        intent.putExtra("template_path", templateJsonPath);
        intent.putExtra("title", project.getTitle());
        intent.putExtra("pid", project.getId());
        intent.putExtra("scene", 0);
        intent.putExtra("auto_capture", true);
        
        startActivity(intent);
        
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_new_story, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
