package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.StoryTag;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


public class StoryOverviewEditActivity extends BaseActivity {

	private ViewGroup mContainerStoryTagsView;
	
	List<StoryTag> mALStoryTags = new ArrayList<StoryTag>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_overview_edit);
		
		mContainerStoryTagsView = (ViewGroup) findViewById(R.id.story_tag_container);
		
		initialize();
		getStoryTags();
		addStoryTags();
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
                Toast.makeText(this, "Edit", Toast.LENGTH_SHORT).show();
                break;
            case R.id.itemSendStory:
            	Toast.makeText(this, "Send", Toast.LENGTH_SHORT).show();
            	break;
            case R.id.itemShareStory:
            	Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
            	break;
                
        }
        
        return super.onOptionsItemSelected(item);
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
		    		
		    		StoryTag stTemp = new StoryTag(232, tagText);
		    		
		    		mALStoryTags.add(stTemp);
		    		
		    		addStoryTag(stTemp);
		    		
		    	}
		    	
		    	tvStoryTag.setText(null);
		    }
		});
		
	}
	
	private void getStoryTags() {
		
		int i = 0;
		
		mALStoryTags.add(new StoryTag(i++, "story"));
		mALStoryTags.add(new StoryTag(i++, "news"));
		mALStoryTags.add(new StoryTag(i++, "morocco"));
		mALStoryTags.add(new StoryTag(i++, "desert"));
		mALStoryTags.add(new StoryTag(i++, "market"));
		mALStoryTags.add(new StoryTag(i++, "camel"));
		mALStoryTags.add(new StoryTag(i++, "politics"));
		mALStoryTags.add(new StoryTag(i++, "arabic"));
	}
	
	private void addStoryTags() { 
	
        for(StoryTag tag: mALStoryTags) {        	
        		addStoryTag(tag);
        }      
    }
	
	private void addStoryTag(StoryTag tag) {
    	
		final StoryTag currentTag = tag;
		
		Button btnTag = new Button(this);
		btnTag.setText(currentTag.getName());
		btnTag.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		mContainerStoryTagsView.addView(btnTag, 0);
		
		//remove button when clicked
		btnTag.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) { 	
		    	mContainerStoryTagsView.removeView(v);
		    }
		});
    }	
}
