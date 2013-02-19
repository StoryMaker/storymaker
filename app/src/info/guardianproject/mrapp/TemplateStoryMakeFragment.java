package info.guardianproject.mrapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

public class TemplateStoryMakeFragment extends Fragment {
    public TemplateStoryMakeFragment() {
    }

    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_make, null);
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
                Intent i = new Intent(getActivity(), SceneEditorActivity.class);
                i.putExtra("template_story", true);
                getActivity().startActivity(i);
                
            }
        });
        return view;
    }
}
