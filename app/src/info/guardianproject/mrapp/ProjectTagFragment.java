package info.guardianproject.mrapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public class ProjectTagFragment extends Fragment {

	private ViewGroup mContainerProjectTagsView;
	String[] projectTags;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_project_tag_fragment, null);
		
		Bundle bundle = getArguments();
		projectTags = bundle.getStringArray("tags");
		
		mContainerProjectTagsView = (ViewGroup) view.findViewById(R.id.project_tag_container);	
		initialize();
		
		return view;
	}
	
	private void initialize() {
		
		for (String tag : projectTags) {
			addProjectTag("#" + tag);
		}	
	}
	
	private void addProjectTag(String tag) {
		
		Button btnTag = new Button(getActivity());
		btnTag.setEnabled(false);
		btnTag.setText(tag);
		btnTag.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		mContainerProjectTagsView.addView(btnTag, 0);
    }
}
