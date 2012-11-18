package info.guardianproject.mrapp;

import info.guardianproject.mrapp.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class StoryNew extends SherlockActivity {

	private RadioGroup rGroup;
	private TextView txtNewStoryDesc;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_story);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        txtNewStoryDesc = (TextView)findViewById(R.id.txtNewStoryDesc);
        
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
                startActivity(new Intent(getBaseContext(), StoryTemplateChooser.class));
            }
        });
    }
    
    private void checkTypeAndLaunchEditor ()
    {
    	Intent intent = new Intent(getBaseContext(), SceneEditorNoSwipe.class);
    	int checkedId = rGroup.getCheckedRadioButtonId();
    	
    	String templateJsonPath = null;
    	int storyMode = -1;
    	
    	
    	if (checkedId == R.id.radioStoryType0)
    	{
    		//video
    		templateJsonPath = "story/templates/video_simple.json";
    		storyMode = SceneEditorNoSwipe.STORY_MODE_VIDEO;
    		
    	}
    	else if (checkedId == R.id.radioStoryType1)
    	{

    		//photo
    		storyMode = SceneEditorNoSwipe.STORY_MODE_PHOTO;
    		templateJsonPath = "story/templates/photo_simple.json";
    	}
    	else if (checkedId == R.id.radioStoryType2)
    	{

    		//audio
    		storyMode = SceneEditorNoSwipe.STORY_MODE_AUDIO;
    		templateJsonPath = "story/templates/audio_simple.json";
    	}
    	else if (checkedId == R.id.radioStoryType3)
    	{
    		//essay
    		storyMode = SceneEditorNoSwipe.STORY_MODE_ESSAY;
    		templateJsonPath = "story/templates/essay_simple.json";
    		
    	}
    	
    	intent.putExtra("story_mode", storyMode);
    	intent.putExtra("template_path", templateJsonPath);
    	
        startActivity(intent);
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
