package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.Project;

import org.holoeverywhere.widget.AutoCompleteTextView;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Spinner;

import android.view.ViewGroup.LayoutParams;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;


public class StoryOverviewEditActivity extends BaseActivity {

	private Project mProject;
	private ViewGroup mContainerStoryTagsView;
	
	private EditText etStoryTitle;
	private EditText etStoryDesc;
	private Spinner spStorySection;
	private Spinner spStoryLocation;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_overview_edit);
		
		startActionMode(mActionModeCallback);
		
		int pid = getIntent().getIntExtra("pid", -1); //project i
		if (pid < 0)
			return;
		mProject = Project.get(getApplicationContext(), pid);
		
		mContainerStoryTagsView = (ViewGroup) findViewById(R.id.story_tag_container);
		
		initialize();
		setProjectInfo();
	}
	
	private void initialize() {
		
		final AutoCompleteTextView tvStoryTag = (AutoCompleteTextView) findViewById(R.id.act_story_info_tag);
		String[] autocompleteTags = getResources().getStringArray(R.array.array_autocomplete_tags);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autocompleteTags);
		tvStoryTag.setAdapter(adapter);
		
		Button btnAddTag = (Button) findViewById(R.id.btn_add_tag);
		btnAddTag.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	
		    	String tagText = tvStoryTag.getText().toString();
		    	
		    	if (!tagText.equals("")) {
		    		
		    		mProject.addTag(tagText);
		    		addProjectTag(tagText);
		    		
		    	}
		    	
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
		
		mProject.setTitle(etStoryTitle.getText().toString());
		mProject.setDescription(etStoryDesc.getText().toString());
		mProject.setSection(spStorySection.getSelectedItem().toString());
		mProject.setLocation(spStoryLocation.getSelectedItem().toString());
		
		//TODO Save Tags
	}
	
	private void addProjectTag(String tag) {
		
		Button btnTag = new Button(this);
		btnTag.setText(tag);
		btnTag.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		mContainerStoryTagsView.addView(btnTag, 0);
		
		//remove button when clicked
		btnTag.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) { 	
		    	mContainerStoryTagsView.removeView(v);
		    }
		});
    }
	
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
	    public void onDestroyActionMode(ActionMode mode) {

	    }

	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	    	int i = item.getItemId();
	    	int j = i + 1;
	    	
	        switch (item.getItemId()) {
	            case R.id.menu_cancel:
	            	mode.finish();
	            	onBackPressed();
	                return false;
	            case R.id.menu_edit_confirm:
	            	saveProjectInfo();
	            	mode.finish();
	            	onBackPressed();
	            	return false;
	            default:
	                mode.finish();
	                onBackPressed();
	                return true;
	       }
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
