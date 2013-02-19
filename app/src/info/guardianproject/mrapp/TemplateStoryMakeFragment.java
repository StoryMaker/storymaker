package info.guardianproject.mrapp;

import info.guardianproject.mrapp.model.template.Scene;
import info.guardianproject.mrapp.model.template.Template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint("ValidFragment") // FIxME maybe we shouldn't supress this
public class TemplateStoryMakeFragment extends Fragment {
    EditorBaseActivity mActivity;
    public static final String ARG_SECTION_NUMBER = "section_number";
    private String mTemplateJsonPath = null;
    
    public TemplateStoryMakeFragment(EditorBaseActivity activity) {
        mActivity = activity;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_make, null);
        
        Intent intent = getActivity().getIntent();
        mTemplateJsonPath = intent.getStringExtra("template_path");
        
        // FIXME fetch template from the Project db record
        Template template = new Template();
        try {
            template.parseAsset(getActivity(), mTemplateJsonPath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ArrayList<Scene> scenes = template.getScenes();
        String[] egTitles = new String[scenes.size() + 1];
        String[] egDescriptions = new String[scenes.size() + 1];
        String[] egStatuses = new String[scenes.size() + 1];
        for (int i = 0 ; i < scenes.size() ; i++) {
            Scene scene = scenes.get(i);
            egTitles[i] = scene.mTitle;
            egDescriptions[i] = scene.mDescription;
            egStatuses[i] = "test 123"; // FIXME status is to be set by how many clips we have left to add
        }

//        String[] egTitles = getResources().getStringArray(R.array.eg_scene_titles);
//        String[] egDescriptions = getResources().getStringArray(R.array.eg_scene_descriptions);
//        String[] egStatuses = getResources().getStringArray(R.array.eg_scene_statuses);
        
        // create the item mapping
        String[] from = new String[] {"title", "description", "status" };
        int[] to = new int[] { R.id.textViewTitle, R.id.textViewDescription, R.id.textViewStatus  };

        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < egTitles.length; i++){
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("title", egTitles[i]);
            map.put("description", egDescriptions[i]);
            map.put("status", egStatuses[i]);
            fillMaps.add(map);
        }
        
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), fillMaps, R.layout.list_item_scene, from, to);
        ListView lv = (ListView) view.findViewById(R.id.listView1);
        lv.setAdapter(adapter);
        
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                Intent intent = new Intent(getActivity(), SceneEditorActivity.class);
                intent.putExtra("template_path", mTemplateJsonPath);
                intent.putExtra("story_mode", mActivity.mMPM.mProject.getStoryType());
                intent.putExtra("pid", mActivity.mMPM.mProject.getId());
                intent.putExtra("title", mActivity.mMPM.mProject.getTitle());
                getActivity().startActivity(intent);
            }
        });
        return view;
    }
}
