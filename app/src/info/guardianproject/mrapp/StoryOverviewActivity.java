package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.Project;

import org.holoeverywhere.widget.Toast;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class StoryOverviewActivity extends BaseActivity {

	private Project mProject;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_story_overview);
			
		int pid = getIntent().getIntExtra("pid", -1); //project i
		if (pid < 0)
			return;
		mProject = Project.get(getApplicationContext(), pid);
		
	    initialize();
		setStoryInfo();
	}
	
	private void initialize() {
		
		addTestData();//TODO remove
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

	    Bundle tags = new Bundle();
	    tags.putStringArray("tags", mProject.getTagsAsArray());
	    
	    ProjectTagFragment fragPT = new ProjectTagFragment();
	    fragPT.setArguments(tags);
        getSupportFragmentManager().beginTransaction().add(R.id.fl_tag_container, fragPT).commit();
	}
	
	private void addTestData() {
	
		mProject.setDescription("A story about a trial.");
		mProject.setLocation("Iraq");
		mProject.setSection("Politics");
		
		mProject.addTag("storymaker");
		mProject.addTag("news");	
	}
	
    	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getSupportMenuInflater().inflate(R.menu.activity_story_overview, menu);
	    return super.onCreateOptionsMenu(menu);   
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		
        switch (item.getItemId()) {
            case R.id.itemEditStory:
            	Intent intent = new Intent(this, StoryOverviewEditActivity.class);
            	intent.putExtra("pid", mProject.getId());
            	startActivity(intent);    
            	
            	return true;
            case R.id.itemSendStory:
            	Toast.makeText(this, "Send", Toast.LENGTH_SHORT).show();
            	
            	return true;
            case R.id.itemShareStory:
            	Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
            	
            	return true;
            case android.R.id.home:
            	//NavUtils.navigateUpFromSameTask(this);
            	finish();
            	
            	return true;
            default:
            	return super.onOptionsItemSelected(item);
        }
    }
	
	private void setStoryInfo() { 
    	
    	TextView tvStoryTitle = (TextView) findViewById(R.id.tv_story_title);
    	TextView tvStoryDesc = (TextView) findViewById(R.id.tv_story_desciption);
    	TextView tvStorySection = (TextView) findViewById(R.id.tv_story_section);
    	TextView tvStoryLocation = (TextView) findViewById(R.id.tv_story_location);
    	
    	tvStoryTitle.setText(mProject.getTitle());
    	tvStoryDesc.setText(mProject.getDescription());
    	tvStorySection.setText(mProject.getSection());
    	tvStoryLocation.setText(mProject.getLocation());
    }
}
