package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.StoryTag;
import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.widget.TextView;

public class StoryOverviewActivity extends BaseActivity {

	List<StoryTag> mALStoryTags = new ArrayList<StoryTag>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_overview);
		
		getStoryInfo();
		addStoryInfo();
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
	
	private void getStoryInfo() {
		
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
	
	private void addStoryInfo() { 
    	
    	TextView tvStoryTitle = (TextView) findViewById(R.id.tv_story_title);
    	TextView tvStoryDesc = (TextView) findViewById(R.id.tv_story_desciption);
    	TextView tvStorySection = (TextView) findViewById(R.id.tv_story_section);
    	TextView tvStoryLocation = (TextView) findViewById(R.id.tv_story_location);
    	
    	tvStoryTitle.setText("Hello");
    	tvStoryDesc.setText("Description of the beautiful story.");
    	tvStorySection.setText("Politics");
    	tvStoryLocation.setText("Alabama");
    }

}
