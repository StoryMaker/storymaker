package info.guardianproject.mrapp;

import java.util.Locale;

import org.holoeverywhere.widget.Toast;

import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.model.Project;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class StoryNewActivity extends SherlockActivity {

	private RadioGroup rGroup;
	private TextView txtNewStoryDesc;
	private EditText editTextStoryName;
	
	private final static int DEFAULT_CLIP_COUNT = 5;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_story);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        txtNewStoryDesc = (TextView)findViewById(R.id.txtNewStoryDesc);
        editTextStoryName = (EditText)findViewById(R.id.editTextStoryName);
        
        rGroup = (RadioGroup)findViewById(R.id.radioGroupStoryType);
        
        rGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				
				if (checkedId == R.id.radioStoryType0)
		    	{
		    		//video
					txtNewStoryDesc.setText(R.string.new_story_video);
		    		
		    	}
		    	else if (checkedId == R.id.radioStoryType1)
		    	{

		    		//photo

					txtNewStoryDesc.setText(R.string.new_story_photo);
		    	}
		    	else if (checkedId == R.id.radioStoryType2)
		    	{

		    		//audio

					txtNewStoryDesc.setText(R.string.new_story_audio);
		    	}
		    	else if (checkedId == R.id.radioStoryType3)
		    	{
		    		//essay

					txtNewStoryDesc.setText(R.string.new_story_essay);
		    		
		    	}
				
			}
        	
        });
        
        ((Button) findViewById(R.id.buttonSimpleStory)).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
            	
            	checkTypeAndLaunchEditor();
            	
            	
            }
        });
        
        ((Button) findViewById(R.id.buttonChooseTemplate)).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), StoryTemplateChooserActivity.class));
            }
        });
    }
    
    private void checkTypeAndLaunchEditor ()
    {
    	
    	String pName = editTextStoryName.getText().toString();
    	
    	int clipCount = DEFAULT_CLIP_COUNT;
    	    
    	if (pName == null || pName.length() == 0)
    	{
    		Toast.makeText(this, R.string.you_must_enter_a_project_name, Toast.LENGTH_SHORT).show();
    	}
    	else
    	{
    		Project project = new Project (this, clipCount);
    		project.setTitle(pName);
    	
	    	int checkedId = rGroup.getCheckedRadioButtonId();
	    	
	    	String templateJsonPath = null;
	    	int storyMode = -1;
	    	String lang = StoryMakerApp.getCurrentLocale().getLanguage();
	    	
	    	
	    	if (checkedId == R.id.radioStoryType0)
	    	{
	    		//video
	    		templateJsonPath = "story/templates/" + lang + "/video_simple.json";
	    		storyMode = Project.STORY_TYPE_VIDEO;
	    		
	    	}
	    	else if (checkedId == R.id.radioStoryType1)
	    	{
	
	    		//photo
	    		storyMode = Project.STORY_TYPE_PHOTO;
	    		templateJsonPath = "story/templates/" + lang + "/photo_simple.json";
	    	}
	    	else if (checkedId == R.id.radioStoryType2)
	    	{
	
	    		//audio
	    		storyMode = Project.STORY_TYPE_AUDIO;
	    		templateJsonPath = "story/templates/" + lang + "/audio_simple.json";
	    	}
	    	else if (checkedId == R.id.radioStoryType3)
	    	{
	    		//essay
	    		storyMode = Project.STORY_TYPE_ESSAY;
	    		templateJsonPath = "story/templates/" + lang + "/essay_simple.json";
	    		
	    	}
	    	
	    	project.setStoryType(storyMode);
	    	project.save();
	    	
	    	Intent intent = new Intent(getBaseContext(), SceneEditorActivity.class);
	    	intent.putExtra("story_mode", storyMode);
	    	intent.putExtra("template_path", templateJsonPath);
	    	intent.putExtra("title", project.getTitle());
	    	intent.putExtra("pid", project.getId());
	        startActivity(intent);
    	}
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
