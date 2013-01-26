package info.guardianproject.mrapp;

import info.guardianproject.mrapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class StoryTemplateChooserActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_template_chooser);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        

        String[] egTitles = getResources().getStringArray(R.array.eg_scene_titles);
        String[] egDescriptions = getResources().getStringArray(R.array.eg_scene_descriptions);
        String[] egStatuses = getResources().getStringArray(R.array.eg_scene_statuses);
        
        // create the item mapping
        String[] from = new String[] {"title", "description", "status" };
        int[] to = new int[] { R.id.textViewTitle, R.id.textViewDescription, R.id.textViewStatus  };

        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < egTitles.length; i++){
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("title", egTitles[i]);
            map.put("description", egDescriptions[i]);
            map.put("status", "");
            fillMaps.add(map);
        }
        
        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.list_item_scene, from, to);
        ListView lv = (ListView) this.findViewById(R.id.listView1);
        lv.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_choose_template, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.itemForward:
                startActivity(new Intent(getBaseContext(), StoryTemplateActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
