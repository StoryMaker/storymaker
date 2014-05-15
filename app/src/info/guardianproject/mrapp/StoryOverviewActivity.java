package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.ProjectTable;

import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.Toast;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
	    
	    LinearLayout activityList = (LinearLayout) findViewById(R.id.activityList);
	    activityList.removeAllViews();
	    TextView activityListHeader = new TextView(this);
	    activityListHeader.setText(getString(R.string.label_activity_log));
	    activityListHeader.setTextSize(20);
	    activityListHeader.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
	    activityList.addView(activityListHeader);
	    new ProjectActivityAdapter(activityList,
                new String[] {"Story Shared on Facebook", "Scene Added", "Story Created"}).addAllViews();
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
	
	/**
	 * Dummy ArrayAdapter that adds views directly to the given
	 * hostView. This is in place until the root ScrollView in
	 * this Activity's layout is removed. We can't have a ListView
	 * within a ScrollView.
	 *
	 */
	class ProjectActivityAdapter extends ArrayAdapter<String>{
	    private final static int sResourceLayoutId = android.R.layout.simple_list_item_1;
	    private ViewGroup mHostView;
	      
        public ProjectActivityAdapter(ViewGroup hostView, String[] objects) {
            super(hostView.getContext(), sResourceLayoutId, objects);
            mHostView = hostView;
        }
        
        public View getView (int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(sResourceLayoutId, null);
            }
            ((TextView) convertView).setText(getItem(position));
            ((TextView) convertView).setPadding(0, 10, 0, 0);
            return convertView;
        }
        
        /**
         * Method to be removed when this is used with a proper
         * ArrayAdapter compatible view.
         */
        public void addAllViews() {
            for (int x = 0; x < this.getCount(); x++) {
                mHostView.addView(getView(x, null, mHostView));
            }
            
        }   
	}
}
