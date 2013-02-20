package info.guardianproject.mrapp;

import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.model.Project;
import info.guardianproject.mrapp.model.Scene;
import info.guardianproject.mrapp.model.template.Template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class StoryTemplateChooserActivity extends BaseActivity {
    private String mTemplatePath;
    private Template mTemplate;
    private String mProjectName;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_template_chooser);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        mProjectName = getIntent().getExtras().getString("project_title");

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
        
        Button buttonEvent = (Button) findViewById(R.id.btnEvent);
        buttonEvent.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
//               mTemplatePath = "video_3_scene.json";
            }
        }); 
        
        // FIXME default to Event template
        String templateJsonPath = null;
        String lang = StoryMakerApp.getCurrentLocale().getLanguage();
        mTemplatePath = "story/templates/" + lang + "/video_3_scene.json";
        mTemplate = new Template();
        try {
            mTemplate.parseAsset(this, mTemplatePath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    // FIXME display template's scenes in list

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
                // FIXME setup new project from template (empty scenes)
                int sceneCount = mTemplate.getScenes().size(); 
                Project project = new Project(this, sceneCount);
                project.setTitle(mProjectName);
                project.setTemplatePath(mTemplatePath);
                project.save();
                int i = 0;
                for (info.guardianproject.mrapp.model.template.Scene s : mTemplate.getScenes()) {
                    Scene scene = new Scene(this, s.getClips().size());
                    scene.setProjectId(project.getId());
                    scene.setProjectIndex(i);
                    scene.save();
                    i++;
                }
                Intent intent = new Intent(getBaseContext(), StoryTemplateActivity.class);
                intent.putExtra("pid", project.getId());
                intent.putExtra("template_path", mTemplatePath);
                
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
