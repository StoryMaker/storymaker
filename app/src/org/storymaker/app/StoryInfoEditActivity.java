package org.storymaker.app;

import java.util.ArrayList;

import org.storymaker.app.model.Project;
import org.storymaker.app.model.ProjectTable;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Display editable Story metadata including tags.
 * 
 * Used in conjunction with {@link StoryInfoActivity}
 *
 */
public class StoryInfoEditActivity extends BaseActivity {

	private Project mProject;
	private ProjectTagFragment mTagFragment;
	
	private EditText etStoryTitle;
	private EditText etStoryDesc;
	private Spinner spStorySection;
	private Spinner spStoryLocation;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_story_info_edit);
		
		startActionMode(mActionModeCallback);
        
		int pid = getIntent().getIntExtra("pid", -1); //project id
		if (pid < 0) {
			return;
		}
		mProject = (Project) (new ProjectTable()).get(getApplicationContext(), pid);
		
		initialize();
		setProjectInfo();
	}
	
	private void initialize() {
		
		final AutoCompleteTextView tvStoryTag = (AutoCompleteTextView) findViewById(R.id.act_story_info_tag);
//		String[] autocompleteTags = getResources().getStringArray(R.array.array_autocomplete_tags);
//		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autocompleteTags);
//		tvStoryTag.setAdapter(adapter);

        mTagFragment = ProjectTagFragment.newInstance(mProject.getId(), true);
        getSupportFragmentManager().beginTransaction().add(R.id.fl_tag_container, mTagFragment).commit();
		
		Button btnAddTag = (Button) findViewById(R.id.btn_add_tag);
		btnAddTag.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	
		    	String tagText = tvStoryTag.getText().toString();
		    	mTagFragment.addTag(tagText);
		    	tvStoryTag.setText(null);
		    }
		});		
	}
	
	private void setProjectInfo() {
		
		etStoryTitle = (EditText) findViewById(R.id.et_story_info_title);
    	etStoryDesc = (EditText) findViewById(R.id.et_story_info_description);
    	spStorySection = (Spinner) findViewById(R.id.sp_story_section);
    	spStoryLocation = (Spinner) findViewById(R.id.sp_story_location);
    	
    	etStoryTitle.setText(mProject.getTitle());
    	etStoryDesc.setText(mProject.getDescription());
    	
    	spStorySection.setSelection(getSpinnerIndex(spStorySection, mProject.getSection()));
    	spStoryLocation.setSelection(getSpinnerIndex(spStoryLocation, mProject.getLocation()));
	}
	
	private void saveProjectInfo() {
	    // ProjectTagFragment manages Tags
		mProject.setTitle(etStoryTitle.getText().toString());
		mProject.setDescription(etStoryDesc.getText().toString());
		mProject.setSection(spStorySection.getSelectedItem().toString());
		mProject.setLocation(spStoryLocation.getSelectedItem().toString());
		
		mProject.save();
	}

	
	private boolean actionModeCancel = false;
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
	    @Override 
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.context_menu_edit, menu);
			return true;
	    }

	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	    	
	        switch (item.getItemId()) {
	            case R.id.menu_cancel:
	            	actionModeCancel = true;
	            	mode.finish();
	                return true;
	            case R.id.menu_edit_confirm:
	            	mode.finish();
	            	return true;
	            default:
	                mode.finish();
	                return true;
	       }
	    }
	    
	    // this has slightly odd save logic so that I can always save exit actionmode as 
        // the checkmark button acts as a cancel but the users will treat it as an accept
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	    	if(!actionModeCancel) {
	    		saveProjectInfo();
	    	}

	    	// StoryInfoEditActivity now refreshes it's 
	    	// project backed views onStart() instead of onCreate()
	    	// so it will re-initialize it's Project-backed views
	    	// without having to be explicitly re-started.
	    	// Now we can start StoryInfoEditActivity from any Activity
	    	StoryInfoEditActivity.this.finish();
	    }
	};
	
	private int getSpinnerIndex(Spinner spinner, String string) {	
		for (int i=0; i < spinner.getCount(); i++) {
			if (spinner.getItemAtPosition(i).equals(string)) {
				return i;
			}
		}
		
		return 0; //set to first by default
	}
}
