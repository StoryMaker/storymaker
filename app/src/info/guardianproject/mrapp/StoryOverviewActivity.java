package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.ProjectTable;

import org.holoeverywhere.widget.Toast;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * Display Project metadata including tags.
 * 
 * 
 * Used in conjunction with {@link StoryOverviewEditActivity}
 *
 */
public class StoryOverviewActivity extends BaseActivity {

	private Project mProject;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_story_overview);
			
		int pid = getIntent().getIntExtra("pid", -1); //project i
		if (pid < 0) {
			return;
		}
		mProject = (Project) (new ProjectTable()).get(getApplicationContext(), pid);
		
	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    initialize();
	}
	
	private void initialize() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_action_info);
	    actionBar.setDisplayHomeAsUpEnabled(true);

	    ProjectInfoFragment infoFrag = ProjectInfoFragment.newInstance(mProject.getId(), false, true);
	    getSupportFragmentManager().beginTransaction().replace(R.id.fl_info_container, infoFrag).commit();
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
            	// To enhance flexibility of this Activity
            	// don't finish it when moving to StoryOverviewEditActivity
            	// and hardcode this activity as the return activity in StoryOverviewEditAtivity.
            	// Instead, allow StoryOverviewEditActivity to finish() itself, presenting
            	// the originating Activity. To adjust, the originating activity should
            	// refresh it's UI with Project based data in onStart() instead of onCreate()
            	// Some day, StoryOverviewActivity and StoryOverviewEditActivity should be Fragments
            	// or DialogFragments
            	//finish();
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
}
